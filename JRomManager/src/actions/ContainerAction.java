package actions;

import java.util.ArrayList;

import data.Container;
import ui.ProgressHandler;

abstract public class ContainerAction
{
	public Container container;
	public ArrayList<EntryAction> entry_actions = new ArrayList<>();
	
	public ContainerAction(Container container)
	{
		this.container = container;
	}

	public void addAction(EntryAction entryAction)
	{
		entry_actions.add(entryAction);
		entryAction.parent = this;
	}
	
	public abstract boolean doAction(ProgressHandler handler);
}
