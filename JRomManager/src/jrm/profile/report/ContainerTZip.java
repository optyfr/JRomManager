package jrm.profile.report;

import java.io.Serializable;
import java.util.List;

import jrm.locale.Messages;
import jrm.profile.data.AnywareBase;
import jrm.profile.data.Container;
/**
 * Subject about container that need to be torrentzipped
 * @author optyfr
 *
 */
public class ContainerTZip extends ContainerSubject implements Serializable
{
	private static final long serialVersionUID = 2L;

	/**
	 * Constructor with no related {@link AnywareBase} (set to <code>null</code>), but a related {@link Container}
	 * @param c the {@link Container} in relation
	 */
	public ContainerTZip(final Container c)
	{
		super(c);
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("ContainerTZip.NeedTZip"), container.getRelFile()); //$NON-NLS-1$
	}

	@Override
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		return new ContainerTZip(container);
	}
}
