package jrm.fx;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import jrm.fx.ui.MainFrame;

import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.Getter;

public class JRomManager
{
	private static @Getter MainFrame mainFrame;

	public static void main(final String[] args)
	{
		System.setProperty("file.encoding", "UTF-8");
		Sessions.setSingleMode(true);
		HTMLRenderer.Options.setHTML5(false);
		var multiuser = false;
		var noupdate = false;
		var debug = false;
		final var options = new Options();
		options.addOption(new Option("m", "multiuser", false, "Multi-user mode"));
		options.addOption(new Option("n", "noupdate", false, "Don't search for update"));
		options.addOption(new Option("d", "debug", false, "Activate debug mode"));
		try
		{
			CommandLine cmd = new DefaultParser().parse(options, args);
			if (cmd.hasOption('m'))
				multiuser = true;
			if (cmd.hasOption('n'))
				noupdate = true;
			if (cmd.hasOption('d'))
				debug = true;
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(), e);
			new HelpFormatter().printHelp("JRomManager", options);
			System.exit(1);
		}
		final var session = Sessions.getSession(multiuser, noupdate);
		Log.init(session.getUser().getSettings().getLogPath() + "/JRM.%g.log", debug, 1024 * 1024, 5);
		if (!debug)
			Log.setLevel(Level.parse(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.debug_level, Log.getLevel().toString())));
		if (JRomManager.lockInstance(session, FilenameUtils.removeExtension(JRomManager.class.getSimpleName()) + ".lock")) //$NON-NLS-1$
		{
			// Launch FX Application
			MainFrame.launch();
		}
	}

	/**
	 * Write lock file and keep it locked (rw) until program shutdown
	 * 
	 * @param lockFile
	 *            the file to lock
	 * @return true if successful, false otherwise
	 */
	private static boolean lockInstance(final Session session, final String lockFile)
	{
		try
		{
			final var fc = getLock(session, lockFile);
			final var fl = fc.tryLock();
			if (fl != null)
			{
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try
					{
						fl.release();
						fc.close();
					}
					catch (final Exception e)
					{
						Log.err("Unable to remove lock file: " + lockFile, e); //$NON-NLS-1$
					}

				}));
				return true;
			}
			else
				fc.close();
		}
		catch (final Exception e)
		{
			Log.err("Unable to create and/or lock file: " + lockFile, e); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * @param session
	 * @param lockFile
	 * @return
	 * @throws IOException
	 */
	private static FileChannel getLock(final Session session, final String lockFile) throws IOException
	{
		return FileChannel.open(session.getUser().getSettings().getWorkPath().resolve(lockFile), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.DELETE_ON_CLOSE);
	}
	
}
