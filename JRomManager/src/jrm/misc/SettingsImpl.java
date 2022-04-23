package jrm.misc;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

public abstract class SettingsImpl
{
	/**
	 * get underlying properties
	 * @return underlying {@link Properties} 
	 */
	public abstract Properties getProperties();
	

	/**
	 * get type T property using default value direct from a special enum
	 * @param property an enum which contains a default value
	 * @param cls the class type to return, as to be compatible with default value 
	 * @return return the property as T
	 */
	public <T> T getProperty(final EnumWithDefault property, Class<T> cls)
	{
		if(property instanceof Enum<?> e)
		{
			if (cls == Boolean.class)
				return cls.cast(getProperty(e.name(), (Boolean) property.getDefault()));
			if (cls == Integer.class)
				return cls.cast(getProperty(e.name(), ((Number) property.getDefault()).intValue()));
			if (cls == String.class)
				return cls.cast(getProperty(e.name(), Optional.ofNullable(property.getDefault()).map(Object::toString).orElse("")));
		}
		return null;
	}
	
	public <T extends Enum<T>> T getEnumProperty(final EnumWithDefault property, Class<T> cls)
	{
		return Enum.valueOf(cls, getProperty(property));
	}
	
	public <T extends Enum<T>> void setEnumProperty(final Enum<?> property, T value)
	{
		setProperty(property, value.name());
	}
	
	public String getProperty(final EnumWithDefault property)
	{
		return getProperty(property, String.class);
	}

	/**
	 * get a boolean property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as boolean
	 */
	protected abstract boolean getProperty(final String property, final boolean def);
	
	/**
	 * get a int property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as int
	 */
	protected abstract int getProperty(final String property, final int def);

	/**
	 * get a string property
	 * @param property the property name
	 * @param def the default value if absent
	 * @return return the property as string
	 */
	protected abstract String getProperty(final String property, final String def);

	/**
	 * load settings from settings file
	 * @param file the {@link File} from which to load settings
	 */
	public abstract void loadSettings(File file);

	/**
	 * save current settings to settings file
	 * @param file the {@link File} to save settings
	 */
	public abstract void saveSettings(File file);

	/**
	 * Set a boolean property
	 * @param property the property name
	 * @param value the property value
	 */
	protected abstract void setProperty(final String property, final boolean value);

	public void setProperty(final Enum<?> property, final boolean value)
	{
		setProperty(property.name(), value);
	}
	
	/**
	 * Set an int property
	 * @param property the property name
	 * @param value the property value
	 */
	protected abstract void setProperty(final String property, final int value);

	public void setProperty(final Enum<?> property, final int value)
	{
		setProperty(property.name(), value);
	}
	
	/**
	 * Set a string property
	 * @param property the property name
	 * @param value the property value
	 */
	protected abstract void setProperty(final String property, final String value);
	
	public void setProperty(final Enum<?> property, final String value)
	{
		setProperty(property.name(), value);
	}

	/**
	 * Does a property exist
	 * @param property the name of the property
	 * @return true if it exists, false otherwise
	 */
	protected abstract boolean hasProperty(final String property);

	public boolean hasProperty(final Enum<?> property)
	{
		return hasProperty(property.name());
	}
}
