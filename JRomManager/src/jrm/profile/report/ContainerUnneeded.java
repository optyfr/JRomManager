package jrm.profile.report;

import java.io.Serializable;
import java.util.List;

import jrm.locale.Messages;
import jrm.profile.data.AnywareBase;
import jrm.profile.data.Container;

/**
 * Subject about an unknown container found
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class ContainerUnneeded extends Subject implements Serializable
{
	/**
	 * The {@link Container} in relation
	 */
	final Container container;

	/**
	 * Constructor with no related {@link AnywareBase} (set to <code>null</code>), but a related {@link Container}
	 * @param c the {@link Container} in relation
	 */
	public ContainerUnneeded(final Container c)
	{
		super(null);
		container = c;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("ContainerUnneeded.Unneeded"), container.getType() == Container.Type.DIR ? Messages.getString("ContainerUnneeded.Directory") : Messages.getString("ContainerUnneeded.File"), container.file); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String getHTML()
	{
		return toString();
	}
	
	@Override
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		return new ContainerUnneeded(container);
	}

	@Override
	public void updateStats()
	{
	}

}
