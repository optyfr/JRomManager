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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profiler.data.Disk;
import jrm.profiler.data.Machine;
import jrm.profiler.data.Rom;
import jrm.ui.ProgressHandler;

@SuppressWarnings("serial")
public class Profile implements Serializable
{
	File file;

	public long machines_cnt = 0;
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
	public HashSet<String> suspicious_crc = new HashSet<>();

	public transient Properties settings = null;

	public Profile()
	{

	}

	public boolean _load(File file, ProgressHandler handler)
	{
		this.file = file;
		handler.setProgress("Parsing " + file, -1);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try
		{
			SAXParser parser = factory.newSAXParser();
			parser.parse(file, new DefaultHandler()
			{
				private HashMap<String, Rom> roms_bycrc = new HashMap<>();
				private boolean in_machine = false;
				private boolean in_description = false;
				private boolean in_header = false;
				private Machine curr_machine = null;
				private Rom curr_rom = null;
				private Disk curr_disk = null;
				
				private String curr_tag;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
				{
					curr_tag = qName;
					if(qName.equals("mame")||qName.equals("datafile"))
					{
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
							{
								case "build":
									build = attributes.getValue(i);
							}
						}
					}
					else if(qName.equals("header"))
					{
						in_header = true;
					}
					else if(qName.equals("machine")||qName.equals("game"))
					{
						in_machine = true;
						curr_machine = new Machine();
						for(int i = 0; i < attributes.getLength(); i++)
						{
							switch(attributes.getQName(i))
							{
								case "name":
									curr_machine.name = attributes.getValue(i);
									machines_byname.put(curr_machine.name, curr_machine);
									break;
								case "romof":
									curr_machine.romof = attributes.getValue(i);
									break;
								case "cloneof":
									curr_machine.cloneof = attributes.getValue(i);
									break;
								case "sampleof":
									curr_machine.sampleof = attributes.getValue(i);
									break;
								case "isbios":
									curr_machine.isbios = attributes.getValue(i).equals("yes");
									break;
								case "ismechanical":
									curr_machine.ismechanical = attributes.getValue(i).equals("yes");
									break;
								case "isdevice":
									curr_machine.isdevice = attributes.getValue(i).equals("yes");
									break;
							}
						}
					}
					else if(qName.equals("description") && in_machine)
					{
						in_description = true;
					}
					else if(qName.equals("rom"))
					{
						if(in_machine)
						{
							curr_rom = new Rom(curr_machine);
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name":
										curr_rom.setName(attributes.getValue(i));
										break;
									case "size":
										curr_rom.size = Long.decode(attributes.getValue(i));
										break;
									case "crc":
										curr_rom.crc = attributes.getValue(i);
										break;
									case "sha1":
										curr_rom.sha1 = attributes.getValue(i);
										sha1_roms = true;
										break;
									case "md5":
										curr_rom.md5 = attributes.getValue(i);
										md5_roms = true;
										break;
									case "merge":
										curr_rom.merge = attributes.getValue(i);
										break;
									case "bios":
										curr_rom.bios = attributes.getValue(i);
										break;
									case "status":
										curr_rom.status = attributes.getValue(i);
										break;
								}
							}
						}
					}
					else if(qName.equals("disk"))
					{
						if(in_machine)
						{
							curr_disk = new Disk(curr_machine);
							for(int i = 0; i < attributes.getLength(); i++)
							{
								switch(attributes.getQName(i))
								{
									case "name":
										curr_disk.setName(attributes.getValue(i));
										break;
									case "sha1":
										curr_disk.sha1 = attributes.getValue(i);
										sha1_disks = true;
										break;
									case "md5":
										curr_disk.md5 = attributes.getValue(i);
										md5_disks = true;
										break;
									case "merge":
										curr_disk.merge = attributes.getValue(i);
										break;
									case "status":
										curr_disk.status = attributes.getValue(i);
										break;
								}
							}
						}
					}
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException
				{
					if(qName.equals("header"))
					{
						in_header = false;
					}
					if(qName.equals("machine")||qName.equals("game"))
					{
						machines.add(curr_machine);
						machines_cnt++;
						in_machine = false;
						handler.setProgress(String.format("Loaded Sets/Roms %d/%d", machines_cnt, roms_cnt));
						if(handler.isCancel())
							throw new BreakException();
					}
					else if(qName.equals("rom"))
					{
						curr_machine.roms.add(curr_rom);
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
					else if(qName.equals("disk"))
					{
						curr_machine.disks.add(curr_disk);
						disks_cnt++;
					}
					else if(qName.equals("description") && in_machine)
					{
						in_description = false;
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException
				{
					if(in_machine && in_description)
						curr_machine.description.append(ch, start, length);
					else if(in_header)
					{
						if(!header.containsKey(curr_tag))
							header.put(curr_tag, new StringBuffer());
						header.get(curr_tag).append(ch, start, length);
					}
				}
			});
			handler.setProgress("Building parent/clones relations...", -1);
			machines.forEach(machine -> {
				if(machine.romof != null)
				{
					machine.parent = machines_byname.get(machine.romof);
					if(machine.parent!=null)
					{
						if(!machine.parent.isbios)
							machine.parent.clones.put(machine.name, machine);
					}
				}
			});
			handler.setProgress("Saving cache...", -1);
			save();
			return true;
		}
		catch(ParserConfigurationException | SAXException e)
		{
			Log.err("Parser Exception", e);
		}
		catch(IOException e)
		{
			Log.err("IO Exception", e);
		}
		catch(BreakException e)
		{
			return false;
		}
		catch(Throwable e)
		{
			Log.err("Other Exception", e);
		}
		return false;
	}

	private static File getCacheFile(File file)
	{
		return new File(file.getParentFile(), file.getName() + ".cache");
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
		if(cachefile.lastModified() >= file.lastModified() && !Settings.getProperty("debug_nocache", false))
		{
			handler.setProgress("Loading cache...", -1);
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
		return new File(file.getParentFile(), file.getName() + ".properties");
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
			Log.err("IO", e);
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
				Log.err("IO", e);
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

}
