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

import lombok.experimental.UtilityClass;

/**
 * Utility class to determine the current OS
 */
public @UtilityClass class OSValidator
{
	private static String os = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

	public static boolean isWindows()
	{
		return (OSValidator.os.indexOf("win") >= 0); //$NON-NLS-1$
	}

	public static boolean isMac()
	{
		return (OSValidator.os.indexOf("mac") >= 0); //$NON-NLS-1$
	}

	public static boolean isUnix()
	{
		return (OSValidator.os.indexOf("nix") >= 0 || OSValidator.os.indexOf("nux") >= 0 || OSValidator.os.indexOf("aix") >= 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static boolean isSolaris()
	{
		return (OSValidator.os.indexOf("sunos") >= 0); //$NON-NLS-1$
	}
}
