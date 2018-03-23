package actions;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import data.Container;
import ui.ProgressHandler;

public class CreateSet extends SetAction
{

	public CreateSet(Container container)
	{
		super(container);
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		Map<String,?> env = new HashMap<>(Collections.singletonMap("create", "true"));
		try(FileSystem fs = FileSystems.newFileSystem(URI.create("jar:"+container.file.toURI()), env);)
		{
			handler.setProgress("Fixing "+container.file.getName());
			for(RomAction rom : roms)
				rom.doAction(fs, handler);
		}
		catch(Throwable e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
