package jrm.profile.fix.actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import jrm.Messages;
import jrm.compressors.Archive;
import jrm.profile.data.Entry;
import jrm.ui.ProgressHandler;

public class RenameEntry extends EntryAction
{
	String newname;

	public RenameEntry(Entry entry)
	{
		super(entry);
		this.newname = UUID.randomUUID() + "_" + entry.size + ".tmp"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public RenameEntry(String newname, Entry entry)
	{
		super(entry);
		this.newname = newname;
	}

	@Override
	public boolean doAction(FileSystem fs, ProgressHandler handler)
	{
		Path dstpath = null;
		try
		{
			handler.setProgress(null, null, null, String.format(Messages.getString("RenameEntry.Renaming"), entry.file, newname)); //$NON-NLS-1$
			Path srcpath = fs.getPath(entry.file);
			dstpath = fs.getPath(newname);
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
			Files.move(srcpath, dstpath, StandardCopyOption.REPLACE_EXISTING);
			entry.file = dstpath.toString();
			// System.out.println("rename "+parent.container.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("rename " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed");
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
			handler.setProgress(null, null, null, String.format(Messages.getString("RenameEntry.Renaming"), entry.file, newname)); //$NON-NLS-1$
			Path srcpath = target.resolve(entry.file);
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
			Files.move(srcpath, dstpath, StandardCopyOption.REPLACE_EXISTING);
			entry.file = dstpath.toString();
			// System.out.println("rename "+parent.container.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("rename " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Archive archive, ProgressHandler handler)
	{
		try
		{
			handler.setProgress(null, null, null, String.format(Messages.getString("RenameEntry.Renaming"), entry.file, newname)); //$NON-NLS-1$
			if(archive.rename(entry.file, newname) == 0)
			{
				entry.file = newname;
				return true;
			}
		}
		catch(Throwable e)
		{
			System.err.println("rename " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed");
		}
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("RenameEntry.Rename"), entry, newname); //$NON-NLS-1$
	}
}