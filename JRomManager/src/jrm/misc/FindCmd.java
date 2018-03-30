package jrm.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuffer output = new StringBuffer();
			new Thread()
			{
				@Override
				public void run()
				{
					String line;
					try
					{
						while(null!=(line=reader.readLine()))
							output.append(line);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				};
			}.start();
			if(process.waitFor()==0)
				return output.toString();
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
