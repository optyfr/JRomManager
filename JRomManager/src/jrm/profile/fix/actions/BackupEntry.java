package jrm.profile.fix.actions;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jrm.compressors.Archive;
import jrm.compressors.SevenZipArchive;
import jrm.profile.data.Container.Type;
import jrm.profile.data.Entry;
import jrm.ui.ProgressHandler;

public class BackupEntry extends EntryAction
{
	public BackupEntry(final Entry entry)
	{
		super(entry);
	}

	@Override
	public boolean doAction(final FileSystem dstfs, final ProgressHandler handler, int i, int max)
	{
		Path dstpath_crc = dstfs.getPath(entry.crc+'_'+entry.size);
		Path dstpath = dstfs.getPath(entry.sha1!=null?entry.sha1:(entry.md5!=null?entry.md5:(entry.crc+'_'+entry.size)));
		handler.setProgress(null, null, null, progress(i, max, String.format("Backup of %s", entry.getName()))); //$NON-NLS-1$
		Path srcpath = null;
		try
		{
			if(dstpath.getParent() != null)
				Files.createDirectories(dstpath.getParent());
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
			System.err.println("add from " + entry.parent.file + "@" + srcpath + " to " + parent.container.file.getName() + "@" + dstpath + " failed"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return false;
	}

	@Override
	public boolean doAction(Archive archive, ProgressHandler handler, int i, int max)
	{
		return false;
	}

	@Override
	public boolean doAction(Path target, ProgressHandler handler, int i, int max)
	{
		return false;
	}

	@Override
	public String toString()
	{
		return String.format("Backup of %s", entry); //$NON-NLS-1$
	}
}
