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
package jrm.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;

/**
 * The Settings back-end
 * @author optyfr
 *
 */
public class GlobalSettings
{
	/**
	 * This is where we store settings
	 */
	public static Settings settings = new Settings();
	
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
			final Path work = Paths.get(System.getProperty("user.home"), ".jrommanager").toAbsolutePath().normalize(); //$NON-NLS-1$ //$NON-NLS-2$
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
		return Paths.get(".").toAbsolutePath().normalize(); //$NON-NLS-1$
	}
	
	public static File getWorkFile(final File parent, final String name, final String ext)
	{
		if(!parent.getAbsoluteFile().toPath().startsWith(getWorkPath().toAbsolutePath()))
		{
			final CRC32 crc = new CRC32();
			crc.update(new File(parent, name).getAbsolutePath().getBytes());
			final File work = getWorkPath().resolve("work").toFile(); //$NON-NLS-1$
			work.mkdirs();
			return new File(work, String.format("%08x", crc.getValue()) + ext); //$NON-NLS-1$
			
		}
		return new File(parent, name + ext); 
	}
	
	/**
	 * get the temporary path 
	 * @param local if local is true, then a "tmp" dir is created into current workpath (multiuser) or a subdir from default temp dir (not multiuser). If local is false, the default system temporary path is directly returned 
	 * @return a valid temporary path
	 */
	public static Path getTmpPath(boolean local)
	{
		if(local)
		{
			if(multiuser)
			{
				try
				{
					return Files.createDirectories(getWorkPath().resolve("tmp")); //$NON-NLS-1$
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				return Files.createTempDirectory("JRM"); //$NON-NLS-1$
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return Paths.get(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
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
		if(GlobalSettings.settings == null)
			GlobalSettings.settings = new Settings();
		GlobalSettings.settings.saveSettings(GlobalSettings.getSettingsFile());
	}

	/**
	 * load settings from settings file
	 */
	public static void loadSettings()
	{
		if(GlobalSettings.settings == null)
			GlobalSettings.settings = new Settings();
		GlobalSettings.settings.loadSettings(GlobalSettings.getSettingsFile());
	}

	/**
	 * Set a boolean property
	 * @param property the property name
	 * @param value the property value
	 */
	public static void setProperty(final String property, final boolean value)
	{
		GlobalSettings.settings.setProperty(property, Boolean.toString(value));
	}

	/**
	 * Set an int property
	 * @param property the property name
	 * @param value the property value
	 */
	public static void setProperty(final String property, final int value)
	{
		GlobalSettings.settings.setProperty(property, Integer.toString(value));
	}

	/**
	 * Set a string property
	 * @param property the property name
	 * @param value the property value
	 */
	public static void setProperty(final String property, final String value)
	{
		GlobalSettings.settings.setProperty(property, value);
	}

	/**
	 * get a boolean property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as boolean
	 */
	public static boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(GlobalSettings.settings.getProperty(property, Boolean.toString(def)));
	}

	/**
	 * get a int property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as int
	 */
	public static int getProperty(final String property, final int def)
	{
		return Integer.parseInt(GlobalSettings.settings.getProperty(property, Integer.toString(def)));
	}

	/**
	 * get a string property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as string
	 */
	public static String getProperty(final String property, final String def)
	{
		return GlobalSettings.settings.getProperty(property, def);
	}
}