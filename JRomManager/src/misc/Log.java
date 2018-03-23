package misc;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
	public Log()
	{
		Logger.getGlobal().addHandler(new ConsoleHandler());
	}
	
	public static void info(String msg)
	{
		Logger.getGlobal().info(msg);
	}

	public static void warn(String msg)
	{
		Logger.getGlobal().warning(msg);
	}

	public static void err(String msg)
	{
		Logger.getGlobal().severe(msg);
	}
	
	public static void err(String msg, Throwable e)
	{
		Logger.getGlobal().log(Level.SEVERE, msg, e);
	}
}
