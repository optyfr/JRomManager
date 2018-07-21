package jrm.misc;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Console Logger
 * @author optyfr
 *
 */
public class Log
{
	public Log()
	{
		Logger.getGlobal().addHandler(new ConsoleHandler());
	}

	public static void info(final String msg)
	{
		Logger.getGlobal().info(msg);
	}

	public static void warn(final String msg)
	{
		Logger.getGlobal().warning(msg);
	}

	public static void err(final String msg)
	{
		Logger.getGlobal().severe(msg);
	}

	public static void err(final String msg, final Throwable e)
	{
		Logger.getGlobal().log(Level.SEVERE, msg, e);
	}
}
