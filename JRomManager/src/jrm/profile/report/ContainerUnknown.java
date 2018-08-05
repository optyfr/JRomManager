package jrm.profile.report;

import java.util.List;

import jrm.Messages;
import jrm.profile.data.AnywareBase;
import jrm.profile.data.Container;

/**
 * Subject about an unknown container found
 * @author optyfr
 *
 */
public class ContainerUnknown extends Subject
{
	/**
	 * The {@link Container} in relation
	 */
	final Container container;

	/**
	 * Constructor with no related {@link AnywareBase} (set to <code>null</code>), but a related {@link Container}
	 * @param c the {@link Container} in relation
	 */
	public ContainerUnknown(final Container c)
	{
		super(null);
		container = c;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("ContainerUnknown.Unknown"), container.getType() == Container.Type.DIR ? Messages.getString("ContainerUnknown.Directory") : Messages.getString("ContainerUnknown.File"), container.file); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		return new ContainerUnknown(container);
	}

	@Override
	public void updateStats()
	{
	}

}
