package jrm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.cli.*;

import jrm.misc.Log;

public class JRomManagerCLI
{

	public JRomManagerCLI() throws IOException
	{
		String commandLine;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

		// we break out with <control><C>
		while (true)
		{
			// read what the user entered
			System.out.print("jrm>");
			commandLine = console.readLine();

			// if the user entered a return, just loop again
			if (commandLine.equals(""))
				continue;

		}
	}

	public static void main(String[] args)
	{
		Options options = new Options();
		try
		{
			CommandLine cmd = new DefaultParser().parse(options, args);
			new JRomManagerCLI();
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(),e);
			new HelpFormatter().printHelp(JRomManagerCLI.class.getName(), options);
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
