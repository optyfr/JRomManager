package jrm.misc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class FindCmd
{
	public static String findCmd(String cmd)
	{
		ProcessBuilder pb;
		if (OSValidator.isWindows())
			pb = new ProcessBuilder("where.exe", cmd);
		else
			pb = new ProcessBuilder("which", cmd);
		try
		{
			pb.redirectError();
			Process process = pb.start();
			String output = IOUtils.toString(process.getInputStream(),(Charset)null).trim();
			if(process.waitFor()==0)
				return output;
		}
		catch (IOException | InterruptedException exp)
		{
			exp.printStackTrace();
		}
		return cmd;

	}

	public static String findZip()
	{
		return findCmd("7z");
	}

	public static String findTZip()
	{
		return findCmd("trrntzip");
	}

	public static String find7z()
	{
		return findCmd("7z");
	}
}
