/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.IntStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.ProfileSettings;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.Device;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityStatus;
import jrm.profile.data.Machine;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.SWList;
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
import jrm.security.Session;
import jrm.ui.progress.ProgressHandler;

/**
 * Load a Profile which consist of :
 * - loading information about related files (dats, settings, ...)
 * - Read Dats (whatever the format is) into jrm.profile.data.* subclasses
 * - Save a serialization of the resulting Profile class (with all contained subclasses) for later cached reload 
 * - Load Profile Settings
 * - Create filters according what was found in dat (systems, years, ...)
 * - Load advanced filters (catver.ini, nplayers.ini, ...)
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Profile implements Serializable
{

	/*
	 * Counters
	 */
	public long machines_cnt = 0;
	public long softwares_list_cnt = 0;
	public long softwares_cnt = 0;
	public long roms_cnt = 0, swroms_cnt = 0;
	public long disks_cnt = 0, swdisks_cnt = 0;
	public long samples_cnt = 0;

	/*
	 * Presence flags
	 */
	public boolean md5_roms = false;
	public boolean md5_disks = false;
	public boolean sha1_roms = false;
	public boolean sha1_disks = false;

	/*
	 * dat build and header informations  
	 */
	public String build = null;
	public final HashMap<String, StringBuffer> header = new HashMap<>();
	
	/**
	 * The main object that will contains all the games AND the related software list
	 */
	public final MachineListList machinelist_list = new MachineListList(this);
	
	/**
	 * Contains all CRCs where there was ROMs with identical CRC but different SHA1/MD5
	 */
	public final HashSet<String> suspicious_crc = new HashSet<>();

	/**
	 * Non permanent filter according scan status of anyware lists
	 */
	public transient EnumSet<AnywareStatus> filter_ll = null;

	/**
	 * Non permanent filter according scan status of anyware (machines, softwares)
	 */
	public transient EnumSet<AnywareStatus> filter_l = null;

	/**
	 * Non permanent filter according scan status of entities (roms, disks, samples)
	 */
	public transient EnumSet<EntityStatus> filter_e = null;

	/*
	 * This is all non serialized object (not included in cache), they are recalculated or reloaded on each Profile load (cached or not) 
	 */
	public transient ProfileSettings settings = null;
	public transient Systms systems = null;
	public transient Collection<String> years = null;
	public transient ProfileNFO nfo = null;
	public transient CatVer catver = null;
	public transient NPlayers nplayers = null;
	public transient Session session = null;

	/**
	 * The Profile class is instantiated via {@link #load(Session, File, ProgressHandler)}
	 */
	private Profile()
	{

	}

	/**
	 * This method will load and parse the dat file
	 * @param file the xml dat file
	 * @param handler a progression handler
	 * @return true on success, false otherwise
	 */
	private boolean _load(final File file, final ProgressHandler handler)
	{
		handler.setProgress(String.format(Messages.getString("Profile.Parsing"), file), -1); //$NON-NLS-1$
		try (InputStream in = handler.getInputStream(new FileInputStream(file), (int) file.length()))
		{
			final SAXParserFactory factory = SAXParserFactory.newInstance();
		/*	factory.setValidating(false);
			factory.setNamespaceAware(true);
			factory.setFeature("http://xml.org/sax/features/namespaces", false);
			factory.setFeature("http://xml.org/sax/features/validation", false);*/
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			final SAXParser parser = factory.newSAXParser();
			parser.parse(in, new DefaultHandler()
			{
				/*
				 * The following variables are loading state variable
				 */
				private final HashMap<String, Rom> roms_bycrc = new HashMap<>();
				private boolean in_description = false;
				private boolean in_year = false;
				private boolean in_manufacturer = false;
				private boolean in_publisher = false;
				private boolean in_header = false;
				private boolean in_cabinet_dipsw = false;
				private final EnumSet<CabinetType> cabtype_set = EnumSet.noneOf(CabinetType.class);
				private SoftwareList curr_software_list = null;
				private Software curr_software = null;
				private Software.Part curr_part = null;
				private Software.Part.DataArea curr_dataarea = null;
				private Software.Part.DiskArea curr_diskarea = null;
				private Machine curr_machine = null;
				private Device curr_device = null;
				private Samples curr_sampleset = null;
				private Rom curr_rom = null;
				private Disk curr_disk = null;
				private Slot curr_slot = null;
				private final HashSet<String> roms = new HashSet<>();
				private final HashSet<String> disks = new HashSet<>();
				private String curr_tag;

				@Override
				public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
				{
					curr_tag = qName;
					if (qName.equals("mame") || qName.equals("datafile")) //$NON-NLS-1$ //$NON-NLS-2$
					{	
						// there is currently no build tag in softwarelist format
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "build": //$NON-NLS-1$
									build = attributes.getValue(i);
							}
						}
					}
					else if (qName.equals("header")) //$NON-NLS-1$
					{
						// entering the header bloc
						in_header = true;
					}
					else if (qName.equals("softwarelist") && curr_machine == null) //$NON-NLS-1$
					{
						// this is a *real* software list
						curr_software_list = new SoftwareList(Profile.this);
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_software_list.setName(attributes.getValue(i).trim());
									machinelist_list.softwarelist_list.putByName(curr_software_list);
									break;
								case "description": //$NON-NLS-1$
									curr_software_list.description.append(attributes.getValue(i).trim());
									break;
							}
						}
					}
					else if (qName.equals("softwarelist")) //$NON-NLS-1$
					{
						// This is a machine containing a software list description
						final SWList swlist = curr_machine.new SWList();
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									swlist.name = attributes.getValue(i);
									break;
								case "status": //$NON-NLS-1$
									swlist.status = SWStatus.valueOf(attributes.getValue(i));
									break;
								case "filter": //$NON-NLS-1$
									swlist.filter = attributes.getValue(i);
									break;
							}
						}
						curr_machine.swlists.put(swlist.name, swlist);
						if (!machinelist_list.softwarelist_defs.containsKey(swlist.name))
							machinelist_list.softwarelist_defs.put(swlist.name, new ArrayList<>());
						machinelist_list.softwarelist_defs.get(swlist.name).add(curr_machine);
					}
					else if (qName.equals("software")) //$NON-NLS-1$
					{
						// We enter in a software block (from a software list)
						curr_software = new Software(Profile.this);
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_software.setName(attributes.getValue(i).trim());
									break;
								case "cloneof": //$NON-NLS-1$
									curr_software.cloneof = attributes.getValue(i);
									break;
								case "supported": //$NON-NLS-1$
									curr_software.supported = Software.Supported.valueOf(attributes.getValue(i));
									break;
							}
						}
					}
					else if (qName.equals("feature") && curr_software != null) //$NON-NLS-1$
					{
						// extract interesting features from current software
						if (attributes.getValue("name").equalsIgnoreCase("compatibility")) //$NON-NLS-1$ //$NON-NLS-2$
							curr_software.compatibility = attributes.getValue("value"); //$NON-NLS-1$
					}
					else if (qName.equals("part") && curr_software != null) //$NON-NLS-1$
					{
						// we enter a part in current current software
						curr_software.parts.add(curr_part = new Part());
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_part.name = attributes.getValue(i).trim();
									break;
								case "interface": //$NON-NLS-1$
									curr_part.intrface = attributes.getValue(i).trim();
									break;
							}
						}
					}
					else if (qName.equals("dataarea") && curr_software != null && curr_part != null) //$NON-NLS-1$
					{
						// we enter a dataarea block in current part
						curr_part.dataareas.add(curr_dataarea = new DataArea());
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_dataarea.name = attributes.getValue(i).trim();
									break;
								case "size": //$NON-NLS-1$
									try
									{
										curr_dataarea.size = Integer.decode(attributes.getValue(i).trim());
									}
									catch (NumberFormatException e)
									{
										try
										{
											curr_dataarea.size = Integer.decode("0x"+attributes.getValue(i).trim());
										}
										catch (NumberFormatException e1)
										{
											curr_dataarea.size = 0;
										}
									}
									break;
								case "width": //$NON-NLS-1$
								case "databits": //$NON-NLS-1$
									curr_dataarea.databits = Integer.valueOf(attributes.getValue(i));
									break;
								case "endianness": //$NON-NLS-1$
								case "endian": //$NON-NLS-1$
									curr_dataarea.endianness = Endianness.valueOf(attributes.getValue(i));
									break;
							}
						}
					}
					else if (qName.equals("diskarea") && curr_software != null && curr_part != null) //$NON-NLS-1$
					{
						// we enter a diskarea block in current part
						curr_part.diskareas.add(curr_diskarea = new DiskArea());
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_diskarea.name = attributes.getValue(i).trim();
									break;
							}
						}
					}
					else if (qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						// we enter a machine (or a game in case of Logiqx format)
						curr_machine = new Machine(Profile.this);
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_machine.setName(attributes.getValue(i).trim());
									machinelist_list.get(0).putByName(curr_machine);
									break;
								case "romof": //$NON-NLS-1$
									curr_machine.romof = attributes.getValue(i).trim();
									break;
								case "cloneof": //$NON-NLS-1$
									curr_machine.cloneof = attributes.getValue(i).trim();
									break;
								case "sampleof": //$NON-NLS-1$
									curr_machine.sampleof = attributes.getValue(i).trim();
									if (!machinelist_list.get(0).samplesets.containsName(curr_machine.sampleof))
										machinelist_list.get(0).samplesets.putByName(curr_sampleset = new Samples(curr_machine.sampleof));
									else
										curr_sampleset = machinelist_list.get(0).samplesets.getByName(curr_machine.sampleof);
									break;
								case "isbios": //$NON-NLS-1$
									curr_machine.isbios = BooleanUtils.toBoolean(attributes.getValue(i));
									break;
								case "ismechanical": //$NON-NLS-1$
									curr_machine.ismechanical = BooleanUtils.toBoolean(attributes.getValue(i));
									break;
								case "isdevice": //$NON-NLS-1$
									curr_machine.isdevice = BooleanUtils.toBoolean(attributes.getValue(i));
									break;
							}
						}
					}
					else if (qName.equals("description") && (curr_machine != null || curr_software != null || curr_software_list != null)) //$NON-NLS-1$
					{
						// we enter a description either for current machine, current software, or current software list
						in_description = true;
					}
					else if (qName.equals("year") && (curr_machine != null || curr_software != null)) //$NON-NLS-1$
					{
						// we enter in year block either for current machine, or current software
						in_year = true;
					}
					else if (qName.equals("manufacturer") && (curr_machine != null)) //$NON-NLS-1$
					{
						// we enter in manufacturer block for current machine
						in_manufacturer = true;
					}
					else if (qName.equals("publisher") && (curr_software != null)) //$NON-NLS-1$
					{
						// we enter in publisher block for current software
						in_publisher = true;
					}
					else if (qName.equals("driver")) //$NON-NLS-1$
					{
						// This is the driver info block of current machine 
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "status": //$NON-NLS-1$
										curr_machine.driver.setStatus(attributes.getValue(i));
										break;
									case "emulation": //$NON-NLS-1$
										curr_machine.driver.setEmulation(attributes.getValue(i));
										break;
									case "cocktail": //$NON-NLS-1$
										curr_machine.driver.setCocktail(attributes.getValue(i));
										break;
									case "savestate": //$NON-NLS-1$
										curr_machine.driver.setSaveState(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if (qName.equals("display")) //$NON-NLS-1$
					{
						// This is the display info block of current machine 
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "rotate": //$NON-NLS-1$
									{
										try
										{
											final Integer orientation = Integer.parseInt(attributes.getValue(i));
											if (orientation == 0 || orientation == 180)
												curr_machine.orientation = Machine.DisplayOrientation.horizontal;
											if (orientation == 90 || orientation == 270)
												curr_machine.orientation = Machine.DisplayOrientation.vertical;
										}
										catch (final NumberFormatException e)
										{
										}
										break;
									}
								}
							}
						}
					}
					else if (qName.equals("input")) //$NON-NLS-1$
					{
						// This is the input info block of current machine 
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "players": //$NON-NLS-1$
										curr_machine.input.setPlayers(attributes.getValue(i));
										break;
									case "coins": //$NON-NLS-1$
										curr_machine.input.setCoins(attributes.getValue(i));
										break;
									case "service": //$NON-NLS-1$
										curr_machine.input.setService(attributes.getValue(i));
										break;
									case "tilt": //$NON-NLS-1$
										curr_machine.input.setTilt(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if (qName.equals("device")) //$NON-NLS-1$
					{
						// This is a device block for current machine, there can be many
						if (curr_machine != null)
						{
							curr_device = new Device();
							curr_machine.devices.add(curr_device);
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "type": //$NON-NLS-1$
										curr_device.type = attributes.getValue(i).trim();
										break;
									case "tag": //$NON-NLS-1$
										curr_device.tag = attributes.getValue(i).trim();
										break;
									case "interface": //$NON-NLS-1$
										curr_device.intrface = attributes.getValue(i).trim();
										break;
									case "fixed_image": //$NON-NLS-1$
										curr_device.fixed_image = attributes.getValue(i).trim();
										break;
									case "mandatory": //$NON-NLS-1$
										curr_device.mandatory = attributes.getValue(i).trim();
										break;
								}
							}
						}
					}
					else if (qName.equals("instance")) //$NON-NLS-1$
					{
						// This is the instance info block for current device
						if (curr_machine != null && curr_device != null)
						{
							curr_device.instance = curr_device.new Instance();
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_device.instance.name = attributes.getValue(i).trim();
										break;
									case "briefname": //$NON-NLS-1$
										curr_device.instance.briefname = attributes.getValue(i).trim();
										break;
								}
							}
						}
					}
					else if (qName.equals("extension")) //$NON-NLS-1$
					{
						// This is an extension block for current device, there can be many for each device
						if (curr_machine != null && curr_device != null)
						{
							Device.Extension ext = curr_device.new Extension();
							curr_device.extensions.add(ext);
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										ext.name = attributes.getValue(i).trim();
										break;
								}
							}
						}
					}
					else if (qName.equals("dipswitch")) //$NON-NLS-1$
					{
						// extract interesting dipswitch value for current machine
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										if ("cabinet".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
											in_cabinet_dipsw = true;
								}
							}
						}
					}
					else if (qName.equals("dipvalue")) //$NON-NLS-1$
					{
						// store dipvalue of interesting dipswitch for current machine
						if (curr_machine != null && in_cabinet_dipsw)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										if ("cocktail".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
											cabtype_set.add(CabinetType.cocktail);
										else if ("upright".equalsIgnoreCase(attributes.getValue(i))) //$NON-NLS-1$
											cabtype_set.add(CabinetType.upright);
								}
							}

						}
					}
					else if (qName.equals("sample")) //$NON-NLS-1$
					{
						// get sample names from current machine
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										if (curr_sampleset == null)
										{
											curr_machine.sampleof = curr_machine.getBaseName();
											if (!machinelist_list.get(0).samplesets.containsName(curr_machine.sampleof))
												machinelist_list.get(0).samplesets.putByName(curr_sampleset = new Samples(curr_machine.sampleof));
											else
												curr_sampleset = machinelist_list.get(0).samplesets.getByName(curr_machine.sampleof);
										}
										curr_machine.samples.add(curr_sampleset.add(new Sample(curr_sampleset, attributes.getValue(i))));
										samples_cnt++;
										break;
								}
							}
						}
					}
					else if (qName.equals("device_ref")) //$NON-NLS-1$
					{
						// get device references for current machine, there can be many, even with the same name
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_machine.device_ref.add(attributes.getValue(i));
								}
							}
						}
					}
					else if (qName.equals("slot")) //$NON-NLS-1$
					{
						// get slots for current machine, there can be many
						if (curr_machine != null)
						{
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_slot=new Slot();
										curr_slot.name = attributes.getValue(i);
										curr_machine.slots.put(curr_slot.name, curr_slot);
										break;
								}
							}
						}
					}
					else if (qName.equals("slotoption")) //$NON-NLS-1$
					{
						// get slot options for current slot in current machine, there can be many per slot
						if (curr_machine != null && curr_slot != null)
						{
							final SlotOption slotoption = new SlotOption();
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										slotoption.setName(attributes.getValue(i));
										curr_slot.add(slotoption);
										break;
									case "devname": //$NON-NLS-1$
										slotoption.devname = attributes.getValue(i);
										break;
									case "default": //$NON-NLS-1$
										slotoption.def = BooleanUtils.toBoolean(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if (qName.equals("rom")) //$NON-NLS-1$
					{
						// we enter a rom block for current machine or current software
						if (curr_machine != null || curr_software != null)
						{
							curr_rom = new Rom(curr_machine != null ? curr_machine : curr_software);
							if (curr_software != null && curr_dataarea != null)
								curr_dataarea.roms.add(curr_rom);
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_rom.setName(attributes.getValue(i).trim());
										break;
									case "size": //$NON-NLS-1$
										curr_rom.size = Long.decode(attributes.getValue(i));
										break;
									case "offset": //$NON-NLS-1$
										try
										{
											curr_rom.offset = Integer.decode(attributes.getValue(i));
										}
										catch (final NumberFormatException e)
										{
											curr_rom.offset = Integer.decode("0x" + attributes.getValue(i)); //$NON-NLS-1$
										}
										break;
									case "value": //$NON-NLS-1$
										curr_rom.value = attributes.getValue(i);
										break;
									case "crc": //$NON-NLS-1$
										curr_rom.crc = attributes.getValue(i).toLowerCase();
										break;
									case "sha1": //$NON-NLS-1$
										curr_rom.sha1 = attributes.getValue(i).toLowerCase();
										sha1_roms = true;
										break;
									case "md5": //$NON-NLS-1$
										curr_rom.md5 = attributes.getValue(i).toLowerCase();
										md5_roms = true;
										break;
									case "merge": //$NON-NLS-1$
										curr_rom.merge = attributes.getValue(i).trim();
										break;
									case "bios": //$NON-NLS-1$
										curr_rom.bios = attributes.getValue(i);
										break;
									case "region": //$NON-NLS-1$
										curr_rom.region = attributes.getValue(i);
										break;
									case "date": //$NON-NLS-1$
										curr_rom.date = attributes.getValue(i);
										break;
									case "optional": //$NON-NLS-1$
										curr_rom.optional = BooleanUtils.toBoolean(attributes.getValue(i));
										break;
									case "status": //$NON-NLS-1$
										curr_rom.status = Entity.Status.valueOf(attributes.getValue(i));
										break;
									case "loadflag": //$NON-NLS-1$
										curr_rom.loadflag = LoadFlag.getEnum(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if (qName.equals("disk")) //$NON-NLS-1$
					{
						// we enter a disk block for current machine or current software
						if (curr_machine != null || curr_software != null)
						{
							curr_disk = new Disk(curr_machine != null ? curr_machine : curr_software);
							if (curr_software != null && curr_diskarea != null)
								curr_diskarea.disks.add(curr_disk);
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_disk.setName(attributes.getValue(i).trim());
										break;
									case "sha1": //$NON-NLS-1$
										curr_disk.sha1 = attributes.getValue(i).toLowerCase();
										sha1_disks = true;
										break;
									case "md5": //$NON-NLS-1$
										curr_disk.md5 = attributes.getValue(i).toLowerCase();
										md5_disks = true;
										break;
									case "merge": //$NON-NLS-1$
										curr_disk.merge = attributes.getValue(i).trim();
										break;
									case "index": //$NON-NLS-1$
										curr_disk.index = Integer.decode(attributes.getValue(i));
										break;
									case "optional": //$NON-NLS-1$
										curr_disk.optional = BooleanUtils.toBoolean(attributes.getValue(i));
										break;
									case "writeable": //$NON-NLS-1$
										curr_disk.writeable = BooleanUtils.toBoolean(attributes.getValue(i));
										break;
									case "region": //$NON-NLS-1$
										curr_disk.region = attributes.getValue(i);
										break;
									case "status": //$NON-NLS-1$
										curr_disk.status = Entity.Status.valueOf(attributes.getValue(i));
										break;
								}
							}
						}
					}
				}

				@Override
				public void endElement(final String uri, final String localName, final String qName) throws SAXException
				{
					if (qName.equals("header")) //$NON-NLS-1$
					{
						// exiting the header block
						in_header = false;
					}
					else if (qName.equals("softwarelist") && curr_software_list != null) //$NON-NLS-1$
					{
						// exiting current software list
						machinelist_list.softwarelist_list.add(curr_software_list);
						softwares_list_cnt++;
						curr_software_list = null;
					}
					else if (qName.equals("software")) //$NON-NLS-1$
					{
						// exiting current software block
						roms.clear();
						disks.clear();
						curr_software_list.add(curr_software);
						softwares_cnt++;
						curr_software = null;
						handler.setProgress(String.format(Messages.getString("Profile.SWLoaded"), softwares_cnt, swroms_cnt, swdisks_cnt)); //$NON-NLS-1$
						if (handler.isCancel())
							throw new BreakException();
					}
					else if (qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						// exiting current machine block
						roms.clear();
						disks.clear();
						machinelist_list.get(0).add(curr_machine);
						machines_cnt++;
						curr_machine = null;
						curr_sampleset = null;
						handler.setProgress(String.format(Messages.getString("Profile.Loaded"), machines_cnt, roms_cnt, disks_cnt, samples_cnt)); //$NON-NLS-1$
						if (handler.isCancel())
							throw new BreakException();
					}
					else if (qName.equals("rom")) //$NON-NLS-1$
					{
						// exiting current rom block
						if (curr_rom.getBaseName() != null)
						{
							if (!roms.contains(curr_rom.getBaseName()))
							{
								roms.add(curr_rom.getBaseName());
								if (curr_machine != null)
								{
									curr_machine.roms.add(curr_rom);
									roms_cnt++;
								}
								else
								{
									curr_software.roms.add(curr_rom);
									swroms_cnt++;
								}
							}
							if (curr_rom.crc != null)
							{
								final Rom old_rom = roms_bycrc.put(curr_rom.crc, curr_rom);
								if (old_rom != null)
								{
									if (old_rom.sha1 != null && curr_rom.sha1 != null)
										if (!old_rom.equals(curr_rom))
											suspicious_crc.add(curr_rom.crc);
									if (old_rom.md5 != null && curr_rom.md5 != null)
										if (!old_rom.equals(curr_rom))
											suspicious_crc.add(curr_rom.crc);
								}
							}
						}
					}
					else if (qName.equals("disk")) //$NON-NLS-1$
					{
						// exiting current disk block
						if (curr_disk.getBaseName() != null)
						{
							if (!disks.contains(curr_disk.getBaseName()))
							{
								disks.add(curr_disk.getBaseName());
								if (curr_machine != null)
								{
									curr_machine.disks.add(curr_disk);
									disks_cnt++;
								}
								else
								{
									curr_software.disks.add(curr_disk);
									swdisks_cnt++;
								}
							}
						}
					}
					else if (qName.equals("description") && (curr_machine != null || curr_software != null || curr_software_list != null)) //$NON-NLS-1$
					{
						// exiting description block
						in_description = false;
					}
					else if (qName.equals("year") && (curr_machine != null || curr_software != null)) //$NON-NLS-1$
					{
						// exiting year block
						in_year = false;
					}
					else if (qName.equals("manufacturer") && (curr_machine != null)) //$NON-NLS-1$
					{
						// exiting manufacturer block
						in_manufacturer = false;
					}
					else if (qName.equals("publisher") && (curr_software != null)) //$NON-NLS-1$
					{
						// exiting publisher block
						in_publisher = false;
					}
					else if (qName.equals("dipswitch") && in_cabinet_dipsw) //$NON-NLS-1$
					{
						// exiting dipswitch block
						if (cabtype_set.contains(CabinetType.cocktail))
						{
							if (cabtype_set.contains(CabinetType.upright))
								curr_machine.cabinetType = CabinetType.any;
							else
								curr_machine.cabinetType = CabinetType.cocktail;
						}
						else
							curr_machine.cabinetType = CabinetType.upright;
						cabtype_set.clear();
						in_cabinet_dipsw = false;
					}
				}

				@Override
				public void characters(final char[] ch, final int start, final int length) throws SAXException
				{
					if (in_description)
					{
						// we are in description block, so fill up description data for current machine/software/softwarelist 
						if (curr_machine != null)
							curr_machine.description.append(ch, start, length);
						else if (curr_software != null)
							curr_software.description.append(ch, start, length);
						else if (curr_software_list != null)
							curr_software_list.description.append(ch, start, length);
					}
					else if (in_year)
					{
						// we are in year block, so fill up year data for current machine/software
						if (curr_machine != null)
							curr_machine.year.append(ch, start, length);
						else if (curr_software != null)
							curr_software.year.append(ch, start, length);
					}
					else if (in_manufacturer && curr_machine != null)
					{
						// we are in manufacturer block, so fill up manufacturer data for current machine
						curr_machine.manufacturer.append(ch, start, length);
					}
					else if (in_publisher && curr_software != null)
					{
						// we are in publisher block, so fill up publisher data for current software
						curr_software.publisher.append(ch, start, length);
					}
					else if (in_header)
					{
						 //  we are in header, so filling header data structure...
						if (!header.containsKey(curr_tag))
							header.put(curr_tag, new StringBuffer());
						header.get(curr_tag).append(ch, start, length);
					}
				}
			});
			return true;
		}
		catch (final ParserConfigurationException | SAXException e)
		{
			Log.err("Parser Exception", e); //$NON-NLS-1$
		}
		catch (final IOException e)
		{
			Log.err("IO Exception", e); //$NON-NLS-1$
		}
		catch (final BreakException e)
		{
			return false;
		}
		catch (final Throwable e)
		{
			Log.err("Other Exception", e); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Save cache (serialization)
	 */
	public void save()
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(session.getUser().settings.getCacheFile(nfo.file)))))
		{
			oos.writeObject(this);
		}
		catch (final Throwable e)
		{

		}
	}

	/**
	 * Load Profile from a {@link File}
	 * @param file the {@link File} to load (should be .jrm, .dat, or .xml)
	 * @param handler the {@link ProgressHandler} to see progression (mandatory)
	 * @return the loaded {@link Profile}
	 */
	public static Profile load(final Session session, final File file, final ProgressHandler handler)
	{
		return Profile.load(session, ProfileNFO.load(session, file), handler);
	}

	/**
	 * Load Profile given a {@link ProfileNFO}
	 * @param nfo the {@link ProfileNFO} from which to load the Profile
	 * @param handler the {@link ProgressHandler} to see progression (mandatory)
	 * @return the loaded {@link Profile}, or null if there was something wrong
	 */
	public static Profile load(final Session session, final ProfileNFO nfo, final ProgressHandler handler)
	{
		Profile profile = null;
		final File cachefile = session.getUser().settings.getCacheFile(nfo.file);
		if (cachefile.lastModified() >= nfo.file.lastModified() && (!nfo.isJRM() || cachefile.lastModified() >= nfo.mame.fileroms.lastModified()) && !session.getUser().settings.getProperty("debug_nocache", false)) //$NON-NLS-1$
		{	// Load from cache if cachefile is not outdated and debug_nocache is disabled
			handler.setProgress(Messages.getString("Profile.LoadingCache"), -1); //$NON-NLS-1$
			try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(handler.getInputStream(new FileInputStream(cachefile), (int) cachefile.length()))))
			{
				profile = (Profile) ois.readObject();
				profile.session = session;
				session.curr_profile = profile;
				profile.nfo = nfo;
			}
			catch (final Throwable e)
			{
				// may fail to load because serialized classes did change since last cache save 
			}
		}
		if (profile == null)	// if cache failed to load or because it is outdated
		{
			profile = new Profile();
			profile.session = session;
			session.curr_profile = profile;
			profile.nfo = nfo;
			if (nfo.isJRM())
			{	// we use JRM file keep ROMs/SL DATs in relation
				if (nfo.mame.fileroms != null)
				{	// load ROMs dat
					if (!nfo.mame.fileroms.exists() || !profile._load(nfo.mame.fileroms, handler))
						return null;
					if (nfo.mame.filesl != null)
					{	// load SL dat (note that loading software list without ROMs dat is NOT recommended)
						if (!nfo.mame.filesl.exists() || !profile._load(nfo.mame.filesl, handler))
							return null;
					}
				}
			}
			else
			{	// load DAT file not attached to a JRM
				if (!nfo.file.exists() || !profile._load(nfo.file, handler))
					return null;
			}
			// save cache
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			profile.save();
		}
		// build parent-clones relations
		handler.setProgress(Messages.getString("Profile.BuildingParentClonesRelations"), -1); //$NON-NLS-1$
		profile.buildParentClonesRelations();
		// update nfo stats (those to keep serialized)
		profile.nfo.stats.version = profile.build != null ? profile.build : (profile.header.containsKey("version") ? profile.header.get("version").toString() : null); //$NON-NLS-1$ //$NON-NLS-2$
		profile.nfo.stats.totalSets = profile.softwares_cnt + profile.machines_cnt;
		profile.nfo.stats.totalRoms = profile.roms_cnt + profile.swroms_cnt;
		profile.nfo.stats.totalDisks = profile.disks_cnt + profile.swdisks_cnt;
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
		
		profile.filter_e = EnumSet.allOf(EntityStatus.class);
		profile.filter_l = EnumSet.allOf(AnywareStatus.class);
		profile.filter_ll = EnumSet.allOf(AnywareStatus.class);
		
		// return the resulting profile
		return profile;
	}

	/**
	 * build Parent-Clones relationships
	 * also add related device_ref machines and slot devices machines
	 */
	private void buildParentClonesRelations()
	{
		machinelist_list.forEach(machine_list -> {
			machine_list.forEach(machine -> {
				if (machine.romof != null)
				{
					machine.setParent(machine_list.getByName(machine.romof));
					if (machine.parent != null)
					{
						if (!machine.getParent().isbios)
							machine.getParent().clones.put(machine.getName(), machine);
					}
				}
				machine.device_ref.forEach(device_ref -> machine.device_machines.putIfAbsent(device_ref, machine_list.getByName(device_ref)));
				machine.slots.values().forEach(slot -> slot.forEach(slotoption -> machine.device_machines.putIfAbsent(slotoption.devname, machine_list.getByName(slotoption.devname))));
			});
		});
		machinelist_list.softwarelist_list.forEach(software_list -> {
			software_list.forEach(software -> {
				if (software.cloneof != null)
				{
					software.setParent(software_list.getByName(software.cloneof));
					if (software.parent != null)
						software.getParent().clones.put(software.getName(), software);
				}
			});
		});
	}

	/**
	 * Save settings as XML
	 */
	public void saveSettings()
	{
		try
		{
			settings = session.getUser().settings.saveProfileSettings(nfo.file, settings);
			nfo.save(session);
		}
		catch (final IOException e)
		{
			Log.err("IO", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Load settings from XML settings file
	 */
	public void loadSettings()
	{
		try
		{
			settings = session.getUser().settings.loadProfileSettings(nfo.file, settings);
		}
		catch (final IOException e)
		{
			Log.err("IO", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Set a boolean property
	 * @param property the property name
	 * @param value the boolean property value
	 */
	public void setProperty(final String property, final boolean value)
	{
		settings.setProperty(property, Boolean.toString(value));
	}

	/**
	 * Set a string property
	 * @param property the property name
	 * @param value the string property value
	 */
	public void setProperty(final String property, final String value)
	{
		settings.setProperty(property, value);
	}

	/**
	 * get a property boolean value
	 * @param property the property name
	 * @param def the default boolean value if no property is defined
	 * @return the property value if it exists, otherwise {@code def} is returned
	 */
	public boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
	}

	/**
	 * get a property string value
	 * @param property the property name
	 * @param def the default string value if no property is defined
	 * @return the property value if it exists, otherwise {@code def} is returned
	 */
	public String getProperty(final String property, final String def)
	{
		return settings.getProperty(property, def);
	}

	/**
	 * will retain the latest properties checkpoint hashCode
	 */
	private int props_hashcode = 0;

	/**
	 * store properties check point
	 */
	public void setPropsCheckPoint()
	{
		props_hashcode = settings.hashCode();
	}

	/**
	 * compare the current properties hashCode with the latest checkpoint
	 * @return true if properties did change from last check point, false otherwise
	 */
	public boolean hasPropsChanged()
	{
		return props_hashcode != settings.hashCode();
	}

	/**
	 * get a descriptive name from current profile to show in status bar
	 * @return an html string
	 */
	public String getName()
	{
		String name = "<html><body>[<span style='color:blue'>" + session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().relativize(nfo.file.toPath()) + "</span>] "; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		if (build != null)
			name += "<b>" + build + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
		else if (header.size() > 0)
		{
			if (header.containsKey("description")) //$NON-NLS-1$
				name += "<b>" + header.get("description") + "</b>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			else if (header.containsKey("name")) //$NON-NLS-1$
			{
				name += "<b>" + header.get("name") + "</b>"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				if (header.containsKey("version")) //$NON-NLS-1$
					name += " (" + header.get("version") + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		String strcnt = ""; //$NON-NLS-1$
		if (machinelist_list.get(0).size() > 0)
			strcnt += machines_cnt + " Machines"; //$NON-NLS-1$
		if (machinelist_list.softwarelist_list.size() > 0)
			strcnt += (strcnt.isEmpty() ? "" : ", ") + softwares_list_cnt + " Software Lists, " + softwares_cnt + " Softwares"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		name += "(" + strcnt + ")</body></html>"; //$NON-NLS-1$ //$NON-NLS-2$
		return name;
	}

	/**
	 * build Systems filter by collecting "bios" machines and software lists (plus some special ones)
	 */
	public void loadSystems()
	{
		systems = new Systms();
		systems.add(SystmStandard.STANDARD);
		systems.add(SystmMechanical.MECHANICAL);
		systems.add(SystmDevice.DEVICE);
		final ArrayList<Machine> machines = new ArrayList<>();
		machinelist_list.get(0).forEach(m -> {
			if (m.isbios)
				machines.add(m);
		});
		machines.sort((a, b) -> a.getName().compareTo(b.getName()));
		machines.forEach(systems::add);
		final ArrayList<SoftwareList> softwarelists = new ArrayList<>();
		machinelist_list.softwarelist_list.forEach(softwarelists::add);
		softwarelists.sort((a, b) -> a.getName().compareTo(b.getName()));
		softwarelists.forEach(systems::add);
	}

	/**
	 * build Years filter by collecting year values from machines and softwares 
	 */
	public void loadYears()
	{
		final HashSet<String> years = new HashSet<>();
		years.add(""); //$NON-NLS-1$
		machinelist_list.get(0).forEach(m -> years.add(m.year.toString()));
		machinelist_list.softwarelist_list.forEach(sl -> sl.forEach(s -> years.add(s.year.toString())));
		years.add("????"); //$NON-NLS-1$
		this.years = years;
	}

	/**
	 * load catver.ini and build a game {@literal <->} cat/subcat relationship
	 * @param handler 
	 */
	public void loadCatVer(ProgressHandler handler)
	{
		try
		{
			final File file = new File(getProperty("filter.catver.ini", null));
			if(file.exists())
			{
				if (handler != null)
					handler.setProgress("Loading catver.ini ...", -1); //$NON-NLS-1$
				catver = CatVer.read(this, file); //$NON-NLS-1$
				for (final Category cat : catver)
				{
					for (final SubCategory subcat : cat)
					{
						for (final String game : subcat)
						{
							final Machine m = machinelist_list.get(0).getByName(game);
							if (m != null)
								m.subcat = subcat;
						}
					}
				}
			}
			else
				catver = null;
		}
		catch (final Throwable e)
		{
			catver = null;
		}
	}

	/**
	 * load nplayers.ini and build a game {@literal <->} nplayer relationship
	 * @param handler 
	 */
	public void loadNPlayers(ProgressHandler handler)
	{
		try
		{
			final File file = new File(getProperty("filter.nplayers.ini", null));
			if(file.exists())
			{
				if (handler != null)
					handler.setProgress("Loading nplayers.ini ...", -1); //$NON-NLS-1$
				nplayers = NPlayers.read(file); //$NON-NLS-1$
				for (final NPlayer nplayer : nplayers)
				{
					for (final String game : nplayer)
					{
						final Machine m = machinelist_list.get(0).getByName(game);
						if (m != null)
							m.nplayer = nplayer;
					}
				}
			}
			else
				nplayers = null;
		}
		catch (final Throwable e)
		{
			nplayers = null;
		}
	}

	/**
	 * get number of lists
	 * @return an int which is 1 + the sum of all software lists
	 */
	public int size()
	{
		return machinelist_list.size() + machinelist_list.softwarelist_list.size();
	}

	/**
	 * get number of entities
	 * @return an int which is the sum of machines, samples, and softwares
	 */
	public int subsize()
	{
		return machinelist_list.get(0).size() + machinelist_list.get(0).samplesets.size() + machinelist_list.softwarelist_list.stream().flatMapToInt(sl -> IntStream.of(sl.size())).sum();
	}
}
