package jrm.misc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import lombok.Getter;
import lombok.val;

public abstract class Settings extends SettingsImpl
{
	private final @Getter Properties properties = new Properties();

	
	protected Settings()
	{
	}

	@Override
	protected boolean getProperty(final String property, final boolean def)
	{
		return Boolean.parseBoolean(properties.getProperty(property, Boolean.toString(def)));
	}

	@Override
	protected int getProperty(final String property, final int def)
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
		if(file.exists())
		{
			try(val is = new BufferedInputStream(new FileInputStream(file)))
			{
				properties.clear();
				properties.loadFromXML(is);
			}
			catch(final IOException e)
			{
				Log.err("IO", e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void saveSettings(final File file)
	{
		Log.debug(()->"file="+file+", propsize="+properties.size());
		try(val os = new BufferedOutputStream(new FileOutputStream(file)))
		{
			Log.debug("before store");
			properties.storeToXML(os, null);
			Log.debug("stored");
		}
		catch(final Exception e)
		{
			Log.err(e.getMessage(),e);
			//Log.err("IO", e); //$NON-NLS-1$
		}
	}

	@Override
	public void setProperty(final String property, final boolean value)
	{
		properties.setProperty(property, Boolean.toString(value));
		propagate(SettingsEnum.from(property), Boolean.toString(value));
	}

	@Override
	public void setProperty(final String property, final int value)
	{
		properties.setProperty(property, Integer.toString(value));
		propagate(SettingsEnum.from(property), Integer.toString(value));
	}

	@Override
	public void setProperty(Enum<?> property, String value)
	{
		super.setProperty(property, value);
		propagate(property, value);
	}
	
	@Override
	public void setProperty(final String property, final String value)
	{
		if(value==null)
			properties.remove(property);
		else
			properties.setProperty(property, value);
		propagate(SettingsEnum.from(property), value);
	}
	
	@Override
	protected boolean hasProperty(String property)
	{
		return properties.containsKey(property);
	}
	
	protected abstract void propagate(final Enum<?> property, final String value);
	
	public JsonObject asJSO()
	{
		final var jso = new JsonObject();
		properties.forEach((k, v) -> {
			try
			{
				JsonValue value = Json.parse((String)v);
				if(value.isObject() || value.isArray() || value.isBoolean())
					jso.add((String)k, value);
				else
					jso.add((String) k, (String) v);
			}
			catch (Exception e)
			{
				jso.add((String) k, (String) v);
			}
		});
		return jso;
	}
}
