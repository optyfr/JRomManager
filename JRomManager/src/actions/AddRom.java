package actions;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import data.Entry;
import data.Rom;
import ui.ProgressHandler;

public class AddRom extends RomAction
{
	Rom rom;

	public AddRom(Rom rom, Entry entry)
	{
		super(entry);
		this.rom = rom;
	}

	@Override
	public boolean doAction(FileSystem dstfs, ProgressHandler handler)
	{
		Path dstpath = dstfs.getPath(rom.name);
		handler.setProgress(null,null,null,"Adding "+dstpath.getFileName());
		try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
		{
			
			Path srcpath = srcfs.getPath(entry.file);
			Files.copy(srcpath, dstpath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		}
		catch(Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
