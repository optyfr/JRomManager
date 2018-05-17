package jrm.profile.fix.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import jrm.Messages;
import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.profile.data.Container.Type;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;
import jrm.ui.ProgressHandler;

public class AddEntry extends EntryAction
{
	private final EntityBase entity;

	public AddEntry(final EntityBase entity, final Entry entry)
	{
		super(entry);
		this.entity = entity;
	}

	@Override
	public boolean doAction(final FileSystem dstfs, final ProgressHandler handler)
	{
		final Path dstpath = dstfs.getPath(entity.getName());
		handler.setProgress(null, null, null, String.format(Messages.getString("AddEntry.Adding"), entity.getName())); //$NON-NLS-1$
		Path srcpath = null;
		try
		{
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
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
				try(Archive srcarchive = new SevenZipArchive(entry.parent.file))
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
			e.printStackTrace();
			System.err.println("add from " + entry.parent.file + "@" + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed");
		}
		return false;
	}

	@Override
	public boolean doAction(final Path target, final ProgressHandler handler)
	{
		final Path dstpath = target.resolve(entity.getName());
		handler.setProgress(null, null, null, String.format(Messages.getString("AddEntry.Adding"), entity.getName())); //$NON-NLS-1$
		Path srcpath = null;
		try
		{
			if(entry.parent.getType() == Type.ZIP)
			{
				try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
				{
					srcpath = srcfs.getPath(entry.file);
					if(dstpath.getParent() != null)
						Files.createDirectories(dstpath.getParent());
					Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
				}
				catch(final Throwable e)
				{
					System.err.println("add from " + entry.parent.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + entity.getName() + " failed");
				}

			}
			else if(entry.parent.getType() == Type.SEVENZIP)
			{

				try(Archive srcarchive = new SevenZipArchive(entry.parent.file))
				{
					if(srcarchive.extract(entry.file) != null)
					{
						srcpath = new File(srcarchive.getTempDir(), entry.file).toPath();
						if(dstpath.getParent() != null)
							Files.createDirectories(dstpath.getParent());
						Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
					}
					// return archive.add_stdin(srcarchive.extract_stdout(entry.file) , entity.getName()) == 0;
				}
				catch(final IOException e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				srcpath = entry.parent.file.toPath().resolve(entry.file);
				if(dstpath.getParent() != null)
					Files.createDirectories(dstpath.getParent());
				Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		}
		catch(final Throwable e)
		{
			e.printStackTrace();
			System.err.println("add from " + entry.parent.file.getName() + "@" + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed");
		}
		return false;
	}

	@Override
	public boolean doAction(final Archive archive, final ProgressHandler handler)
	{
		handler.setProgress(null, null, null, String.format(Messages.getString("AddEntry.Adding"), entity.getName())); //$NON-NLS-1$
		if(entry.parent.getType() == Type.ZIP)
		{
			try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
			{
				return archive.add_stdin(Files.newInputStream(srcfs.getPath(entry.file)), entity.getName()) == 0;
			}
			catch(final Throwable e)
			{
				System.err.println("add from " + entry.parent.file.getName() + "@" + entry.file + " to " + parent.container.file.getName() + "@" + entity.getName() + " failed");
			}
		}
		else if(entry.parent.getType() == Type.SEVENZIP)
		{
			try(Archive srcarchive = new SevenZipArchive(entry.parent.file))
			{
				if(srcarchive.extract(entry.file) != null)
					return archive.add(srcarchive.getTempDir(), entry.file) == 0;
				// return archive.add_stdin(srcarchive.extract_stdout(entry.file) , entity.getName()) == 0;
			}
			catch(final IOException e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("AddEntry.Add"), entry, entity); //$NON-NLS-1$
	}
}
