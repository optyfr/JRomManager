package jrm.profile.filter;

import jrm.profile.data.PropertyStub;

/**
 * nplayer mode which list games compatible with that mode
 * 
 * @author optyfr
 */
@SuppressWarnings("serial")
public final class NPlayer extends GamesList implements PropertyStub
{
	/**
	 * the name of the nplayer mode
	 */
	public final String name;
	
	/**
	 * The NPlayer constructor
	 * 
	 * @param name
	 *            the mode name
	 */
	public NPlayer(final String name)
	{
		this.name = name;
	}

	@Override
	public String toString()
	{
		return name + " (" + games.size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getPropertyName()
	{
		return "filter.nplayer." + name; //$NON-NLS-1$
	}
}
