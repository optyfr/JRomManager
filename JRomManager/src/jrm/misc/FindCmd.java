package jrm.misc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

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
/**
 * Wrapper to which or where.exe system commands (depends of the OS)
 * @author optyfr
 *
 */
public class FindCmd
{
	/**
	 * shortcut for finding {@code cmd} command
	 * @param cmd the command to find (without path)
	 * @return full path to {@code cmd} if successful, otherwise return initial {@code cmd}
	 */
	public static String findCmd(final String cmd)
	{
		ProcessBuilder pb;
		if (OSValidator.isWindows())
			pb = new ProcessBuilder("where.exe", cmd); //$NON-NLS-1$
		else
			pb = new ProcessBuilder("which", cmd); //$NON-NLS-1$
		try
		{
			pb.redirectError();
			final Process process = pb.start();
			final String output = IOUtils.toString(process.getInputStream(),(Charset)null).trim();
			if(process.waitFor()==0)
				return output;
		}
		catch (IOException | InterruptedException exp)
		{
			Log.err(exp.getMessage(),exp);
		}
		return cmd;

	}

	/**
	 * shortcut for finding zip command (in fact it is currently 7zip)
	 * @return full path if successful, otherwise "7z"
	 */
	public static String findZip()
	{
		return FindCmd.findCmd("7z"); //$NON-NLS-1$
	}

	/**
	 * shortcut for finding trrntzip command
	 * @return full path if successful, otherwise "trrntzip"
	 */
	public static String findTZip()
	{
		return FindCmd.findCmd("trrntzip"); //$NON-NLS-1$
	}

	/**
	 * shortcut for finding 7z command
	 * @return full path if successful, otherwise "7z"
	 */
	public static String find7z()
	{
		return FindCmd.findCmd("7z"); //$NON-NLS-1$
	}
}
