package actions;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

import data.Container;
import ui.ProgressHandler;

public class DeleteContainer extends ContainerAction
{

	public DeleteContainer(Container container)
	{
		super(container);
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		if(container.getType()==Container.Type.ZIP)
			return container.file.delete();
		else if(container.getType()==Container.Type.DIR)
		{
			try
			{
				FileUtils.deleteDirectory(container.file);
				return true;
			}
			catch (IOException e)
			{
				System.err.println("failed to delete "+container.file.getName());
				return false;
			}
		}
		return false;
	}

}
