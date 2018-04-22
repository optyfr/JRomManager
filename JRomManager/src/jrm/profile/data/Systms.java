package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class Systms extends AbstractListModel<Systm> implements Serializable
{
	ArrayList<Systm> systems = new ArrayList<>();

	public Systms()
	{
	}

	@Override
	public int getSize()
	{
		return systems.size();
	}

	@Override
	public Systm getElementAt(int index)
	{
		return systems.get(index);
	}

	public boolean add(Systm system)
	{
		return systems.add(system);
	}
}
