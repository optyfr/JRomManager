package jrm.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.eclipsesource.json.Json;

import jrm.aui.basic.ResultColUpdater;
import jrm.aui.basic.SrcDstResult;
import jrm.aui.status.PlainTextRenderer;
import jrm.aui.status.StatusRendererFactory;
import jrm.batch.Compressor;
import jrm.batch.Compressor.FileResult;
import jrm.batch.CompressorFormat;
import jrm.batch.DirUpdater;
import jrm.batch.TorrentChecker;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.BreakException;
import jrm.misc.EnumWithDefault;
import jrm.misc.Log;
import jrm.misc.ProfileSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.profile.scan.ScanException;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.val;

public class JRomManagerCLI
{
	private static final String CLI_ERR_UNKNOWN_COMMAND = "CLI_ERR_UnknownCommand";
	private static final String CLI_ERR_WRONG_ARGS = "CLI_ERR_WrongArgs";
	Session session;
	Path cwdir = null;
	Path rootdir = null;

	Progress handler = null;

	@Parameters(separators = " =")
	private static class Args
	{
		@Parameter(names = { "--help", "-h" }, help = true)
		private boolean help = false;
	
		@Parameter(names = { "--interactive", "-i" }, description = "Interactive sheel")
		private boolean interactive = false;
	
		@Parameter(names = { "--file", "-f" }, description = "Input file", arity = 1)
		private String file = null;
	}

	
	@SuppressWarnings("exports")
	public JRomManagerCLI(Args cmd) throws IOException
	{
		session = Sessions.getSession(true, false);
		rootdir = cwdir = session.getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize(); //$NON-NLS-1$
		StatusRendererFactory.Factory.setInstance(new PlainTextRenderer());
		Log.init(session.getUser().getSettings().getLogPath() + "/JRM.%g.log", false, 1024 * 1024, 5); //$NON-NLS-1$
		handler = new Progress();

		if (cmd.interactive)
		{
			interactive();
		}
		else
		{
			stream(cmd);
		}
	}

	/**
	 * @param cmd
	 * @throws FileNotFoundException
	 */
	private void stream(Args cmd) throws FileNotFoundException
	{
		Reader reader = cmd.file!=null?new FileReader(cmd.file):new InputStreamReader(System.in);
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
			Log.err(e.getMessage());
		}
	}

	/**
	 * @throws IOException
	 */
	private void interactive() throws IOException
	{
		try (final var console = new BufferedReader(new InputStreamReader(System.in));)
		{
			do
			{
				if (session.getCurrProfile() != null)
					System.out.format("jrm [%s]> ", session.getCurrProfile().getNfo().getFile().getName()); //$NON-NLS-1$ //NOSONAR
				else
					System.out.print("jrm> "); //$NON-NLS-1$ //NOSONAR
				final var line = console.readLine();
				if (line == null)
					break;
				analyze(splitLine(line));
			}
			while (true); // we break out with <control><C>
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
					return set(args);
				case CD:
					return cd(args);
				case RM:
					return rm(args);
				case MD:
					return md(args);
				case PREFS:
					return prefs(args);
				case LOAD:
					return load(args);
				case SETTINGS:
					return settings(args);
				case SCAN:
					return scan();
				case SCANRESULT:
					return scanResult();
				case FIX:
					return fix();
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
						return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); //$NON-NLS-1$
					return compressor(Arrays.copyOfRange(args, 1, args.length));
				case HELP:
					return help();
				case EXIT:
					return exit(0);
				case EMPTY:
					return 0;
				case UNKNOWN:
					return error(() -> CLIMessages.getString(CLI_ERR_UNKNOWN_COMMAND) + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
		catch (ScanException|ParameterException e)
		{
			System.out.println(e.getMessage());	//NOSONAR
			Log.err(e.getMessage(), e);
		}
		return -1;
	}

	/**
	 * @return
	 */
	private int help()
	{
		for (val cmd : CMD.values())
		{
			if (cmd != CMD.EMPTY && cmd != CMD.UNKNOWN)
			{
				System.out.append(cmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$ 	//NOSONAR
				System.out.append(": ").append(CLIMessages.getString("CLI_HELP_" + cmd.name())); //$NON-NLS-1$ //$NON-NLS-2$	//NOSONAR
				System.out.append("\n"); //$NON-NLS-1$ 	//NOSONAR
			}
		}
		return 0;
	}

	/**
	 * @param args
	 * @return
	 */
	private int cd(String... args)
	{
		if (args.length == 1)
			return pwd();
		if (args.length == 2)
			return cd(args[1]);
		return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); //$NON-NLS-1$
	}

	/**
	 * @param args
	 * @return
	 */
	private int prefs(String... args)
	{
		if (args.length == 1)
			return prefs();
		if (args.length == 2)
			return prefs(jrm.misc.SettingsEnum.from(args[1]));
		if (args.length == 3)
			return prefs(jrm.misc.SettingsEnum.from(args[1]), args[2]);
		return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); //$NON-NLS-1$
	}

	/**
	 * @param args
	 * @return
	 */
	private int load(String... args)
	{
		if (args.length == 2)
			return load(args[1]);
		return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); //$NON-NLS-1$
	}

	/**
	 * @param args
	 * @return
	 */
	private int settings(String... args)
	{
		if (session.getCurrProfile() == null)
			return error(CLIMessages.getString("CLI_ERR_NoProfileLoaded")); //$NON-NLS-1$
		if (args.length == 1)
			return settings();
		if (args.length == 2)
			return settings(jrm.misc.SettingsEnum.from(args[1]));
		if (args.length == 3)
			return settings(jrm.misc.SettingsEnum.from(args[1]), args[2]);
		return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); //$NON-NLS-1$
	}

	@Parameters(separators = " =")
	private static class RmArgs
	{
		@Parameter(names = { "--recursive", "-r" }, description = "Recursive delete")
		private boolean recurisve;

		@Parameter(description = "Files")
		private List<String> files = new ArrayList<>();
	}
	
	/**
	 * @param args
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	private int rm(String... args) throws ParameterException, IOException
	{
		final var jArgs = new RmArgs();
		JCommander.newBuilder().addObject(jArgs).build().parse(Arrays.copyOfRange(args, 1, args.length));
		for(String arg : jArgs.files)
			recursiveDelete(Paths.get(arg), jArgs.recurisve);
		return 0;
	}

	/**
	 * @return
	 */
	private int fix()
	{
		if (session.getCurrScan() == null)
			return error(CLIMessages.getString("CLI_ERR_ShouldScanFirst")); //$NON-NLS-1$
		if (session.getCurrProfile().hasPropsChanged())
			return error(CLIMessages.getString("CLI_ERR_PropsChanged")); //$NON-NLS-1$
		if (session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() == 0)
			return error(CLIMessages.getString("CLI_ERR_NothingToFix")); //$NON-NLS-1$
		final var fix = new Fix(session.getCurrProfile(), session.getCurrScan(), handler);
		System.out.format(CLIMessages.getString("CLI_MSG_ActionRemaining"), fix.getActionsRemain()); //$NON-NLS-1$	//NOSONAR
		return fix.getActionsRemain();
	}

	/**
	 * @return
	 */
	private int scanResult()
	{
		if (session.getCurrScan() == null)
			return error(CLIMessages.getString("CLI_ERR_ShouldScanFirst")); //$NON-NLS-1$
		if (session.getCurrProfile().hasPropsChanged())
			return error(CLIMessages.getString("CLI_ERR_PropsChanged")); //$NON-NLS-1$
		if (session.getReport() == null)
			return error(CLIMessages.getString("CLI_ERR_NoReport")); //$NON-NLS-1$
		System.out.println(session.getReport().getStats().getStatus());	//NOSONAR
		return 0;
	}

	/**
	 * @return
	 * @throws BreakException
	 * @throws ScanException
	 */
	private int scan() throws BreakException, ScanException
	{
		if (session.getCurrProfile() == null)
			return error(CLIMessages.getString("CLI_ERR_NoProfileLoaded")); //$NON-NLS-1$
		session.setCurrScan(new Scan(session.getCurrProfile(), handler));
		return session.getCurrScan().actions.stream().mapToInt(Collection::size).sum();
	}

	@Parameters(separators = " =")
	private static class MdArgs
	{
		@Parameter(names = { "--parents", "-p" }, description = "Create parents up to this directory")
		private boolean parents;

		@Parameter(description = "Files")
		private List<String> files = new ArrayList<>();
	}
	
	/**
	 * @param args
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	private int md(String... args) throws ParameterException, IOException
	{
		final var jArgs = new MdArgs();
		JCommander.newBuilder().addObject(jArgs).build().parse(Arrays.copyOfRange(args, 1, args.length));
		for(String arg : jArgs.files)
		{
			final var path = Paths.get(arg);
			if(!Files.exists(path))
			{
				if(jArgs.parents)
					Files.createDirectories(path);
				else
					Files.createDirectory(path);
			}
		}
		return 0;
	}

	/**
	 * @param args
	 * @return
	 */
	private int set(String... args)
	{
		if (args.length == 1)
		{
			System.getProperties().forEach((k, v) -> System.out.println(k + "=" + v)); //$NON-NLS-1$ //NOSONAR
			System.getenv().forEach((k, v) -> System.out.println(k + "=" + v)); //$NON-NLS-1$ //NOSONAR
			return 0;
		}
		if (args.length == 2)
		{
			getEnv(args[1]).ifPresent(System.out::println); // NOSONAR
			return 0;
		}
		if (args.length == 3)
		{
			if (args[2].isEmpty())
				System.clearProperty(args[1]);
			else
				System.setProperty(args[1], args[2]);
			return 0;
		}
		return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); // $NON-NLS-1$
	}

	/**
	 * @param cmdline
	 * @param path
	 * @throws IOException
	 */
	private void recursiveDelete(final Path path, final boolean recurse) throws IOException
	{
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
					if(recurse)	// recursively delete from bottom to top
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

	private int dirupd8r(String cmd, String... args) throws ParameterException
	{
		switch (CMD_DIRUPD8R.of(cmd))
		{
			case LSSRC:
				System.out.append("srcdirs = [\n").append(Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_srcdirs), '|')).map(s -> "\t" + Json.value(s).toString()).collect(Collectors.joining(",\n"))).append("\n];\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$	//NOSONAR
				return 0;
			case LSSDR:
				System.out.append("sdr = [\n").append(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr)).stream().map(sdr -> "\t" + sdr.toJSONObject().toString()).collect(Collectors.joining(",\n"))).append("\n];\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$	//NOSONAR
				return 0;
			case CLEARSRC:
				prefs(jrm.misc.SettingsEnum.dat2dir_srcdirs, ""); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case CLEARSDR:
				prefs(jrm.misc.SettingsEnum.dat2dir_sdr, "[]"); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case PRESETS:
				return dirupd8rPresets(args);
			case SETTINGS:
				return dirupd8rSettings(args);
			case ADDSRC:
			{
				val list = Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_srcdirs), '|')).collect(Collectors.toCollection(ArrayList::new)); //$NON-NLS-1$ //$NON-NLS-2$
				list.add(args[0]);
				prefs(jrm.misc.SettingsEnum.dat2dir_srcdirs, list.stream().collect(Collectors.joining("|"))); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			case ADDSDR:
			{
				val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr)); //$NON-NLS-1$ //$NON-NLS-2$
				list.add(new SrcDstResult(args[0],args[1]));
				prefs(jrm.misc.SettingsEnum.dat2dir_sdr, SrcDstResult.toJSON(list)); //$NON-NLS-1$
				break;
			}
			case START:
				return dirupd8rStart(args);
			case HELP:
				return dirupd8rHelp();
			case EMPTY:
				break;
			case UNKNOWN:
				return error(() -> CLIMessages.getString(CLI_ERR_UNKNOWN_COMMAND) + cmd + " " + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return 0;
	}

	/**
	 * @return
	 */
	private int dirupd8rHelp()
	{
		for (val ducmd : CMD_DIRUPD8R.values())
		{
			if (ducmd != CMD_DIRUPD8R.EMPTY && ducmd != CMD_DIRUPD8R.UNKNOWN)
			{
				System.out.append(ducmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$	//NOSONAR
				System.out.append(": ").append(CLIMessages.getString("CLI_HELP_DIRUPD8R_" + ducmd.name())); //$NON-NLS-1$ //$NON-NLS-2$	//NOSONAR
				System.out.append("\n"); //$NON-NLS-1$	//NOSONAR
			}
		}
		return 0;
	}

	@Parameters(separators = " =")
	private static class DirUpdaterArgs
	{
		@Parameter(names = { "--dryrun", "-d" }, description = "Dry run")
		private boolean dryrun;
	}

	/**
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private int dirupd8rStart(String... args) throws ParameterException
	{
		List<SrcDstResult> sdrl = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr)); //$NON-NLS-1$ //$NON-NLS-2$
		List<File> srcdirs = Stream.of(StringUtils.split(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_srcdirs), '|')).map(File::new).collect(Collectors.toCollection(ArrayList::new)); //$NON-NLS-1$ //$NON-NLS-2$
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
		final var jArgs = new DirUpdaterArgs();
		JCommander.newBuilder().addObject(jArgs).build().parse(args);
		new DirUpdater(session, sdrl, handler, srcdirs, resulthandler, jArgs.dryrun);
		for (var i = 0; i < results.length; i++)
			System.out.println(i + " = " + results[i]); //$NON-NLS-1$	//NOSONAR
		return 0;
	}

	/**
	 * @param args
	 * @return
	 * @throws NumberFormatException
	 * @throws SecurityException
	 */
	private int dirupd8rSettings(String... args) throws NumberFormatException, SecurityException
	{
		if (args.length > 0)
		{
			val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr)); //$NON-NLS-1$ //$NON-NLS-2$
			final var index = Integer.parseInt(args[0]);
			if (index < list.size())
			{
				ProfileSettings settings = session.getUser().getSettings().loadProfileSettings(PathAbstractor.getAbsolutePath(session, list.get(index).getSrc()).toFile(), null);
				if (args.length == 3)
				{
					settings.setProperty(jrm.misc.SettingsEnum.from(args[1]), args[2]);
					session.getUser().getSettings().saveProfileSettings(PathAbstractor.getAbsolutePath(session, list.get(index).getSrc()).toFile(), settings);
				}
				else if (args.length == 2)
					System.out.format("%s%n", settings.getProperty(jrm.misc.SettingsEnum.from(args[1]))); //$NON-NLS-1$ //$NON-NLS-2$	//NOSONAR
				else
					for (Map.Entry<Object, Object> entry : settings.getProperties().entrySet())
						System.out.format("%s=%s%n", entry.getKey(), entry.getValue()); //$NON-NLS-1$	//NOSONAR
			}
			return 0;
		}
		else
			return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); //$NON-NLS-1$
	}

	/**
	 * @param args
	 * @return
	 * @throws NumberFormatException
	 * @throws SecurityException
	 */
	private int dirupd8rPresets(String... args) throws NumberFormatException, SecurityException
	{
		if(args.length==0)
		{
			System.out.println("TZIP"); //$NON-NLS-1$	//NOSONAR
			System.out.println("DIR"); //$NON-NLS-1$	//NOSONAR
			return 0;
		}
		else if(args.length==2)
		{
			val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dat2dir_sdr)); //$NON-NLS-1$ //$NON-NLS-2$
			final var index = Integer.parseInt(args[0]);
			if(index < list.size())
			{
				switch (args[1])
				{
					case "TZIP": //$NON-NLS-1$
						ProfileSettings.TZIP(session, PathAbstractor.getAbsolutePath(session, list.get(index).getSrc()).toFile());
						break;
					case "DIR": //$NON-NLS-1$
						ProfileSettings.DIR(session, PathAbstractor.getAbsolutePath(session, list.get(index).getSrc()).toFile());
						break;
					default:
						break;
				}
			}
			return 0;
		}
		else
			return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS)); //$NON-NLS-1$
	}

	private int trntchk(String cmd, String... args) throws IOException, ParameterException
	{
		switch (CMD_TRNTCHK.of(cmd))
		{
			case LSSDR:
				System.out.append("sdr = [\n").append(SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.trntchk_sdr)).stream().map(sdr -> "\t" + sdr.toJSONObject().toString()).collect(Collectors.joining(",\n"))).append("\n];\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$	//NOSONAR
				break;
			case CLEARSDR:
				prefs(jrm.misc.SettingsEnum.trntchk_sdr); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case ADDSDR:
				if(args.length==2)
				{
					val list = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.trntchk_sdr)); //$NON-NLS-1$ //$NON-NLS-2$
					list.add(new SrcDstResult(args[0],args[1]));
					prefs(jrm.misc.SettingsEnum.trntchk_sdr, SrcDstResult.toJSON(list)); //$NON-NLS-1$
				}
				else
					return error(CLIMessages.getString(CLI_ERR_WRONG_ARGS));
				break;
			case START:
				return trntchkStart(args);
			case HELP:
				return trntchkHelp();
			case EMPTY:
				break;
			case UNKNOWN:
				return error(() -> CLIMessages.getString(CLI_ERR_UNKNOWN_COMMAND) + cmd + " " + Stream.of(args).map(s -> s.contains(" ") ? ('"' + s + '"') : s).collect(Collectors.joining(" "))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return 0;
	}

	/**
	 * @return
	 */
	private int trntchkHelp()
	{
		for (val ducmd : CMD_TRNTCHK.values())
		{
			if (ducmd != CMD_TRNTCHK.EMPTY && ducmd != CMD_TRNTCHK.UNKNOWN)
			{
				System.out.append(ducmd.allStrings().collect(Collectors.joining(", "))); //$NON-NLS-1$	//NOSONAR
				System.out.append(": ").append(CLIMessages.getString("CLI_HELP_TRNTCHK_" + ducmd.name())); //$NON-NLS-1$ //$NON-NLS-2$	//NOSONAR
				System.out.append("\n"); //$NON-NLS-1$	//NOSONAR
			}
		}
		return 0;
	}

	@Parameters(separators = " =")
	private static class TrntchkArgs
	{
		@Parameter(names = { "--checkmode", "-m" }, arity = 1, description = "Check mode")
		private String checkmode = null;
	
		@Parameter(names = { "--removeunknown", "-u" }, description = "Remove unknown files")
		private boolean removeunknown = false;
		
		@Parameter(names = { "--removewrongsized", "-w" }, description = "Remove wrong sized files")
		private boolean removewrongsized = false;
		
		@Parameter(names = { "--detectarchives", "-a" }, description = "Detect archived folders")
		private boolean detectarchives = false;
	}

	/**
	 * @param args
	 * @return
	 * @throws ParseException
	 */
	private int trntchkStart(String... args) throws ParameterException
	{
		List<SrcDstResult> sdrl = SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.trntchk_sdr)); //$NON-NLS-1$ //$NON-NLS-2$
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
		final var jArgs = new TrntchkArgs();
		JCommander.newBuilder().addObject(jArgs).build().parse(args);
		TrntChkMode mode = jArgs.checkmode!=null?TrntChkMode.valueOf(jArgs.checkmode):TrntChkMode.FILESIZE;
		final var opts = EnumSet.noneOf(TorrentChecker.Options.class);
		if(jArgs.removeunknown) opts.add(TorrentChecker.Options.REMOVEUNKNOWNFILES);
		if(jArgs.removewrongsized) opts.add(TorrentChecker.Options.REMOVEWRONGSIZEDFILES);
		if(jArgs.detectarchives) opts.add(TorrentChecker.Options.DETECTARCHIVEDFOLDERS);
		new TorrentChecker(session, handler, sdrl, mode, resulthandler, opts);
		return 0;
	}

	@Parameters(separators = " =")
	private static class CompressorArgs
	{
		@Parameter(names = { "--compressor", "-c" }, arity = 1, required = true, description = "Compression format")
		private String compressor;

		@Parameter(names = { "--force", "-f" }, description = "Force compression")
		private boolean force;

		@Parameter(description = "Files")
		private List<String> files = new ArrayList<>();
	}

	private int compressor(String... args) throws IOException, ParameterException
	{
		final var jArgs = new CompressorArgs();
		final var cmd = JCommander.newBuilder().addObject(jArgs).build();
		try
		{
			cmd.parse(args);
			CompressorFormat format = jArgs.compressor!=null?CompressorFormat.valueOf(jArgs.compressor):CompressorFormat.TZIP;
			for(final var arg : jArgs.files)
			{
				final var path = Paths.get(arg);
				final List<FileResult> frl;
				if(Files.isDirectory(path))
				{
					try(final var stream = Files.walk(path))
					{
						frl = stream.filter(p -> Files.isRegularFile(p) && FilenameUtils.isExtension(p.getFileName().toString(), Compressor.getExtensions())).map(FileResult::new).toList();
					}
				}
				else
					frl = Arrays.asList(new FileResult(path));
				final var cnt = new AtomicInteger();
				final var compressor = new Compressor(session, cnt, frl.size(), handler);
				frl.parallelStream().forEach(fr -> {
					Path file = fr.getFile();
					cnt.incrementAndGet();
					Compressor.UpdResultCallBack cb = fr::setResult;
					Compressor.UpdSrcCallBack scb = src -> fr.setFile(src.toPath());
					compressor.compress(format, file.toFile(), jArgs.force, cb, scb);
				});
			}
		}
		catch (ParameterException e)
		{
			Log.err(e.getMessage(), e);
			cmd.usage();
			throw e;
		}
		return 0;
	}

	private int prefs()
	{
		for (final var e : SettingsEnum.values())
			System.out.format("%s=%s%n", e.toString(), session.getUser().getSettings().getProperty(e)); //$NON-NLS-1$	//NOSONAR
		return 0;
	}

	private int prefs(final Enum<?> name)
	{
		if (!session.getUser().getSettings().hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$	//NOSONAR
		else if(name instanceof EnumWithDefault n)
			System.out.format("%s=%s%n", name, session.getUser().getSettings().getProperty(n)); //$NON-NLS-1$ //$NON-NLS-2$	//NOSONAR
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
		for (final var e : ProfileSettingsEnum.values())
			System.out.format("%s=%s%n", e.toString(), session.getCurrProfile().getSettings().getProperty(e)); //$NON-NLS-1$	//NOSONAR
		return 0;
	}

	private int settings(final Enum<?> name)
	{
		if (!session.getCurrProfile().getSettings().hasProperty(name))
			System.out.format(CLIMessages.getString("CLI_MSG_PropIsNotSet"), name); //$NON-NLS-1$	//NOSONAR
		else if(name instanceof EnumWithDefault n)
			System.out.format("%s=%s%n", name, session.getCurrProfile().getSettings().getProperty(n)); //$NON-NLS-1$ //$NON-NLS-2$	//NOSONAR
		return 0;
	}

	private int settings(final Enum<?> name, final String value)
	{
		session.getCurrProfile().getSettings().setProperty(name, value);
		session.getCurrProfile().saveSettings();
		return 0;
	}

	private int exit(int status)
	{
		System.exit(status);
		return status;
	}

	private int error(String msg)
	{
		System.out.println(msg);	//NOSONAR
		return -1;
	}

	private int error(Supplier<String> supplier)
	{
		System.out.println(supplier.get());	//NOSONAR
		return -1;
	}

	private int load(String profile)
	{
		Path candidate = cwdir.resolve(profile);
		if (Files.isRegularFile(candidate))
			session.setCurrProfile(Profile.load(session, candidate.toFile(), handler));
		else
			System.out.format(CLIMessages.getString("CLI_ERR_ProfileNotExist"), profile); //$NON-NLS-1$	//NOSONAR
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
				System.out.format(CLIMessages.getString("CLI_ERR_CantGoUpDir"), dir); //$NON-NLS-1$	//NOSONAR
			}
			else if (Files.isDirectory(candidate))
			{
				if (candidate.startsWith(rootdir))
					cwdir = candidate;
				else
					System.out.format(CLIMessages.getString("CLI_ERR_CantChangeDir"), dir); //$NON-NLS-1$	//NOSONAR
			}
			else
				System.out.format(CLIMessages.getString("CLI_ERR_UnknownDir"), dir); //$NON-NLS-1$	//NOSONAR
		}
		return 0;
	}

	private int pwd()
	{
		System.out.println("~/" + rootdir.relativize(cwdir)); //$NON-NLS-1$	//NOSONAR
		return 0;
	}

	private int list() throws IOException
	{
		try(final var stream = Files.walk(cwdir, 1))
		{
			stream.filter(p -> Files.isDirectory(p) && !p.equals(cwdir)).sorted(Path::compareTo).map(cwdir::relativize).forEachOrdered(p -> System.out.format("<DIR>\t%s%n", p)); //NOSONAR //$NON-NLS-1$
		}
		for (val row : ProfileNFO.list(session, cwdir.toFile()))
			System.out.format("<DAT>\t%s\n", row.getName()); //$NON-NLS-1$	//NOSONAR
		return 0;
	}

	public static void main(String[] args)
	{
		final var jArgs = new Args();
		final var cmd = JCommander.newBuilder().addObject(jArgs).build();
		try
		{
			cmd.parse(args);
			new JRomManagerCLI(jArgs);
		}
		catch (ParameterException e)
		{
			Log.err(e.getMessage(), e);
			cmd.usage();
			System.exit(1);
		}
		catch (IOException e)
		{
			Log.err(e.getMessage());
		}
	}

}
