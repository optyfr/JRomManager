package actions;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;

import data.Container;
import ui.ProgressHandler;

public class DeleteSet extends SetAction
{

	public DeleteSet(Container container)
	{
		super(container);
	}

	@Override
	public boolean doAction(ProgressHandler handler)
	{
		return container.file.delete();
	}

}
