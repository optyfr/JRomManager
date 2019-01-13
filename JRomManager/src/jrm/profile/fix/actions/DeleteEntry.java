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

import jrm.compressors.Archive;
import jrm.locale.Messages;
import jrm.profile.data.Entry;
import jrm.security.Session;
import jrm.ui.progress.ProgressHandler;

/**
 * Delete an entry from its container
 * @author optyfr
 *
 */
public class DeleteEntry extends EntryAction
{
	/**
	 * constructor
	 * @param entry the entry to delete
	 */
	public DeleteEntry(final Entry entry)
	{
		super(entry);
	}

	@Override
	public boolean doAction(final Session session, final FileSystem dstfs, final ProgressHandler handler, int i, int max)
	{
		Path path = null;
		try
		{
			handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("DeleteEntry.Deleting"), entry.file))); //$NON-NLS-1$
			path = dstfs.getPath(entry.file);
			Files.deleteIfExists(path);
			return true;
		}
		catch(final Throwable e)
		{
			System.err.println("delete " + parent.container.file.getName() + "@" + path + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Path target, final ProgressHandler handler, int i, int max)
	{
		Path path = null;
		try
		{
			handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("DeleteEntry.Deleting"), entry.file))); //$NON-NLS-1$
			path = target.resolve(entry.file);
			Files.deleteIfExists(path);
			return true;
		}
		catch(final Throwable e)
		{
			System.err.println("delete " + parent.container.file.getName() + "@" + path + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, final Archive archive, final ProgressHandler handler, int i, int max)
	{
		try
		{
			handler.setProgress(null, null, null, progress(i, max, String.format(session.msgs.getString("DeleteEntry.Deleting"), entry.file))); //$NON-NLS-1$
			return archive.delete(entry.file) == 0;
		}
		catch(final Throwable e)
		{
			System.err.println("delete " + parent.container.file.getName() + "@" + entry.file + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("DeleteEntry.Delete"), entry); //$NON-NLS-1$
	}
}
