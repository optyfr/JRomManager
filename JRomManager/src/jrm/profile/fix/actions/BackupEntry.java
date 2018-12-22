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
import java.nio.file.*;

import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.misc.Log;
import jrm.profile.data.Container.Type;
import jrm.security.Session;
import jrm.profile.data.Entry;
import jrm.ui.progress.ProgressHandler;

/**
 * Describe an entry to backup, will take appropriate actions to extract entry before copying to provided backup {@link FileSystem} 
 * @author optyfr
 *
 */
public class BackupEntry extends EntryAction
{
	/**
	 * Constructor 
	 * @param entry the entry to backup
	 */
	public BackupEntry(final Entry entry)
	{
		super(entry);
	}

	@Override
	public boolean doAction(final Session session, final FileSystem dstfs, final ProgressHandler handler, int i, int max)
	{
		Path dstpath_crc = dstfs.getPath(entry.crc+'_'+entry.size);
		Path dstpath = dstfs.getPath(entry.sha1!=null?entry.sha1:(entry.md5!=null?entry.md5:(entry.crc+'_'+entry.size)));
		handler.setProgress(null, null, null, progress(i, max, String.format("Backup of %s", entry.getName()))); //$NON-NLS-1$
		Path srcpath = null;
		try
		{
			Path parent2 = dstpath.getParent();
			if(parent2 != null)
				Files.createDirectories(parent2);
			if(!dstpath.equals(dstpath_crc) && Files.exists(dstpath_crc))
				Files.delete(dstpath_crc);
			if (Files.exists(dstpath))
				return true;
			if(entry.parent.getType() == Type.DIR)
			{
				srcpath = entry.parent.file.toPath().resolve(entry.file);
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				return true;
			}
			else if(entry.parent.getType() == Type.ZIP)
			{
				try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
				{
					srcpath = srcfs.getPath(entry.file);
					Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
					return true;
				}
			}
			else if(entry.parent.getType() == Type.SEVENZIP)
			{
				try(Archive srcarchive = new SevenZipArchive(session, entry.parent.file))
				{
					if(srcarchive.extract(entry.file) != null)
					{
						srcpath = new File(srcarchive.getTempDir(), entry.file).toPath();
						Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
						return true;
					}
				}
			}
		}
		catch(final Throwable e)
		{
			Log.err(e.getMessage(),e);
			System.err.println("add from " + entry.parent.file + "@" + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public boolean doAction(final Session session, Archive archive, ProgressHandler handler, int i, int max)
	{
		return false;
	}

	@Override
	public boolean doAction(final Session session, Path target, ProgressHandler handler, int i, int max)
	{
		return false;
	}

	@Override
	public String toString()
	{
		return String.format("Backup of %s", entry); //$NON-NLS-1$
	}
}
