package jrm.profiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

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
import jrm.profiler.data.Disk;
import jrm.profiler.data.Entity;
import jrm.profiler.data.Machine;
import jrm.profiler.data.Rom;
import jrm.profiler.data.Software;
import jrm.profiler.data.SoftwareList;
import jrm.ui.ProgressHandler;

@SuppressWarnings("serial")
public class Profile implements Serializable
{
	File file;

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
	
	public ArrayList<Machine> machines = new ArrayList<>();
	public HashMap<String, Machine> machines_byname = new HashMap<>();

	public ArrayList<SoftwareList> software_lists = new ArrayList<>();
	public HashMap<String, SoftwareList> software_list_byname = new HashMap<>();

	public HashSet<String> suspicious_crc = new HashSet<>();

	public transient Properties settings = null;

	public Profile()
	{

	}

	public boolean _load(File file, ProgressHandler handler)
	{
		this.file = file;
		handler.setProgress(String.format(Messages.getString("Profile.Parsing"), file), -1); //$NON-NLS-1$
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			SAXParser parser = factory.newSAXParser();
			parser.parse(file, new DefaultHandler()
			{
				private HashMap<String, Rom> roms_bycrc = new HashMap<>();
				private boolean in_softwares_list = false;
				private boolean in_software = false;
				private boolean in_machine = false;
				private boolean in_description = false;
				private boolean in_header = false;
				private SoftwareList curr_software_list = null;
				private Software curr_software = null;
				private Machine curr_machine = null;
				private Rom curr_rom = null;
				private Disk curr_disk = null;
				
				private String curr_tag;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
				{
					curr_tag = qName;
					if(qName.equals("mame")||qName.equals("datafile")) //$NON-NLS-1$ //$NON-NLS-2$
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
					else if(qName.equals("softwarelist")) //$NON-NLS-1$
					{
						in_softwares_list = true;
						curr_software_list = new SoftwareList();
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_software_list.name = attributes.getValue(i);
									software_list_byname.put(curr_software_list.name, curr_software_list);
									break;
							}
						}
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
									curr_software.name = attributes.getValue(i);
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
					else if(qName.equals("machine")||qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						in_machine = true;
						curr_machine = new Machine();
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
							{
								case "name": //$NON-NLS-1$
									curr_machine.name = attributes.getValue(i);
									machines_byname.put(curr_machine.name, curr_machine);
									break;
								case "romof": //$NON-NLS-1$
									curr_machine.romof = attributes.getValue(i);
									break;
								case "cloneof": //$NON-NLS-1$
									curr_machine.cloneof = attributes.getValue(i);
									break;
								case "sampleof": //$NON-NLS-1$
									curr_machine.sampleof = attributes.getValue(i);
									break;
								case "isbios": //$NON-NLS-1$
									curr_machine.isbios =  BooleanUtils.toBoolean(attributes.getValue(i));
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
					else if(qName.equals("description") && (in_machine || in_software || in_softwares_list)) //$NON-NLS-1$
					{
						in_description = true;
					}
					else if(qName.equals("rom")) //$NON-NLS-1$
					{
						if(in_machine || in_software)
						{
							curr_rom = new Rom(in_machine?curr_machine:curr_software);
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_rom.setName(attributes.getValue(i));
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
										curr_rom.merge = attributes.getValue(i);
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
							curr_disk = new Disk(in_machine?curr_machine:curr_software);
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name": //$NON-NLS-1$
										curr_disk.setName(attributes.getValue(i));
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
										curr_disk.merge = attributes.getValue(i);
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
					else if(qName.equals("softwarelist")) //$NON-NLS-1$
					{
						software_lists.add(curr_software_list);
						softwares_list_cnt++;
						in_softwares_list = false;
					}
					else if(qName.equals("software")) //$NON-NLS-1$
					{
						curr_software_list.add(curr_software);
						softwares_cnt++;
						in_software = false;
						handler.setProgress(String.format(Messages.getString("Profile.Loaded"), softwares_cnt, roms_cnt)); //$NON-NLS-1$
						if(handler.isCancel())
							throw new BreakException();
					}
					else if(qName.equals("machine")||qName.equals("game")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						machines.add(curr_machine);
						machines_cnt++;
						in_machine = false;
						handler.setProgress(String.format(Messages.getString("Profile.Loaded"), machines_cnt, roms_cnt)); //$NON-NLS-1$
						if(handler.isCancel())
							throw new BreakException();
					}
					else if(qName.equals("rom")) //$NON-NLS-1$
					{
						(in_machine?curr_machine:curr_software).roms.add(curr_rom);
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
					else if(qName.equals("disk")) //$NON-NLS-1$
					{
						(in_machine?curr_machine:curr_software).disks.add(curr_disk);
						disks_cnt++;
					}
					else if(qName.equals("description") && (in_machine || in_software || in_softwares_list)) //$NON-NLS-1$
					{
						in_description = false;
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
						else if(in_softwares_list)
							curr_software_list.description.append(ch, start, length);
					}
					else if(in_header)
					{
						if(!header.containsKey(curr_tag))
							header.put(curr_tag, new StringBuffer());
						header.get(curr_tag).append(ch, start, length);
					}
				}
			});
			handler.setProgress(Messages.getString("Profile.BuildingParentClonesRelations"), -1); //$NON-NLS-1$
			machines.forEach(machine -> {
				if(machine.romof != null)
				{
					machine.parent = machines_byname.get(machine.romof);
					if(machine.parent!=null)
					{
						if(!machine.getParent().isbios)
							machine.parent.clones.put(machine.name, machine);
					}
				}
			});
			software_lists.forEach(software_list -> {
				software_list.softwares.forEach(software -> {
					if(software.cloneof != null)
					{
						software.parent = software_list.softwares_byname.get(software.cloneof);
						if(software.parent!=null)
							software.parent.clones.put(software.name, software);
					}
				});
			});
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			save();
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
		try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(file)))))
		{
			oos.writeObject(this);
		}
		catch(Throwable e)
		{

		}
	}

	public static Profile load(File file, ProgressHandler handler)
	{
		File cachefile = getCacheFile(file);
		if(cachefile.lastModified() >= file.lastModified() && !Settings.getProperty("debug_nocache", false)) //$NON-NLS-1$
		{
			handler.setProgress(Messages.getString("Profile.LoadingCache"), -1); //$NON-NLS-1$
			try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
			{
				Profile profile = (Profile) ois.readObject();
				profile.loadSettings();
				return profile;
			}
			catch(Throwable e)
			{

			}
		}
		Profile profile = new Profile();
		if(profile._load(file, handler))
		{
			profile.loadSettings();
			return profile;
		}
		return null;
	}

	private File getSettingsFile(File file)
	{
		return new File(file.getParentFile(), file.getName() + ".properties"); //$NON-NLS-1$
	}

	public void saveSettings()
	{
		if(settings == null)
			settings = new Properties();
		try(FileOutputStream os = new FileOutputStream(getSettingsFile(file)))
		{
			settings.storeToXML(os, null);
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
		if(getSettingsFile(file).exists())
		{
			try(FileInputStream is = new FileInputStream(getSettingsFile(file)))
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
		String name = "<html><body>[<span color='blue'>"+Paths.get(".", "xmlfiles").toAbsolutePath().normalize().relativize(file.toPath())+"</span>] "; //$NON-NLS-2$ //$NON-NLS-3$
		if(build!=null)
			name += "<b>"+build+"</b>";
		else if(header.size() > 0)
		{
			if(header.containsKey("description")) //$NON-NLS-1$
				name += "<b>"+header.get("description")+"</b>"; //$NON-NLS-2$
			else if(header.containsKey("name")) //$NON-NLS-1$
			{
				name += "<b>"+header.get("name")+"</b>"; //$NON-NLS-2$
				if(header.containsKey("version")) //$NON-NLS-1$
					name += " ("+header.get("version")+")"; //$NON-NLS-2$
			}
		}
		if(machines.size()>0)
			name += " ("+machines_cnt+" Machines)";
		else if(software_lists.size()>0)
			name += " ("+softwares_list_cnt+" Software Lists, "+softwares_cnt+" Softwares)";
		name += "</body></html>";
		return name;
	}
}
