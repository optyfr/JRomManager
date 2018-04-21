package jrm.profile.data;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class Systems extends AbstractListModel<System>
{
	ArrayList<System> systems = new ArrayList<>();

	public Systems()
	{
	}

	@Override
	public int getSize()
	{
		return systems.size();
	}

	@Override
	public System getElementAt(int index)
	{
		return systems.get(index);
	}

	public boolean add(System system)
	{
		return systems.add(system);
	}
}
