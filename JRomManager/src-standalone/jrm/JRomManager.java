/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import jrm.misc.Log;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.ui.MainFrame;
import jupdater.JUpdater;

/**
 * Main class
 * @author optyfr
 * @version %I%, %G%
 * @since 1.0
 */
public final class JRomManager
{
	public static void main(final String[] args)
	{
		System.setProperty("file.encoding", "UTF-8");
		Sessions.single_mode = true;
		boolean multiuser = false, noupdate = false, debug = false;
		Options options = new Options();
		options.addOption(new Option("m", "multiuser", false, "Multi-user mode"));
		options.addOption(new Option("n", "noupdate", false, "Don't search for update"));
		options.addOption(new Option("d", "debug", false, "Activate debug mode"));
		try
		{
			CommandLine cmd = new DefaultParser().parse(options, args);
			if(cmd.hasOption('m'))
				multiuser = true;
			if(cmd.hasOption('n'))
				noupdate = true;
			if(cmd.hasOption('d'))
				debug = true;
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(),e);
			new HelpFormatter().printHelp("JRomManager", options);
			System.exit(1);
		}
		Session session  = Sessions.getSession(multiuser,noupdate);
		Log.init(session.getUser().getSettings().getLogPath() + "/JRM.%g.log", debug, 1024 * 1024, 5);
		if(!debug) Log.setLevel(Level.parse(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.debug_level, Log.getLevel().toString())));
		if (JRomManager.lockInstance(session, FilenameUtils.removeExtension(JRomManager.class.getSimpleName()) + ".lock")) //$NON-NLS-1$
		{
			if(!session.noupdate)
			{
				// check for update
				JUpdater updater = new JUpdater("optyfr","JRomManager"); //$NON-NLS-1$ //$NON-NLS-2$
				if(updater.updateAvailable())
					updater.showMessage();	// Will show changes since your version and a link to updater
			}
			// Open main window
			new MainFrame(session).setVisible(true);
		}
	}

	/**
	 * Write lock file and keep it locked (rw) until program shutdown
	 * @param lockFile the file to lock
	 * @return true if successful, false otherwise
	 */
	private static boolean lockInstance(final Session session, final String lockFile)
	{
		try
		{
			final File file = new File(session.getUser().getSettings().getWorkPath().toFile(),lockFile);
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw"); //$NON-NLS-1$
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if (fileLock != null)
			{
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
					try
					{
						fileLock.release();
						randomAccessFile.close();
						file.delete();
					}
					catch (final Exception e)
					{
						Log.err("Unable to remove lock file: " + lockFile, e); //$NON-NLS-1$
					}

				}));
				return true;
			}
		}
		catch (final Exception e)
		{
			Log.err("Unable to create and/or lock file: " + lockFile, e); //$NON-NLS-1$
		}
		return false;
	}
}
