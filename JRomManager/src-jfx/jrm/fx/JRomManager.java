package jrm.fx;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import jrm.aui.status.NeutralRenderer;
import jrm.aui.status.StatusRendererFactory;
import jrm.fx.ui.MainFrame;

import jrm.misc.Log;
import jrm.security.Session;
import jrm.security.Sessions;
import lombok.Getter;

public class JRomManager
{
	private static @Getter MainFrame mainFrame;

	@Parameters(separators = " =")
	private static class Args
	{
		@Parameter(names = {"--multiuser", "-m"}, description = "Multi-user mode")
		private boolean multiuser = false;
		@Parameter(names = {"--noupdate", "-n"}, description = "Don't search for update")
		private boolean noupdate = false;
		@Parameter(names = {"--debug", "-d"}, description = "Activate debug mode")
		private boolean debug = false;
	}
	
	public static void main(final String[] args)
	{
		System.setProperty("file.encoding", "UTF-8");
		Sessions.setSingleMode(true);
		StatusRendererFactory.Factory.setInstance(new NeutralRenderer());
		final var jArgs = new Args();
		final var cmd = JCommander.newBuilder().addObject(jArgs).build();
		try
		{
			cmd.parse(args);
		}
		catch (ParameterException e)
		{
			Log.err(e.getMessage(), e);
			cmd.usage();
			System.exit(1);
		}
		final var session = Sessions.getSession(jArgs.multiuser, jArgs.noupdate);
		Log.init(session.getUser().getSettings().getLogPath() + "/JRM.%g.log", jArgs.debug, 1024 * 1024, 5);
		if (!jArgs.debug)
			Log.setLevel(Level.parse(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.debug_level)));
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
