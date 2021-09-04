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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.NonNull;

/**
 * Console Logger
 * @author optyfr
 *
 */
public class Log
{
	public static class Formatter extends java.util.logging.Formatter
	{
		@Override
		public String format(LogRecord theRecord)
		{
			final var currDate = new Date();
			currDate.setTime(theRecord.getMillis());
			String message = formatMessage(theRecord);
			var throwableMsg = "";
			if (theRecord.getThrown() != null)
			{
				final var sw = new StringWriter();
				final var pw = new PrintWriter(sw);
				pw.println();
				theRecord.getThrown().printStackTrace(pw);
				pw.close();
				throwableMsg = sw.toString();
			}
			return String.format("[%1$tF %1$tT] [%2$s] %3$s%4$s%n", currDate, theRecord.getLevel().getName(), message, throwableMsg);
		}
	}

	public static final Formatter formatter = new Formatter();
	private static @Getter boolean init = false;
	
	private Log()
	{
		Logger.getGlobal().addHandler(new ConsoleHandler());
	}
	
	public static void init(final String file, final boolean debug, final int limit, final int count)	//NOSONAR
	{
		try
		{
			final var filehandler = new FileHandler(file, 100 * 1024 * 1024, 5, false);
			filehandler.setFormatter(Log.formatter);
			Logger.getGlobal().setUseParentHandlers(false);
			Logger.getGlobal().addHandler(filehandler);
			if(debug)
			{
				final var consolehandler = new ConsoleHandler();
				consolehandler.setLevel(Level.FINE);
				consolehandler.setFormatter(Log.formatter);
				Logger.getGlobal().addHandler(consolehandler);
				Logger.getGlobal().setLevel(Level.FINE);
			}
			init = true;
		}
		catch (SecurityException | IOException e)
		{
			System.console().format("%s%n", e.getMessage());
		}
	}
	
	public static void setLevel(Level level)
	{
		for(Handler h : Logger.getGlobal().getHandlers())
			h.setLevel(level);
		Logger.getGlobal().setLevel(level);
	}

	public static @NonNull Level getLevel()
	{
		return Optional.ofNullable(Logger.getGlobal().getLevel()).orElse(Level.INFO);
	}
	
	public static void info(final Object msg)
	{
		if(msg==null)
			return;
		if(msg instanceof String)
			Logger.getGlobal().info((String)msg);
		else
			Logger.getGlobal().info(msg::toString);
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
			Logger.getGlobal().warning(msg::toString);
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
			Logger.getGlobal().severe(msg::toString);
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
			Logger.getGlobal().fine(msg::toString);
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
			Logger.getGlobal().finest(msg::toString);
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
			Logger.getGlobal().config(msg::toString);
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
