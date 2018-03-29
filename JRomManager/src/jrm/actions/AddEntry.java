package jrm.actions;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jrm.data.Entity;
import jrm.data.Entry;
import jrm.ui.ProgressHandler;

public class AddEntry extends EntryAction
{
	private Entity entity;

	public AddEntry(Entity entity, Entry entry)
	{
		super(entry);
		this.entity = entity;
	}

	@Override
	public boolean doAction(FileSystem dstfs, ProgressHandler handler)
	{
		Path dstpath = dstfs.getPath(entity.getName());
		handler.setProgress(null,null,null,"Adding "+dstpath.getFileName());
		Path srcpath = null;
		try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
		{
			
			srcpath = srcfs.getPath(entry.file);
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("add from "+entry.parent.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath+" failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Path target, ProgressHandler handler)
	{
		Path dstpath = target.resolve(entity.getName());
		handler.setProgress(null,null,null,"Adding "+dstpath.getFileName());
		Path srcpath = null;
		try
		{
			srcpath = entry.parent.file.toPath().resolve(entry.file);
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			return true;
		}
		catch (Throwable e)
		{
			System.err.println("add from "+entry.parent.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath+" failed");
		}
		return false;
	}

}
