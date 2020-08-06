package jrm.cli;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class CLIMessages
{
	private static final String BUNDLE_NAME = "jrm.resources.CLIMessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private CLIMessages()
	{
	}

	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		}
		catch (MissingResourceException e)
		{
			return '!' + key + '!';
		}
	}
}
