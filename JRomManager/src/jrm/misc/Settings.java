package jrm.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class Settings implements SettingsImpl
{
	private final Properties properties = new Properties();

	
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
		System.out.println("file="+file+", propsize="+properties.size());
		try(FileOutputStream os = new FileOutputStream(file))
		{
			System.out.println("before store");
			properties.storeToXML(os, null);
			System.out.println("stored");
		}
		catch(final Throwable e)
		{
			e.printStackTrace();
			//Log.err("IO", e); //$NON-NLS-1$
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
	
	@SuppressWarnings("serial")
	public JsonObject asJSO()
	{
		return new JsonObject()
		{{
			properties.forEach((k, v) -> {
				try
				{
					JsonValue value = Json.parse((String)v);
					if(value.isObject() || value.isArray() || value.isBoolean())
						add((String)k, value);
					else
						add((String) k, (String) v);
				}
				catch (Exception e)
				{
					add((String) k, (String) v);
				}
			});
		}};
	}
}
