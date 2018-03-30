package jrm.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import javax.swing.JRadioButtonMenuItem;

public class Settings
{
	public static Properties settings = new Properties();

	private static File getSettingsFile()
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File cachedir = new File(workdir, "settings");
		File settingsfile = new File(cachedir, "JRomManager.xml");
		settingsfile.getParentFile().mkdirs();
		return settingsfile;
		
	}

	public static void saveSettings()
	{
		if(settings==null)
			settings = new Properties();
		try(FileOutputStream os = new FileOutputStream(getSettingsFile()))
		{
			settings.storeToXML(os, null);
		}
		catch (IOException e)
		{
			Log.err("IO", e);
		}
	}
	
	public static void loadSettings()
	{
		if(settings==null)
			settings = new Properties();
		if(getSettingsFile().exists())
		{
			try(FileInputStream is = new FileInputStream(getSettingsFile()))
			{
				settings.loadFromXML(is);
			}
			catch (IOException e)
			{
				Log.err("IO", e);
			}
		}
	}
	
	public static void setProperty(String property, boolean value)
	{
		settings.setProperty(property, Boolean.toString(value));
	}
	
	public static void setProperty(String property, String value)
	{
		settings.setProperty(property, value);
	}
	
	public static boolean getProperty(String property, boolean def)
	{
		return Boolean.parseBoolean(settings.getProperty(property, Boolean.toString(def)));
	}
	
	public static String getProperty(String property, String def)
	{
		return settings.getProperty(property, def);
	}
}
