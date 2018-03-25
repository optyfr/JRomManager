package actions;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import data.Container;
import ui.ProgressHandler;

public class OpenSet extends SetAction
{

	public OpenSet(Container container)
	{
		super(container);
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		Map<String,Object> env = new HashMap<>();
		env.put("create", "false");
		env.put("useTempFile", Boolean.TRUE);
		try(FileSystem fs = FileSystems.newFileSystem(URI.create("jar:"+container.file.toURI()), env);)
		{
			handler.setProgress("Fixing "+container.file.getName());
			for(RomAction rom : roms)
				if(!rom.doAction(fs, handler))
					return false;
			return true;
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return false;
	}

}
