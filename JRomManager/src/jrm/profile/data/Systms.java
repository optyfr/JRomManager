package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.AbstractListModel;

/**
 * ListModel of systems
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Systms extends AbstractListModel<Systm> implements Serializable
{
	/**
	 * The internal {@link ArrayList} of {@link Systm}s
	 */
	private final ArrayList<Systm> systems = new ArrayList<>();

	@Override
	public int getSize()
	{
		return systems.size();
	}

	@Override
	public Systm getElementAt(final int index)
	{
		return systems.get(index);
	}

	/**
	 * add a {@link Systm} to the list
	 * @param system the {@link Systm} to add
	 * @return return true if successful
	 */
	public boolean add(final Systm system)
	{
		return systems.add(system);
	}
}
