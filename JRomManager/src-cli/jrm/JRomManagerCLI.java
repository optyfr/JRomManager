package jrm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

import com.eclipsesource.json.Json;

import jrm.cli.CMD;
import jrm.cli.CMD_DIRUPD8R;
import jrm.cli.Progress;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.ui.basic.SrcDstResult;
import jrm.ui.progress.ProgressHandler;
import lombok.val;

public class JRomManagerCLI
{
	Session session;
	Path cwdir = null;
	Path rootdir = null;
	
	ProgressHandler handler = null;
	
	public JRomManagerCLI(CommandLine cmd) throws IOException
	{
		session = Sessions.getSession(true, false);
		rootdir = cwdir = session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize(); //$NON-NLS-1$
		HTMLRenderer.Options.setPlain(true);
		Log.init(session.getUser().settings.getLogPath() + "/JRM.%g.log", false, 1024 * 1024, 5); //$NON-NLS-1$
		handler = new Progress();

		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		do
		{
			if (session.curr_profile != null)
				System.out.format("jrm [%s]> ", session.curr_profile.nfo.file.getName()); //$NON-NLS-1$
			else
				System.out.format("jrm> "); //$NON-NLS-1$
			analyze(splitLine(console.readLine()));
		}
		while (true); // we break out with <control><C>
	}
	
	Pattern splitLinePattern = Pattern.compile("\"([^\"]*)\"|(\\S+)"); //$NON-NLS-1$

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
				case PWD:
					return pwd();
				case CD:
					if (args.length == 1)
						return pwd();
					if (args.length == 2)
						return cd(args[1]);
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case PREFS:
					if (args.length == 1)
						return prefs();
					if (args.length == 2)
						return prefs(args[1]);
					if (args.length == 3)
						return prefs(args[1], args[2]);
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case LOAD:
					if (args.length == 2)
						return load(args[1]);
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case SETTINGS:
					if(session.curr_profile==null)
						return error(CLIMessages.getString("CLI_ERR_NoProfileLoaded")); //$NON-NLS-1$
					if (args.length == 1)
						return settings();
					if (args.length == 2)
						return settings(args[1]);
					if (args.length == 3)
						return settings(args[1], args[2]);
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case SCAN:
					if(session.curr_profile==null)
						return error(CLIMessages.getString("CLI_ERR_NoProfileLoaded")); //$NON-NLS-1$
					session.curr_scan = new Scan(session.curr_profile, handler);
				case SCANRESULT:
					if(session.curr_scan==null)
						return error(CLIMessages.getString("CLI_ERR_ShouldScanFirst")); //$NON-NLS-1$
					if(session.curr_profile.hasPropsChanged())
						return error(CLIMessages.getString("CLI_ERR_PropsChanged")); //$NON-NLS-1$
					if(session.report==null)
						return error(CLIMessages.getString("CLI_ERR_NoReport")); //$NON-NLS-1$
					System.out.println(session.report.stats.getStatus());
					return 0;
				case FIX:
					if(session.curr_scan==null)
						return error(CLIMessages.getString("CLI_ERR_ShouldScanFirst")); //$NON-NLS-1$
					if(session.curr_profile.hasPropsChanged())
						return error(CLIMessages.getString("CLI_ERR_PropsChanged")); //$NON-NLS-1$
					if(session.curr_scan.actions.stream().mapToInt(Collection::size).sum() == 0)
						return error(CLIMessages.getString("CLI_ERR_NothingToFix")); //$NON-NLS-1$
					final Fix fix = new Fix(session.curr_profile, session.curr_scan, handler);
					System.out.format(CLIMessages.getString("CLI_MSG_ActionRemaining"),fix.getActionsRemain()); //$NON-NLS-1$
					return 0;
				case DIRUPD8R:
					if(args.length==1)
						error(CLIMessages.getString("CLI_ERR_DIRUPD8R_SubCmdMissing"));
					return dirupd8r(args[1], Arrays.copyOfRange(args, 2, args.length));
				case TRNTCCK:
					return 0;
				case COMPRESSOR:
					return 0;
				case HELP:
					for(val cmd : CMD.values())
					{
						if(cmd!=CMD.EMPTY && cmd!=CMD.UNKNOWN)
						{
							System.out.append(cmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$
							System.out.append(": ").append(CLIMessages.getString("CLI_HELP_"+cmd.name())); //$NON-NLS-1$
							System.out.append("\n");
						}
					}
					return 0;
				case EXIT:
					return exit(0);
				case EMPTY:
					return 0;
				case UNKNOWN:
					return error(() -> CLIMessages.getString("CLI_ERR_UnknownCommand") + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	
	private int dirupd8r(String cmd, String...args)
	{
		switch(CMD_DIRUPD8R.of(cmd))
		{
			case LSSRC:
			{
				System.out.append("srcdirs = [\n")
					.append(Stream.of(StringUtils.split(session.getUser().settings.getProperty("dat2dir.srcdirs", ""), '|')).map(s->"\t"+Json.value(s).toString()).collect(Collectors.joining(",\n")))
					.append("\n];\n");
				break;
			}
			case LSSDR:
			{
				System.out.append("sdr = [\n")
					.append(SrcDstResult.fromJSON(session.getUser().settings.getProperty("dat2dir.sdr", "[]")).stream().map(sdr->"\t"+sdr.toJSONObject().toString()).collect(Collectors.joining(",\n")))
					.append("\n];\n");
				break;
			}
			case CLEARSRC:
				prefs("dat2dir.srcdirs", "");
				break;
			case CLEARSDR:
				prefs("dat2dir.sdr", "[]");
				break;
			case ADDSRC:
			{
				val list = Stream.of(StringUtils.split(session.getUser().settings.getProperty("dat2dir.srcdirs", ""), '|')).collect(Collectors.toCollection(ArrayList::new));
				list.add(args[0]);
				prefs("dat2dir.srcdirs", list.stream().collect(Collectors.joining("|")));
				break;
			}
			case ADDSDR:
			{
				val list = SrcDstResult.fromJSON(session.getUser().settings.getProperty("dat2dir.sdr", "[]"));
				val sdr = new SrcDstResult();
				sdr.src = new File(args[0]);
				sdr.dst = new File(args[1]);
				list.add(sdr);
				prefs("dat2dir.sdr", SrcDstResult.toJSON(list));
				break;
			}
			case HELP:
			{
				for(val ducmd : CMD_DIRUPD8R.values())
				{
					if(ducmd!=CMD_DIRUPD8R.EMPTY && ducmd!=CMD_DIRUPD8R.UNKNOWN)
					{
						System.out.append(ducmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$
						System.out.append(": ").append(CLIMessages.getString("CLI_HELP_DIRUPD8R_"+ducmd.name())); //$NON-NLS-1$
						System.out.append("\n");
					}
				}
				break;
			}
			case EMPTY:
				break;
			case UNKNOWN:
				return error(() -> CLIMessages.getString("CLI_ERR_UnknownCommand") + cmd + " " + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return 0;
	}
	
	private int prefs()
	{
		for(Map.Entry<Object, Object> entry : session.getUser().settings.getProperties().entrySet())
			System.out.format("%s=%s\n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
		return 0;
	}
	
	private int prefs(final String name)
	{
		if(!session.getUser().settings.hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$
		else
			System.out.format("%s=%s\n", name, session.getUser().settings.getProperty(name,"")); //$NON-NLS-1$ //$NON-NLS-2$
		return 0;
	}

	private int prefs(final String name, final String value)
	{
		session.getUser().settings.setProperty(name, value);
		session.getUser().settings.saveSettings();
		return 0;
	}

	private int settings()
	{
		for(Map.Entry<Object, Object> entry : session.curr_profile.settings.getProperties().entrySet())
			System.out.format("%s=%s\n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
		return 0;
	}

	private int settings(final String name)
	{
		if(!session.curr_profile.settings.hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$
		else
			System.out.format("%s=%s\n", name, session.curr_profile.settings.getProperty(name,"")); //$NON-NLS-1$ //$NON-NLS-2$
		return 0;
	}

	private int settings(final String name, final String value)
	{
		session.curr_profile.settings.setProperty(name, value);
		session.curr_profile.saveSettings();
		return 0;
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
	
	private int error(Supplier<String> supplier)
	{
		System.out.println(supplier.get());
		return -1;
	}
	
	private int load(String profile)
	{
		Path candidate = cwdir.resolve(profile);
		if(Files.isRegularFile(candidate))
			session.curr_profile = Profile.load(session, candidate.toFile(), handler);
		else
			System.out.format(CLIMessages.getString("CLI_ERR_ProfileNotExist"),profile); //$NON-NLS-1$
		return 0;
	}

	private int cd(String dir)
	{
		if(dir.equals(File.separator))
			cwdir = rootdir;
		else
		{
			if(dir.startsWith("~")) //$NON-NLS-1$
				dir = dir.replace("~", rootdir.toString()); //$NON-NLS-1$
			Path candidate = cwdir.resolve(dir).normalize();
			if(rootdir.startsWith(candidate) && !rootdir.equals(candidate))
			{
				cwdir = rootdir;
				System.out.format(CLIMessages.getString("CLI_ERR_CantGoUpDir"), dir); //$NON-NLS-1$
			}
			else if(Files.isDirectory(candidate))
			{
				if(candidate.startsWith(rootdir))
					cwdir = candidate;
				else
					System.out.format(CLIMessages.getString("CLI_ERR_CantChangeDir"), dir); //$NON-NLS-1$
			}
			else
				System.out.format(CLIMessages.getString("CLI_ERR_UnknownDir"), dir); //$NON-NLS-1$
		}
		return 0;
	}
		

	private int pwd()
	{
		System.out.println("~/" + rootdir.relativize(cwdir)); //$NON-NLS-1$
		return 0;
	}

	private int list() throws IOException
	{
		Files.walk(cwdir,  1).filter(p->Files.isDirectory(p)&&!p.equals(cwdir)).sorted(Path::compareTo).map(cwdir::relativize).forEachOrdered(p->System.out.format("<DIR>\t%s\n",p)); //$NON-NLS-1$
		for(val row : ProfileNFO.list(session, cwdir.toFile()))
			System.out.format("<DAT>\t%s\n",row.getName()); //$NON-NLS-1$
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
