package actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import data.Entry;
import ui.ProgressHandler;

public class DeleteRom extends RomAction
{
	public DeleteRom(Entry entry)
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

}
