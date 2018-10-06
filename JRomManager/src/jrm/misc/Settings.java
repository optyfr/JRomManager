package jrm.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;

public class Settings implements SettingsImpl
{
	private final Properties properties = new Properties();

	/**
	 * The merge mode used while filtering roms/disks
	 */
	public transient MergeOptions merge_mode;
	/**
	 * Must we strictly conform to merge tag (explicit), or search merge-able ROMs by ourselves (implicit)
	 */
	public transient Boolean implicit_merge;
	/**
	 * What hash collision mode is used?
	 */
	public transient HashCollisionOptions hash_collision_mode;
	
	
	public Settings()
	{
	}

	@Override
	public boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(properties.getProperty(property, Boolean.toString(def)));
	}

	@Override
	public int getProperty(final String property, final int def)
	{
		return Integer.parseInt(properties.getProperty(property, Integer.toString(def)));
	}

	@Override
	public String getProperty(final String property, final String def)
	{
		return properties.getProperty(property, def);
	}

	@Override
	public void loadSettings(final File file)
	{
		try(FileInputStream is = new FileInputStream(file))
		{
			properties.loadFromXML(is);
		}
		catch(final IOException e)
		{
			Log.err("IO", e); //$NON-NLS-1$
		}
	}

	@Override
	public void saveSettings(final File file)
	{
		try(FileOutputStream os = new FileOutputStream(file))
		{
			properties.storeToXML(os, null);
		}
		catch(final IOException e)
		{
			Log.err("IO", e); //$NON-NLS-1$
		}
	}

	@Override
	public void setProperty(final String property, final boolean value)
	{
		properties.setProperty(property, Boolean.toString(value));
	}

	@Override
	public void setProperty(final String property, final int value)
	{
		properties.setProperty(property, Integer.toString(value));
	}

	@Override
	public void setProperty(final String property, final String value)
	{
		properties.setProperty(property, value);
	}

	@Override
	public Properties getProperties()
	{
		return properties;
	}
}
