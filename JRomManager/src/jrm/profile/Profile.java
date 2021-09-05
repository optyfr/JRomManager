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
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.aui.progress.ProgressHandler;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.ExceptionUtils;
import jrm.misc.Log;
import jrm.misc.ProfileSettings;
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
 * Load a Profile which consist of : - loading information about related files
 * (dats, settings, ...) - Read Dats (whatever the format is) into
 * jrm.profile.data.* subclasses - Save a serialization of the resulting Profile
 * class (with all contained subclasses) for later cached reload - Load Profile
 * Settings - Create filters according what was found in dat (systems, years,
 * ...) - Load advanced filters (catver.ini, nplayers.ini, ...)
 * 
 * @author optyfr
 *
 */
public class Profile implements Serializable
{
	private static final long serialVersionUID = 2L;
	
	private static final String DESCRIPTION = "description";
	private static final String VERSION = "version";
	/*
	 * Counters
	 */
	private @Getter long machinesCnt = 0;
	private @Getter long softwaresListCnt = 0;
	private @Getter long softwaresCnt = 0;
	private @Getter long romsCnt = 0;
	private @Getter long swromsCnt = 0;
	private @Getter long disksCnt = 0;
	private @Getter long swdisksCnt = 0;
	private @Getter long samplesCnt = 0;

	/*
	 * Presence flags
	 */
	private @Getter boolean md5Roms = false;
	private @Getter boolean md5Disks = false;
	private @Getter boolean sha1Roms = false;
	private @Getter boolean sha1Disks = false;

	/*
	 * dat build and header informations
	 */
	private @Getter String build = null;
	private final @Getter Map<String, StringBuilder> header = new HashMap<>();

	/**
	 * The main object that will contains all the games AND the related software
	 * list
	 */
	private final @Getter MachineListList machineListList = new MachineListList(this);

	/**
	 * Contains all CRCs where there was ROMs with identical CRC but different
	 * SHA1/MD5
	 */
	private final @Getter Set<String> suspiciousCRC = new HashSet<>();

	/**
	 * Non permanent filter according scan status of anyware lists
	 */
	private transient @Getter @Setter Set<AnywareStatus> filterListLists = null;

	/**
	 * Non permanent filter according scan status of anyware (machines, softwares)
	 */
	private transient @Getter @Setter Set<AnywareStatus> filterList = null;

	/**
	 * Non permanent filter according scan status of entities (roms, disks, samples)
	 */
	private transient @Getter @Setter Set<EntityStatus> filterEntities = null;

	/*
	 * This is all non serialized object (not included in cache), they are
	 * recalculated or reloaded on each Profile load (cached or not)
	 */
	private transient @Getter ProfileSettings settings = null;
	private transient @Getter Systms systems = null;
	private transient @Getter Collection<String> years = null;
	private transient @Getter ProfileNFO nfo = null;
	private transient @Getter @Setter CatVer catver = null;
	private transient @Getter @Setter NPlayers nplayers = null;
	private transient @Getter Session session = null;

	/**
	 * The Profile class is instantiated via
	 * {@link #load(Session, File, ProgressHandler)}
	 */
	private Profile()
	{

	}

	private class ProfileHandler extends DefaultHandler
	{
		private static final String STATUS = "status";
		/*
		 * The following variables are loading state variable
		 */
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

		public ProfileHandler(ProgressHandler handler)
		{
			this.handler = handler;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
		{
			try
			{
				currTag = qName;
				switch(qName)
				{
					case "name":
					case "datafile":
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
					case "machine":
					case "game":
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
			}
			catch (Exception e)
			{
				throw new ProfileHandlerException(getDebugMsg(attributes, qName, e), e);
			}
		}

		private class ProfileHandlerException extends RuntimeException
		{
			private static final long serialVersionUID = 1L;

			public ProfileHandlerException(String message, Exception e)
			{
				super(message, e);
			}
			
		}

		private String getDebugMsg(Attributes attributes, String qName, Exception e)
		{
			final var msg = new StringBuilder("Error");
			if (currMachine != null)
				msg.append(" for machine ").append(currMachine.getName());
			else if (currSoftwareList != null)
			{
				msg.append(" for software list ").append(currSoftwareList.getName());
				if (currSoftware != null)
					msg.append(", software ").append(currSoftware.getName());
			}
			if (currRom != null)
				msg.append(", rom ").append(currRom.getName());
			if (currDisk != null)
				msg.append(", disk ").append(currDisk.getName());
			msg.append(", xmltag=" + qName);
			msg.append(", xmlattributes={");
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if (i > 0)
					msg.append(", ");
				msg.append(attributes.getQName(i) + "=" + attributes.getValue(i));
			}
			msg.append("}");
			msg.append("\nOriginal exception=").append(e.getClass().getSimpleName() + " " + e.getMessage());
			return msg.toString();
		}

		
		/**
		 * @param attributes
		 * @throws NumberFormatException
		 */
		private void startDisk(final Attributes attributes) throws NumberFormatException
		{
			// we enter a disk block for current machine or current software
			if (currMachine == null && currSoftware == null)
				return;
			currDisk = new Disk(currMachine != null ? currMachine : currSoftware);
			if (currSoftware != null && currDiskArea != null)
				currDiskArea.getDisks().add(currDisk);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
					case "name": //$NON-NLS-1$
					{
						String name = attributes.getValue(i).trim();
						if (name.endsWith(".chd"))
							name = name.substring(0, name.length() - 4);
						currDisk.setName(name);
						break;
					}
					case "sha1": //$NON-NLS-1$
						currDisk.setSha1(attributes.getValue(i).toLowerCase());
						sha1Disks = true;
						break;
					case "md5": //$NON-NLS-1$
						currDisk.setMd5(attributes.getValue(i).toLowerCase());
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
					case STATUS: //$NON-NLS-1$
						currDisk.setDumpStatus(Entity.Status.valueOf(attributes.getValue(i)));
						break;
					default:
						break;
				}
			}
		}

		/**
		 * @param attributes
		 * @throws NumberFormatException
		 */
		private void startRom(final Attributes attributes) throws NumberFormatException
		{
			// we enter a rom block for current machine or current software
			if (currMachine == null && currSoftware == null)
				return;
			currRom = new Rom(currMachine != null ? currMachine : currSoftware);
			if (currSoftware != null && currDataArea != null)
				currDataArea.getRoms().add(currRom);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
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
						currRom.setCrc(attributes.getValue(i).toLowerCase());
						break;
					case "sha1": //$NON-NLS-1$
						currRom.setSha1(attributes.getValue(i).toLowerCase());
						sha1Roms = true;
						break;
					case "md5": //$NON-NLS-1$
						currRom.setMd5(attributes.getValue(i).toLowerCase());
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
		 * @param attributes
		 */
		private void startSlotOption(final Attributes attributes)
		{
			// get slot options for current slot in current machine, there can be many per
			// slot
			if (currMachine == null || currSlot == null)
				return;
			final var slotoption = new SlotOption();
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
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
		 * @param attributes
		 */
		private void startSlot(final Attributes attributes)
		{
			// get slots for current machine, there can be many
			if (currMachine == null)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if ("name".equals(attributes.getQName(i)))
				{
					currSlot = new Slot();
					currSlot.setName(attributes.getValue(i));
					currMachine.getSlots().put(currSlot.getName(), currSlot);
				}
			}
		}

		/**
		 * @param attributes
		 */
		private void startDeviceRef(final Attributes attributes)
		{
			// get device references for current machine, there can be many, even with the
			// same name
			if (currMachine == null)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if ("name".equals(attributes.getQName(i)))
					currMachine.getDeviceRef().add(attributes.getValue(i));
			}
		}

		/**
		 * @param attributes
		 */
		private void startSample(final Attributes attributes)
		{
			// get sample names from current machine
			if (currMachine == null)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if (attributes.getQName(i).equals("name"))
				{
					if (currSampleSet == null)
					{
						currMachine.setSampleof(currMachine.getBaseName());
						if (!machineListList.get(0).samplesets.containsName(currMachine.getSampleof()))
						{
							currSampleSet = new Samples(currMachine.getSampleof());
							machineListList.get(0).samplesets.putByName(currSampleSet);
						}
						else
							currSampleSet = machineListList.get(0).samplesets.getByName(currMachine.getSampleof());
					}
					currMachine.getSamples().add(currSampleSet.add(new Sample(currSampleSet, attributes.getValue(i))));
					samplesCnt++;
				}
			}
		}

		/**
		 * @param attributes
		 */
		private void startDipValue(final Attributes attributes)
		{
			// store dipvalue of interesting dipswitch for current machine
			if (currMachine == null || !inCabinetDipSW)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if (attributes.getQName(i).equals("name"))
				{
					if ("cocktail".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
						cabTypeSet.add(CabinetType.cocktail);
					else if ("upright".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
						cabTypeSet.add(CabinetType.upright);
				}
			}
		}

		/**
		 * @param attributes
		 */
		private void startDipSwitch(final Attributes attributes)
		{
			// extract interesting dipswitch value for current machine
			if (currMachine == null)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if ("name".equals(attributes.getQName(i)) && "cabinet".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
					inCabinetDipSW = true;
			}
		}

		/**
		 * @param attributes
		 */
		private void startExtension(final Attributes attributes)
		{
			// This is an extension block for current device, there can be many for each device
			if (currMachine == null || currDevice == null)
				return;
			final var ext = currDevice.new Extension();
			currDevice.getExtensions().add(ext);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if (attributes.getQName(i).equals("name"))
					ext.setName(attributes.getValue(i).trim());
			}
		}

		/**
		 * @param attributes
		 */
		private void startInstance(final Attributes attributes)
		{
			// This is the instance info block for current device
			if (currMachine == null || currDevice == null)
				return;
			currDevice.setInstance(currDevice.new Instance());
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if ("name".equals(attributes.getQName(i)))
					currDevice.getInstance().setName(attributes.getValue(i).trim());
				else if ("briefname".equals(attributes.getQName(i)))
					currDevice.getInstance().setBriefname(attributes.getValue(i).trim());
			}
		}

		/**
		 * @param attributes
		 */
		private void startDevice(final Attributes attributes)
		{
			// This is a device block for current machine, there can be many
			if (currMachine == null)
				return;
			currDevice = new Device();
			currMachine.getDevices().add(currDevice);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
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
		 * @param attributes
		 */
		private void startInput(final Attributes attributes)
		{
			// This is the input info block of current machine
			if (currMachine == null)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
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
		 * 
		 */
		private void startPublisher()
		{
			if (currSoftware == null)
				return;
			// we enter in publisher block for current software
			inPublisher = true;
		}

		/**
		 * 
		 */
		private void startManufacturer()
		{
			if (currMachine == null)
				return;
			// we enter in manufacturer block for current machine
			inManufacturer = true;
		}

		/**
		 * 
		 */
		private void startYear()
		{
			if (currMachine == null && currSoftware == null)
				return;
			// we enter in year block either for current machine, or current software
			inYear = true;
		}

		private void startDatfile(final Attributes attributes)
		{
			// there is currently no build tag in softwarelist format
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if ("build".equals(attributes.getQName(i)))
					build = attributes.getValue(i);
			}
		}

		private void startHeader(final Attributes attributes)	//NOSONAR
		{
			// entering the header bloc
			inHeader = true;
		}

		private void startSoftwareList(final Attributes attributes)
		{
			if (currSoftwareList != null)
				startSoftwareListDesc(attributes);
			else
			{// this is a *real* software list
				currSoftwareList = new SoftwareList(Profile.this);
				for (var i = 0; i < attributes.getLength(); i++)
				{
					switch (attributes.getQName(i))
					{
						case "name": //$NON-NLS-1$
							currSoftwareList.setName(attributes.getValue(i).trim());
							machineListList.getSoftwareListList().putByName(currSoftwareList);
							break;
						case DESCRIPTION: //$NON-NLS-1$
							currSoftwareList.getDescription().append(attributes.getValue(i).trim());
							break;
						default:
							break;
					}
				}
			}
		}

		private void startSoftwareListDesc(final Attributes attributes)
		{
			// This is a machine containing a software list description
			final var swlist = currMachine.new SWList();
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
					case "name": //$NON-NLS-1$
						swlist.setName(attributes.getValue(i));
						break;
					case STATUS: //$NON-NLS-1$
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

		private void startSoftware(final Attributes attributes)
		{
			// We enter in a software block (from a software list)
			currSoftware = new Software(Profile.this);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
					case "name": //$NON-NLS-1$
						currSoftware.setName(attributes.getValue(i).trim());
						break;
					case "cloneof": //$NON-NLS-1$
						currSoftware.setCloneof(attributes.getValue(i));
						break;
					case "supported": //$NON-NLS-1$
						currSoftware.setSupported(Software.Supported.valueOf(attributes.getValue(i)));
						break;
					default:
						break;
				}
			}
		}

		private void startSoftwareFeature(Attributes attributes)
		{
			if (currSoftware == null)
				return;
			// extract interesting features from current software
			if (attributes.getValue("name").equalsIgnoreCase("compatibility")) //$NON-NLS-1$ //$NON-NLS-2$
				currSoftware.setCompatibility(attributes.getValue("value")); //$NON-NLS-1$
		}

		private void startSoftwarePart(Attributes attributes)
		{
			if (currSoftware == null)
				return;
			currPart = new Part();
			// we enter a part in current current software
			currSoftware.getParts().add(currPart);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if ("name".equals(attributes.getQName(i)))
					currPart.setName(attributes.getValue(i).trim());
				else if ("interface".equals(attributes.getQName(i)))
					currPart.setIntrface(attributes.getValue(i).trim());
			}
		}

		private void startSoftwarePartDataarea(Attributes attributes)
		{
			if (currSoftware == null || currPart == null)
				return;
			// we enter a dataarea block in current part
			currDataArea = new DataArea();
			currPart.getDataareas().add(currDataArea);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
					case "name": //$NON-NLS-1$
						currDataArea.setName(attributes.getValue(i).trim());
						break;
					case "size": //$NON-NLS-1$
					{
						final var value = attributes.getValue(i).trim();
						ExceptionUtils.unthrowF(currDataArea::setSize, Integer::decode, value, t -> ExceptionUtils.test(t, "0x" + value, 0));
						break;
					}
					case "width": //$NON-NLS-1$
					case "databits": //$NON-NLS-1$
						currDataArea.setDatabits(Integer.valueOf(attributes.getValue(i)));
						break;
					case "endianness": //$NON-NLS-1$
					case "endian": //$NON-NLS-1$
						currDataArea.setEndianness(Endianness.valueOf(attributes.getValue(i)));
						break;
					default:
						break;
				}
			}
		}

		private void startSoftwarePartDiskarea(Attributes attributes)
		{
			if (currSoftware == null || currPart == null)
				return;
			// we enter a diskarea block in current part
			currDiskArea = new DiskArea();
			currPart.getDiskareas().add(currDiskArea);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if ("name".equals(attributes.getQName(i)))
					currDiskArea.setName(attributes.getValue(i).trim());
			}
		}

		private void startMachine(Attributes attributes)
		{
			// we enter a machine (or a game in case of Logiqx format)
			currMachine = new Machine(Profile.this);
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
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
						if (!machineListList.get(0).samplesets.containsName(currMachine.getSampleof()))
						{
							currSampleSet = new Samples(currMachine.getSampleof());
							machineListList.get(0).samplesets.putByName(currSampleSet);
						}
						else
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
					default:
						break;
				}
			}
			if (currMachine.getRomof() != null && currMachine.getRomof().equals(currMachine.getBaseName()))
				currMachine.setRomof(null);
			if (currMachine.getCloneof() != null && currMachine.getCloneof().equals(currMachine.getBaseName()))
				currMachine.setCloneof(null);
		}

		private void startDescription(Attributes attributes)	//NOSONAR
		{
			if (currMachine == null && currSoftware == null && currSoftwareList == null)
				return;
			// we enter a description either for current machine, current software, or
			// current software list
			inDescription = true;
		}
		
		private void startDriver(Attributes attributes)
		{
			// This is the driver info block of current machine
			if (currMachine == null)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				switch (attributes.getQName(i))
				{
					case STATUS: //$NON-NLS-1$
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
		 * @param attributes
		 */
		private void startDisplay(final Attributes attributes)
		{
			// This is the display info block of current machine
			if (currMachine == null)
				return;
			for (var i = 0; i < attributes.getLength(); i++)
			{
				if (attributes.getQName(i).equals("rotate"))
				{
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
		public void endElement(final String uri, final String localName, final String qName) throws SAXException
		{
			if (qName.equals("header")) //$NON-NLS-1$
			{
				// exiting the header block
				inHeader = false;
			}
			else if (qName.equals("softwarelist")) //$NON-NLS-1$
			{
				endSoftwareList();
			}
			else if (qName.equals("software")) //$NON-NLS-1$
			{
				endSoftware();
			}
			else if (qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
			{
				endMachine();
			}
			else if (qName.equals("rom")) //$NON-NLS-1$
			{
				endRom();
			}
			else if (qName.equals("disk")) //$NON-NLS-1$
			{
				endDisk();
			}
			else if (qName.equals(DESCRIPTION)) //$NON-NLS-1$
			{
				endDescription();
			}
			else if (qName.equals("year")) //$NON-NLS-1$
			{
				endYear();
			}
			else if (qName.equals("manufacturer")) //$NON-NLS-1$
			{
				endManufacturer();
			}
			else if (qName.equals("publisher")) //$NON-NLS-1$
			{
				endPublisher();
			}
			else if (qName.equals("dipswitch")) //$NON-NLS-1$
			{
				endDipSwitch();
			}
		}

		/**
		 * 
		 */
		private void endPublisher()
		{
			if(currSoftware == null)
				return;
			// exiting publisher block
			inPublisher = false;
		}

		/**
		 * 
		 */
		private void endManufacturer()
		{
			if(currMachine == null)
				return;
			// exiting manufacturer block
			inManufacturer = false;
		}

		/**
		 * 
		 */
		private void endYear()
		{
			if(currMachine == null && currSoftware == null)
				return;
			// exiting year block
			inYear = false;
		}

		/**
		 * 
		 */
		private void endDescription()
		{
			if(currMachine == null && currSoftware == null && currSoftwareList == null)
				return;
			// exiting description block
			inDescription = false;
		}

		/**
		 * 
		 */
		private void endDipSwitch()
		{
			if(!inCabinetDipSW || currMachine == null)
				return;
			// exiting dipswitch block
			if (cabTypeSet.contains(CabinetType.cocktail))
			{
				if (cabTypeSet.contains(CabinetType.upright))
					currMachine.setCabinetType(CabinetType.any);
				else
					currMachine.setCabinetType(CabinetType.cocktail);
			}
			else
				currMachine.setCabinetType(CabinetType.upright);
			cabTypeSet.clear();
			inCabinetDipSW = false;
		}

		/**
		 * 
		 */
		private void endDisk()
		{
			// exiting current disk block
			if (currDisk.getBaseName() != null && !disks.contains(currDisk.getBaseName()))
			{
				disks.add(currDisk.getBaseName());
				if (currMachine != null)
				{
					currMachine.getDisks().add(currDisk);
					disksCnt++;
				}
				else if (currSoftware != null)
				{
					currSoftware.getDisks().add(currDisk);
					swdisksCnt++;
				}
			}
			currDisk = null;
		}

		/**
		 * 
		 */
		private void endRom()
		{
			// exiting current rom block
			if (currRom.getBaseName() != null)
			{
				if (!roms.contains(currRom.getBaseName()))
				{
					roms.add(currRom.getBaseName());
					if (currMachine != null)
					{
						currMachine.getRoms().add(currRom);
						romsCnt++;
					}
					else if (currSoftware != null)
					{
						currSoftware.getRoms().add(currRom);
						swromsCnt++;
					}
				}
				endRomCheckSuspiciousCRC();
			}
			currRom = null;
		}

		/**
		 * 
		 */
		private void endRomCheckSuspiciousCRC()
		{
			if (currRom.getCrc() != null)
			{
				final var oldRom = romsByCRC.put(currRom.getCrc(), currRom);
				if (oldRom != null)
				{
					if (oldRom.getSha1() != null && currRom.getSha1() != null && !oldRom.equals(currRom))
						suspiciousCRC.add(currRom.getCrc());
					if (oldRom.getMd5() != null && currRom.getMd5() != null && !oldRom.equals(currRom))
						suspiciousCRC.add(currRom.getCrc());
				}
			}
		}

		/**
		 * 
		 */
		private void endSoftwareList()
		{
			if(currSoftwareList == null)
				return;
			// exiting current software list
			machineListList.getSoftwareListList().add(currSoftwareList);
			softwaresListCnt++;
			currSoftwareList = null;
		}

		/**
		 * @throws BreakException
		 */
		private void endMachine() throws BreakException
		{
			// exiting current machine block
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
		 * @throws BreakException
		 */
		private void endSoftware() throws BreakException
		{
			if(currSoftwareList == null || currSoftware == null)
				return;
			// exiting current software block
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
		public void characters(final char[] ch, final int start, final int length) throws SAXException
		{
			final var value = new String(ch, start, length);
			if(value.isBlank())
				return;
			if (inDescription)
			{
				// we are in description block, so fill up description data for current
				// machine/software/softwarelist
				if (currMachine != null)
					currMachine.description.append(value);
				else if (currSoftware != null)
					currSoftware.description.append(value);
				else if (currSoftwareList != null)
					currSoftwareList.getDescription().append(value);
			}
			else if (inYear)
			{
				// we are in year block, so fill up year data for current machine/software
				if (currMachine != null)
					currMachine.year.append(value);
				else if (currSoftware != null)
					currSoftware.year.append(value);
			}
			else if (inManufacturer && currMachine != null)
			{
				// we are in manufacturer block, so fill up manufacturer data for current
				// machine
				currMachine.manufacturer.append(value);
			}
			else if (inPublisher && currSoftware != null)
			{
				// we are in publisher block, so fill up publisher data for current software
				currSoftware.getPublisher().append(value);
			}
			else if (inHeader)
			{
				// we are in header, so filling header data structure...
				header.computeIfAbsent(currTag, k -> new StringBuilder()).append(value);
			}
		}
	}

	/**
	 * This method will load and parse the dat file
	 * 
	 * @param file
	 *            the xml dat file
	 * @param handler
	 *            a progression handler
	 * @return true on success, false otherwise
	 */
	private boolean internalLoad(final File file, final ProgressHandler handler)
	{
		handler.setProgress(String.format(Messages.getString("Profile.Parsing"), new PathAbstractor(session).getRelativePath(file.toPath())), -1); //$NON-NLS-1$
		try (var in = handler.getInputStream(new FileInputStream(file), (int) file.length()))
		{
			final var factory = SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			final var parser = factory.newSAXParser();
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Compliant
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""); // compliant
			parser.parse(in, new ProfileHandler(handler));
			return true;
		}
		catch (final ParserConfigurationException | SAXException e)
		{
			handler.addError(e.getMessage());
			Log.err("Parser Exception", e); //$NON-NLS-1$
		}
		catch (final IOException e)
		{
			handler.addError(e.getMessage());
			Log.err("IO Exception", e); //$NON-NLS-1$
		}
		catch (final BreakException e)
		{
			return false;
		}
		catch (final Exception e)
		{
			handler.addError(e.getMessage());
			Log.err("Other Exception", e); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Save cache (serialization)
	 */
	public void save()
	{
		try (final var oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(session.getUser().getSettings().getCacheFile(nfo.getFile())))))
		{
			oos.writeObject(this);
		}
		catch (final Exception e)
		{
			// do nothing
		}
	}

	/**
	 * Load Profile from a {@link File}
	 * 
	 * @param file
	 *            the {@link File} to load (should be .jrm, .dat, or .xml)
	 * @param handler
	 *            the {@link ProgressHandler} to see progression (mandatory)
	 * @return the loaded {@link Profile}
	 */
	public static Profile load(final Session session, final File file, final ProgressHandler handler)
	{
		return Profile.load(session, ProfileNFO.load(session, file), handler);
	}

	/**
	 * Load Profile given a {@link ProfileNFO}
	 * 
	 * @param nfo
	 *            the {@link ProfileNFO} from which to load the Profile
	 * @param handler
	 *            the {@link ProgressHandler} to see progression (mandatory)
	 * @return the loaded {@link Profile}, or null if there was something wrong
	 */
	public static Profile load(final Session session, final ProfileNFO nfo, final ProgressHandler handler)
	{
		Profile profile = null;
		final var cachefile = session.getUser().getSettings().getCacheFile(nfo.getFile());
		if (cachefile.lastModified() >= nfo.getFile().lastModified() && (!nfo.isJRM() || cachefile.lastModified() >= nfo.getMame().getFileroms().lastModified()) && !session.getUser().getSettings().getProperty(SettingsEnum.debug_nocache, false)) // $NON-NLS-1$
		{ // Load from cache if cachefile is not outdated and debug_nocache is disabled
			profile = loadCache(session, nfo, handler, profile, cachefile);
		}
		if (profile == null) // if cache failed to load or because it is outdated
			profile = loadThenSaveToCache(session, nfo, handler);
		if(profile==null)
			return null;
		// build parent-clones relations
		handler.setProgress(Messages.getString("Profile.BuildingParentClonesRelations"), -1); //$NON-NLS-1$
		profile.buildParentClonesRelations();
		// update nfo stats (those to keep serialized)
		if(profile.build!=null)
			profile.nfo.getStats().setVersion(profile.build);
		else
			profile.nfo.getStats().setVersion(profile.header.containsKey(VERSION) ? profile.header.get(VERSION).toString() : null); //$NON-NLS-1$ //$NON-NLS-2$
		profile.nfo.getStats().setTotalSets(profile.softwaresCnt + profile.machinesCnt);
		profile.nfo.getStats().setTotalRoms(profile.romsCnt + profile.swromsCnt);
		profile.nfo.getStats().setTotalDisks(profile.disksCnt + profile.swdisksCnt);
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
	 * @param session
	 * @param nfo
	 * @param handler
	 * @return
	 */
	private static Profile loadThenSaveToCache(final Session session, final ProfileNFO nfo, final ProgressHandler handler)
	{
		Profile profile;
		handler.setInfos(1, true);
		profile = new Profile();
		profile.session = session;
		session.setCurrProfile(null);
		profile.nfo = nfo;
		if(!load(nfo, profile, handler))
			return null;
		session.setCurrProfile(profile);
		// save cache
		handler.setInfos(1, null);
		handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
		profile.save();
		return profile;
	}

	/**
	 * @param nfo
	 * @param profile
	 * @param handler
	 */
	private static boolean load(final ProfileNFO nfo, Profile profile, final ProgressHandler handler)
	{
		if (!nfo.isJRM()) // load DAT file not attached to a JRM
			return (nfo.getFile().exists() && profile.internalLoad(nfo.getFile(), handler));
		
		// we use JRM file keep ROMs/SL DATs in relation
		if (nfo.getMame().getFileroms() != null)
		{ // load ROMs dat
			if (!nfo.getMame().getFileroms().exists() || !profile.internalLoad(nfo.getMame().getFileroms(), handler))
				return false;
			if (nfo.getMame().getFilesl() != null && (!nfo.getMame().getFilesl().exists() || !profile.internalLoad(nfo.getMame().getFilesl(), handler)))
			{
				// load SL dat (note that loading software list without ROMs dat is NOT recommended)
				return false;
			}
		}
		return true;
	}

	/**
	 * @param session
	 * @param nfo
	 * @param handler
	 * @param profile
	 * @param cachefile
	 * @return
	 */
	private static Profile loadCache(final Session session, final ProfileNFO nfo, final ProgressHandler handler, Profile profile, final File cachefile)
	{
		handler.setInfos(1, null);
		handler.setProgress(Messages.getString("Profile.LoadingCache"), -1); //$NON-NLS-1$
		try (final var ois = new ObjectInputStream(new BufferedInputStream(handler.getInputStream(new FileInputStream(cachefile), (int) cachefile.length()))))
		{
			profile = (Profile) ois.readObject();
			profile.session = session;
			session.setCurrProfile(profile);
			profile.nfo = nfo;
		}
		catch (final Exception e)
		{
			// may fail to load because serialized classes did change since last cache save
		}
		return profile;
	}

	/**
	 * build Parent-Clones relationships also add related device_ref machines and
	 * slot devices machines
	 */
	private void buildParentClonesRelations()
	{
		machineListList.forEach(machineList -> machineList.forEach(machine -> {
			if (machine.getRomof() != null)
			{
				machine.setParent(machineList.getByName(machine.getRomof()));
				if (machine.getParent() != null && !machine.getParent().isIsbios())
					machine.getParent().getClones().put(machine.getName(), machine);
			}
			machine.getDeviceRef().forEach(deviceRef -> machine.getDeviceMachines().putIfAbsent(deviceRef, machineList.getByName(deviceRef)));
			machine.getSlots().values().forEach(slot -> slot.forEach(slotoption -> machine.getDeviceMachines().putIfAbsent(slotoption.getDevName(), machineList.getByName(slotoption.getDevName()))));
		}));
		machineListList.getSoftwareListList().forEach(softwareList -> softwareList.forEach(software -> {
			if (software.getCloneof() != null)
			{
				software.setParent(softwareList.getByName(software.getCloneof()));
				if (software.getParent() != null)
					software.getParent().getClones().put(software.getName(), software);
			}
		}));
	}

	/**
	 * Save settings as XML
	 */
	public void saveSettings()
	{
		saveSettings(nfo.getFile());
	}

	/**
	 * Save settings as XML
	 */
	public void saveSettings(File file)
	{
		settings = session.getUser().getSettings().saveProfileSettings(file, settings);
		nfo.save(session);
	}

	/**
	 * Load settings from XML settings file
	 */
	public void loadSettings()
	{
		loadSettings(nfo.getFile());
	}

	/**
	 * Load settings from XML settings file
	 */
	public void loadSettings(File file)
	{
		settings = session.getUser().getSettings().loadProfileSettings(file, settings);
	}

	/**
	 * Set a boolean property
	 * 
	 * @param property
	 *            the property name
	 * @param value
	 *            the boolean property value
	 */
	public void setProperty(final Enum<?> property, final boolean value)
	{
		settings.setProperty(property, Boolean.toString(value));
	}

	public void setProperty(final String property, final boolean value)
	{
		settings.setProperty(property, Boolean.toString(value));
	}

	/**
	 * Set a string property
	 * 
	 * @param property
	 *            the property name
	 * @param value
	 *            the string property value
	 */
	public void setProperty(final Enum<?> property, final String value)
	{
		settings.setProperty(property, value);
	}

	public void setProperty(final String property, final String value)
	{
		settings.setProperty(property, value);
	}

	/**
	 * get a property boolean value
	 * 
	 * @param property
	 *            the property name
	 * @param def
	 *            the default boolean value if no property is defined
	 * @return the property value if it exists, otherwise {@code def} is returned
	 */
	public boolean getProperty(final Enum<?> property, final boolean def)
	{
		return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
	}

	public boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
	}

	/**
	 * get a property int value
	 * 
	 * @param property
	 *            the property name
	 * @param def
	 *            the default int value if no property is defined
	 * @return the property value if it exists, otherwise {@code def} is returned
	 */
	public int getProperty(final Enum<?> property, final int def)
	{
		return Integer.parseInt(settings.getProperty(property, Integer.toString(def)));
	}

	public int getProperty(final String property, final int def)
	{
		return Integer.parseInt(settings.getProperty(property, Integer.toString(def)));
	}

	/**
	 * get a property string value
	 * 
	 * @param property
	 *            the property name
	 * @param def
	 *            the default string value if no property is defined
	 * @return the property value if it exists, otherwise {@code def} is returned
	 */
	public String getProperty(final Enum<?> property, final String def)
	{
		return settings.getProperty(property, def);
	}

	public String getProperty(final String property, final String def)
	{
		return settings.getProperty(property, def);
	}

	/**
	 * will retain the latest properties checkpoint hashCode
	 */
	private int propsHashCode = 0;

	/**
	 * store properties check point
	 */
	public void setPropsCheckPoint()
	{
		propsHashCode = settings.hashCode();
	}

	/**
	 * compare the current properties hashCode with the latest checkpoint
	 * 
	 * @return true if properties did change from last check point, false otherwise
	 */
	public boolean hasPropsChanged()
	{
		return propsHashCode != settings.hashCode();
	}

	/**
	 * get a descriptive name from current profile to show in status bar
	 * 
	 * @return an html string
	 */
	public String getName()
	{
		String name = "<html><body>[<span style='color:blue'>" + session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().relativize(nfo.getFile().toPath()) + "</span>] "; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (build != null)
			name += "<b>" + build + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
		else if (header.size() > 0)
		{
			if (header.containsKey(DESCRIPTION)) //$NON-NLS-1$
				name += "<b>" + header.get(DESCRIPTION) + "</b>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			else if (header.containsKey("name")) //$NON-NLS-1$
			{
				name += "<b>" + header.get("name") + "</b>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				if (header.containsKey(VERSION)) //$NON-NLS-1$
					name += " (" + header.get(VERSION) + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		var strcnt = ""; //$NON-NLS-1$
		if (!machineListList.get(0).isEmpty())
			strcnt += machinesCnt + " Machines"; //$NON-NLS-1$
		if (!machineListList.getSoftwareListList().isEmpty())
			strcnt += (strcnt.isEmpty() ? "" : ", ") + softwaresListCnt + " Software Lists, " + softwaresCnt + " Softwares"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		name += "(" + strcnt + ")</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$
		return name;
	}

	/**
	 * build Systems filter by collecting "bios" machines and software lists (plus
	 * some special ones)
	 */
	public void loadSystems()
	{
		systems = new Systms();
		systems.add(SystmStandard.STANDARD);
		systems.add(SystmMechanical.MECHANICAL);
		systems.add(SystmDevice.DEVICE);
		final ArrayList<Machine> machines = new ArrayList<>();
		machineListList.get(0).forEach(m -> {
			if (m.isIsbios())
				machines.add(m);
		});
		machines.sort((a, b) -> a.getName().compareTo(b.getName()));
		machines.forEach(systems::add);
		final ArrayList<SoftwareList> softwarelists = new ArrayList<>();
		machineListList.getSoftwareListList().forEach(softwarelists::add);
		softwarelists.sort((a, b) -> a.getName().compareTo(b.getName()));
		softwarelists.forEach(systems::add);
	}

	/**
	 * build Years filter by collecting year values from machines and softwares
	 */
	public void loadYears()
	{
		final var y = new HashSet<String>();
		y.add(""); //$NON-NLS-1$
		machineListList.get(0).forEach(m -> y.add(m.year.toString()));
		machineListList.getSoftwareListList().forEach(sl -> sl.forEach(s -> y.add(s.year.toString())));
		y.add("????"); //$NON-NLS-1$
		this.years = y;
	}

	/**
	 * load catver.ini and build a game {@literal <->} cat/subcat relationship
	 * 
	 * @param handler
	 */
	public void loadCatVer(ProgressHandler handler)
	{
		try
		{
			final var file = PathAbstractor.getAbsolutePath(session, getProperty(SettingsEnum.filter_catver_ini, null)).toFile();
			if (file.exists())
			{
				catver=null;
				return;
			}
			if (handler != null)
				handler.setProgress("Loading catver.ini ...", -1); //$NON-NLS-1$
			catver = CatVer.read(this, file); // $NON-NLS-1$
			for (final Category cat : catver)
			{
				for (final SubCategory subcat : cat)
				{
					for (final String game : subcat)
					{
						final Machine m = machineListList.get(0).getByName(game);
						if (m != null)
							m.setSubcat(subcat);
					}
				}
			}
		}
		catch (final Exception e)
		{
			catver = null;
		}
	}

	/**
	 * load nplayers.ini and build a game {@literal <->} nplayer relationship
	 * 
	 * @param handler
	 */
	public void loadNPlayers(ProgressHandler handler)
	{
		try
		{
			final var file = PathAbstractor.getAbsolutePath(session, getProperty(SettingsEnum.filter_nplayers_ini, null)).toFile();
			if (file.exists())
			{
				if (handler != null)
					handler.setProgress("Loading nplayers.ini ...", -1); //$NON-NLS-1$
				nplayers = NPlayers.read(file); // $NON-NLS-1$
				for (final NPlayer nplayer : nplayers)
				{
					for (final String game : nplayer)
					{
						final Machine m = machineListList.get(0).getByName(game);
						if (m != null)
							m.setNplayer(nplayer);
					}
				}
			}
			else
				nplayers = null;
		}
		catch (final Exception e)
		{
			nplayers = null;
		}
	}

	/**
	 * get number of lists
	 * 
	 * @return an int which is 1 + the sum of all software lists
	 */
	public int size()
	{
		return machineListList.size() + machineListList.getSoftwareListList().size();
	}

	/**
	 * get number of entities after filtering
	 * 
	 * @return an int which is the sum of machines, samples, and softwares after
	 *         filtering
	 */
	public int filteredSubsize()
	{
		return (int) machineListList.get(0).getFilteredStream().count() + machineListList.get(0).samplesets.size() + (int) machineListList.getSoftwareListList().getFilteredStream().mapToLong(sl -> sl.getFilteredStream().count()).sum();
	}

	/**
	 * get number of entities
	 * 
	 * @return an int which is the sum of machines, samples, and softwares
	 */
	public int subsize()
	{
		return machineListList.get(0).size() + machineListList.get(0).samplesets.size() + machineListList.getSoftwareListList().stream().mapToInt(SoftwareList::size).sum();
	}
}
