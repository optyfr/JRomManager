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
	final String newname;

	public RenameEntry(final Entry entry)
	{
		super(entry);
		newname = UUID.randomUUID() + "_" + entry.size + ".tmp"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public RenameEntry(final String newname, final Entry entry)
	{
		super(entry);
		this.newname = newname;
	}

	@Override
	public boolean doAction(final FileSystem fs, final ProgressHandler handler, int i, int max)
	{
		Path dstpath = null;
		try
		{
			handler.setProgress(null, null, null, progress(i, max) + String.format(Messages.getString("RenameEntry.Renaming"), entry.file, newname)); //$NON-NLS-1$
			final Path srcpath = fs.getPath(entry.file);
			dstpath = fs.getPath(newname);
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
			Files.move(srcpath, dstpath, StandardCopyOption.REPLACE_EXISTING);
			entry.file = dstpath.toString();
			// System.out.println("rename "+parent.container.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath);
			return true;
		}
		catch(final Throwable e)
		{
			System.err.println("rename " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public boolean doAction(final Path target, final ProgressHandler handler, int i, int max)
	{
		Path dstpath = null;
		try
		{
			dstpath = target.resolve(newname);
			handler.setProgress(null, null, null, progress(i, max) + String.format(Messages.getString("RenameEntry.Renaming"), entry.file, newname)); //$NON-NLS-1$
			final Path srcpath = target.resolve(entry.file);
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
			Files.move(srcpath, dstpath, StandardCopyOption.REPLACE_EXISTING);
			entry.file = dstpath.toString();
			// System.out.println("rename "+parent.container.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath);
			return true;
		}
		catch(final Throwable e)
		{
			System.err.println("rename " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public boolean doAction(final Archive archive, final ProgressHandler handler, int i, int max)
	{
		try
		{
			handler.setProgress(null, null, null, progress(i, max) + String.format(Messages.getString("RenameEntry.Renaming"), entry.file, newname)); //$NON-NLS-1$
			if(archive.rename(entry.file, newname) == 0)
			{
				entry.file = newname;
				return true;
			}
		}
		catch(final Throwable e)
		{
			System.err.println("rename " + parent.container.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + newname + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("RenameEntry.Rename"), entry, newname); //$NON-NLS-1$
	}
}
