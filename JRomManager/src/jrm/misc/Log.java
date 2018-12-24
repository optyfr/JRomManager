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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Console Logger
 * @author optyfr
 *
 */
public class Log
{
	static class Formatter extends java.util.logging.Formatter
	{
		@Override
		public String format(LogRecord record)
		{
			Date dat = new Date();
			dat.setTime(record.getMillis());
			String message = formatMessage(record);
			String throwable = "";
			if (record.getThrown() != null)
			{
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				pw.println();
				record.getThrown().printStackTrace(pw);
				pw.close();
				throwable = sw.toString();
			}
			return String.format("[%1$tF %1$tT] [%2$s] %3$s%4$s%n", dat, record.getLevel().getName(), message, throwable);
		}
	}

	public final static Formatter formatter = new Formatter();
	
	public Log()
	{
		Logger.getGlobal().addHandler(new ConsoleHandler());
	}
	
	public static void init(final String file, final boolean debug, final int limit, final int count)
	{
		try
		{
			final FileHandler filehandler = new FileHandler(file, 1024 * 1024, 5, false);
			filehandler.setFormatter(Log.formatter);
			Logger.getGlobal().setUseParentHandlers(false);
			Logger.getGlobal().addHandler(filehandler);
			if(debug)
			{
				final ConsoleHandler consolehandler = new ConsoleHandler();
				consolehandler.setLevel(Level.FINE);
				consolehandler.setFormatter(Log.formatter);
				Logger.getGlobal().addHandler(consolehandler);
				Logger.getGlobal().setLevel(Level.FINE);
			}
		}
		catch (SecurityException | IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void setLevel(Level level)
	{
		for(Handler h : Logger.getGlobal().getHandlers())
			h.setLevel(level);
		Logger.getGlobal().setLevel(level);
	}

	public static Level getLevel()
	{
		return Logger.getGlobal().getLevel();
	}
	
	public static void info(final Object msg)
	{
		if(msg==null)
			return;
		if(msg instanceof String)
			Logger.getGlobal().info((String)msg);
		else
			Logger.getGlobal().info(msg.toString());
	}

	public static void info(Supplier<String> msgSupplier)
	{
		Logger.getGlobal().info(msgSupplier);
	}

	public static void warn(final Object msg)
	{
		if(msg==null)
			return;
		if(msg instanceof String)
			Logger.getGlobal().warning((String)msg);
		else
			Logger.getGlobal().warning(msg.toString());
	}

	public static void warn(Supplier<String> msgSupplier)
	{
		Logger.getGlobal().warning(msgSupplier);
	}

	public static void err(final Object msg)
	{
		if(msg==null)
			return;
		if(msg instanceof String)
			Logger.getGlobal().severe((String)msg);
		else
			Logger.getGlobal().severe(msg.toString());
	}

	public static void err(Supplier<String> msgSupplier)
	{
		Logger.getGlobal().severe(msgSupplier);
	}

	public static void err(final String msg, final Throwable e)
	{
		Logger.getGlobal().log(Level.SEVERE, msg, e);
	}
	
	public static void err(final Supplier<String> msgSupplier, final Throwable e)
	{
		Logger.getGlobal().log(Level.SEVERE, e, msgSupplier);
	}
	
	public static void debug(final Object msg)
	{
		if(msg==null)
			return;
		if(msg instanceof String)
			Logger.getGlobal().fine((String)msg);
		else
			Logger.getGlobal().fine(msg.toString());
	}
	
	public static void debug(Supplier<String> msgSupplier)
	{
		Logger.getGlobal().fine(msgSupplier);
	}
	
	public static void trace(final Object msg)
	{
		if(msg==null)
			return;
		if(msg instanceof String)
			Logger.getGlobal().finest((String)msg);
		else
			Logger.getGlobal().finest(msg.toString());
	}
	
	public static void trace(Supplier<String> msgSupplier)
	{
		Logger.getGlobal().finest(msgSupplier);
	}
	
	public static void config(final Object msg)
	{
		if(msg==null)
			return;
		if(msg instanceof String)
			Logger.getGlobal().config((String)msg);
		else
			Logger.getGlobal().config(msg.toString());
	}
	
	public static void config(Supplier<String> msgSupplier)
	{
		Logger.getGlobal().config(msgSupplier);
	}
	
	public static void throwing(String sourceClass, String sourceMethod, Throwable thrown)
	{
		Logger.getGlobal().throwing(sourceClass, sourceMethod, thrown);
	}
}
