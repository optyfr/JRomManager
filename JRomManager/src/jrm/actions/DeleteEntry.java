package jrm.actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import jrm.data.Entry;
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
			handler.setProgress(null,null,null,"Deleting "+entry.file);
			path = dstfs.getPath(entry.file);
			Files.delete(path);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("delete "+parent.container.file.getName()+"@"+path+" failed");
		}
		return false;
	}

	@Override
	public boolean doAction(Path target, ProgressHandler handler)
	{
		Path path = null;
		try
		{
			handler.setProgress(null,null,null,"Deleting "+entry.file);
			path = target.resolve(entry.file);
			Files.delete(path);
			return true;
		}
		catch (Throwable e)
		{
			System.err.println("delete "+parent.container.file.getName()+"@"+path+" failed");
		}
		// TODO Auto-generated method stub
		return false;
	}

}
