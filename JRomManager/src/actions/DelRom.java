package actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import data.Entry;
import ui.ProgressHandler;

public class DelRom extends RomAction
{
	public DelRom(Entry entry)
	{
		super(entry);
	}

	@Override
	public boolean doAction(FileSystem dstfs, ProgressHandler handler)
	{
		try
		{
			handler.setProgress(null,null,null,"Deleting "+entry.file);
			Path path = dstfs.getPath(entry.file);
			Files.delete(path);
		}
		catch(Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
