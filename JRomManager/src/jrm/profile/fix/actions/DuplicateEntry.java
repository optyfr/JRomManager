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

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jrm.aui.progress.ProgressHandler;
import jrm.compressors.Archive;
import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.profile.data.Entry;
import jrm.security.Session;

/**
 * Duplicate an entry inside the *same* container
 * @author optyfr
 *
 */
public class DuplicateEntry extends EntryAction
{
	/**
	 * the new name of the entry
	 */
	final String newname;

	/**
	 * the constructor
	 * @param newname the new name io the entry
	 * @param entry the {@link Entry} to duplicated
	 */
	public DuplicateEntry(final String newname, final Entry entry)
	{
		super(entry);
		this.newname = newname;
	}

	@Override
	public boolean doAction(final Session session, final FileSystem fs, final ProgressHandler handler, int i, int max)
	{
		final var dstpath = fs.getPath(newname);
		try
		{
			handler.setProgress(null, null, null, progress(i, max, String.format(session.getMsgs().getString("DuplicateEntry.Duplicating"), entry.getRelFile(), newname))); //$NON-NLS-1$
			final var srcpath = fs.getPath(entry.getFile());
			final var parent2 = dstpath.getParent();
			if(parent2 != null)
				Files.createDirectories(parent2);
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch(final Exception e)
		{
			Log.err(e.getMessage(),e);
			System.err.println("duplicate " + parent.container.getFile().getName() + "@" + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + newname + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Path target, final ProgressHandler handler, int i, int max)
	{
		Path dstpath = null;
		try
		{
			dstpath = target.resolve(newname);
			if(dstpath!=null)
			{
				handler.setProgress(null, null, null, progress(i, max, String.format(session.getMsgs().getString("DuplicateEntry.Duplicating"), entry.getRelFile(), newname))); //$NON-NLS-1$
				final var srcpath = target.resolve(entry.getFile());
				final var parent2 = dstpath.getParent();
				if(parent2 != null)
					Files.createDirectories(parent2);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES);
				return true;
			}
		}
		catch(final Exception e)
		{
			System.err.println("duplicate " + parent.container.getFile().getName() + "@" + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + newname + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Archive archive, final ProgressHandler handler, int i, int max)
	{
		try
		{
			handler.setProgress(null, null, null, progress(i, max, String.format(session.getMsgs().getString("DuplicateEntry.Duplicating"), entry.getRelFile(), newname))); //$NON-NLS-1$
			return archive.duplicate(entry.getFile(), newname) == 0;
		}
		catch(final Exception e)
		{
			System.err.println("duplicate " + parent.container.getFile().getName() + "@" + entry.getRelFile() + " to " + parent.container.getFile().getName() + "@" + newname + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("DuplicateEntry.Duplicate"), entry, newname); //$NON-NLS-1$
	}
}
