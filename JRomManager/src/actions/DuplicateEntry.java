package actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import data.Entry;
import ui.ProgressHandler;

public class DuplicateEntry extends EntryAction
{
	String newname;

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
			handler.setProgress(null,null,null,"Renaming "+entry.file+" to "+newname);
			Path srcpath = fs.getPath(entry.file);
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("duplicate "+parent.container.file.getName()+"@"+entry.file+" to "+parent.container.file.getName()+"@"+newname+" failed");
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
			handler.setProgress(null,null,null,"Renaming "+entry.file+" to "+newname);
			Path srcpath = target.resolve(entry.file);
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("duplicate "+parent.container.file.getName()+"@"+entry.file+" to "+parent.container.file.getName()+"@"+newname+" failed");
		}
		return false;
	}

}
