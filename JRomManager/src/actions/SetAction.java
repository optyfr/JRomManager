package actions;

import java.util.ArrayList;

import data.Container;
import ui.ProgressHandler;

abstract public class SetAction
{
	public Container container;
	public ArrayList<RomAction> roms = new ArrayList<>();
	
	public SetAction(Container container)
	{
		this.container = container;
	}

	public void addRomAction(RomAction action)
	{
		roms.add(action);
		action.parent = this;
	}
	
	public abstract boolean doAction(ProgressHandler handler);
}
