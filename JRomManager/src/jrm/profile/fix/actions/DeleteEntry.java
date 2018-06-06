package jrm.profile.fix.actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import jrm.Messages;
import jrm.compressors.Archive;
import jrm.profile.data.Entry;
import jrm.ui.ProgressHandler;

public class DeleteEntry extends EntryAction
{
	public DeleteEntry(final Entry entry)
	{
		super(entry);
	}

	@Override
	public boolean doAction(final FileSystem dstfs, final ProgressHandler handler, int i, int max)
	{
		Path path = null;
		try
		{
			handler.setProgress(null, null, null, progress(i, max) + String.format(Messages.getString("DeleteEntry.Deleting"), entry.file)); //$NON-NLS-1$
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
	public boolean doAction(final Path target, final ProgressHandler handler, int i, int max)
	{
		Path path = null;
		try
		{
			handler.setProgress(null, null, null, progress(i, max) + String.format(Messages.getString("DeleteEntry.Deleting"), entry.file)); //$NON-NLS-1$
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
	public boolean doAction(final Archive archive, final ProgressHandler handler, int i, int max)
	{
		try
		{
			handler.setProgress(null, null, null, progress(i, max) + String.format(Messages.getString("DeleteEntry.Deleting"), entry.file)); //$NON-NLS-1$
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
