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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.ZipArchive;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.compressors.zipfs.ZipLevel;
import jrm.compressors.zipfs.ZipTempThreshold;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.security.Session;
import jrm.ui.progress.ProgressHandler;

/**
 * specialized class when an already existing container have to be opened before doing actions on entries (which should be only {@link AddEntry}) 
 * @author optyfr
 */
public class OpenContainer extends ContainerAction
{
	/**
	 * the uncompressed datasize of all entries to add (for temp file threshold purpose)
	 */
	private final long dataSize;

	/**
	 * constructor
	 * @param container the container to open
	 * @param format the desired format
	 * @param dataSize the uncompressed data size supposed to be added
	 */
	public OpenContainer(final Container container, final FormatOptions format, final long dataSize)
	{
		super(container, format);
		this.dataSize = dataSize;
	}

	/**
	 * shortcut static method to get an instance of {@link OpenContainer}
	 * @param action the potentially already existing {@link OpenContainer} 
	 * @param container the container to open
	 * @param format the desired format
	 * @param dataSize the uncompressed data size supposed to be added
	 * @return a {@link OpenContainer}
	 */
	public static OpenContainer getInstance(OpenContainer action, final Container container, final FormatOptions format, final long dataSize)
	{
		if (action == null)
			action = new OpenContainer(container, format, dataSize);
		return action;
	}

	@Override
	public boolean doAction(final Session session, final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(session.msgs.getString("OpenContainer.Fixing")), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.getDescription()))))); //$NON-NLS-1$
		if (container.getType() == Container.Type.ZIP)
		{
			if (format == FormatOptions.ZIP || format == FormatOptions.TZIP)
			{
				final Map<String, Object> env = new HashMap<>();
				env.put("useTempFile", dataSize > ZipTempThreshold.valueOf(session.getUser().settings.getProperty(jrm.misc.Options.zip_temp_threshold, ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
				env.put("compressionLevel", format == FormatOptions.TZIP ? 1 : ZipLevel.valueOf(session.getUser().settings.getProperty(jrm.misc.Options.zip_compression_level, ZipLevel.DEFAULT.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
				try (FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + container.file.toURI()), env);) //$NON-NLS-1$
				{
					int i = 0;
					for (final EntryAction action : entry_actions)
					{
						i++;
						if (!action.doAction(session, fs, handler, i, entry_actions.size()))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							return false;
						}
					}
					deleteEmptyFolders(fs.getPath("/")); //$NON-NLS-1$
					return true;
				}
				catch (final Throwable e)
				{
					Log.err(e.getMessage(),e);
				}
			}
			else if (format == FormatOptions.ZIPE)
			{
				try (Archive archive = new ZipArchive(session, container.file))
				{
					int i = 0;
					for (final EntryAction action : entry_actions)
					{
						i++;
						if (!action.doAction(session, archive, handler, i, entry_actions.size()))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							return false;
						}
					}
					return true;
				}
				catch (final Throwable e)
				{
					Log.err(e.getMessage(),e);
				}
			}
		}
		else if (container.getType() == Container.Type.SEVENZIP)
		{
			try (Archive archive = new SevenZipArchive(session, container.file))
			{
				int i = 0;
				for (final EntryAction action : entry_actions)
				{
					i++;
					if (!action.doAction(session, archive, handler, i, entry_actions.size()))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return false;
					}
				}
				return true;
			}
			catch (final Throwable e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		else if (container.getType() == Container.Type.DIR)
		{
			final Path target = container.file.toPath();
			int i = 0;
			for (final EntryAction action : entry_actions)
			{
				i++;
				if (!action.doAction(session, target, handler, i, entry_actions.size()))
				{
					System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false;
				}
			}
			deleteEmptyFolders(container.file);
			return true;
		}
		return false;
	}

	/**
	 * Delete recursively folders only if they are empty
	 * @param baseFolder the base folder as a {@link File} (may also be deleted if nothing left)
	 * @return the number of bytes left in folders, 0 mean all folders were deleted
	 */
	public long deleteEmptyFolders(final File baseFolder)
	{
		long totalSize = 0;
		if(baseFolder!=null)
		{
			File[] folders = baseFolder.listFiles();
			if(folders!=null)
			{
				for (final File folder : folders)
				{
					if (folder.isDirectory())
						totalSize += deleteEmptyFolders(folder);
					else
						totalSize += folder.length();
				}
			}
			if (totalSize == 0)
				baseFolder.delete();
		}
		return totalSize;
	}

	/**
	 * Delete recursively folders only if they are empty
	 * @param baseFolder the base folder as a {@link Path} (may also be deleted if nothing left)
	 * @return the number of bytes left in folders, 0 mean all folders were deleted
	 */
	public long deleteEmptyFolders(final Path baseFolder)
	{
		long filescnt = 0;
		if(baseFolder==null)
			return filescnt;
		try
		{
			for (final Path folder : Files.list(baseFolder).collect(Collectors.toList()))
				filescnt += Files.isDirectory(folder) ? deleteEmptyFolders(folder) : 1;
			if (filescnt == 0)
				Files.delete(baseFolder);
		}
		catch (final IOException e)
		{
			Log.err(e.getMessage(),e);
		}
		return filescnt;
	}

	@Override
	public String toString()
	{
		String str = Messages.getString("OpenContainer.Open") + container; //$NON-NLS-1$
		for (final EntryAction action : entry_actions)
			str += "\n\t" + action; //$NON-NLS-1$
		return str;
	}
	
	@Override
	public long estimatedSize()
	{
		long size = 0;
		for (final EntryAction action : entry_actions)
			size += action.estimatedSize();
		return size;
	}
	
	@Override
	public int count()
	{
		return entry_actions.size();
	}
}
