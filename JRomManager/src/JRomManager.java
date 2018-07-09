import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import org.apache.commons.io.FilenameUtils;

import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.ui.MainFrame;
import jupdater.JUpdater;

public final class JRomManager
{
	public static void main(final String[] args)
	{
		for(int i = 0; i < args.length; i++)
		{
			switch(args[i])
			{
				case "--multiuser":
					Settings.multiuser = true;
					break;
				case "--noupdate":
					Settings.noupdate = true;
					break;
			}
		}
		if (JRomManager.lockInstance(FilenameUtils.removeExtension(JRomManager.class.getSimpleName()) + ".lock")) //$NON-NLS-1$
		{
			if(!Settings.noupdate)
			{
				JUpdater updater = new JUpdater("optyfr","JRomManager");
				if(updater.updateAvailable())
					updater.showMessage();
			}
			new MainFrame().setVisible(true);
		}
	}

	private static boolean lockInstance(final String lockFile)
	{
		try
		{
			final File file = new File(Settings.getWorkPath().toFile(),lockFile);
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
