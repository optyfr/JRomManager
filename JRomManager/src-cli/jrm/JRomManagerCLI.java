package jrm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.eclipsesource.json.Json;

import jrm.batch.Compressor;
import jrm.batch.Compressor.FileResult;
import jrm.batch.CompressorFormat;
import jrm.batch.DirUpdater;
import jrm.batch.TorrentChecker;
import jrm.cli.CMD;
import jrm.cli.CMD_DIRUPD8R;
import jrm.cli.CMD_TRNTCHK;
import jrm.cli.Progress;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.misc.ProfileSettings;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SrcDstResult;
import lombok.val;

public class JRomManagerCLI
{
	Session session;
	Path cwdir = null;
	Path rootdir = null;

	Progress handler = null;

	public JRomManagerCLI(CommandLine cmd) throws IOException
	{
		session = Sessions.getSession(true, false);
		rootdir = cwdir = session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize(); //$NON-NLS-1$
		HTMLRenderer.Options.setPlain(true);
		Log.init(session.getUser().settings.getLogPath() + "/JRM.%g.log", false, 1024 * 1024, 5); //$NON-NLS-1$
		handler = new Progress();

		if (cmd.hasOption('i'))
		{
			try (BufferedReader console = new BufferedReader(new InputStreamReader(System.in));)
			{
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
		}
		else
		{
			Reader reader = cmd.hasOption('f')?new FileReader(cmd.getOptionValue('f')):new InputStreamReader(System.in);
			try (BufferedReader in = new BufferedReader(reader);)
			{
				String line;
				while (null != (line = in.readLine()))
				{
					if(line.startsWith("#"))
						continue;
					analyze(splitLine(line));
				}
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private final Pattern splitLinePattern = Pattern.compile("\"([^\"]*)\"|(\\S+)"); //$NON-NLS-1$
	private final Pattern envPattern = Pattern.compile("\\$(?:([\\w\\.]+)|\\{([\\w\\.]+)\\})"); //$NON-NLS-1$

	private Optional<String> getEnv(String name)
	{
		Optional<String> ret = Optional.ofNullable(System.getProperty(name));
		if(!ret.isPresent())
			ret = Optional.ofNullable(System.getenv(name));
		return ret;
	}
	
	private String[] splitLine(String line)
	{
		List<String> list = new ArrayList<>();
		Matcher m = splitLinePattern.matcher(line);
		while (m.find())
		{
			Matcher im = envPattern.matcher(m.group(m.group(1) != null ? 1 : 2));
			StringBuffer sb = new StringBuffer();
			while (im.find())
				im.appendReplacement(sb, getEnv(im.group(im.group(1) != null ? 1 : 2)).map(Matcher::quoteReplacement).orElse(""));
			im.appendTail(sb);
			list.add(sb.toString());
		}
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
				case QUIET:
					handler.quiet(true);
					return 0;
				case VERBOSE:
					handler.quiet(false);
					return 0;
				case SET:
					if(args.length == 1)
					{
						System.getProperties().forEach((k,v)->System.out.println(k+"="+v));
						System.getenv().forEach((k,v)->System.out.println(k+"="+v));
						return 0;
					}
					if (args.length == 2)
					{
						getEnv(args[1]).ifPresent(System.out::println);
						return 0;
					}
					if(args.length == 3)
					{
						if(args[2].isEmpty())
							System.clearProperty(args[1]);
						else
							System.setProperty(args[1], args[2]);
						return 0;
					}
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case CD:
					if (args.length == 1)
						return pwd();
					if (args.length == 2)
						return cd(args[1]);
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case RM:
				{
					Options options = new Options().addOption("r", "recursive", false, "Recursive delete");
					CommandLine cmdline = new DefaultParser().parse(options, Arrays.copyOfRange(args, 1, args.length), true);
					for(String arg : cmdline.getArgList())
					{
						Path path = Paths.get(arg);
						if(Files.exists(path))
						{
							if(Files.isDirectory(path))
							{
								try
								{
									Files.delete(path);
								}
								catch(DirectoryNotEmptyException e)
								{
									if(cmdline.hasOption('r'))	// recursively delete from bottom to top
										Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
								}
							}
							else
								Files.delete(path);
						}
					}
					return 0;
				}
				case MD:
				{
					Options options = new Options().addOption("p", "parents", false, "create parents up to this directory");
					CommandLine cmdline = new DefaultParser().parse(options, Arrays.copyOfRange(args, 1, args.length), true);
					for(String arg : cmdline.getArgList())
					{
						Path path = Paths.get(arg);
						if(!Files.exists(path))
						{
							if(cmdline.hasOption('p'))
								Files.createDirectories(path);
							else
								Files.createDirectory(path);
						}
					}
					break;
				}
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
					if (session.curr_profile == null)
						return error(CLIMessages.getString("CLI_ERR_NoProfileLoaded")); //$NON-NLS-1$
					if (args.length == 1)
						return settings();
					if (args.length == 2)
						return settings(args[1]);
					if (args.length == 3)
						return settings(args[1], args[2]);
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case SCAN:
					if (session.curr_profile == null)
						return error(CLIMessages.getString("CLI_ERR_NoProfileLoaded")); //$NON-NLS-1$
					session.curr_scan = new Scan(session.curr_profile, handler);
					return session.curr_scan.actions.stream().mapToInt(c->c.size()).sum();
				case SCANRESULT:
					if (session.curr_scan == null)
						return error(CLIMessages.getString("CLI_ERR_ShouldScanFirst")); //$NON-NLS-1$
					if (session.curr_profile.hasPropsChanged())
						return error(CLIMessages.getString("CLI_ERR_PropsChanged")); //$NON-NLS-1$
					if (session.report == null)
						return error(CLIMessages.getString("CLI_ERR_NoReport")); //$NON-NLS-1$
					System.out.println(session.report.stats.getStatus());
					return 0;
				case FIX:
					if (session.curr_scan == null)
						return error(CLIMessages.getString("CLI_ERR_ShouldScanFirst")); //$NON-NLS-1$
					if (session.curr_profile.hasPropsChanged())
						return error(CLIMessages.getString("CLI_ERR_PropsChanged")); //$NON-NLS-1$
					if (session.curr_scan.actions.stream().mapToInt(Collection::size).sum() == 0)
						return error(CLIMessages.getString("CLI_ERR_NothingToFix")); //$NON-NLS-1$
					final Fix fix = new Fix(session.curr_profile, session.curr_scan, handler);
					System.out.format(CLIMessages.getString("CLI_MSG_ActionRemaining"), fix.getActionsRemain()); //$NON-NLS-1$
					return fix.getActionsRemain();
				case DIRUPD8R:
					if (args.length == 1)
						return error(CLIMessages.getString("CLI_ERR_DIRUPD8R_SubCmdMissing"));
					return dirupd8r(args[1], Arrays.copyOfRange(args, 2, args.length));
				case TRNTCHK:
					if (args.length == 1)
						return error(CLIMessages.getString("CLI_ERR_TRNTCHK_SubCmdMissing"));
					return trntchk(args[1], Arrays.copyOfRange(args, 2, args.length));
				case COMPRESSOR:
					if (args.length < 3)
						return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
					return compressor(Arrays.copyOfRange(args, 1, args.length));
				case HELP:
					for (val cmd : CMD.values())
					{
						if (cmd != CMD.EMPTY && cmd != CMD.UNKNOWN)
						{
							System.out.append(cmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$
							System.out.append(": ").append(CLIMessages.getString("CLI_HELP_" + cmd.name())); //$NON-NLS-1$
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
			Log.err(e.getMessage(), e);
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			Log.err(e.getMessage(), e);
		}
		return -1;
	}

	private int dirupd8r(String cmd, String... args) throws ParseException
	{
		switch (CMD_DIRUPD8R.of(cmd))
		{
			case LSSRC:
			{
				System.out.append("srcdirs = [\n").append(Stream.of(StringUtils.split(session.getUser().settings.getProperty("dat2dir.srcdirs", ""), '|')).map(s -> "\t" + Json.value(s).toString()).collect(Collectors.joining(",\n"))).append("\n];\n");
				break;
			}
			case LSSDR:
			{
				System.out.append("sdr = [\n").append(SrcDstResult.fromJSON(session.getUser().settings.getProperty("dat2dir.sdr", "[]")).stream().map(sdr -> "\t" + sdr.toJSONObject().toString()).collect(Collectors.joining(",\n"))).append("\n];\n");
				break;
			}
			case CLEARSRC:
				prefs("dat2dir.srcdirs", "");
				break;
			case CLEARSDR:
				prefs("dat2dir.sdr", "[]");
				break;
			case PRESETS:
			{
				if(args.length==0)
				{
					System.out.println("TZIP");
					System.out.println("DIR");
					return 0;
				}
				else if(args.length==2)
				{
					val list = SrcDstResult.fromJSON(session.getUser().settings.getProperty("dat2dir.sdr", "[]"));
					int index = Integer.parseInt(args[0]);
					if(index < list.size())
					{
						try
						{
							switch (args[1])
							{
								case "TZIP":
									ProfileSettings.TZIP(session, list.get(index).src);
									break;
								case "DIR":
									ProfileSettings.DIR(session, list.get(index).src);
									break;
							}
						}
						catch (IOException e)
						{
							Log.err(e.getMessage(), e);
						}
					}
					return 0;
				}
				else
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
			}
			case SETTINGS:
			{
				if (args.length > 0)
				{
					val list = SrcDstResult.fromJSON(session.getUser().settings.getProperty("dat2dir.sdr", "[]"));
					if (args.length > 0)
					{
						int index = Integer.parseInt(args[0]);
						if (index < list.size())
						{
							try
							{
								ProfileSettings settings = session.getUser().settings.loadProfileSettings(list.get(index).src, null);
								if (args.length == 3)
								{
									settings.setProperty(args[1], args[2]);
									session.getUser().settings.saveProfileSettings(list.get(index).src, settings);
								}
								else if (args.length == 2)
									System.out.format("%s\n", settings.getProperty(args[1], "")); //$NON-NLS-1$
								else
									for (Map.Entry<Object, Object> entry : settings.getProperties().entrySet())
										System.out.format("%s=%s\n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
							}
							catch (IOException e)
							{
								Log.err(e.getMessage(), e);
							}
						}
					}
					return 0;
				}
				else
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
			}
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
			case START:
			{
				List<SrcDstResult> sdrl = SrcDstResult.fromJSON(session.getUser().settings.getProperty("dat2dir.sdr", "[]"));
				List<File> srcdirs = Stream.of(StringUtils.split(session.getUser().settings.getProperty("dat2dir.srcdirs", ""), '|')).map(s -> new File(s)).collect(Collectors.toCollection(ArrayList::new));
				final String[] results = new String[sdrl.size()];
				ResultColUpdater resulthandler = new ResultColUpdater()
				{
					@Override
					public void updateResult(int row, String result)
					{
						results[row] = result;
					}

					@Override
					public void clearResults()
					{
						for (int i = 0; i < results.length; i++)
							results[i] = "";
					}
				};
				Options options = new Options().addOption("d", "dryrun", false, "Dry run");
				CommandLine cmdline = new DefaultParser().parse(options, args);
				new DirUpdater(session, sdrl, handler, srcdirs, resulthandler, cmdline.hasOption('d'));
				for (int i = 0; i < results.length; i++)
					System.out.println(i + " = " + results[i]);
				break;
			}
			case HELP:
			{
				for (val ducmd : CMD_DIRUPD8R.values())
				{
					if (ducmd != CMD_DIRUPD8R.EMPTY && ducmd != CMD_DIRUPD8R.UNKNOWN)
					{
						System.out.append(ducmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$
						System.out.append(": ").append(CLIMessages.getString("CLI_HELP_DIRUPD8R_" + ducmd.name())); //$NON-NLS-1$
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

	private int trntchk(String cmd, String... args) throws IOException, ParseException
	{
		switch (CMD_TRNTCHK.of(cmd))
		{
			case LSSDR:
			{
				System.out.append("sdr = [\n").append(SrcDstResult.fromJSON(session.getUser().settings.getProperty("trntchk.sdr", "[]")).stream().map(sdr -> "\t" + sdr.toJSONObject().toString()).collect(Collectors.joining(",\n"))).append("\n];\n");
				break;
			}
			case CLEARSDR:
				prefs("trntchk.sdr", "[]");
				break;
			case ADDSDR:
			{
				val list = SrcDstResult.fromJSON(session.getUser().settings.getProperty("trntchk.sdr", "[]"));
				val sdr = new SrcDstResult();
				sdr.src = new File(args[0]);
				sdr.dst = new File(args[1]);
				list.add(sdr);
				prefs("trntchk.sdr", SrcDstResult.toJSON(list));
				break;
			}
			case START:
			{
				List<SrcDstResult> sdrl = SrcDstResult.fromJSON(session.getUser().settings.getProperty("trntchk.sdr", "[]"));
				final String[] results = new String[sdrl.size()];
				ResultColUpdater resulthandler = new ResultColUpdater()
				{
					@Override
					public void updateResult(int row, String result)
					{
						results[row] = result;
					}

					@Override
					public void clearResults()
					{
						for (int i = 0; i < results.length; i++)
							results[i] = "";
					}
				};
				Options options = new Options()
						.addOption("m", "checkmode", true, "Check mode")
						.addOption("u", "removeunknown", true, "Remove unknown files")
						.addOption("w", "removewrongsized", true, "Remove wrong sized files")
						.addOption("a", "detectarchives", true, "Detect archived folders");
				CommandLine cmdline = new DefaultParser().parse(options, args);
				TrntChkMode mode = cmdline.hasOption('m')?TrntChkMode.valueOf(cmdline.getOptionValue('m')):TrntChkMode.FILESIZE;
				boolean removeunknown = cmdline.hasOption('u');
				boolean removewrongsized = cmdline.hasOption('w');
				boolean detectarchives = cmdline.hasOption('d');
				new TorrentChecker(session, handler, sdrl, mode, resulthandler, removeunknown, removewrongsized, detectarchives);
				break;
			}
			case HELP:
			{
				for (val ducmd : CMD_TRNTCHK.values())
				{
					if (ducmd != CMD_TRNTCHK.EMPTY && ducmd != CMD_TRNTCHK.UNKNOWN)
					{
						System.out.append(ducmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$
						System.out.append(": ").append(CLIMessages.getString("CLI_HELP_TRNTCHK_" + ducmd.name())); //$NON-NLS-1$
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

	private int compressor(String... args) throws IOException, ParseException
	{
		Options options = new Options()
				.addRequiredOption("c", "compressor", true, "Compression format")
				.addOption("f", "force", false, "Force recompression");
		try
		{
			CommandLine cmdline = new DefaultParser().parse(options, args, true);
			CompressorFormat format = cmdline.hasOption('c')?CompressorFormat.valueOf(cmdline.getOptionValue('c')):CompressorFormat.TZIP;
			boolean force = cmdline.hasOption('f');
			for(String arg : cmdline.getArgList())
			{
				File path = new File(arg);
				List<FileResult> frl = path.isDirectory() ? Files.walk(path.toPath()).filter(p -> Files.isRegularFile(p) && FilenameUtils.isExtension(p.getFileName().toString(), Compressor.extensions)).map(p -> new FileResult(p.toFile())).collect(Collectors.toList()) : Arrays.asList(new FileResult(path));
				AtomicInteger cnt = new AtomicInteger();
				Compressor compressor = new Compressor(session, cnt, frl.size(), handler);
				frl.parallelStream().forEach(fr -> {
					File file = fr.file;
					cnt.incrementAndGet();
					Compressor.UpdResultCallBack cb = txt -> fr.result = txt;
					Compressor.UpdSrcCallBack scb = src -> fr.file = src;
					compressor.compress(format, file, force, cb, scb);
				});
			}
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(), e);
			new HelpFormatter().printHelp(CMD.COMPRESSOR.toString(), options);
			throw e;
		}
		return 0;
	}

	private int prefs()
	{
		for (Map.Entry<Object, Object> entry : session.getUser().settings.getProperties().entrySet())
			System.out.format("%s=%s\n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
		return 0;
	}

	private int prefs(final String name)
	{
		if (!session.getUser().settings.hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$
		else
			System.out.format("%s=%s\n", name, session.getUser().settings.getProperty(name, "")); //$NON-NLS-1$ //$NON-NLS-2$
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
		for (Map.Entry<Object, Object> entry : session.curr_profile.settings.getProperties().entrySet())
			System.out.format("%s=%s\n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
		return 0;
	}

	private int settings(final String name)
	{
		if (!session.curr_profile.settings.hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$
		else
			System.out.format("%s=%s\n", name, session.curr_profile.settings.getProperty(name, "")); //$NON-NLS-1$ //$NON-NLS-2$
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
		if (Files.isRegularFile(candidate))
			session.curr_profile = Profile.load(session, candidate.toFile(), handler);
		else
			System.out.format(CLIMessages.getString("CLI_ERR_ProfileNotExist"), profile); //$NON-NLS-1$
		return 0;
	}

	private int cd(String dir)
	{
		if (dir.equals(File.separator))
			cwdir = rootdir;
		else
		{
			if (dir.startsWith("~")) //$NON-NLS-1$
				dir = dir.replace("~", rootdir.toString()); //$NON-NLS-1$
			Path candidate = cwdir.resolve(dir).normalize();
			if (rootdir.startsWith(candidate) && !rootdir.equals(candidate))
			{
				cwdir = rootdir;
				System.out.format(CLIMessages.getString("CLI_ERR_CantGoUpDir"), dir); //$NON-NLS-1$
			}
			else if (Files.isDirectory(candidate))
			{
				if (candidate.startsWith(rootdir))
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
		Files.walk(cwdir, 1).filter(p -> Files.isDirectory(p) && !p.equals(cwdir)).sorted(Path::compareTo).map(cwdir::relativize).forEachOrdered(p -> System.out.format("<DIR>\t%s\n", p)); //$NON-NLS-1$
		for (val row : ProfileNFO.list(session, cwdir.toFile()))
			System.out.format("<DAT>\t%s\n", row.getName()); //$NON-NLS-1$
		return 0;
	}

	public static void main(String[] args)
	{
		Options options = new Options();
		options.addOption(new Option("i", "interactive", false, "Interactive shell"));
		options.addOption(new Option("f", "file", true, "Input file"));
		try
		{
			new JRomManagerCLI(new DefaultParser().parse(options, args));
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(), e);
			new HelpFormatter().printHelp(JRomManagerCLI.class.getName(), options);
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
