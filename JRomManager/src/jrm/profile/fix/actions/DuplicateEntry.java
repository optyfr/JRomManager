package jrm.profile.fix.actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jrm.Messages;
import jrm.compressors.Archive;
import jrm.profile.data.Entry;
import jrm.ui.ProgressHandler;

public class DuplicateEntry extends EntryAction
{
	final String newname;

	public DuplicateEntry(String newname, Entry entry)
	{
		super(entry);
		this.newname = newname;
	}

	@Override
	public boolean doAction(FileSystem fs, ProgressHandler handler)
	{
		Path dstpath = fs.getPath(newname);
		try
		{
			handler.setProgress(null, null, null, String.format(Messages.getString("DuplicateEntry.Duplicating"), entry.file, newname)); //$NON-NLS-1$
			Path srcpath = fs.getPath(entry.file);
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			System.err.println("duplicate " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Path target, ProgressHandler handler)
	{
		Path dstpath = null;
		try
		{
			dstpath = target.resolve(newname);
			handler.setProgress(null, null, null, String.format(Messages.getString("DuplicateEntry.Duplicating"), entry.file, newname)); //$NON-NLS-1$
			Path srcpath = target.resolve(entry.file);
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("duplicate " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Archive archive, ProgressHandler handler)
	{
		try
		{
			handler.setProgress(null, null, null, String.format(Messages.getString("DuplicateEntry.Duplicating"), entry.file, newname)); //$NON-NLS-1$
			return archive.duplicate(entry.file, newname) == 0;
		}
		catch(Throwable e)
		{
			System.err.println("duplicate " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed");
		}
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("DuplicateEntry.Duplicate"), entry, newname); //$NON-NLS-1$
	}
}
