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
import jrm.ui.progress.ProgressHandler;

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
		final Path dstpath = dstfs.getPath(entity.getName());
		handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("AddEntry.Adding"), entity.getName()))); //$NON-NLS-1$
		Path srcpath = null;
		if(entry.parent.getType() == Type.DIR)
		{
			try
			{
				Path parent_dstpath = dstpath.getParent(); 
				if(parent_dstpath != null)
					Files.createDirectories(parent_dstpath);
				srcpath = entry.parent.file.toPath().resolve(entry.file);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else if(entry.parent.getType() == Type.ZIP)
		{
			try(FileSystem srcfs = new ZipFileSystemProvider().newFileSystem(entry.parent.file.toPath(), Collections.singletonMap("readOnly", true));)
			{
				Path parent_dstpath = dstpath.getParent(); 
				if(parent_dstpath != null)
					Files.createDirectories(parent_dstpath);
				srcpath = srcfs.getPath(entry.file);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + entry.parent.file + "@" + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else
		{
			try(Archive srcarchive = new SevenZipArchive(session, entry.parent.file))
			{
				final File srcfile;
				if((srcfile=srcarchive.extract(entry.file)) != null)
				{
					Path parent_dstpath = dstpath.getParent(); 
					if(parent_dstpath != null)
						Files.createDirectories(parent_dstpath);
					srcpath = srcfile.toPath();
					Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
					return true;
				}
			}
			catch (IOException e)
			{
				Log.err("add from " + entry.parent.file + "@" + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Path target, final ProgressHandler handler, int i, int max)
	{
		final Path dstpath = target.resolve(entity.getName());
		handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("AddEntry.Adding"), entity.getName()))); //$NON-NLS-1$
		Path srcpath = null;
		if(entry.parent.getType() == Type.DIR)
		{
			try
			{
				srcpath = entry.parent.file.toPath().resolve(entry.file);
				Path parent = dstpath.getParent(); 
				if(parent != null)
					Files.createDirectories(parent);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch (IOException e)
			{
				Log.err("add from " + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else if(entry.parent.getType() == Type.ZIP)
		{
			try(FileSystem srcfs = new ZipFileSystemProvider().newFileSystem(entry.parent.file.toPath(), Collections.singletonMap("readOnly", true));)
			{
				srcpath = srcfs.getPath(entry.file);
				Path parent = dstpath.getParent(); 
				if(parent != null)
					Files.createDirectories(parent);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			catch(final Throwable e)
			{
				Log.err("add from " + entry.parent.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + entity.getName() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}

		}
		else
		{

			try(Archive srcarchive = new SevenZipArchive(session, entry.parent.file))
			{
				final File srcfile;
				if((srcfile=srcarchive.extract(entry.file)) != null)
				{
					srcpath = srcfile.toPath();
					Path parent = dstpath.getParent();
					if(parent != null)
						Files.createDirectories(parent);
					Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				}
				return true;
			}
			catch(final IOException e)
			{
				Log.err("add from " + entry.parent.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + entity.getName() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Archive dstarchive, final ProgressHandler handler, int i, int max)
	{
		handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("AddEntry.Adding"), entity.getName()))); //$NON-NLS-1$
		if(entry.parent.getType() == Type.DIR)
		{
			try
			{
				Path srcpath = entry.parent.file.toPath().resolve(entry.file);
				return dstarchive.add(srcpath.toFile(), entity.getName()) == 0;
			}
			catch (IOException e)
			{
				Log.err("add from " + entry.file + " to " + parent.container.file.getName() + "@" + entity.getName() + " failed",e);
			}
		}
		else if(entry.parent.getType() == Type.ZIP)
		{
			try(FileSystem srcfs = new ZipFileSystemProvider().newFileSystem(entry.parent.file.toPath(), Collections.singletonMap("readOnly", true));)
			{
				return dstarchive.add_stdin(Files.newInputStream(srcfs.getPath(entry.file)), entity.getName()) == 0;
			}
			catch(final Throwable e)
			{
				Log.err("add from " + entry.parent.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + entity.getName() + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			}
		}
		else
		{
			try(Archive srcarchive = new SevenZipArchive(session, entry.parent.file))
			{
				final File file;
				if ((file = srcarchive.extract(entry.file)) != null)
					return dstarchive.add(file, entity.getName()) == 0;
				// return archive.add_stdin(srcarchive.extract_stdout(entry.file) , entity.getName()) == 0;
			}
			catch(final IOException e)
			{
				Log.err("add from " + entry.parent.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + entity.getName() + " failed",e);
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
