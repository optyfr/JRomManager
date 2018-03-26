package actions;

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
