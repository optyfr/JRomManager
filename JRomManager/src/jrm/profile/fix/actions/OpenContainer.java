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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;

import jrm.aui.progress.ProgressHandler;
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
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(session.msgs.getString("OpenContainer.Fixing")), toBlue(container.getRelAW().getFullName(container.getFile().getName())), toPurple(container.getRelAW().getDescription()))))); //$NON-NLS-1$
		if (container.getType() == Container.Type.ZIP)
		{
			if (format == FormatOptions.ZIP || format == FormatOptions.TZIP)
			{
				return doActionZip(session, handler);
			}
			else if (format == FormatOptions.ZIPE)
			{
				return doActionZipE(session, handler);
			}
		}
		else if (container.getType() == Container.Type.SEVENZIP)
		{
			return doAction7z(session, handler);
		}
		else if (container.getType() == Container.Type.DIR || container.getType() == Container.Type.FAKE)
		{
			return doActionDir(session, handler);
		}
		return false;
	}

	/**
	 * @param session
	 * @param handler
	 * @return
	 */
	private boolean doActionDir(final Session session, final ProgressHandler handler)
	{
		final Path target = container.getType() == Container.Type.DIR ? container.getFile().toPath() : container.getFile().getParentFile().toPath();
		var i = 0;
		for (final EntryAction action : entryActions)
		{
			i++;
			if (!action.doAction(session, target, handler, i, entryActions.size()))
			{
				System.err.println("action to " + container.getFile().getName() + "@" + action.entry.getRelFile() + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return false;
			}
		}
		if (container.getType() == Container.Type.DIR)
			deleteEmptyFolders(container.getFile());
		return true;
	}

	/**
	 * @param session
	 * @param handler
	 * @return
	 */
	private boolean doAction7z(final Session session, final ProgressHandler handler)
	{
		try (Archive archive = new SevenZipArchive(session, container.getFile()))
		{
			var i = 0;
			for (final EntryAction action : entryActions)
			{
				i++;
				if (!action.doAction(session, archive, handler, i, entryActions.size()))
				{
					System.err.println("action to " + container.getFile().getName() + "@" + action.entry.getRelFile() + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false;
				}
			}
			return true;
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(),e);
		}
		return false;
	}

	/**
	 * @param session
	 * @param handler
	 * @return
	 */
	private boolean doActionZipE(final Session session, final ProgressHandler handler)
	{
		try (Archive archive = new ZipArchive(session, container.getFile()))
		{
			var i = 0;
			for (final EntryAction action : entryActions)
			{
				i++;
				if (!action.doAction(session, archive, handler, i, entryActions.size()))
				{
					System.err.println("action to " + container.getFile().getName() + "@" + action.entry.getRelFile() + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false;
				}
			}
			return true;
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(),e);
		}
		return false;
	}

	/**
	 * @param session
	 * @param handler
	 * @return
	 */
	private boolean doActionZip(final Session session, final ProgressHandler handler)
	{
		final Map<String, Object> env = new HashMap<>();
		env.put("useTempFile", dataSize > ZipTempThreshold.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_temp_threshold, ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
		env.put("compressionLevel", format == FormatOptions.TZIP ? 1 : ZipLevel.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_compression_level, ZipLevel.DEFAULT.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
		try (final var fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + container.getFile().toURI()), env);) //$NON-NLS-1$
		{
			var i = 0;
			for (final EntryAction action : entryActions)
			{
				i++;
				if (!action.doAction(session, fs, handler, i, entryActions.size()))
				{
					System.err.println("action to " + container.getFile().getName() + "@" + action.entry.getRelFile() + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					return false;
				}
			}
			deleteEmptyFolders(fs.getPath("/")); //$NON-NLS-1$
			return true;
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(),e);
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
				try
				{
					Files.deleteIfExists(baseFolder.toPath());
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(), e);
				}
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
			try(final var stream = Files.list(baseFolder))
			{
				for (final Path folder : stream.collect(Collectors.toList()))
					filescnt += Files.isDirectory(folder) ? deleteEmptyFolders(folder) : 1;
			}
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
		StringBuilder str = new StringBuilder(Messages.getString("OpenContainer.Open")).append(container); //$NON-NLS-1$
		for (final EntryAction action : entryActions)
			str.append("\n\t").append(action); //$NON-NLS-1$
		return str.toString();
	}
	
	@Override
	public long estimatedSize()
	{
		long size = 0;
		for (final EntryAction action : entryActions)
			size += action.estimatedSize();
		return size;
	}
	
	@Override
	public int count()
	{
		return entryActions.size();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
