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
