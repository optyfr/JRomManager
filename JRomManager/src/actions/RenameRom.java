package actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import data.Entry;
import ui.ProgressHandler;

public class RenameRom extends RomAction
{
	String newname;

	public RenameRom(Entry entry)
	{
		super(entry);
		this.newname =  UUID.randomUUID()+"_"+entry.size+".tmp";
	}

	public RenameRom(String newname, Entry entry)
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
			Files.move(srcpath, dstpath);
			entry.file = dstpath.toString();
			//System.out.println("rename "+parent.container.file.getName()+"@"+srcpath+" to "+parent.container.file.getName()+"@"+dstpath);
			return true;
		}
		catch(Throwable e)
		{
			System.err.println("rename "+parent.container.file.getName()+"@"+entry.file+" to "+parent.container.file.getName()+"@"+newname);
		}
		return false;
	}

}
