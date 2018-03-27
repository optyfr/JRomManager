package profiler;

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
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FilenameUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import data.Machine;
import data.Rom;
import misc.Log;
import ui.ProgressHandler;

@SuppressWarnings("serial")
public class Profile implements Serializable
{
	File file;

	long machines_cnt = 0;
	long roms_cnt = 0;

	String build;
	ArrayList<Machine> machines = new ArrayList<>();
	HashMap<String, Machine> machines_byname = new HashMap<>();
	// HashMap<String,Rom> roms_bycrc = new HashMap<>();
	// HashMap<String,Rom> roms_bysha1 = new HashMap<>();
	
	transient Properties settings = null;

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
				private boolean in_machine = false;
				private boolean in_description = false;
				private Machine curr_machine = null;
				private Rom curr_rom = null;

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
				{
					if (qName.equals("mame"))
						build = attributes.getValue("build");
					else if (qName.equals("machine"))
					{
						in_machine = true;
						machines.add(curr_machine = new Machine());
						machines_cnt++;
						for (int i = 0; i < attributes.getLength(); i++)
						{
							switch (attributes.getQName(i))
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
					else if (qName.equals("description"))
					{
						in_description = true;
					}
					else if (qName.equals("rom"))
					{
						if (in_machine)
						{
							curr_machine.roms.add(curr_rom = new Rom());
							roms_cnt++;
							for (int i = 0; i < attributes.getLength(); i++)
							{
								switch (attributes.getQName(i))
								{
									case "name":
										curr_rom.name = attributes.getValue(i);
										break;
									case "size":
										curr_rom.size = Long.decode(attributes.getValue(i));
										break;
									case "crc":
										curr_rom.crc = attributes.getValue(i);
										// roms_bycrc.put(curr_rom.crc, curr_rom);
										break;
									case "sha1":
										curr_rom.sha1 = attributes.getValue(i);
										// roms_bysha1.put(curr_rom.sha1, curr_rom);
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
				}

				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException
				{
					if (qName.equals("machine"))
					{
						in_machine = false;
						handler.setProgress(String.format("Loaded Sets/Roms %d/%d", machines_cnt, roms_cnt));
					}
					else if (qName.equals("description"))
					{
						in_description = false;
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException
				{
					if (in_machine && in_description)
						curr_machine.description.append(ch, start, length);
				}
			});
			handler.setProgress("Saving cache...", -1);
			save();
			return true;
		}
		catch (ParserConfigurationException | SAXException e)
		{
			Log.err("Parser Exception", e);
		}
		catch (IOException e)
		{
			Log.err("IO Exception", e);
		}
		return false;
	}

	private static File getCacheFile(File file)
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File cachedir = new File(workdir, "cache");
		cachedir.mkdirs();
		return new File(cachedir, file.getName() + ".cache");
	}

	public void save()
	{
		try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(file)))))
		{
			oos.writeObject(this);
		}
		catch (Throwable e)
		{

		}
	}

	public static Profile load(File file, ProgressHandler handler)
	{
		File cachefile = getCacheFile(file);
		if (cachefile.lastModified() >= file.lastModified())
		{
			handler.setProgress("Loading cache...", -1);
			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
			{
				Profile profile = (Profile) ois.readObject();
				profile.loadSettings();
				return profile;
			}
			catch (Throwable e)
			{

			}
		}
		Profile profile = new Profile();
		if (profile._load(file, handler))
		{
			profile.loadSettings();
			return profile;
		}
		return null;
	}
	
	private File getSettingsFile(File file)
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File cachedir = new File(workdir, "settings");
		File settingsfile = new File(cachedir, FilenameUtils.getBaseName(file.getName()) + ".xml");
		settingsfile.getParentFile().mkdirs();
		return settingsfile;
		
	}

	public void saveSettings()
	{
		if(settings==null)
			settings = new Properties();
		try(FileOutputStream os = new FileOutputStream(getSettingsFile(file)))
		{
			settings.storeToXML(os, null);
		}
		catch (IOException e)
		{
			Log.err("IO", e);
		}
	}
	
	public void loadSettings()
	{
		if(settings==null)
			settings = new Properties();
		if(getSettingsFile(file).exists())
		{
			try(FileInputStream is = new FileInputStream(getSettingsFile(file)))
			{
				settings.loadFromXML(is);
			}
			catch (IOException e)
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
