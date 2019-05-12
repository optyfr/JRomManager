package jrm.misc;

import java.io.File;
import java.util.Properties;

public interface SettingsImpl
{
	/**
	 * get underlying properties
	 * @return underlying {@link Properties} 
	 */
	public Properties getProperties();
	
	/**
	 * get a boolean property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as boolean
	 */
	public boolean getProperty(final String property, final boolean def);

	/**
	 * get a int property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as int
	 */
	public int getProperty(final String property, final int def);

	/**
	 * get a string property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as string
	 */
	public String getProperty(final String property, final String def);

	/**
	 * load settings from settings file
	 * @param file the {@link File} from which to load settings
	 */
	public void loadSettings(File file);

	/**
	 * save current settings to settings file
	 * @param file the {@link File} to save settings
	 */
	public void saveSettings(File file);

	/**
	 * Set a boolean property
	 * @param property the property name
	 * @param value the property value
	 */
	public void setProperty(final String property, final boolean value);

	/**
	 * Set an int property
	 * @param property the property name
	 * @param value the property value
	 */
	public void setProperty(final String property, final int value);

	/**
	 * Set a string property
	 * @param property the property name
	 * @param value the property value
	 */
	public void setProperty(final String property, final String value);
	
	/**
	 * Does a property exist
	 * @param property the name of the property
	 * @return true if it exists, false otherwise
	 */
	public boolean hasProperty(final String property);
}
