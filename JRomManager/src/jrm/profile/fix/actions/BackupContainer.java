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
package jrm.profile.fix.actions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CRC32;

import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.profile.data.Container;
import jrm.profile.data.Entry;
import jrm.profile.scan.options.FormatOptions;
import jrm.security.Session;
import jrm.ui.progress.ProgressHandler;

/**
 * Special class aimed to backup some or all entries from a container
 * @author optyfr
 *
 */
public class BackupContainer extends ContainerAction
{
	/**
	 * constructor
	 * @param container the container to backup
	 */
	public BackupContainer(Container container)
	{
		super(container, FormatOptions.ZIP);
	}

	/**
	 * shortcut static method to get an instance of {@link BackupContainer}
	 * @param action the potentially already existing {@link BackupContainer} 
	 * @param container the container to backup
	 * @return a new {@link BackupContainer}, or the already existing {@code container} parameter
	 */
	public static BackupContainer getInstance(BackupContainer action, final Container container)
	{
		if (action == null)
			action = new BackupContainer(container);
		return action;
	}

	/**
	 * a maintained list of opened Zip {@link FileSystem}s
	 */
	private final static Map<String, FileSystem> filesystems = new HashMap<String, FileSystem>();

	/**
	 * get a {@link FileSystem} to backup {@link EntryAction} file from {@link Container}
	 * @param container the originating entry's {@link Container}
	 * @param action the {@link EntryAction} describing the entry 
	 * @return a valid Zip {@link FileSystem} for which to save the entry, {@link FileSystem} archive will be created if not already existing, otherwise it will be opened for writing or reused if has already be returned for another entry 
	 * @throws IOException if the zip archive could not be opened or created for writing
	 */
	public static synchronized FileSystem getFS(final Session session, Container container, EntryAction action) throws IOException
	{
		String crc2 = action.entry.crc.substring(0, 2);
		if (!filesystems.containsKey(crc2))
		{
			final File workdir = session.getUser().settings.getWorkPath().toFile(); //$NON-NLS-1$
			final File backupdir = new File(workdir, "backup"); //$NON-NLS-1$
			final CRC32 crc = new CRC32();
			crc.update(container.file.getAbsoluteFile().getParent().getBytes());
			final File backupfile = new File(new File(backupdir, String.format("%08x", crc.getValue())), crc2 + ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			backupfile.getParentFile().mkdirs();
			final Map<String, Object> env = new HashMap<>();
			if (!backupfile.exists())
				env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
			env.put("useTempFile", true); //$NON-NLS-1$
			env.put("compressionLevel", 1); //$NON-NLS-1$
			filesystems.put(crc2, new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + backupfile.toURI()), env)); //$NON-NLS-1$
		}
		return filesystems.get(crc2);
	}

	/**
	 * close all opened zip archive {@link FileSystem}s (when all backup actions are finished)
	 */
	public static void closeAllFS()
	{
		filesystems.values().forEach(fs -> {
			try
			{
				synchronized (fs)
				{
					fs.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		});
		filesystems.clear();
	}

	@Override
	public boolean doAction(final Session session, ProgressHandler handler)
	{
		try
		{
			int i = 0;
			if (entry_actions.size() == 0)
				for (Entry entry : container.getEntries())
					addAction(new BackupEntry(entry));
			for (final EntryAction action : entry_actions)
			{
				i++;
				final FileSystem fs = getFS(session, container, action);
				synchronized (fs)
				{
					if (!action.doAction(fs, handler, i, entry_actions.size()))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return false;
					}
				}
			}
			return true;
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}

}
