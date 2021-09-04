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

import java.io.FileOutputStream;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipOutputStream;

import org.apache.commons.text.StringEscapeUtils;

import jrm.aui.progress.ProgressHandler;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.ZipArchive;
import jrm.compressors.zipfs.ZipLevel;
import jrm.locale.Messages;
import jrm.misc.IOUtils;
import jrm.misc.Log;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.security.Session;

/**
 * specialized class when container need to be created before doing actions on entries (which should be only {@link AddEntry}) 
 * @author optyfr
 */
public class CreateContainer extends ContainerAction
{
	/**
	 * the uncompressed datasize of all entries to add (for temp file threshold purpose)
	 */
	private final long dataSize;

	/**
	 * constructor
	 * @param container the container to create
	 * @param format the desired format
	 * @param dataSize the uncompressed data size supposed to be added
	 */
	public CreateContainer(final Container container, final FormatOptions format, final long dataSize)
	{
		super(container, format);
		this.dataSize = dataSize;
	}

	/**
	 * shortcut static method to get an instance of {@link CreateContainer}
	 * @param action the potentially already existing {@link CreateContainer} 
	 * @param container the container to create
	 * @param format the desired format
	 * @param dataSize the uncompressed data size supposed to be added
	 * @return a {@link CreateContainer}
	 */
	public static CreateContainer getInstance(CreateContainer action, final Container container, final FormatOptions format, final long dataSize)
	{
		if (action == null)
			action = new CreateContainer(container, format, dataSize);
		return action;
	}

	/**
	 * shortcut static method to get an instance of {@link CreateContainer}
	 * @param action the potentially already existing {@link CreateContainer} 
	 * @param container the container to create
	 * @param format the desired format
	 * @param dataSize the uncompressed data size supposed to be added
	 * @return a {@link CreateContainer}
	 */
	public static CreateContainer getInstance(AtomicReference<CreateContainer> action, final Container container, final FormatOptions format, final long dataSize)
	{
		if (action.get() == null)
			action.set(new CreateContainer(container, format, dataSize));
		return action.get();
	}

	@Override
	public boolean doAction(final Session session, final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(session.getMsgs().getString("CreateContainer.Creating")), toBlue(container.getRelAW().getFullName(container.getFile().getName())), toPurple(container.getRelAW().getDescription()))))); //$NON-NLS-1$
		if (container.getType() == Container.Type.ZIP)
		{
			if (format == FormatOptions.ZIP || format == FormatOptions.TZIP)
			{
				return createZip(session, handler);
			}
			else if (format == FormatOptions.ZIPE)
			{
				return createZipE(session, handler);
			}
			return false;
		}
		else if (container.getType() == Container.Type.SEVENZIP)
		{
			return createSevenZip(session, handler);
		}
		else if (container.getType() == Container.Type.DIR || container.getType() == Container.Type.FAKE)
		{
			return createDirOrFake(session, handler);
		}
		return false;
	}

	/**
	 * @param session
	 * @param handler
	 * @return
	 */
	private boolean createDirOrFake(final Session session, final ProgressHandler handler)
	{
		try
		{
			final Path target;
			if (container.getType() == Container.Type.DIR)
			{
				target = container.getFile().toPath();
				IOUtils.createDirectories(target);
			}
			else
				target = container.getFile().getParentFile().toPath();
			return pathAction(session, handler, target);
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
	private boolean createSevenZip(final Session session, final ProgressHandler handler)
	{
		container.getFile().getParentFile().mkdirs();
		try
		{
			return archiveAction(session, handler, new SevenZipArchive(session, container.getFile()));
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
	private boolean createZipE(final Session session, final ProgressHandler handler)
	{
		container.getFile().getParentFile().mkdirs();
		try
		{
			return archiveAction(session, handler, new ZipArchive(session, container.getFile()));
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
	private boolean createZip(final Session session, final ProgressHandler handler)
	{
		try(final var zout = new ZipOutputStream(new FileOutputStream(container.getFile())))
		{
			zout.setLevel(format == FormatOptions.TZIP ? 1 : ZipLevel.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_compression_level, ZipLevel.DEFAULT.toString())).getLevel());
			return zosAction(session, handler, zout);
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(),e);
		}
/*		
		final Map<String, Object> env = new HashMap<>();
		env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
		env.put("useTempFile", dataSize > ZipTempThreshold.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_temp_threshold, ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
		env.put("compressionLevel", format == FormatOptions.TZIP ? 1 : ZipLevel.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_compression_level, ZipLevel.DEFAULT.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
		container.getFile().getParentFile().mkdirs();
		try (final var fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + container.getFile().toURI()), env);) //$NON-NLS-1$
		{
			return fsAction(session, handler, fs);
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(),e);
		}*/
		return false;
	}

	@Override
	public String toString()
	{
		final var str = new StringBuilder(Messages.getString("CreateContainer.Create")).append(container); //$NON-NLS-1$
		for (final EntryAction action : entryActions)
			str.append("\n\t").append(action); //$NON-NLS-1$
		return str.toString();
	}

	@Override
	public long estimatedSize()
	{
		return dataSize<Long.MAX_VALUE?dataSize:0;
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
