package jrm.profile;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.BooleanUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profile.data.*;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.SWList;
import jrm.profile.data.Machine.SWStatus;
import jrm.ui.ProgressHandler;

@SuppressWarnings("serial")
public class Profile implements Serializable
{

	public long machines_cnt = 0;
	public long softwares_list_cnt = 0;
	public long softwares_cnt = 0;
	public long roms_cnt = 0;
	public long disks_cnt = 0;

	public boolean md5_roms = false;
	public boolean md5_disks = false;
	public boolean sha1_roms = false;
	public boolean sha1_disks = false;

	public String build = null;
	public HashMap<String, StringBuffer> header = new HashMap<>();
	public MachineListList machinelist_list = new MachineListList();
	public HashSet<String> suspicious_crc = new HashSet<>();

	public transient Properties settings = null;
	public transient Systms systems;
	public transient ProfileNFO nfo;
	public static transient Profile curr_profile;

	private Profile()
	{

	}

	public boolean _load(File file, ProgressHandler handler)
	{
		handler.setProgress(String.format(Messages.getString("Profile.Parsing"), file), -1); //$NON-NLS-1$
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			SAXParser parser = factory.newSAXParser();
			parser.parse(file, new DefaultHandler()
			{
				private HashMap<String, Rom> roms_bycrc = new HashMap<>();
				private boolean in_software_list = false;
				private boolean in_software = false;
				private boolean in_machine = false;
				private boolean in_description = false;
				private boolean in_year = false;
				private boolean in_manufacturer = false;
				private boolean in_publisher = false;
				private boolean in_header = false;
				private boolean in_cabinet_dipsw = false;
				private EnumSet<CabinetType> cabtype_set = EnumSet.noneOf(CabinetType.class);
				private SoftwareList curr_software_list = null;
				private Software curr_software = null;
				private Machine curr_machine = null;
				private Rom curr_rom = null;
				private Disk curr_disk = null;
				private HashMap<String, Rom> roms = new HashMap<>();
				private HashMap<String, Disk> disks = new HashMap<>();

				private String curr_tag;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
				{
					curr_tag = qName;
					if(qName.equals("mame") || qName.equals("datafile")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
							{
								case "build": //$NON-NLS-1$
									build = attributes.getValue(i);
							}
						}
					}
					else if(qName.equals("header")) //$NON-NLS-1$
					{
						in_header = true;
					}
					else if(qName.equals("softwarelist") && !in_machine) //$NON-NLS-1$
					{
						in_software_list = true;
						curr_software_list = new SoftwareList();
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_software_list.name = attributes.getValue(i).trim();
									machinelist_list.softwarelist_list.sl_byname.put(curr_software_list.name, curr_software_list);
									break;
								case "description": //$NON-NLS-1$
									curr_software_list.description = new StringBuffer(attributes.getValue(i).trim());
									break;
							}
						}
					}
					else if(qName.equals("softwarelist")) //$NON-NLS-1$
					{
						SWList swlist = curr_machine.new SWList();
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
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
						curr_machine.swlists.put(swlist.name,swlist);
						if(!machinelist_list.softwarelist_defs.containsKey(swlist.name))
							machinelist_list.softwarelist_defs.put(swlist.name, new ArrayList<>());
						machinelist_list.softwarelist_defs.get(swlist.name).add(curr_machine);
					}
					else if(qName.equals("software")) //$NON-NLS-1$
					{
						in_software = true;
						curr_software = new Software();
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
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
					else if(qName.equals("feature") && in_software) //$NON-NLS-1$
					{
						if(attributes.getValue("name").equalsIgnoreCase("compatibility"))
							curr_software.compatibility = attributes.getValue("value");
					}
					else if(qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						in_machine = true;
						curr_machine = new Machine();
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_machine.setName(attributes.getValue(i).trim());
									machinelist_list.get(0).m_byname.put(curr_machine.getName(), curr_machine);
									break;
								case "romof": //$NON-NLS-1$
									curr_machine.romof = attributes.getValue(i).trim();
									break;
								case "cloneof": //$NON-NLS-1$
									curr_machine.cloneof = attributes.getValue(i).trim();
									break;
								case "sampleof": //$NON-NLS-1$
									curr_machine.sampleof = attributes.getValue(i).trim();
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
					else if(qName.equals("description") && (in_machine || in_software || in_software_list)) //$NON-NLS-1$
					{
						in_description = true;
					}
					else if(qName.equals("year") && (in_machine || in_software)) //$NON-NLS-1$
					{
						in_year = true;
					}
					else if(qName.equals("manufacturer") && (in_machine)) //$NON-NLS-1$
					{
						in_manufacturer = true;
					}
					else if(qName.equals("publisher") && (in_software)) //$NON-NLS-1$
					{
						in_publisher = true;
					}
					else if(qName.equals("driver")) //$NON-NLS-1$
					{
						if(in_machine)
						{
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
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
					else if(qName.equals("display")) //$NON-NLS-1$
					{
						if(in_machine)
						{
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "rotate":
									{
										try
										{
											Integer orientation = Integer.parseInt(attributes.getValue(i));
											if(orientation == 0 || orientation == 180)
												curr_machine.orientation = Machine.DisplayOrientation.horizontal;
											if(orientation == 90 || orientation == 270)
												curr_machine.orientation = Machine.DisplayOrientation.vertical;
										}
										catch(NumberFormatException e)
										{
										}
										break;
									}
								}
							}
						}
					}
					else if(qName.equals("input")) //$NON-NLS-1$
					{
						if(in_machine)
						{
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
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
					else if(qName.equals("dipswitch")) //$NON-NLS-1$
					{
						if(in_machine)
						{
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name":
										if("cabinet".equalsIgnoreCase(attributes.getValue(i)))
											in_cabinet_dipsw = true;
								}
							}
						}
					}
					else if(qName.equals("dipvalue")) //$NON-NLS-1$
					{
						if(in_machine && in_cabinet_dipsw)
						{
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name":
										if("cocktail".equalsIgnoreCase(attributes.getValue(i)))
											cabtype_set.add(CabinetType.cocktail);
										else if("upright".equalsIgnoreCase(attributes.getValue(i)))
											cabtype_set.add(CabinetType.upright);
								}
							}

						}
					}
					else if(qName.equals("rom")) //$NON-NLS-1$
					{
						if(in_machine || in_software)
						{
							curr_rom = new Rom(in_machine ? curr_machine : curr_software);
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_rom.setName(attributes.getValue(i).trim());
										break;
									case "size": //$NON-NLS-1$
										curr_rom.size = Long.decode(attributes.getValue(i));
										break;
									case "crc": //$NON-NLS-1$
										curr_rom.crc = attributes.getValue(i);
										break;
									case "sha1": //$NON-NLS-1$
										curr_rom.sha1 = attributes.getValue(i);
										sha1_roms = true;
										break;
									case "md5": //$NON-NLS-1$
										curr_rom.md5 = attributes.getValue(i);
										md5_roms = true;
										break;
									case "merge": //$NON-NLS-1$
										curr_rom.merge = attributes.getValue(i).trim();
										break;
									case "bios": //$NON-NLS-1$
										curr_rom.bios = attributes.getValue(i);
										break;
									case "status": //$NON-NLS-1$
										curr_rom.status = Entity.Status.valueOf(attributes.getValue(i));
										break;
								}
							}
						}
					}
					else if(qName.equals("disk")) //$NON-NLS-1$
					{
						if(in_machine || in_software)
						{
							curr_disk = new Disk(in_machine ? curr_machine : curr_software);
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_disk.setName(attributes.getValue(i).trim());
										break;
									case "sha1": //$NON-NLS-1$
										curr_disk.sha1 = attributes.getValue(i);
										sha1_disks = true;
										break;
									case "md5": //$NON-NLS-1$
										curr_disk.md5 = attributes.getValue(i);
										md5_disks = true;
										break;
									case "merge": //$NON-NLS-1$
										curr_disk.merge = attributes.getValue(i).trim();
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
				public void endElement(String uri, String localName, String qName) throws SAXException
				{
					if(qName.equals("header")) //$NON-NLS-1$
					{
						in_header = false;
					}
					else if(qName.equals("softwarelist") && in_software_list) //$NON-NLS-1$
					{
						machinelist_list.softwarelist_list.add(curr_software_list);
						softwares_list_cnt++;
						in_software_list = false;
					}
					else if(qName.equals("software")) //$NON-NLS-1$
					{
						curr_software.roms = new ArrayList<>(roms.values());
						roms.clear();
						curr_software.disks = new ArrayList<>(disks.values());
						disks.clear();
						curr_software_list.add(curr_software);
						softwares_cnt++;
						in_software = false;
						handler.setProgress(String.format(Messages.getString("Profile.Loaded"), softwares_cnt, roms_cnt)); //$NON-NLS-1$
						if(handler.isCancel())
							throw new BreakException();
					}
					else if(qName.equals("machine") || qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						curr_machine.roms = new ArrayList<>(roms.values());
						roms.clear();
						curr_machine.disks = new ArrayList<>(disks.values());
						disks.clear();
						machinelist_list.get(0).add(curr_machine);
						machines_cnt++;
						in_machine = false;
						handler.setProgress(String.format(Messages.getString("Profile.Loaded"), machines_cnt, roms_cnt)); //$NON-NLS-1$
						if(handler.isCancel())
							throw new BreakException();
					}
					else if(qName.equals("rom")) //$NON-NLS-1$
					{
						if(curr_rom.getName() != null)
						{
							if(null == roms.put(curr_rom.name, curr_rom))
								roms_cnt++;
							if(curr_rom.crc != null)
							{
								Rom old_rom = roms_bycrc.put(curr_rom.crc, curr_rom);
								if(old_rom != null)
								{
									if(old_rom.sha1 != null && curr_rom.sha1 != null)
										if(!old_rom.equals(curr_rom))
											suspicious_crc.add(curr_rom.crc);
									if(old_rom.md5 != null && curr_rom.md5 != null)
										if(!old_rom.equals(curr_rom))
											suspicious_crc.add(curr_rom.crc);
								}
							}
						}
					}
					else if(qName.equals("disk")) //$NON-NLS-1$
					{
						if(curr_disk.getName() != null)
						{
							if(null == disks.put(curr_disk.name, curr_disk))
								disks_cnt++;
						}
					}
					else if(qName.equals("description") && (in_machine || in_software || in_software_list)) //$NON-NLS-1$
					{
						in_description = false;
					}
					else if(qName.equals("year") && (in_machine || in_software)) //$NON-NLS-1$
					{
						in_year = false;
					}
					else if(qName.equals("manufacturer") && (in_machine)) //$NON-NLS-1$
					{
						in_manufacturer = false;
					}
					else if(qName.equals("publisher") && (in_software)) //$NON-NLS-1$
					{
						in_publisher = false;
					}
					else if(qName.equals("dipswitch") && in_cabinet_dipsw) //$NON-NLS-1$
					{
						if(cabtype_set.contains(CabinetType.cocktail))
						{
							if(cabtype_set.contains(CabinetType.upright))
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
				public void characters(char[] ch, int start, int length) throws SAXException
				{
					if(in_description)
					{
						if(in_machine)
							curr_machine.description.append(ch, start, length);
						else if(in_software)
							curr_software.description.append(ch, start, length);
						else if(in_software_list)
							curr_software_list.description.append(ch, start, length);
					}
					else if(in_year)
					{
						if(in_machine)
							curr_machine.year.append(ch, start, length);
						else if(in_software)
							curr_software.year.append(ch, start, length);
					}
					else if(in_manufacturer && in_machine)
					{
						curr_machine.manufacturer.append(ch, start, length);
					}
					else if(in_publisher && in_software)
					{
						curr_software.publisher.append(ch, start, length);
					}
					else if(in_header)
					{
						if(!header.containsKey(curr_tag))
							header.put(curr_tag, new StringBuffer());
						header.get(curr_tag).append(ch, start, length);
					}
				}
			});
			return true;
		}
		catch(ParserConfigurationException | SAXException e)
		{
			Log.err("Parser Exception", e); //$NON-NLS-1$
		}
		catch(IOException e)
		{
			Log.err("IO Exception", e); //$NON-NLS-1$
		}
		catch(BreakException e)
		{
			return false;
		}
		catch(Throwable e)
		{
			Log.err("Other Exception", e); //$NON-NLS-1$
		}
		return false;
	}

	private static File getCacheFile(File file)
	{
		return new File(file.getParentFile(), file.getName() + ".cache"); //$NON-NLS-1$
	}

	public void save()
	{
		try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(nfo.file)))))
		{
			oos.writeObject(this);
		}
		catch(Throwable e)
		{

		}
	}

	public static Profile load(File file, ProgressHandler handler)
	{
		return load(ProfileNFO.load(file), handler);
	}

	public static Profile load(ProfileNFO nfo, ProgressHandler handler)
	{
		Profile profile = null;
		File cachefile = getCacheFile(nfo.file);
		if(cachefile.lastModified() >= nfo.file.lastModified() && !Settings.getProperty("debug_nocache", false)) //$NON-NLS-1$
		{
			handler.setProgress(Messages.getString("Profile.LoadingCache"), -1); //$NON-NLS-1$
			try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
			{
				profile = (Profile) ois.readObject();
				profile.nfo = nfo;
			}
			catch(Throwable e)
			{
			}
		}
		if(profile == null)
		{
			profile = new Profile();
			profile.nfo = nfo;
			if(nfo.isJRM())
			{
				if(nfo.mame.fileroms != null)
				{
					if(!profile._load(nfo.mame.fileroms, handler))
						return null;
					if(nfo.mame.filesl != null)
					{
						if(!profile._load(nfo.mame.filesl, handler))
							return null;
					}
				}
			}
			else if(!profile._load(nfo.file, handler))
				return null;
			handler.setProgress(Messages.getString("Profile.BuildingParentClonesRelations"), -1); //$NON-NLS-1$
			profile.buildParentClonesRelations();
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			profile.save();
		}
		profile.nfo.stats.version = profile.build != null ? profile.build : (profile.header.containsKey("version") ? profile.header.get("version").toString() : null);
		profile.nfo.stats.totalSets = profile.softwares_cnt > 0 ? profile.softwares_cnt : profile.machines_cnt;
		profile.nfo.stats.totalRoms = profile.roms_cnt;
		profile.nfo.stats.totalDisks = profile.disks_cnt;
		profile.loadSettings();
		profile.loadSystems();
		return profile;
	}

	private void buildParentClonesRelations()
	{
		this.machinelist_list.forEach(machine_list -> {
			machine_list.forEach(machine -> {
				if(machine.romof != null)
				{
					machine.parent = machine_list.m_byname.get(machine.romof);
					if(machine.parent != null)
					{
						if(!machine.getParent().isbios)
							machine.parent.clones.put(machine.getName(), machine);
					}
				}
			});
		});
		this.machinelist_list.softwarelist_list.forEach(software_list -> {
			software_list.forEach(software -> {
				if(software.cloneof != null)
				{
					software.parent = software_list.s_byname.get(software.cloneof);
					if(software.parent != null)
						software.parent.clones.put(software.getName(), software);
				}
			});
		});
	}
	
	private File getSettingsFile(File file)
	{
		return new File(file.getParentFile(), file.getName() + ".properties"); //$NON-NLS-1$
	}

	public void saveSettings()
	{
		if(settings == null)
			settings = new Properties();
		try(FileOutputStream os = new FileOutputStream(getSettingsFile(nfo.file)))
		{
			settings.storeToXML(os, null);
			nfo.save();
		}
		catch(IOException e)
		{
			Log.err("IO", e); //$NON-NLS-1$
		}
	}

	public void loadSettings()
	{
		if(settings == null)
			settings = new Properties();
		if(getSettingsFile(nfo.file).exists())
		{
			try(FileInputStream is = new FileInputStream(getSettingsFile(nfo.file)))
			{
				settings.loadFromXML(is);
			}
			catch(IOException e)
			{
				Log.err("IO", e); //$NON-NLS-1$
			}
		}
	}

	public void setProperty(String property, boolean value)
	{
		settings.setProperty(property, Boolean.toString(value));
	}

	public void setProperty(String property, String value)
	{
		settings.setProperty(property, value);
	}

	public boolean getProperty(String property, boolean def)
	{
		return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
	}

	public String getProperty(String property, String def)
	{
		return settings.getProperty(property, def);
	}

	public String getName()
	{
		String name = "<html><body>[<span color='blue'>" + Paths.get(".", "xmlfiles").toAbsolutePath().normalize().relativize(nfo.file.toPath()) + "</span>] "; //$NON-NLS-2$ //$NON-NLS-3$
		if(build != null)
			name += "<b>" + build + "</b>";
		else if(header.size() > 0)
		{
			if(header.containsKey("description")) //$NON-NLS-1$
				name += "<b>" + header.get("description") + "</b>"; //$NON-NLS-2$
			else if(header.containsKey("name")) //$NON-NLS-1$
			{
				name += "<b>" + header.get("name") + "</b>"; //$NON-NLS-2$
				if(header.containsKey("version")) //$NON-NLS-1$
					name += " (" + header.get("version") + ")"; //$NON-NLS-2$
			}
		}
		if(machinelist_list.get(0).size() > 0)
			name += " (" + machines_cnt + " Machines)";
		if(machinelist_list.softwarelist_list.size() > 0)
			name += " (" + softwares_list_cnt + " Software Lists, " + softwares_cnt + " Softwares)";
		name += "</body></html>";
		return name;
	}

	public void loadSystems()
	{
		systems = new Systms();
		systems.add(SystmStandard.STANDARD);
		systems.add(SystmMechanical.MECHANICAL);
		systems.add(SystmDevice.DEVICE);
		ArrayList<Machine> machines = new ArrayList<>();
		machinelist_list.get(0).forEach(m -> {
			if(m.isbios)
				machines.add(m);
		});
		machines.sort((a, b) -> a.getName().compareTo(b.getName()));
		machines.forEach(systems::add);
		ArrayList<SoftwareList> softwarelists = new ArrayList<SoftwareList>();
		machinelist_list.softwarelist_list.forEach(softwarelists::add);
		softwarelists.sort((a, b) -> a.name.compareTo(b.name));
		softwarelists.forEach(systems::add);
	}

}
