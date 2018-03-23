package actions;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import data.Entry;
import data.Rom;
import ui.ProgressHandler;

public class RenameRom extends RomAction
{
	Rom rom;

	public RenameRom(Rom rom, Entry entry)
	{
		super(entry);
		this.rom = rom;
	}

	@Override
	public boolean doAction(FileSystem fs, ProgressHandler handler)
	{
		Path dstpath = fs.getPath(rom.name);
		try
		{
			handler.setProgress(null,null,null,"Renaming "+entry.file+" to "+rom.name);
			Path srcpath = fs.getPath(entry.file);
			Files.move(srcpath, dstpath);
		}
		catch(Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
