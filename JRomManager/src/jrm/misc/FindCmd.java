package jrm.misc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

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
			exp.printStackTrace();
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
