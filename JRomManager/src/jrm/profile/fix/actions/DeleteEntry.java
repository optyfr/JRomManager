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
	public DeleteEntry(Entry entry)
	{
		super(entry);
	}

	@Override
	public boolean doAction(FileSystem dstfs, ProgressHandler handler)
	{
		Path path = null;
		try
		{
			handler.setProgress(null, null, null, String.format(Messages.getString("DeleteEntry.Deleting"), entry.file)); //$NON-NLS-1$
			path = dstfs.getPath(entry.file);
			Files.deleteIfExists(path);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("delete " + parent.container.file.getName() + "@" + path + " failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Path target, ProgressHandler handler)
	{
		Path path = null;
		try
		{
			handler.setProgress(null, null, null, String.format(Messages.getString("DeleteEntry.Deleting"), entry.file)); //$NON-NLS-1$
			path = target.resolve(entry.file);
			Files.deleteIfExists(path);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("delete " + parent.container.file.getName() + "@" + path + " failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Archive archive, ProgressHandler handler)
	{
		try
		{
			handler.setProgress(null, null, null, String.format(Messages.getString("DeleteEntry.Deleting"), entry.file)); //$NON-NLS-1$
			return archive.delete(entry.file) == 0;
		}
		catch(Throwable e)
		{
			System.err.println("delete " + parent.container.file.getName() + "@" + entry.file + " failed");
		}
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("DeleteEntry.Delete"), entry); //$NON-NLS-1$
	}
}
