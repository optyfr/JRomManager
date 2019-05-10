package jrm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import jrm.cli.Progress;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.manager.ProfileNFO;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.ui.progress.ProgressHandler;
import lombok.val;

public class JRomManagerCLI
{
	Session session;
	Path cwdir = null;
	Path rootdir = null;
	
	ProgressHandler handler = null;
	
	Profile current_profile = null;

	public JRomManagerCLI() throws IOException
	{
		String commandLine;
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		session = Sessions.getSession(true, false);
		rootdir = cwdir = session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
		Log.init(session.getUser().settings.getLogPath() + "/JRM.%g.log", true, 1024 * 1024, 5);
		handler = new Progress();

		// we break out with <control><C>
		while (true)
		{
			// read what the user entered
			System.out.print("jrm> ");
			commandLine = console.readLine();

			// if the user entered a return, just loop again
			if (commandLine.equals(""))
				continue;

			
			String[] args = StringUtils.split(commandLine);
			switch (args[0].toLowerCase())
			{
				case "ls":
				case "list":
				case "dir":
					list();
					break;
				case "cd":
					if(args.length==1)
						pwd();
					else
						cd(args[1]);
					break;
				case "load":
					if(args.length==2)
						load(args[1]);
					else
						System.out.println("wrong arguments");
					break;
				case "pwd":
					pwd();
					break;
				case "exit":
				case "quit":
				case "bye":
					System.exit(0);
					break;
				default:
					System.out.println("Unknown command : "+Stream.of(args).collect(Collectors.joining(" ")));
					break;
			}
		}
	}

	private void load(String profile)
	{
		Path candidate = cwdir.resolve(profile);
		if(Files.isRegularFile(candidate))
			current_profile = Profile.load(session, candidate.toFile(), handler);
		else
			System.out.format("Error: profile \"%s\" does not exist\n",profile);
	}

	private void cd(String dir)
	{
		if(dir.equals(File.separator))
			cwdir = rootdir;
		else
		{
			if(dir.startsWith("~"))
				dir = dir.replace("~", rootdir.toString());
			Path candidate = cwdir.resolve(dir).normalize();
			if(rootdir.startsWith(candidate) && !rootdir.equals(candidate))
			{
				cwdir = rootdir;
				System.out.format("Can't go up from profiles dir\n", dir);
			}
			else if(Files.isDirectory(candidate))
			{
				if(candidate.startsWith(rootdir))
					cwdir = candidate;
				else
					System.out.format("Can't change to directory \"%s\"\n", dir);
			}
			else
				System.out.format("Unknown directory \"%s\"\n", dir);
		}
	}
		

	private void pwd()
	{
		System.out.println("~/" + rootdir.relativize(cwdir));
	}

	private void list()
	{
		val rows = ProfileNFO.list(session, cwdir.toFile());
		for(val row : rows)
		{
			System.out.println(row.getName());
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
