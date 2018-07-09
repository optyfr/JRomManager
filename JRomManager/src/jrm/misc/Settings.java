package jrm.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings
{
	public static Properties settings = new Properties();
	
	public static boolean multiuser = false;
	public static boolean noupdate = false;
	

	public static Path getWorkPath()
	{
		if (multiuser)
		{
			Path work = Paths.get(System.getProperty("user.home"), ".jrommanager").toAbsolutePath().normalize();
			if (!Files.exists(work))
			{
				try
				{
					Files.createDirectories(work);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			return work;
		}
		return Paths.get(".").toAbsolutePath().normalize();
	}
	
	private static File getSettingsFile()
	{
		final File workdir = getWorkPath().toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
		final File cachedir = new File(workdir, "settings"); //$NON-NLS-1$
		final File settingsfile = new File(cachedir, "JRomManager.properties"); //$NON-NLS-1$
		settingsfile.getParentFile().mkdirs();
		return settingsfile;

	}

	public static void saveSettings()
	{
		if(Settings.settings == null)
			Settings.settings = new Properties();
		try(FileOutputStream os = new FileOutputStream(Settings.getSettingsFile()))
		{
			Settings.settings.storeToXML(os, null);
		}
		catch(final IOException e)
		{
			Log.err("IO", e); //$NON-NLS-1$
		}
	}

	public static void loadSettings()
	{
		if(Settings.settings == null)
			Settings.settings = new Properties();
		if(Settings.getSettingsFile().exists())
		{
			try(FileInputStream is = new FileInputStream(Settings.getSettingsFile()))
			{
				Settings.settings.loadFromXML(is);
			}
			catch(final IOException e)
			{
				Log.err("IO", e); //$NON-NLS-1$
			}
		}
	}

	public static void setProperty(final String property, final boolean value)
	{
		Settings.settings.setProperty(property, Boolean.toString(value));
	}

	public static void setProperty(final String property, final int value)
	{
		Settings.settings.setProperty(property, Integer.toString(value));
	}

	public static void setProperty(final String property, final String value)
	{
		Settings.settings.setProperty(property, value);
	}

	public static boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(Settings.settings.getProperty(property, Boolean.toString(def)));
	}

	public static int getProperty(final String property, final int def)
	{
		return Integer.parseInt(Settings.settings.getProperty(property, Integer.toString(def)));
	}

	public static String getProperty(final String property, final String def)
	{
		return Settings.settings.getProperty(property, def);
	}
}
