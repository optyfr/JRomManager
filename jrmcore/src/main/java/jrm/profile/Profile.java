/*
 * Copyright (C) 2018 optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.status.StatusRendererFactory;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.ExceptionUtils;
import jrm.misc.Log;
import jrm.misc.ProfileSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.misc.SettingsEnum;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.Device;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityStatus;
import jrm.profile.data.Machine;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.SWStatus;
import jrm.profile.data.MachineListList;
import jrm.profile.data.Rom;
import jrm.profile.data.Rom.LoadFlag;
import jrm.profile.data.Sample;
import jrm.profile.data.Samples;
import jrm.profile.data.Slot;
import jrm.profile.data.SlotOption;
import jrm.profile.data.Software;
import jrm.profile.data.Software.Part;
import jrm.profile.data.Software.Part.DataArea;
import jrm.profile.data.Software.Part.DataArea.Endianness;
import jrm.profile.data.Software.Part.DiskArea;
import jrm.profile.data.SoftwareList;
import jrm.profile.data.Source;
import jrm.profile.data.Sources;
import jrm.profile.data.SystmDevice;
import jrm.profile.data.SystmMechanical;
import jrm.profile.data.SystmStandard;
import jrm.profile.data.Systms;
import jrm.profile.filter.CatVer;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.Category.SubCategory;
import jrm.profile.filter.NPlayer;
import jrm.profile.filter.NPlayers;
import jrm.profile.manager.ProfileNFO;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import lombok.Getter;
import lombok.Setter;

/**
 * Parses and models retro system profile databases from DAT catalogs. Loads dat
 * files, matches system constraints, serializes resulting profiles for caching,
 * and configures global filter structures (systems, release years, catver.ini,
 * nplayers.ini).
 * 
 * @author optyfr
 * @since 1.0
 */
public class Profile implements Serializable, StatusRendererFactory {
    private static final long serialVersionUID = 3L;

    private static final String DESCRIPTION = "description";
    private static final String VERSION = "version";

    /**
     * Scanned machines count.
     * 
     * @return count of processed machines
     */
    private @Getter long machinesCnt = 0;
    /**
     * Scanned software lists count.
     * 
     * @return count of software lists
     */
    private @Getter long softwaresListCnt = 0;
    /**
     * Scanned software entries count.
     * 
     * @return count of software entries
     */
    private @Getter long softwaresCnt = 0;
    /**
     * Scanned ROM files count.
     * 
     * @return count of ROM files
     */
    private @Getter long romsCnt = 0;
    /**
     * Scanned software ROM files count.
     * 
     * @return count of software ROM files
     */
    private @Getter long swromsCnt = 0;
    /**
     * Scanned disk files count.
     * 
     * @return count of disk files
     */
    private @Getter long disksCnt = 0;
    /**
     * Scanned software disk files count.
     * 
     * @return count of software disk files
     */
    private @Getter long swdisksCnt = 0;
    /**
     * Scanned sound sample files count.
     * 
     * @return count of processed sample files
     */
    private @Getter long samplesCnt = 0;

    /**
     * Whether MD5 checksum values are declared on ROM elements in the profile.
     * 
     * @return true if MD5 values are present on ROMs
     */
    private @Getter boolean md5Roms = false;
    /**
     * Whether MD5 checksum values are declared on CHD elements in the profile.
     * 
     * @return true if MD5 values are present on CHDs
     */
    private @Getter boolean md5Disks = false;
    /**
     * Whether SHA-1 checksum values are declared on ROM elements in the profile.
     * 
     * @return true if SHA-1 values are present on ROMs
     */
    private @Getter boolean sha1Roms = false;
    /**
     * Whether SHA-1 checksum values are declared on CHD elements in the profile.
     * 
     * @return true if SHA-1 values are present on CHDs
     */
    private @Getter boolean sha1Disks = false;

    /**
     * Build timestamp or identifier string.
     * 
     * @return the build version string
     */
    private @Getter String build = null;
    /**
     * Custom DAT properties read from the XML header elements block.
     * 
     * @return map containing XML header elements key-value associations
     */
    private final @Getter Map<String, StringBuilder> header = new HashMap<>();

    /**
     * Global collection grouping parsed machines, computer clones, and associated
     * software catalogs.
     * 
     * @return the unified target machines listing representation
     */
    private final @Getter MachineListList machineListList = new MachineListList(this);

    /**
     * Set storing ROM CRCs which resolve to distinct SHA1/MD5 signatures.
     * 
     * @return suspicious CRC checksum values set
     */
    private final @Getter Set<String> suspiciousCRC = new HashSet<>();

    /**
     * Dynamic anyware lists visibility status filter settings.
     * 
     * @param filterListLists anyware list visibility filters
     * @return visibility filters set
     */
    private transient @Getter @Setter Set<AnywareStatus> filterListLists = null;

    /**
     * Dynamic single machine anyware visibility status filter settings.
     * 
     * @param filterList single anyware item filters
     * @return visibility filters set
     */
    private transient @Getter @Setter Set<AnywareStatus> filterList = null;

    /**
     * Dynamic physical entities visibility status filter settings.
     * 
     * @param filterEntities physical item visibility filters
     * @return visibility filters set
     */
    private transient @Getter @Setter Set<EntityStatus> filterEntities = null;

    /**
     * Local profiles Settings parameters.
     * 
     * @return profiles settings container
     */
    private transient @Getter ProfileSettings settings = null;
    /**
     * Categorized and grouped system boundaries filter.
     * 
     * @return standard and custom systems filters
     */
    private transient @Getter Systms systems = null;
    /**
     * Dynamic years list collected from scanned elements.
     * 
     * @return sorted collection of years
     */
    private transient @Getter Collection<String> years = null;
    /**
     * JRomManager database profile information stats summary.
     * 
     * @return profile NFO summary reference
     */
    private transient @Getter ProfileNFO nfo = null;
    /**
     * Parsed categories configuration mapping.
     * 
     * @param catver parsed category ruleset mapping
     * @return categories config mapping
     */
    private transient @Getter @Setter CatVer catver = null;
    /**
     * Parsed multiplayer specifications configuration mapping.
     * 
     * @param nplayers parsed multiplayer capabilities mapping
     * @return multiplayer config mapping
     */
    private transient @Getter @Setter NPlayers nplayers = null;
    /**
     * Active execution context workspace session.
     * 
     * @return active workspace session
     */
    private transient @Getter Session session = null;
    /**
     * Parsed metadata DAT catalogs specifications tracking metrics.
     * 
     * @return standard dat definitions tracking metrics
     */
    private transient @Getter Sources sources = null;

    /**
     * Protected zero-argument constructor initializing an empty profile.
     */
    private Profile() {

    }

    /**
     * SAX Handler mapping parsed XML tags back into profile domain components.
     */
    private class ProfileHandler extends DefaultHandler {
        private static final String STATUS = "status";

        private final HashMap<String, Rom> romsByCRC = new HashMap<>();
        private boolean inDescription = false;
        private boolean inYear = false;
        private boolean inManufacturer = false;
        private boolean inPublisher = false;
        private boolean inHeader = false;
        private boolean inCabinetDipSW = false;
        private final EnumSet<CabinetType> cabTypeSet = EnumSet.noneOf(CabinetType.class);
        private SoftwareList currSoftwareList = null;
        private Software currSoftware = null;
        private Software.Part currPart = null;
        private Software.Part.DataArea currDataArea = null;
        private Software.Part.DiskArea currDiskArea = null;
        private Machine currMachine = null;
        private Device currDevice = null;
        private Samples currSampleSet = null;
        private Rom currRom = null;
        private Disk currDisk = null;
        private Slot currSlot = null;
        private final HashSet<String> roms = new HashSet<>();
        private final HashSet<String> disks = new HashSet<>();
        private String currTag;

        private final ProgressHandler handler;

        /**
         * Instantiates a new parsing XML SAX handler.
         * 
         * @param handler the progress handler monitor
         */
        public ProfileHandler(ProgressHandler handler) {
            this.handler = handler;
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
            try {
                currTag = qName;
                switch (qName) {
                    case "mame", "datafile":
                        startDatfile(attributes);
                        break;
                    case "header":
                        startHeader(attributes);
                        break;
                    case "softwarelist":
                        startSoftwareList(attributes);
                        break;
                    case "software":
                        startSoftware(attributes);
                        break;
                    case "feature":
                        startSoftwareFeature(attributes);
                        break;
                    case "part":
                        startSoftwarePart(attributes);
                        break;
                    case "dataarea":
                        startSoftwarePartDataarea(attributes);
                        break;
                    case "diskarea":
                        startSoftwarePartDiskarea(attributes);
                        break;
                    case "machine", "game":
                        startMachine(attributes);
                        break;
                    case DESCRIPTION:
                        startDescription(attributes);
                        break;
                    case "year":
                        startYear();
                        break;
                    case "manufacturer":
                        startManufacturer();
                        break;
                    case "publisher":
                        startPublisher();
                        break;
                    case "driver":
                        startDriver(attributes);
                        break;
                    case "display":
                        startDisplay(attributes);
                        break;
                    case "input":
                        startInput(attributes);
                        break;
                    case "device":
                        startDevice(attributes);
                        break;
                    case "instance":
                        startInstance(attributes);
                        break;
                    case "extension":
                        startExtension(attributes);
                        break;
                    case "dipswitch":
                        startDipSwitch(attributes);
                        break;
                    case "dipvalue":
                        startDipValue(attributes);
                        break;
                    case "sample":
                        startSample(attributes);
                        break;
                    case "device_ref":
                        startDeviceRef(attributes);
                        break;
                    case "slot":
                        startSlot(attributes);
                        break;
                    case "slotoption":
                        startSlotOption(attributes);
                        break;
                    case "rom":
                        startRom(attributes);
                        break;
                    case "disk":
                        startDisk(attributes);
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                throw new ProfileHandlerException(getDebugMsg(attributes, qName, e), e);
            }
        }

        /**
         * Exception thrown during parsing errors inside the SAX parser pipeline.
         */
        private class ProfileHandlerException extends RuntimeException {
            private static final long serialVersionUID = 1L;

            /**
             * Constructs a new parser exception.
             * 
             * @param message debug explanation message
             * @param e       nested source exception
             */
            public ProfileHandlerException(String message, Exception e) {
                super(message, e);
            }

        }

        /**
         * Generates detailed debug information string mapping where error was met.
         * 
         * @param attributes parsed XML attributes list
         * @param qName      active parsed element tag name
         * @param e          source exception
         * @return debug string details
         */
        private String getDebugMsg(Attributes attributes, String qName, Exception e) {
            final var msg = new StringBuilder("Error");
            if (currMachine != null)
                msg.append(" for machine ").append(currMachine.getName());
            else if (currSoftwareList != null) {
                msg.append(" for software list ").append(currSoftwareList.getName());
                if (currSoftware != null)
                    msg.append(", software ").append(currSoftware.getName());
            }
            if (currRom != null)
                msg.append(", rom ").append(currRom.getName());
            if (currDisk != null)
                msg.append(", disk ").append(currDisk.getName());
            msg.append(", xmltag=").append(qName);
            msg.append(", xmlattributes={");
            for (var i = 0; i < attributes.getLength(); i++) {
                if (i > 0)
                    msg.append(", ");
                msg.append(attributes.getQName(i)).append("=").append(attributes.getValue(i));
            }
            msg.append("}");
            msg.append("\nOriginal exception=").append(e.getClass().getSimpleName()).append(" ").append(e.getMessage());
            return msg.toString();
        }

        /**
         * Parsed "disk" element parser callback.
         * 
         * @param attributes element attributes list
         * @throws NumberFormatException if integer values are invalid
         */
        private void startDisk(final Attributes attributes) throws NumberFormatException {
            if (currMachine == null && currSoftware == null)
                return;
            currDisk = new Disk(currMachine != null ? currMachine : currSoftware);
            if (currSoftware != null && currDiskArea != null)
                currDiskArea.getDisks().add(currDisk);
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "name": //$NON-NLS-1$
                    {
                        String name = attributes.getValue(i).trim();
                        if (name.endsWith(".chd"))
                            name = name.substring(0, name.length() - 4);
                        currDisk.setName(name);
                        break;
                    }
                    case "sha1": //$NON-NLS-1$
                        currDisk.setSha1(safeHex(attributes.getValue(i), 40));
                        sha1Disks = true;
                        break;
                    case "md5": //$NON-NLS-1$
                        currDisk.setMd5(safeHex(attributes.getValue(i), 32));
                        md5Disks = true;
                        break;
                    case "merge": //$NON-NLS-1$
                        currDisk.setMerge(attributes.getValue(i).trim());
                        break;
                    case "index": //$NON-NLS-1$
                        currDisk.setIndex(Integer.decode(attributes.getValue(i)));
                        break;
                    case "optional": //$NON-NLS-1$
                        currDisk.setOptional(BooleanUtils.toBoolean(attributes.getValue(i)));
                        break;
                    case "writeable": //$NON-NLS-1$
                        currDisk.setWriteable(BooleanUtils.toBoolean(attributes.getValue(i)));
                        break;
                    case "region": //$NON-NLS-1$
                        currDisk.setRegion(attributes.getValue(i));
                        break;
                    case STATUS: // $NON-NLS-1$
                        currDisk.setDumpStatus(Entity.Status.valueOf(attributes.getValue(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Parsed "rom" element parser callback.
         * 
         * @param attributes element attributes list
         * @throws NumberFormatException if values are invalid
         */
        private void startRom(final Attributes attributes) throws NumberFormatException {
            if (currMachine == null && currSoftware == null)
                return;
            currRom = new Rom(currMachine != null ? currMachine : currSoftware);
            if (currSoftware != null && currDataArea != null)
                currDataArea.getRoms().add(currRom);
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "name": //$NON-NLS-1$
                        currRom.setName(attributes.getValue(i).trim());
                        break;
                    case "size": //$NON-NLS-1$
                        currRom.setSize(Long.decode(attributes.getValue(i)));
                        break;
                    case "offset": //$NON-NLS-1$
                        if (attributes.getValue(i).toLowerCase().startsWith("0x"))
                            currRom.setOffset(Long.decode(attributes.getValue(i)));
                        else
                            currRom.setOffset(Long.decode("0x" + attributes.getValue(i))); //$NON-NLS-1$
                        break;
                    case "value": //$NON-NLS-1$
                        currRom.setValue(attributes.getValue(i));
                        break;
                    case "crc": //$NON-NLS-1$
                        currRom.setCrc(safeHex(attributes.getValue(i), 8));
                        break;
                    case "sha1": //$NON-NLS-1$
                        currRom.setSha1(safeHex(attributes.getValue(i), 40));
                        sha1Roms = true;
                        break;
                    case "md5": //$NON-NLS-1$
                        currRom.setMd5(safeHex(attributes.getValue(i), 32));
                        md5Roms = true;
                        break;
                    case "merge": //$NON-NLS-1$
                        currRom.setMerge(attributes.getValue(i).trim());
                        break;
                    case "bios": //$NON-NLS-1$
                        currRom.setBios(attributes.getValue(i));
                        break;
                    case "region": //$NON-NLS-1$
                        currRom.setRegion(attributes.getValue(i));
                        break;
                    case "date": //$NON-NLS-1$
                        currRom.setDate(attributes.getValue(i));
                        break;
                    case "optional": //$NON-NLS-1$
                        currRom.setOptional(BooleanUtils.toBoolean(attributes.getValue(i)));
                        break;
                    case STATUS: // $NON-NLS-1$
                        currRom.setDumpStatus(Entity.Status.valueOf(attributes.getValue(i)));
                        break;
                    case "loadflag": //$NON-NLS-1$
                        currRom.setLoadflag(LoadFlag.getEnum(attributes.getValue(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Formats strings securely into lowercase hexadecimal representations with
         * leading zeros.
         * 
         * @param value raw hexadecimal string
         * @param len   expected output length
         * @return formatted hexadecimal string representation
         */
        private String safeHex(String value, int len) {
            value = value.trim();
            if (value.startsWith("0x")) {
                if (len > 8) {
                    final var bi = new BigInteger(value.substring(2), 16).toString(16);
                    return StringUtils.leftPad(bi.toLowerCase(), len - bi.length(), '0');
                } else {
                    final var fmt = "%0" + len + "x";
                    return String.format(fmt, Long.decode(value));
                }
            } else if (value.length() == len)
                return value.toLowerCase();
            else
                return StringUtils.leftPad(value.toLowerCase(), len - value.length(), '0');
        }

        /**
         * Parsed "slotoption" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSlotOption(final Attributes attributes) {
            if (currMachine == null || currSlot == null)
                return;
            final var slotoption = new SlotOption();
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "name": //$NON-NLS-1$
                        slotoption.setName(attributes.getValue(i));
                        currSlot.add(slotoption);
                        break;
                    case "devname": //$NON-NLS-1$
                        slotoption.setDevName(attributes.getValue(i));
                        break;
                    case "default": //$NON-NLS-1$
                        slotoption.setDef(BooleanUtils.toBoolean(attributes.getValue(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Parsed "slot" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSlot(final Attributes attributes) {
            if (currMachine == null)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                if ("name".equals(attributes.getQName(i))) {
                    currSlot = new Slot();
                    currSlot.setName(attributes.getValue(i));
                    currMachine.getSlots().put(currSlot.getName(), currSlot);
                }
            }
        }

        /**
         * Parsed "device_ref" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDeviceRef(final Attributes attributes) {
            if (currMachine == null)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                if ("name".equals(attributes.getQName(i)))
                    currMachine.getDeviceRef().add(attributes.getValue(i));
            }
        }

        /**
         * Parsed "sample" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSample(final Attributes attributes) {
            if (currMachine == null)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equals("name")) {
                    if (currSampleSet == null) {
                        currMachine.setSampleof(currMachine.getBaseName());
                        if (!machineListList.get(0).samplesets.containsName(currMachine.getSampleof())) {
                            currSampleSet = new Samples(currMachine.getSampleof());
                            machineListList.get(0).samplesets.putByName(currSampleSet);
                        } else
                            currSampleSet = machineListList.get(0).samplesets.getByName(currMachine.getSampleof());
                    }
                    currMachine.getSamples().add(currSampleSet.add(new Sample(currSampleSet, attributes.getValue(i))));
                    samplesCnt++;
                }
            }
        }

        /**
         * Parsed "dipvalue" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDipValue(final Attributes attributes) {
            if (currMachine == null || !inCabinetDipSW)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equals("name")) {
                    if ("cocktail".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
                        cabTypeSet.add(CabinetType.cocktail);
                    else if ("upright".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
                        cabTypeSet.add(CabinetType.upright);
                }
            }
        }

        /**
         * Parsed "dipswitch" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDipSwitch(final Attributes attributes) {
            if (currMachine == null)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                if ("name".equals(attributes.getQName(i)) && "cabinet".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
                    inCabinetDipSW = true;
            }
        }

        /**
         * Parsed "extension" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startExtension(final Attributes attributes) {
            if (currMachine == null || currDevice == null)
                return;
            final var ext = currDevice.new Extension();
            currDevice.getExtensions().add(ext);
            for (var i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equals("name"))
                    ext.setName(attributes.getValue(i).trim());
            }
        }

        /**
         * Parsed "instance" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startInstance(final Attributes attributes) {
            if (currMachine == null || currDevice == null)
                return;
            currDevice.setInstance(currDevice.new Instance());
            for (var i = 0; i < attributes.getLength(); i++) {
                if ("name".equals(attributes.getQName(i)))
                    currDevice.getInstance().setName(attributes.getValue(i).trim());
                else if ("briefname".equals(attributes.getQName(i)))
                    currDevice.getInstance().setBriefname(attributes.getValue(i).trim());
            }
        }

        /**
         * Parsed "device" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDevice(final Attributes attributes) {
            if (currMachine == null)
                return;
            currDevice = new Device();
            currMachine.getDevices().add(currDevice);
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "type": //$NON-NLS-1$
                        currDevice.setType(attributes.getValue(i).trim());
                        break;
                    case "tag": //$NON-NLS-1$
                        currDevice.setTag(attributes.getValue(i).trim());
                        break;
                    case "interface": //$NON-NLS-1$
                        currDevice.setIntrface(attributes.getValue(i).trim());
                        break;
                    case "fixed_image": //$NON-NLS-1$
                        currDevice.setFixedImage(attributes.getValue(i).trim());
                        break;
                    case "mandatory": //$NON-NLS-1$
                        currDevice.setMandatory(attributes.getValue(i).trim());
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Parsed "input" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startInput(final Attributes attributes) {
            if (currMachine == null)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "players": //$NON-NLS-1$
                        currMachine.input.setPlayers(attributes.getValue(i));
                        break;
                    case "coins": //$NON-NLS-1$
                        currMachine.input.setCoins(attributes.getValue(i));
                        break;
                    case "service": //$NON-NLS-1$
                        currMachine.input.setService(attributes.getValue(i));
                        break;
                    case "tilt": //$NON-NLS-1$
                        currMachine.input.setTilt(attributes.getValue(i));
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Parsed "publisher" element callback.
         */
        private void startPublisher() {
            if (currSoftware == null)
                return;
            inPublisher = true;
        }

        /**
         * Parsed "manufacturer" element callback.
         */
        private void startManufacturer() {
            if (currMachine == null)
                return;
            inManufacturer = true;
        }

        /**
         * Parsed "year" element callback.
         */
        private void startYear() {
            if (currMachine == null && currSoftware == null)
                return;
            inYear = true;
        }

        /**
         * Parsed global datafile element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDatfile(final Attributes attributes) {
            for (var i = 0; i < attributes.getLength(); i++) {
                if ("build".equals(attributes.getQName(i)))
                    build = attributes.getValue(i);
            }
        }

        /**
         * Parsed "header" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startHeader(@SuppressWarnings("unused") final Attributes attributes) {
            inHeader = true;
        }

        /**
         * Parsed "softwarelist" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSoftwareList(final Attributes attributes) {
            if (currMachine != null)
                startSoftwareListDesc(attributes);
            else {
                currSoftwareList = new SoftwareList(Profile.this);
                for (var i = 0; i < attributes.getLength(); i++) {
                    switch (attributes.getQName(i)) {
                        case "name": //$NON-NLS-1$
                            currSoftwareList.setName(attributes.getValue(i).trim());
                            machineListList.getSoftwareListList().putByName(currSoftwareList);
                            break;
                        case DESCRIPTION: // $NON-NLS-1$
                            currSoftwareList.getDescription().append(attributes.getValue(i).trim());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        /**
         * Parsed machine-associated "softwarelist" descriptor callback.
         * 
         * @param attributes element attributes list
         */
        private void startSoftwareListDesc(final Attributes attributes) {
            final var swlist = currMachine.new SWList();
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "name": //$NON-NLS-1$
                        swlist.setName(attributes.getValue(i));
                        break;
                    case STATUS: // $NON-NLS-1$
                        swlist.setStatus(SWStatus.valueOf(attributes.getValue(i)));
                        break;
                    case "filter": //$NON-NLS-1$
                        swlist.setFilter(attributes.getValue(i));
                        break;
                    default:
                        break;
                }
            }
            currMachine.getSwlists().put(swlist.getName(), swlist);
            if (!machineListList.getSoftwareListDefs().containsKey(swlist.getName()))
                machineListList.getSoftwareListDefs().put(swlist.getName(), new ArrayList<>());
            machineListList.getSoftwareListDefs().get(swlist.getName()).add(currMachine);
        }

        /**
         * Parsed "software" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSoftware(final Attributes attributes) {
            currSoftware = new Software(Profile.this);
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "name": //$NON-NLS-1$
                        currSoftware.setName(attributes.getValue(i).trim());
                        break;
                    case "cloneof": //$NON-NLS-1$
                        currSoftware.setCloneof(attributes.getValue(i).trim());
                        break;
                    case "supported": //$NON-NLS-1$
                        currSoftware.setSupported(Software.Supported.valueOf(attributes.getValue(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Parsed software "feature" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSoftwareFeature(Attributes attributes) {
            if (currSoftware == null)
                return;
            if (attributes.getValue("name").equalsIgnoreCase("compatibility")) //$NON-NLS-1$ //$NON-NLS-2$
                currSoftware.setCompatibility(attributes.getValue("value")); //$NON-NLS-1$
        }

        /**
         * Parsed software "part" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSoftwarePart(Attributes attributes) {
            if (currSoftware == null)
                return;
            currPart = new Part();
            currSoftware.getParts().add(currPart);
            for (var i = 0; i < attributes.getLength(); i++) {
                if ("name".equals(attributes.getQName(i)))
                    currPart.setName(attributes.getValue(i).trim());
                else if ("interface".equals(attributes.getQName(i)))
                    currPart.setIntrface(attributes.getValue(i).trim());
            }
        }

        /**
         * Parsed software "dataarea" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSoftwarePartDataarea(Attributes attributes) {
            if (currSoftware == null || currPart == null)
                return;
            currDataArea = new DataArea();
            currPart.getDataareas().add(currDataArea);
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "name": //$NON-NLS-1$
                        currDataArea.setName(attributes.getValue(i).trim());
                        break;
                    case "size": //$NON-NLS-1$
                    {
                        final var value = attributes.getValue(i).trim();
                        ExceptionUtils.unthrowF(currDataArea::setSize, Integer::decode, value, t -> ExceptionUtils.test(t, "0x" + value, 0));
                        break;
                    }
                    case "width", "databits": //$NON-NLS-1$
                        currDataArea.setDatabits(Integer.valueOf(attributes.getValue(i)));
                        break;
                    case "endianness", "endian": //$NON-NLS-1$
                        currDataArea.setEndianness(Endianness.valueOf(attributes.getValue(i)));
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Parsed software "diskarea" element callback.
         * 
         * @param attributes element attributes list
         */
        private void startSoftwarePartDiskarea(Attributes attributes) {
            if (currSoftware == null || currPart == null)
                return;
            currDiskArea = new DiskArea();
            currPart.getDiskareas().add(currDiskArea);
            for (var i = 0; i < attributes.getLength(); i++) {
                if ("name".equals(attributes.getQName(i)))
                    currDiskArea.setName(attributes.getValue(i).trim());
            }
        }

        /**
         * Parsed machine or game container element callback.
         * 
         * @param attributes element attributes list
         */
        private void startMachine(Attributes attributes) {
            currMachine = new Machine(Profile.this);
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case "name": //$NON-NLS-1$
                        currMachine.setName(attributes.getValue(i).trim());
                        machineListList.get(0).putByName(currMachine);
                        break;
                    case "romof": //$NON-NLS-1$
                        currMachine.setRomof(attributes.getValue(i).trim());
                        break;
                    case "cloneof": //$NON-NLS-1$
                        currMachine.setCloneof(attributes.getValue(i).trim());
                        break;
                    case "sampleof": //$NON-NLS-1$
                        currMachine.setSampleof(attributes.getValue(i).trim());
                        if (!machineListList.get(0).samplesets.containsName(currMachine.getSampleof())) {
                            currSampleSet = new Samples(currMachine.getSampleof());
                            machineListList.get(0).samplesets.putByName(currSampleSet);
                        } else
                            currSampleSet = machineListList.get(0).samplesets.getByName(currMachine.getSampleof());
                        break;
                    case "isbios": //$NON-NLS-1$
                        currMachine.setIsbios(BooleanUtils.toBoolean(attributes.getValue(i)));
                        break;
                    case "ismechanical": //$NON-NLS-1$
                        currMachine.setIsmechanical(BooleanUtils.toBoolean(attributes.getValue(i)));
                        break;
                    case "isdevice": //$NON-NLS-1$
                        currMachine.setIsdevice(BooleanUtils.toBoolean(attributes.getValue(i)));
                        break;
                    case "sourcefile": //$NON-NLS-1$
                        currMachine.setSourcefile(attributes.getValue(i));
                        break;
                    default:
                        break;
                }
            }
            if (currMachine.getRomof() != null && currMachine.getRomof().equals(currMachine.getBaseName()))
                currMachine.setRomof(null);
            if (currMachine.getCloneof() != null && currMachine.getCloneof().equals(currMachine.getBaseName()))
                currMachine.setCloneof(null);
        }

        /**
         * Parsed description container element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDescription(@SuppressWarnings("unused") Attributes attributes) {
            if (currMachine == null && currSoftware == null && currSoftwareList == null)
                return;
            inDescription = true;
        }

        /**
         * Parsed driver specifications element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDriver(Attributes attributes) {
            if (currMachine == null)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                switch (attributes.getQName(i)) {
                    case STATUS: // $NON-NLS-1$
                        currMachine.driver.setStatus(attributes.getValue(i));
                        break;
                    case "emulation": //$NON-NLS-1$
                        currMachine.driver.setEmulation(attributes.getValue(i));
                        break;
                    case "cocktail": //$NON-NLS-1$
                        currMachine.driver.setCocktail(attributes.getValue(i));
                        break;
                    case "savestate": //$NON-NLS-1$
                        currMachine.driver.setSaveState(attributes.getValue(i));
                        break;
                    default:
                        break;
                }
            }
        }

        /**
         * Parsed display specifications element callback.
         * 
         * @param attributes element attributes list
         */
        private void startDisplay(final Attributes attributes) {
            if (currMachine == null)
                return;
            for (var i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equals("rotate")) {
                    ExceptionUtils.unthrow(orientation -> {
                        if (orientation == 0 || orientation == 180)
                            currMachine.setOrientation(Machine.DisplayOrientation.horizontal);
                        if (orientation == 90 || orientation == 270)
                            currMachine.setOrientation(Machine.DisplayOrientation.vertical);
                    }, Integer::parseInt, attributes.getValue(i));
                }
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (qName.equals("header")) //$NON-NLS-1$
            {
                inHeader = false;
            } else if (qName.equals("softwarelist")) //$NON-NLS-1$
            {
                endSoftwareList();
            } else if (qName.equals("software")) //$NON-NLS-1$
            {
                endSoftware();
            } else if (qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
            {
                endMachine();
            } else if (qName.equals("rom")) //$NON-NLS-1$
            {
                endRom();
            } else if (qName.equals("disk")) //$NON-NLS-1$
            {
                endDisk();
            } else if (qName.equals(DESCRIPTION)) // $NON-NLS-1$
            {
                endDescription();
            } else if (qName.equals("year")) //$NON-NLS-1$
            {
                endYear();
            } else if (qName.equals("manufacturer")) //$NON-NLS-1$
            {
                endManufacturer();
            } else if (qName.equals("publisher")) //$NON-NLS-1$
            {
                endPublisher();
            } else if (qName.equals("dipswitch")) //$NON-NLS-1$
            {
                endDipSwitch();
            }
        }

        /**
         * Closes software lists publisher context callback.
         */
        private void endPublisher() {
            if (currSoftware == null)
                return;
            inPublisher = false;
        }

        /**
         * Closes machines manufacturer context callback.
         */
        private void endManufacturer() {
            if (currMachine == null)
                return;
            inManufacturer = false;
        }

        /**
         * Closes year parser context callback.
         */
        private void endYear() {
            if (currMachine == null && currSoftware == null)
                return;
            inYear = false;
        }

        /**
         * Closes description parser context callback.
         */
        private void endDescription() {
            if (currMachine == null && currSoftware == null && currSoftwareList == null)
                return;
            inDescription = false;
        }

        /**
         * Formulates CabinetType orientation constraints from parsed dipswitch
         * elements.
         */
        private void endDipSwitch() {
            if (!inCabinetDipSW || currMachine == null)
                return;
            if (cabTypeSet.contains(CabinetType.cocktail)) {
                if (cabTypeSet.contains(CabinetType.upright))
                    currMachine.setCabinetType(CabinetType.any);
                else
                    currMachine.setCabinetType(CabinetType.cocktail);
            } else
                currMachine.setCabinetType(CabinetType.upright);
            cabTypeSet.clear();
            inCabinetDipSW = false;
        }

        /**
         * Validates and stores parsed CHD disk properties.
         */
        private void endDisk() {
            if (currDisk.getBaseName() != null && !disks.contains(currDisk.getBaseName())) {
                disks.add(currDisk.getBaseName());
                if (currMachine != null) {
                    currMachine.getDisks().add(currDisk);
                    disksCnt++;
                } else if (currSoftware != null) {
                    currSoftware.getDisks().add(currDisk);
                    swdisksCnt++;
                }
            }
            currDisk = null;
        }

        /**
         * Validates, registers, and tracks parsed ROM properties.
         */
        private void endRom() {
            if (currRom.getBaseName() != null) {
                if (!roms.contains(currRom.getBaseName())) {
                    roms.add(currRom.getBaseName());
                    if (currMachine != null) {
                        currMachine.getRoms().add(currRom);
                        romsCnt++;
                    } else if (currSoftware != null) {
                        currSoftware.getRoms().add(currRom);
                        swromsCnt++;
                    }
                }
                endRomCheckSuspiciousCRC();
            }
            currRom = null;
        }

        /**
         * Detects whether ROM element contains suspicious CRC associations.
         */
        private void endRomCheckSuspiciousCRC() {
            if (currRom.getCrc() != null) {
                final var oldRom = romsByCRC.put(currRom.getCrc(), currRom);
                if (oldRom != null) {
                    if (oldRom.getSha1() != null && currRom.getSha1() != null && !oldRom.equals(currRom))
                        suspiciousCRC.add(currRom.getCrc());
                    if (oldRom.getMd5() != null && currRom.getMd5() != null && !oldRom.equals(currRom))
                        suspiciousCRC.add(currRom.getCrc());
                }
            }
        }

        /**
         * Validates and finalizes software list contexts registrations.
         */
        private void endSoftwareList() {
            if (currSoftwareList == null)
                return;
            machineListList.getSoftwareListList().add(currSoftwareList);
            softwaresListCnt++;
            currSoftwareList = null;
        }

        /**
         * Closes machine element building parsing and checks execution cancellation
         * limits.
         * 
         * @throws BreakException if execution is stopped by the user
         */
        private void endMachine() throws BreakException {
            roms.clear();
            disks.clear();
            machineListList.get(0).add(currMachine);
            machinesCnt++;
            currMachine = null;
            currSampleSet = null;
            handler.setProgress(null, null, null, String.format(Messages.getString("Profile.Loaded"), machinesCnt, romsCnt, disksCnt, samplesCnt)); //$NON-NLS-1$
            if (handler.isCancel())
                throw new BreakException();
        }

        /**
         * Closes software item context parsing and validates cancellation controls.
         * 
         * @throws BreakException if execution is stopped by the user
         */
        private void endSoftware() throws BreakException {
            if (currSoftwareList == null || currSoftware == null)
                return;
            roms.clear();
            disks.clear();
            currSoftwareList.add(currSoftware);
            softwaresCnt++;
            currSoftware = null;
            handler.setProgress(null, null, null, String.format(Messages.getString("Profile.SWLoaded"), softwaresCnt, swromsCnt, swdisksCnt)); //$NON-NLS-1$
            if (handler.isCancel())
                throw new BreakException();
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            final var value = new String(ch, start, length);
            if (value.isBlank())
                return;
            if (inDescription) {
                if (currMachine != null)
                    currMachine.description.append(value);
                else if (currSoftware != null)
                    currSoftware.description.append(value);
                else if (currSoftwareList != null)
                    currSoftwareList.getDescription().append(value);
            } else if (inYear) {
                if (currMachine != null)
                    currMachine.year.append(value);
                else if (currSoftware != null)
                    currSoftware.year.append(value);
            } else if (inManufacturer && currMachine != null) {
                currMachine.manufacturer.append(value);
            } else if (inPublisher && currSoftware != null) {
                currSoftware.getPublisher().append(value);
            } else if (inHeader) {
                header.computeIfAbsent(currTag, k -> new StringBuilder()).append(value);
            }
        }
    }

    /**
     * Private internal load parser orchestration.
     * 
     * @param file    the source xml catalog dat
     * @param handler progressive feedback reporter
     * @return true on success, false on errors or cancellations
     */
    private boolean internalLoad(final File file, final ProgressHandler handler) {
        handler.setProgress(String.format(Messages.getString("Profile.Parsing"), new PathAbstractor(session).getRelativePath(file.toPath())), -1); //$NON-NLS-1$
        try (var in = handler.getInputStream(new FileInputStream(file), (int) file.length())) {
            final var factory = SAXParserFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            final var parser = factory.newSAXParser();
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
            parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
            parser.parse(in, new ProfileHandler(handler));
            return true;
        } catch (final ParserConfigurationException | SAXException e) {
            handler.addError(e.getMessage());
            Log.err("Parser Exception", e); //$NON-NLS-1$
        } catch (final IOException e) {
            handler.addError(e.getMessage());
            Log.err("IO Exception", e); //$NON-NLS-1$
        } catch (final BreakException e) {
            return false;
        } catch (final Exception e) {
            handler.addError(e.getMessage());
            Log.err("Other Exception", e); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * Serializes current profile state properties to cached binary files.
     */
    public void save() {
        try (final var oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(session.getUser().getSettings().getCacheFile(nfo.getFile()))))) {
            oos.writeObject(this);
        } catch (final Exception e) {
            // do nothing
        }
    }

    /**
     * Loads profile database configurations from physical file descriptors.
     * 
     * @param session execution workspace context
     * @param file    target source config catalog file (.jrm, .dat, .xml)
     * @param handler progressive feedback reporter
     * @return parsed profile metadata container
     */
    public static Profile load(final Session session, final File file, final ProgressHandler handler) {
        return Profile.load(session, ProfileNFO.load(session, file), handler);
    }

    /**
     * Loads profile properties matching cached descriptors or walk parsers.
     * 
     * @param session execution workspace context
     * @param nfo     JRomManager database profile information stats summary
     * @param handler progressive feedback reporter
     * @return parsed profile metadata container
     */
    public static Profile load(final Session session, final ProfileNFO nfo, final ProgressHandler handler) {
        Profile profile = null;
        final var cachefile = session.getUser().getSettings().getCacheFile(nfo.getFile());
        if (cachefile.lastModified() >= nfo.getFile().lastModified() && (!nfo.isJRM() || cachefile.lastModified() >= nfo.getMame().getFileroms().lastModified())
                && Boolean.TRUE.equals(!session.getUser().getSettings().getProperty(SettingsEnum.debug_nocache, Boolean.class))) // $NON-NLS-1$
        { // Load from cache if cachefile is not outdated and debug_nocache is disabled
            profile = loadCache(session, nfo, handler, profile, cachefile);
        }
        if (profile == null) // if cache failed to load or because it is outdated
            profile = loadThenSaveToCache(session, nfo, handler);
        if (profile == null)
            return null;
        // build parent-clones relations
        handler.setProgress(Messages.getString("Profile.BuildingParentClonesRelations"), -1); //$NON-NLS-1$
        profile.buildParentClonesRelations();
        // update nfo stats (those to keep serialized)
        if (profile.build != null)
            profile.nfo.getStats().setVersion(profile.build);
        else
            profile.nfo.getStats().setVersion(profile.header.containsKey(VERSION) ? profile.header.get(VERSION).toString() : null); // $NON-NLS-1$ //$NON-NLS-2$
        profile.nfo.getStats().setTotalSets(profile.softwaresCnt + profile.machinesCnt);
        profile.nfo.getStats().setTotalRoms(profile.romsCnt + profile.swromsCnt);
        profile.nfo.getStats().setTotalDisks(profile.disksCnt + profile.swdisksCnt);
        profile.nfo.save(session);
        // Load profile settings
        handler.setProgress("Loading settings...", -1); //$NON-NLS-1$
        profile.loadSettings();
        // Build Systems filters
        handler.setProgress("Creating Systems filters...", -1); //$NON-NLS-1$
        profile.loadSystems();
        // Build Years filters
        handler.setProgress("Creating Years filters...", -1); //$NON-NLS-1$
        profile.loadYears();
        // Load cartver.ini (if any)
        profile.loadCatVer(handler);
        // Load nplayers.ini (if any)
        profile.loadNPlayers(handler);

        profile.filterEntities = EnumSet.allOf(EntityStatus.class);
        profile.filterList = EnumSet.allOf(AnywareStatus.class);
        profile.filterListLists = EnumSet.allOf(AnywareStatus.class);

        // return the resulting profile
        return profile;
    }

    /**
     * Parses profile catalog DAT file content and serializes binary database
     * states.
     * 
     * @param session active session context
     * @param nfo     catalogs info summary
     * @param handler progressive feedback reporter
     * @return parsed profile metadata database
     */
    private static Profile loadThenSaveToCache(final Session session, final ProfileNFO nfo, final ProgressHandler handler) {
        Profile profile;
        handler.setInfos(1, true);
        profile = new Profile();
        profile.session = session;
        session.setCurrProfile(null);
        profile.nfo = nfo;
        if (!load(nfo, profile, handler))
            return null;
        session.setCurrProfile(profile);
        // save cache
        handler.setInfos(1, null);
        handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
        profile.save();
        return profile;
    }

    /**
     * Triggers XML parsing on single dat files or paired ROMs + SoftwareLists.
     * 
     * @param nfo     catalogs details stats
     * @param profile parent target empty database
     * @param handler progressive feedback reporter
     * @return true on success, false on failure or cancellation
     */
    private static boolean load(final ProfileNFO nfo, Profile profile, final ProgressHandler handler) {
        if (!nfo.isJRM()) // load DAT file not attached to a JRM
            return (nfo.getFile().exists() && profile.internalLoad(nfo.getFile(), handler));

        // we use JRM file keep ROMs/SL DATs in relation
        if (nfo.getMame().getFileroms() != null) { // load ROMs dat
            if (!nfo.getMame().getFileroms().exists() || !profile.internalLoad(nfo.getMame().getFileroms(), handler))
                return false;
            if (nfo.getMame().getFilesl() != null && (!nfo.getMame().getFilesl().exists() || !profile.internalLoad(nfo.getMame().getFilesl(), handler))) {
                // load SL dat (note that loading software list without ROMs dat is NOT
                // recommended)
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves profile database state properties from standard cache files.
     * 
     * @param session   active session context
     * @param nfo       catalogs info stats
     * @param handler   progressive reporter
     * @param profile   target empty profile object
     * @param cachefile target cache binary file
     * @return loaded profile, or null on cache mismatches
     */
    private static Profile loadCache(final Session session, final ProfileNFO nfo, final ProgressHandler handler, Profile profile, final File cachefile) {
        handler.setInfos(1, null);
        handler.setProgress(Messages.getString("Profile.LoadingCache"), -1); //$NON-NLS-1$
        try (final var ois = new ObjectInputStream(new BufferedInputStream(handler.getInputStream(new FileInputStream(cachefile), (int) cachefile.length())))) {
            profile = (Profile) ois.readObject();
            profile.session = session;
            session.setCurrProfile(profile);
            profile.nfo = nfo;
        } catch (final Exception e) {
            // may fail to load because serialized classes did change since last cache save
        }
        return profile;
    }

    /**
     * Maps parent-clones database relationships sequentially after loading metadata
     * catalog elements.
     */
    private void buildParentClonesRelations() {
        machineListList.forEach(machineList -> machineList.forEach(machine -> {
            if (machine.getRomof() != null) {
                machine.setParent(machineList.getByName(machine.getRomof()));
                if (machine.getParent() != null && !machine.getParent().isIsbios())
                    machine.getParent().getClones().put(machine.getName(), machine);
            }
            machine.getDeviceRef().forEach(deviceRef -> machine.getDeviceMachines().putIfAbsent(deviceRef, machineList.getByName(deviceRef)));
            machine.getSlots().values()
                    .forEach(slot -> slot.forEach(slotoption -> machine.getDeviceMachines().putIfAbsent(slotoption.getDevName(), machineList.getByName(slotoption.getDevName()))));
        }));
        machineListList.getSoftwareListList().forEach(softwareList -> softwareList.forEach(software -> {
            if (software.getCloneof() != null) {
                software.setParent(softwareList.getByName(software.getCloneof()));
                if (software.getParent() != null)
                    software.getParent().getClones().put(software.getName(), software);
            }
        }));
    }

    /**
     * Saves profile XML settings files.
     */
    public void saveSettings() {
        saveSettings(nfo.getFile());
    }

    /**
     * Saves profile XML settings files to custom locations.
     * 
     * @param file destination configurations XML path
     */
    public void saveSettings(File file) {
        settings = session.getUser().getSettings().saveProfileSettings(file, settings);
        nfo.save(session);
    }

    /**
     * Loads profiles XML settings properties.
     */
    public void loadSettings() {
        loadSettings(nfo.getFile());
    }

    /**
     * Loads profiles XML settings properties from specific files.
     * 
     * @param file source configuration xml file
     */
    public void loadSettings(File file) {
        settings = session.getUser().getSettings().loadProfileSettings(file, settings);
    }

    /**
     * Updates Boolean property associations in local profiles configuration maps.
     * 
     * @param property target configuration option key
     * @param value    target option state value
     */
    public void setProperty(final ProfileSettingsEnum property, final boolean value) {
        Log.info(() -> "%s : %b".formatted(property, value));
        settings.setProperty(property, Boolean.toString(value));
    }

    /**
     * Updates string-mapped Boolean property associations.
     * 
     * @param property target option key
     * @param value    target option state value
     */
    public void setProperty(final String property, final boolean value) {
        Log.info(() -> "%s : %b".formatted(property, value));
        settings.setProperty(property, Boolean.toString(value));
    }

    /**
     * Updates string property associations.
     * 
     * @param property target configuration option key
     * @param value    target option text value
     */
    public void setProperty(final ProfileSettingsEnum property, final String value) {
        settings.setProperty(property, value);
    }

    /**
     * Updates string property associations using text keys.
     * 
     * @param property target option text key
     * @param value    target option text value
     */
    public void setProperty(final String property, final String value) {
        settings.setProperty(property, value);
    }

    /**
     * Resolves Boolean settings values or returns defaults if keys are missing.
     * 
     * @param property target option text key
     * @param def      the default option state value
     * @return option state value
     */
    public boolean getProperty(final String property, final boolean def) {
        return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
    }

    /**
     * Resolves integer settings values.
     * 
     * @param property target option text key
     * @param def      the default integer option value
     * @return option value
     */
    public int getProperty(final String property, final int def) {
        return Integer.parseInt(settings.getProperty(property, Integer.toString(def)));
    }

    /**
     * Resolves text settings values.
     * 
     * @param property target option key
     * @param def      the default text value
     * @return option value
     */
    public String getProperty(final String property, final String def) {
        return settings.getProperty(property, def);
    }

    /**
     * Resolves settings values matching expected output classes.
     * 
     * @param property target option key
     * @param cls      expected output class type
     * @param <T>      class template argument
     * @return option value
     */
    public <T> T getProperty(final ProfileSettingsEnum property, Class<T> cls) {
        return settings.getProperty(property, cls);
    }

    /**
     * Resolves settings string values.
     * 
     * @param property target option key
     * @return option text value
     */
    public String getProperty(final ProfileSettingsEnum property) {
        return settings.getProperty(property, String.class);
    }

    private int propsHashCode = 0;

    /**
     * Saves properties checkpoints hash codes to track modifications state.
     */
    public void setPropsCheckPoint() {
        propsHashCode = settings.hashCode();
    }

    /**
     * Compares active properties configurations against saved checkpoints.
     * 
     * @return true if properties changed, false otherwise
     */
    public boolean hasPropsChanged() {
        return propsHashCode != settings.hashCode();
    }

    /**
     * Generates detailed HTML formatted text representation of profile catalogs
     * counts.
     * 
     * @return HTML format text summary
     */
    public String getName() {
        final var xmlpath = session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
        final String fname;
        if (nfo.getFile().toPath().startsWith(xmlpath))
            fname = xmlpath.relativize(nfo.getFile().toPath()).toString();
        else
            fname = nfo.getFile().getName();
        String name = "[" + toBlue(fname) + "] "; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        if (build != null)
            name += toBoldBlack(build); // $NON-NLS-1$ //$NON-NLS-2$
        else if (header.size() > 0) {
            if (header.containsKey(DESCRIPTION)) // $NON-NLS-1$
                name += toBoldBlack(header.get(DESCRIPTION)); // $NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            else if (header.containsKey("name")) //$NON-NLS-1$
            {
                name += toBoldBlack(header.get("name")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                if (header.containsKey(VERSION)) // $NON-NLS-1$
                    name += " (" + header.get(VERSION) + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        var strcnt = ""; //$NON-NLS-1$
        if (!machineListList.get(0).isEmpty())
            strcnt += machinesCnt + " Machines"; //$NON-NLS-1$
        if (!machineListList.getSoftwareListList().isEmpty())
            strcnt += (strcnt.isEmpty() ? "" : ", ") + softwaresListCnt + " Software Lists, " + softwaresCnt + " Softwares"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        name += "(" + strcnt + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return toDocument(name);
    }

    /**
     * Populates standard, mechanical, bios, or software lists categories inside
     * System filter sets.
     */
    public void loadSystems() {
        systems = new Systms();
        systems.add(SystmStandard.STANDARD);
        systems.add(SystmMechanical.MECHANICAL);
        systems.add(SystmDevice.DEVICE);
        final ArrayList<Machine> machines = new ArrayList<>();
        this.sources = new Sources();
        final var srces = new TreeMap<String, Source>();
        machineListList.get(0).forEach(m -> {
            if (m.isIsbios())
                machines.add(m);
            Optional.ofNullable(m.getSourcefile()).ifPresent(s -> srces.compute(s, (k, v) -> v == null ? new Source(k) : v.inc()));
        });
        machines.sort((a, b) -> a.getName().compareTo(b.getName()));
        machines.forEach(systems::add);
        srces.forEach((name, src) -> sources.add(src));
        machineListList.get(0).stream().filter(m -> m.getSourcefile() != null).forEach(m -> m.setSource(srces.get(m.getSourcefile())));

        final ArrayList<SoftwareList> softwarelists = new ArrayList<>();
        machineListList.getSoftwareListList().forEach(softwarelists::add);
        softwarelists.sort((a, b) -> a.getName().compareTo(b.getName()));
        softwarelists.forEach(systems::add);
    }

    /**
     * Compiles distinct release years across parsed machines or software catalogs.
     */
    public void loadYears() {
        final var y = new HashSet<String>();
        y.add(""); //$NON-NLS-1$
        machineListList.get(0).forEach(m -> y.add(m.year.toString()));
        machineListList.getSoftwareListList().forEach(sl -> sl.forEach(s -> y.add(s.year.toString())));
        y.add("????"); //$NON-NLS-1$
        this.years = y;
    }

    /**
     * Loads catver.ini mappings if files are found, linking machines back to
     * subcategories.
     * 
     * @param handler progress reporting monitor
     */
    public void loadCatVer(ProgressHandler handler) {
        try {
            final var file = PathAbstractor.getAbsolutePath(session, getProperty(ProfileSettingsEnum.filter_catver_ini, String.class)).toFile();
            if (!file.exists()) {
                catver = null;
                return;
            }
            if (handler != null)
                handler.setProgress("Loading catver.ini ...", -1); //$NON-NLS-1$
            catver = CatVer.read(this, file); // $NON-NLS-1$
            for (final Category cat : catver) {
                for (final SubCategory subcat : cat) {
                    for (final String game : subcat) {
                        final Machine m = machineListList.get(0).getByName(game);
                        if (m != null)
                            m.setSubcat(subcat);
                    }
                }
            }
        } catch (final Exception e) {
            catver = null;
        }
    }

    /**
     * Loads nplayers.ini capability configurations if files are present.
     * 
     * @param handler progress reporting monitor
     */
    public void loadNPlayers(ProgressHandler handler) {
        try {
            final var file = PathAbstractor.getAbsolutePath(session, getProperty(ProfileSettingsEnum.filter_nplayers_ini, String.class)).toFile();
            if (file.exists()) {
                if (handler != null)
                    handler.setProgress("Loading nplayers.ini ...", -1); //$NON-NLS-1$
                nplayers = NPlayers.read(file); // $NON-NLS-1$
                for (final NPlayer nplayer : nplayers) {
                    for (final String game : nplayer) {
                        final Machine m = machineListList.get(0).getByName(game);
                        if (m != null)
                            m.setNplayer(nplayer);
                    }
                }
            } else
                nplayers = null;
        } catch (final Exception e) {
            nplayers = null;
        }
    }

    /**
     * Computes the cumulative size across all parsed software lists and target
     * machines.
     * 
     * @return cumulative size count
     */
    public int size() {
        return machineListList.size() + machineListList.getSoftwareListList().size();
    }

    /**
     * Computes cumulative size count across filtered visible machine items and
     * software catalogs.
     * 
     * @return filtered visibility size count
     */
    public int filteredSubsize() {
        return (int) machineListList.get(0).getFilteredStream().count() + machineListList.get(0).samplesets.size()
                + (int) machineListList.getSoftwareListList().getFilteredStream().mapToLong(sl -> sl.getFilteredStream().count()).sum();
    }

    /**
     * Computes cumulative size count across all raw machine elements and software
     * lists.
     * 
     * @return raw cumulative entries count
     */
    public int subsize() {
        return machineListList.get(0).size() + machineListList.get(0).samplesets.size() + machineListList.getSoftwareListList().stream().mapToInt(SoftwareList::size).sum();
    }
}
