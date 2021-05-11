package jrm.cli;

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

import jrm.aui.basic.ResultColUpdater;
import jrm.aui.basic.SrcDstResult;
import jrm.batch.Compressor;
import jrm.batch.Compressor.FileResult;
import jrm.batch.CompressorFormat;
import jrm.batch.DirUpdater;
import jrm.batch.TorrentChecker;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.misc.ProfileSettings;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.security.Sessions;
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
		rootdir = cwdir = session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize(); //$NON-NLS-1$
		HTMLRenderer.Options.setPlain(true);
		Log.init(session.getUser().getSettings().getLogPath() + "/JRM.%g.log", false, 1024 * 1024, 5); //$NON-NLS-1$
		handler = new Progress();

		if (cmd.hasOption('i'))
		{
			try (final var console = new BufferedReader(new InputStreamReader(System.in));)
			{
				do
				{
					if (session.curr_profile != null)
						System.out.format("jrm [%s]> ", session.curr_profile.nfo.file.getName()); //$NON-NLS-1$
					else
						System.out.print("jrm> "); //$NON-NLS-1$
					analyze(splitLine(console.readLine()));
				}
				while (true); // we break out with <control><C>
			}
		}
		else
		{
			Reader reader = cmd.hasOption('f')?new FileReader(cmd.getOptionValue('f')):new InputStreamReader(System.in);
			try (final var in = new BufferedReader(reader);)
			{
				String line;
				while (null != (line = in.readLine()))
				{
					if(line.startsWith("#")) //$NON-NLS-1$
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
		final var m = splitLinePattern.matcher(line);
		while (m.find())
		{
			final var im = envPattern.matcher(m.group(m.group(1) != null ? 1 : 2));
			final var sb = new StringBuilder();
			while (im.find())
				im.appendReplacement(sb, getEnv(im.group(im.group(1) != null ? 1 : 2)).map(Matcher::quoteReplacement).orElse("")); //$NON-NLS-1$
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
						System.getProperties().forEach((k,v)->System.out.println(k+"="+v)); //$NON-NLS-1$
						System.getenv().forEach((k,v)->System.out.println(k+"="+v)); //$NON-NLS-1$
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
					final var options = new Options().addOption("r", "recursive", false, "Recursive delete"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					CommandLine cmdline = new DefaultParser().parse(options, Arrays.copyOfRange(args, 1, args.length), true);
					for(String arg : cmdline.getArgList())
					{
						final var path = Paths.get(arg);
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
										try(final var stream = Files.walk(path))
										{
											stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
										}
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
					final var options = new Options().addOption("p", "parents", false, "create parents up to this directory"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					CommandLine cmdline = new DefaultParser().parse(options, Arrays.copyOfRange(args, 1, args.length), true);
					for(String arg : cmdline.getArgList())
					{
						final var path = Paths.get(arg);
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
						return prefs(jrm.misc.SettingsEnum.from(args[1]));
					if (args.length == 3)
						return prefs(jrm.misc.SettingsEnum.from(args[1]), args[2]);
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
						return settings(jrm.misc.SettingsEnum.from(args[1]));
					if (args.length == 3)
						return settings(jrm.misc.SettingsEnum.from(args[1]), args[2]);
					return error(CLIMessages.getString("CLI_ERR_WrongArgs")); //$NON-NLS-1$
				case SCAN:
					if (session.curr_profile == null)
						return error(CLIMessages.getString("CLI_ERR_NoProfileLoaded")); //$NON-NLS-1$
					session.curr_scan = new Scan(session.curr_profile, handler);
					return session.curr_scan.actions.stream().mapToInt(Collection::size).sum();
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
					final var fix = new Fix(session.curr_profile, session.curr_scan, handler);
					System.out.format(CLIMessages.getString("CLI_MSG_ActionRemaining"), fix.getActionsRemain()); //$NON-NLS-1$
					return fix.getActionsRemain();
				case DIRUPD8R:
					if (args.length == 1)
						return error(CLIMessages.getString("CLI_ERR_DIRUPD8R_SubCmdMissing")); //$NON-NLS-1$
					return dirupd8r(args[1], Arrays.copyOfRange(args, 2, args.length));
				case TRNTCHK:
					if (args.length == 1)
						return error(CLIMessages.getString("CLI_ERR_TRNTCHK_SubCmdMissing")); //$NON-NLS-1$
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
							System.out.append(": ").append(CLIMessages.getString("CLI_HELP_" + cmd.name())); //$NON-NLS-1$ //$NON-NLS-2$
							System.out.append("\n"); //$NON-NLS-1$
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
				System.out.append("srcdirs = [\n").append(Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_srcdirs, ""), '|')).map(s -> "\t" + Json.value(s).toString()).collect(Collectors.joining(",\n"))).append("\n];\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				
				break;
			}
			case LSSDR:
			{
				System.out.append("sdr = [\n").append(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr, "[]")).stream().map(sdr -> "\t" + sdr.toJSONObject().toString()).collect(Collectors.joining(",\n"))).append("\n];\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				break;
			}
			case CLEARSRC:
				prefs(jrm.misc.SettingsEnum.dat2dir_srcdirs, ""); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case CLEARSDR:
				prefs(jrm.misc.SettingsEnum.dat2dir_sdr, "[]"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case PRESETS:
			{
				if(args.length==0)
				{
					System.out.println("TZIP"); //$NON-NLS-1$
					System.out.println("DIR"); //$NON-NLS-1$
					return 0;
				}
				else if(args.length==2)
				{
					val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr, "[]")); //$NON-NLS-1$ //$NON-NLS-2$
					final var index = Integer.parseInt(args[0]);
					if(index < list.size())
					{
						try
						{
							switch (args[1])
							{
								case "TZIP": //$NON-NLS-1$
									ProfileSettings.TZIP(session, PathAbstractor.getAbsolutePath(session, list.get(index).src).toFile());
									break;
								case "DIR": //$NON-NLS-1$
									ProfileSettings.DIR(session, PathAbstractor.getAbsolutePath(session, list.get(index).src).toFile());
									break;
								default:
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
					val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr, "[]")); //$NON-NLS-1$ //$NON-NLS-2$
					if (args.length > 0)
					{
						final var index = Integer.parseInt(args[0]);
						if (index < list.size())
						{
							try
							{
								ProfileSettings settings = session.getUser().getSettings().loadProfileSettings(PathAbstractor.getAbsolutePath(session, list.get(index).src).toFile(), null);
								if (args.length == 3)
								{
									settings.setProperty(jrm.misc.SettingsEnum.from(args[1]), args[2]);
									session.getUser().getSettings().saveProfileSettings(PathAbstractor.getAbsolutePath(session, list.get(index).src).toFile(), settings);
								}
								else if (args.length == 2)
									System.out.format("%s%n", settings.getProperty(jrm.misc.SettingsEnum.from(args[1]), "")); //$NON-NLS-1$ //$NON-NLS-2$
								else
									for (Map.Entry<Object, Object> entry : settings.getProperties().entrySet())
										System.out.format("%s=%s%n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
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
				val list = Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_srcdirs, ""), '|')).collect(Collectors.toCollection(ArrayList::new)); //$NON-NLS-1$ //$NON-NLS-2$
				list.add(args[0]);
				prefs(jrm.misc.SettingsEnum.dat2dir_srcdirs, list.stream().collect(Collectors.joining("|"))); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			case ADDSDR:
			{
				val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr, "[]")); //$NON-NLS-1$ //$NON-NLS-2$
				list.add(new SrcDstResult(args[0],args[1]));
				prefs(jrm.misc.SettingsEnum.dat2dir_sdr, SrcDstResult.toJSON(list)); //$NON-NLS-1$
				break;
			}
			case START:
			{
				List<SrcDstResult> sdrl = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr, "[]")); //$NON-NLS-1$ //$NON-NLS-2$
				List<File> srcdirs = Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_srcdirs, ""), '|')).map(File::new).collect(Collectors.toCollection(ArrayList::new)); //$NON-NLS-1$ //$NON-NLS-2$
				final var results = new String[sdrl.size()];
				final var resulthandler = new ResultColUpdater()
				{
					@Override
					public void updateResult(int row, String result)
					{
						results[row] = result;
					}

					@Override
					public void clearResults()
					{
						for (var i = 0; i < results.length; i++)
							results[i] = ""; //$NON-NLS-1$
					}
				};
				final var options = new Options().addOption("d", "dryrun", false, "Dry run"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				CommandLine cmdline = new DefaultParser().parse(options, args);
				new DirUpdater(session, sdrl, handler, srcdirs, resulthandler, cmdline.hasOption('d'));
				for (var i = 0; i < results.length; i++)
					System.out.println(i + " = " + results[i]); //$NON-NLS-1$
				break;
			}
			case HELP:
			{
				for (val ducmd : CMD_DIRUPD8R.values())
				{
					if (ducmd != CMD_DIRUPD8R.EMPTY && ducmd != CMD_DIRUPD8R.UNKNOWN)
					{
						System.out.append(ducmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$
						System.out.append(": ").append(CLIMessages.getString("CLI_HELP_DIRUPD8R_" + ducmd.name())); //$NON-NLS-1$ //$NON-NLS-2$
						System.out.append("\n"); //$NON-NLS-1$
					}
				}
				break;
			}
			case EMPTY:
				break;
			case UNKNOWN:
				return error(() -> CLIMessages.getString("CLI_ERR_UnknownCommand") + cmd + " " + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return 0;
	}

	private int trntchk(String cmd, String... args) throws IOException, ParseException
	{
		switch (CMD_TRNTCHK.of(cmd))
		{
			case LSSDR:
			{
				System.out.append("sdr = [\n").append(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.trntchk_sdr, "[]")).stream().map(sdr -> "\t" + sdr.toJSONObject().toString()).collect(Collectors.joining(",\n"))).append("\n];\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				break;
			}
			case CLEARSDR:
				prefs(jrm.misc.SettingsEnum.trntchk_sdr, "[]"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case ADDSDR:
			{
				if(args.length==2)
				{
					val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.trntchk_sdr, "[]")); //$NON-NLS-1$ //$NON-NLS-2$
					list.add(new SrcDstResult(args[0],args[1]));
					prefs(jrm.misc.SettingsEnum.trntchk_sdr, SrcDstResult.toJSON(list)); //$NON-NLS-1$
				}
				else
					return error(CLIMessages.getString("CLI_ERR_WrongArgs"));
				break;
			}
			case START:
			{
				List<SrcDstResult> sdrl = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.trntchk_sdr, "[]")); //$NON-NLS-1$ //$NON-NLS-2$
				final var results = new String[sdrl.size()];
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
						for (var i = 0; i < results.length; i++)
							results[i] = ""; //$NON-NLS-1$
					}
				};
				final var options = new Options()
						.addOption("m", "checkmode", true, "Check mode") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.addOption("u", "removeunknown", false, "Remove unknown files") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.addOption("w", "removewrongsized", false, "Remove wrong sized files") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						.addOption("a", "detectarchives", false, "Detect archived folders"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
						System.out.append(": ").append(CLIMessages.getString("CLI_HELP_TRNTCHK_" + ducmd.name())); //$NON-NLS-1$ //$NON-NLS-2$
						System.out.append("\n"); //$NON-NLS-1$
					}
				}
				break;
			}
			case EMPTY:
				break;
			case UNKNOWN:
				return error(() -> CLIMessages.getString("CLI_ERR_UnknownCommand") + cmd + " " + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return 0;
	}

	private int compressor(String... args) throws IOException, ParseException
	{
		final var options = new Options()
				.addRequiredOption("c", "compressor", true, "Compression format") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.addOption("f", "force", false, "Force recompression"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		try
		{
			CommandLine cmdline = new DefaultParser().parse(options, args, true);
			CompressorFormat format = cmdline.hasOption('c')?CompressorFormat.valueOf(cmdline.getOptionValue('c')):CompressorFormat.TZIP;
			boolean force = cmdline.hasOption('f');
			for(final var arg : cmdline.getArgList())
			{
				final var path = Paths.get(arg);
				final List<FileResult> frl;
				if(Files.isDirectory(path))
				{
					try(final var stream = Files.walk(path))
					{
						frl = stream.filter(p -> Files.isRegularFile(p) && FilenameUtils.isExtension(p.getFileName().toString(), Compressor.extensions)).map(FileResult::new).collect(Collectors.toList());
					}
				}
				else
					frl = Arrays.asList(new FileResult(path));
				final var cnt = new AtomicInteger();
				final var compressor = new Compressor(session, cnt, frl.size(), handler);
				frl.parallelStream().forEach(fr -> {
					Path file = fr.file;
					cnt.incrementAndGet();
					Compressor.UpdResultCallBack cb = txt -> fr.result = txt;
					Compressor.UpdSrcCallBack scb = src -> fr.file = src.toPath();
					compressor.compress(format, file.toFile(), force, cb, scb);
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
		for (Map.Entry<Object, Object> entry : session.getUser().getSettings().getProperties().entrySet())
			System.out.format("%s=%s%n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
		return 0;
	}

	private int prefs(final Enum<?> name)
	{
		if (!session.getUser().getSettings().hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$
		else
			System.out.format("%s=%s%n", name, session.getUser().getSettings().getProperty(name, "")); //$NON-NLS-1$ //$NON-NLS-2$
		return 0;
	}

	private int prefs(final Enum<?> name, final String value)
	{
		session.getUser().getSettings().setProperty(name, value);
		session.getUser().getSettings().saveSettings();
		return 0;
	}

	private int settings()
	{
		for (Map.Entry<Object, Object> entry : session.curr_profile.settings.getProperties().entrySet())
			System.out.format("%s=%s%n", entry.getKey(), entry.getValue()); //$NON-NLS-1$
		return 0;
	}

	private int settings(final Enum<?> name)
	{
		if (!session.curr_profile.settings.hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$
		else
			System.out.format("%s=%s%n", name, session.curr_profile.settings.getProperty(name, "")); //$NON-NLS-1$ //$NON-NLS-2$
		return 0;
	}

	private int settings(final Enum<?> name, final String value)
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
		try(final var stream = Files.walk(cwdir, 1))
		{
			stream.filter(p -> Files.isDirectory(p) && !p.equals(cwdir)).sorted(Path::compareTo).map(cwdir::relativize).forEachOrdered(p -> System.out.format("<DIR>\t%s%n", p)); //$NON-NLS-1$
		}
		for (val row : ProfileNFO.list(session, cwdir.toFile()))
			System.out.format("<DAT>\t%s\n", row.getName()); //$NON-NLS-1$
		return 0;
	}

	public static void main(String[] args)
	{
		final var options = new Options();
		options.addOption(new Option("i", "interactive", false, "Interactive shell")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		options.addOption(new Option("f", "file", true, "Input file")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
