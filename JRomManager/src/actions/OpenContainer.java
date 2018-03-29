package actions;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import data.Container;
import ui.ProgressHandler;

public class OpenContainer extends ContainerAction
{

	public OpenContainer(Container container)
	{
		super(container);
	}
	
	public static OpenContainer getInstance(OpenContainer action, Container container)
	{
		if(action == null)
			action = new OpenContainer(container);
		return action;
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		if(container.getType()==Container.Type.ZIP)
		{
			Map<String,Object> env = new HashMap<>();
			env.put("create", "false");
			env.put("useTempFile", Boolean.TRUE);
			try(FileSystem fs = FileSystems.newFileSystem(URI.create("jar:"+container.file.toURI()), env);)
			{
				handler.setProgress("Fixing "+container.file.getName());
				for(EntryAction action : entry_actions)
					if(!action.doAction(fs, handler))
					{
						System.err.println("action to "+container.file.getName()+"@"+action.entry.file+" failed");
						return false;
					}
				return true;
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		else if(container.getType()==Container.Type.DIR)
		{
			Path target = container.file.toPath();
			handler.setProgress("Fixing "+container.file.getName());
			for(EntryAction action : entry_actions)
				if(!action.doAction(target, handler))
				{
					System.err.println("action to "+container.file.getName()+"@"+action.entry.file+" failed");
					return false;
				}
			return true;
		}
		return false;
	}

}
