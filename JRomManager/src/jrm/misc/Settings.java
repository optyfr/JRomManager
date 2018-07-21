package jrm.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * The Settings back-end
 * @author optyfr
 *
 */
public class Settings
{
	/**
	 * This is where we store settings
	 */
	public static Properties settings = new Properties();
	
	// Extra settings coming from cmdline args
	public static boolean multiuser = false;
	public static boolean noupdate = false;
	

	/**
	 * Return the current work path, the one where we save working dirs (xml, cache, backup, ...)
	 * By default, this where the program reside... but if multiuser mode is enabled, it will be $HOME/.jrommanager (or %HOMEPATH%\.jrommanager for Windows)
	 * @return the current working path
	 */
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
	
	/**
	 * get settings file
	 * @return a {@link File} wich is the settings file
	 */
	private static File getSettingsFile()
	{
		final File workdir = getWorkPath().toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
		final File cachedir = new File(workdir, "settings"); //$NON-NLS-1$
		final File settingsfile = new File(cachedir, "JRomManager.properties"); //$NON-NLS-1$
		settingsfile.getParentFile().mkdirs();
		return settingsfile;

	}

	/**
	 * save current settings to settings file
	 */
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

	/**
	 * load settings from settings file
	 */
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

	/**
	 * Set a boolean property
	 * @param property the property name
	 * @param value the property value
	 */
	public static void setProperty(final String property, final boolean value)
	{
		Settings.settings.setProperty(property, Boolean.toString(value));
	}

	/**
	 * Set an int property
	 * @param property the property name
	 * @param value the property value
	 */
	public static void setProperty(final String property, final int value)
	{
		Settings.settings.setProperty(property, Integer.toString(value));
	}

	/**
	 * Set a string property
	 * @param property the property name
	 * @param value the property value
	 */
	public static void setProperty(final String property, final String value)
	{
		Settings.settings.setProperty(property, value);
	}

	/**
	 * get a boolean property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as boolean
	 */
	public static boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(Settings.settings.getProperty(property, Boolean.toString(def)));
	}

	/**
	 * get a int property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as int
	 */
	public static int getProperty(final String property, final int def)
	{
		return Integer.parseInt(Settings.settings.getProperty(property, Integer.toString(def)));
	}

	/**
	 * get a string property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as string
	 */
	public static String getProperty(final String property, final String def)
	{
		return Settings.settings.getProperty(property, def);
	}
}
