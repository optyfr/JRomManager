package actions;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashMap;
import java.util.Map;

import data.Container;
import ui.ProgressHandler;

public class CreateContainer extends ContainerAction
{

	public CreateContainer(Container container)
	{
		super(container);
	}

	public static CreateContainer getInstance(CreateContainer action, Container container)
	{
		if(action == null)
			action = new CreateContainer(container);
		return action;
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		if(container.getType()==Container.Type.ZIP)
		{
			Map<String,Object> env = new HashMap<>();
			env.put("create", "true");
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(container.getType()==Container.Type.DIR)
		{
			try
			{
				Path target = container.file.toPath();
				Files.createDirectories(target, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x")));
				handler.setProgress("Fixing "+container.file.getName());
				for(EntryAction action : entry_actions)
					if(!action.doAction(target, handler))
					{
						System.err.println("action to "+container.file.getName()+"@"+action.entry.file+" failed");
						return false;
					}
				return true;
			}
			catch (Throwable e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

}
