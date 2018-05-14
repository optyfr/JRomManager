package jrm.profile.fix.actions;

import java.util.ArrayList;
import java.util.List;

import jrm.misc.HTMLRenderer;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

abstract public class ContainerAction implements HTMLRenderer
{
	public final Container container;
	public final FormatOptions format;
	public final ArrayList<EntryAction> entry_actions = new ArrayList<>();

	public ContainerAction(Container container, FormatOptions format)
	{
		this.container = container;
		this.format = format;
	}

	public void addAction(EntryAction entryAction)
	{
		entry_actions.add(entryAction);
		entryAction.parent = this;
	}

	public static void addToList(List<ContainerAction> list, ContainerAction action)
	{
		if(action != null && action.entry_actions.size() > 0)
			list.add(action);
	}

	public abstract boolean doAction(ProgressHandler handler);

}
