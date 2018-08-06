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

import jrm.Messages;
import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.ZipArchive;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.compressors.zipfs.ZipLevel;
import jrm.compressors.zipfs.ZipTempThreshold;
import jrm.misc.Settings;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

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
	public boolean doAction(final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("OpenContainer.Fixing")), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.getDescription()))))); //$NON-NLS-1$
		if (container.getType() == Container.Type.ZIP)
		{
			if (format == FormatOptions.ZIP || format == FormatOptions.TZIP)
			{
				final Map<String, Object> env = new HashMap<>();
				env.put("useTempFile", dataSize > ZipTempThreshold.valueOf(Settings.getProperty("zip_temp_threshold", ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$
				env.put("compressionLevel", format == FormatOptions.TZIP ? 1 : ZipLevel.valueOf(Settings.getProperty("zip_compression_level", ZipLevel.DEFAULT.toString())).getLevel());
				try (FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + container.file.toURI()), env);) //$NON-NLS-1$
				{
					int i = 0;
					for (final EntryAction action : entry_actions)
					{
						i++;
						if (!action.doAction(fs, handler, i, entry_actions.size()))
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
					e.printStackTrace();
				}
			}
			else if (format == FormatOptions.ZIPE)
			{
				try (Archive archive = new ZipArchive(container.file))
				{
					int i = 0;
					for (final EntryAction action : entry_actions)
					{
						i++;
						if (!action.doAction(archive, handler, i, entry_actions.size()))
						{
							System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							return false;
						}
					}
					return true;
				}
				catch (final Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
		else if (container.getType() == Container.Type.SEVENZIP)
		{
			try (Archive archive = new SevenZipArchive(container.file))
			{
				int i = 0;
				for (final EntryAction action : entry_actions)
				{
					i++;
					if (!action.doAction(archive, handler, i, entry_actions.size()))
					{
						System.err.println("action to " + container.file.getName() + "@" + action.entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return false;
					}
				}
				return true;
			}
			catch (final Throwable e)
			{
				e.printStackTrace();
			}
		}
		else if (container.getType() == Container.Type.DIR)
		{
			final Path target = container.file.toPath();
			int i = 0;
			for (final EntryAction action : entry_actions)
			{
				i++;
				if (!action.doAction(target, handler, i, entry_actions.size()))
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
		for (final File folder : baseFolder.listFiles())
		{
			if (folder.isDirectory())
				totalSize += deleteEmptyFolders(folder);
			else
				totalSize += folder.length();
		}
		if (totalSize == 0)
			baseFolder.delete();
		return totalSize;
	}

	/**
	 * Delete recursively folders only if they are empty
	 * @param baseFolder the base folder as a {@link Path} (may also be deleted if nothing left)
	 * @return the number of bytes left in folders, 0 mean all folders were deleted
	 */
	public long deleteEmptyFolders(final Path baseFolder)
	{
		long totalSize = 0;
		try
		{
			for (final Path folder : Files.list(baseFolder).collect(Collectors.toList()))
			{
				if (Files.isDirectory(folder))
					totalSize += deleteEmptyFolders(folder);
				else
					totalSize += Files.size(folder);
			}
			if (totalSize == 0)
				Files.delete(baseFolder);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		return totalSize;
	}

	@Override
	public String toString()
	{
		String str = Messages.getString("OpenContainer.Open") + container; //$NON-NLS-1$
		for (final EntryAction action : entry_actions)
			str += "\n\t" + action; //$NON-NLS-1$
		return str;
	}
}
