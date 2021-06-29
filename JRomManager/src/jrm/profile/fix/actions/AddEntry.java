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
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import jrm.aui.progress.ProgressHandler;
import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.profile.data.Container.Type;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;
import jrm.profile.data.Rom;
import jrm.security.Session;

/**
 * Add an entry to a container
 * @author optyfr
 */
public class AddEntry extends EntryAction
{
	/**
	 * the related entity
	 */
	private final EntityBase entity;

	/**
	 * constructor
	 * @param entity the related {@link EntityBase} (a {@link Rom} for example) 
	 * @param entry the {@link Entry} to add
	 */
	public AddEntry(final EntityBase entity, final Entry entry)
	{
		super(entry);
		this.entity = entity;
	}

	@Override
	public boolean doAction(final Session session, final FileSystem dstfs, final ProgressHandler handler, int i, int max)
	{
		final var dstpath = dstfs.getPath(entity.getName());
		handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("AddEntry.Adding"), entity.getName()))); //$NON-NLS-1$
		Path srcpath = null;
		if(entry.getParent().getType() == Type.DIR)
		{
			try
			{
				var parentDstPath = dstpath.getParent(); 
				if(parentDstPath != null)
					Files.createDirectories(parentDstPath);
				srcpath = entry.getParent().getFile().toPath().resolve(entry.getFile());
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + srcpath + " to " + parent.container.getFile().getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else if(entry.getParent().getType() == Type.FAKE)
		{
			try
			{
				final var parentDstPath = dstpath.getParent(); 
				if(parentDstPath != null)
					Files.createDirectories(parentDstPath);
				srcpath = entry.getParent().getFile().getParentFile().toPath().resolve(entry.getFile());
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + srcpath + " to " + parent.container.getFile().getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else if(entry.getParent().getType() == Type.ZIP)
		{
			try(final var srcfs = new ZipFileSystemProvider().newFileSystem(entry.getParent().getFile().toPath(), Collections.singletonMap("readOnly", true));)
			{
				final var parentDstPath = dstpath.getParent(); 
				if(parentDstPath != null)
					Files.createDirectories(parentDstPath);
				srcpath = srcfs.getPath(entry.getFile());
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + entry.getParent().getRelFile() + "@" + srcpath + " to " + parent.container.getFile().getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else
		{
			try(Archive srcarchive = new SevenZipArchive(session, entry.getParent().getFile()))
			{
				final File srcfile;
				if((srcfile=srcarchive.extract(entry.getFile())) != null)
				{
					final var parentDstPath = dstpath.getParent(); 
					if(parentDstPath != null)
						Files.createDirectories(parentDstPath);
					srcpath = srcfile.toPath();
					Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
					return true;
				}
			}
			catch (IOException e)
			{
				Log.err("add from " + entry.getParent().getRelFile() + "@" + srcpath + " to " + parent.container.getFile().getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Path target, final ProgressHandler handler, int i, int max)
	{
		final var dstpath = target.resolve(entity.getName());
		handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("AddEntry.Adding"), entity.getName()))); //$NON-NLS-1$
		Path srcpath = null;
		if(entry.getParent().getType() == Type.DIR)
		{
			try
			{
				srcpath = entry.getParent().getFile().toPath().resolve(entry.getFile());
				final var parent = dstpath.getParent(); 
				if(parent != null)
					Files.createDirectories(parent);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + srcpath + " to " + parent.container.getFile().getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else if(entry.getParent().getType() == Type.FAKE)
		{
			try
			{
				srcpath = entry.getParent().getFile().getParentFile().toPath().resolve(entry.getFile());
				final var parent = dstpath.getParent(); 
				if(parent != null)
					Files.createDirectories(parent);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + srcpath + " to " + parent.container.getFile().getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else if(entry.getParent().getType() == Type.ZIP)
		{
			try(final var srcfs = new ZipFileSystemProvider().newFileSystem(entry.getParent().getFile().toPath(), Collections.singletonMap("readOnly", true));)
			{
				srcpath = srcfs.getPath(entry.getFile());
				final var parent = dstpath.getParent(); 
				if(parent != null)
					Files.createDirectories(parent);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch(final Exception e)
			{
				Log.err("add from " + entry.getParent().getFile().getName() + "@" + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + entity.getName() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}

		}
		else
		{

			try(Archive srcarchive = new SevenZipArchive(session, entry.getParent().getFile()))
			{
				final File srcfile;
				if((srcfile=srcarchive.extract(entry.getFile())) != null)
				{
					srcpath = srcfile.toPath();
					final var parent = dstpath.getParent();
					if(parent != null)
						Files.createDirectories(parent);
					Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				}
				return true;
			}
			catch(final IOException e)
			{
				Log.err("add from " + entry.getParent().getFile().getName() + "@" + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + entity.getName() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Archive dstarchive, final ProgressHandler handler, int i, int max)
	{
		handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("AddEntry.Adding"), entity.getName()))); //$NON-NLS-1$
		if(entry.getParent().getType() == Type.DIR)
		{
			try
			{
				Path srcpath = entry.getParent().getFile().toPath().resolve(entry.getFile());
				return dstarchive.add(srcpath.toFile(), entity.getName()) == 0;
			}
			catch (IOException e)
			{
				Log.err("add from " + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + entity.getName() + " failed",e);
			}
		}
		else if(entry.getParent().getType() == Type.FAKE)
		{
			try
			{
				Path srcpath = entry.getParent().getFile().getParentFile().toPath().resolve(entry.getFile());
				return dstarchive.add(srcpath.toFile(), entity.getName()) == 0;
			}
			catch (IOException e)
			{
				Log.err("add from " + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + entity.getName() + " failed",e);
			}
		}
		else if(entry.getParent().getType() == Type.ZIP)
		{
			try(final var srcfs = new ZipFileSystemProvider().newFileSystem(entry.getParent().getFile().toPath(), Collections.singletonMap("readOnly", true));)
			{
				try(final var in = Files.newInputStream(srcfs.getPath(entry.getFile())))
				{
					return dstarchive.add_stdin(in, entity.getName()) == 0;
				}
			}
			catch(final Exception e)
			{
				Log.err("add from " + entry.getParent().getFile().getName() + "@" + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + entity.getName() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else
		{
			try(Archive srcarchive = new SevenZipArchive(session, entry.getParent().getFile()))
			{
				final File file;
				if ((file = srcarchive.extract(entry.getFile())) != null)
					return dstarchive.add(file, entity.getName()) == 0;
				// return archive.add_stdin(srcarchive.extract_stdout(entry.file) , entity.getName()) == 0;
			}
			catch(final IOException e)
			{
				Log.err("add from " + entry.getParent().getFile().getName() + "@" + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + entity.getName() + " failed",e);
			}
		}

		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("AddEntry.Add"), entry, entity); //$NON-NLS-1$
	}
	
	@Override
	public long estimatedSize()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getSize();
		return 0L;
	}
}
