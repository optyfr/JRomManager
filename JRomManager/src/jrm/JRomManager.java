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

import org.apache.commons.io.FilenameUtils;

import jrm.misc.Log;
import jrm.misc.GlobalSettings;
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
		for(int i = 0; i < args.length; i++)
		{
			switch(args[i])
			{
				case "--multiuser":	// will write settings, cache and dat into home directory instead of app directory //$NON-NLS-1$
					GlobalSettings.multiuser = true;
					break;
				case "--noupdate":	// will disable update check //$NON-NLS-1$
					GlobalSettings.noupdate = true;
					break;
			}
		}
		if (JRomManager.lockInstance(FilenameUtils.removeExtension(JRomManager.class.getSimpleName()) + ".lock")) //$NON-NLS-1$
		{
			if(!GlobalSettings.noupdate)
			{
				// check for update
				JUpdater updater = new JUpdater("optyfr","JRomManager"); //$NON-NLS-1$ //$NON-NLS-2$
				if(updater.updateAvailable())
					updater.showMessage();	// Will show changes since your version and a link to updater
			}
			// Open main window
			new MainFrame().setVisible(true);
		}
	}

	/**
	 * Write lock file and keep it locked (rw) until program shutdown
	 * @param lockFile the file to lock
	 * @return true if successful, false otherwise
	 */
	private static boolean lockInstance(final String lockFile)
	{
		try
		{
			final File file = new File(GlobalSettings.getWorkPath().toFile(),lockFile);
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
