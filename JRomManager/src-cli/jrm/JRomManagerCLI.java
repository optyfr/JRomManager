package jrm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import jrm.cli.CMD;
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

	public JRomManagerCLI(CommandLine cmd) throws IOException
	{
		session = Sessions.getSession(true, false);
		rootdir = cwdir = session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
		Log.init(session.getUser().settings.getLogPath() + "/JRM.%g.log", true, 1024 * 1024, 5);
		handler = new Progress();

		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		do
		{
			if (current_profile != null)
				System.out.format("jrm [%s]> ", current_profile.nfo.file.getName());
			else
				System.out.format("jrm> ");
			analyze(splitLine(console.readLine()));
		}
		while (true); // we break out with <control><C>
	}
	
	Pattern splitLinePattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");

	private String[] splitLine(String line)
	{
		List<String> list = new ArrayList<>();
		Matcher m = splitLinePattern.matcher(line);
		while (m.find())
			list.add(m.group(m.group(1) != null?1:2));
		return list.stream().toArray(String[]::new);
	}
	
	protected int analyze(String... args)
	{
		if (args.length == 0)
			return 0;
		try
		{
			switch (CMD.of(args[0]))
			{
				case LS:
					return list();
				case CD:
					if (args.length == 1)
						return pwd();
					return cd(args[1]);
				case LOAD:
					if (args.length == 2)
						return load(args[1]);
					return error("wrong arguments");
				case PREFS:
					if(args.length == 1)
					{
						for(Map.Entry<Object, Object> entry : session.getUser().settings.getProperties().entrySet())
							System.out.format("%s=%s\n", entry.getKey(), entry.getValue());
					}
					return 0;
				case SETTINGS:
					if(current_profile!=null)
					{
						if(args.length == 1)
						{
							for(Map.Entry<Object, Object> entry : current_profile.settings.getProperties().entrySet())
								System.out.format("%s=%s\n", entry.getKey(), entry.getValue());
						}
					}
					else
						return error("No profile loaded");
					return 0;
				case PWD:
					return pwd();
				case EXIT:
					return exit(0);
				case EMPTY:
					return 0;
				case UNKNOWN:
					return error("Unknown command : " + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" ")));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	private int exit(int status)
	{
		System.exit(status);
		return status;
	}
	
	private int error(String msg)
	{
		System.out.println(msg);
		return -1;
	}
	
	private int load(String profile)
	{
		Path candidate = cwdir.resolve(profile);
		if(Files.isRegularFile(candidate))
			current_profile = Profile.load(session, candidate.toFile(), handler);
		else
			System.out.format("Error: profile \"%s\" does not exist\n",profile);
		return 0;
	}

	private int cd(String dir)
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
		return 0;
	}
		

	private int pwd()
	{
		System.out.println("~/" + rootdir.relativize(cwdir));
		return 0;
	}

	private int list() throws IOException
	{
		Files.walk(cwdir,  1).filter(p->Files.isDirectory(p)&&!p.equals(cwdir)).sorted(Path::compareTo).map(cwdir::relativize).forEachOrdered(p->System.out.format("<DIR>\t%s\n",p));
		for(val row : ProfileNFO.list(session, cwdir.toFile()))
			System.out.format("<DAT>\t%s\n",row.getName());
		return 0;
	}

	public static void main(String[] args)
	{
		Options options = new Options();
		try
		{
			new JRomManagerCLI(new DefaultParser().parse(options, args));
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
