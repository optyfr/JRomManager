package jrm.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultEnvironmentProperties
{

	private final Map<String, String> map;
	private static final Pattern REPLACEMENT_PATTERN = Pattern.compile("([$][$])|([$][{].*[}])|([$]\\w+)");

	public DefaultEnvironmentProperties(InputStream is)
	{
		Properties properties = new Properties();
		try
		{
			properties.load(is);
		}
		catch(IOException|NullPointerException ex)
		{
			properties.clear();
		}
		map = init(properties);
	}
	
	public DefaultEnvironmentProperties(Properties properties)
	{
		map = init(properties);
	}

	public static DefaultEnvironmentProperties getInstance(Class<?> loader)
	{
		try (final var is = loader.getResourceAsStream(loader.getSimpleName() + ".properties"))
		{
			return new DefaultEnvironmentProperties(is);
		}
		catch(IOException e)
		{
			return new DefaultEnvironmentProperties(new Properties());
		}
	}
	
	private Map<String, String> init(Properties properties)
	{
		var tempMap = properties.entrySet().stream().collect(Collectors.toUnmodifiableMap(e -> (String)e.getKey(), e -> (String)e.getValue()));
		return resolvePlaceHolders(addEnvironment(tempMap));
	}
	
	private Optional<String> getEnvironmentProperties(String key)
	{
		return Optional.ofNullable(map.get(key));
	}
	
	public String getProperty(String key, String def)
	{
		return getEnvironmentProperties(key).orElseGet(() -> System.getProperty(key, def));
	}

	public Integer getProperty(String key, Integer def)
	{
		return getEnvironmentProperties(key).map(v -> {
			try
			{
				return Integer.valueOf(v);
			}
			catch(NumberFormatException e)
			{
				return null;
			}
		}).orElseGet(() -> {
			try
			{
				return Integer.valueOf(System.getProperty(key, def.toString()));
			}
			catch(NumberFormatException e)
			{
				return def;
			}
		});
	}

	public Boolean getProperty(String key, Boolean def)
	{
		return getEnvironmentProperties(key).map(v -> {
			try
			{
				return Boolean.valueOf(v);
			}
			catch(NumberFormatException e)
			{
				return null;
			}
		}).orElseGet(() -> {
			try
			{
				return Boolean.valueOf(System.getProperty(key, def.toString()));
			}
			catch(NumberFormatException e)
			{
				return def;
			}
		});
	}

	public <T extends Enum<T>> T getProperty(Class<T> cls, String key, T def)
	{
		return getEnvironmentProperties(key).map(v -> {
			try
			{
				return Enum.valueOf(cls, v);
			}
			catch(Exception e)
			{
				return null;
			}
		}).orElseGet(() -> {
			try
			{
				return Enum.valueOf(cls, System.getProperty(key, def.toString()));
			}
			catch(Exception e)
			{
				return def;
			}
		});
	}

	public String getProperty(String key)
	{
		return getEnvironmentProperties(key).orElseGet(() -> System.getProperty(key));
	}

	private static Map<String, String> resolvePlaceHolders(final Map<String, String> mapin)
	{
		final Function<? super Map.Entry<String, String>, ? extends String> mapper = e -> e.getKey().startsWith("JRM_")?replaceValue(e.getValue(), mapin):e.getValue();
		return mapin.entrySet().stream().collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, mapper));
	}

	private static String replaceValue(final String value, final Map<String, String> mapin)
	{
		return REPLACEMENT_PATTERN.matcher(value).replaceAll(mr -> {
			String ret = null;
			String item = mr.group();
			if("$$".equals(item))
				ret = "\\$";
			else if(item.startsWith("${") && item.endsWith("}"))
				ret = mapin.get(item.substring(2, item.length() - 1));
			else if(item.length() > 1)
				ret = mapin.get(item.substring(1));
			if(ret == null)
				return mr.group();
			ret = replaceValue(ret, mapin);
			return ret;
		});
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> addEnvironment(final Map<String, String> mapin)
	{
		final var env = new HashMap<String, String>(System.getenv());
		final var temp = new HashMap<String, String>(mapin);
		resolvePlaceHolders(env).entrySet().forEach(e -> temp.put(e.getKey().toLowerCase().replace('_', '.'), e.getValue()));
		return Map.ofEntries(temp.entrySet().toArray(new Map.Entry[temp.entrySet().size()]));
	}

}