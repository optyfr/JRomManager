package jrm.profile.report;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Stream;

import jrm.locale.Messages;
import jrm.profile.data.AnywareBase;
import jrm.profile.data.Container;

/**
 * Subject about an unknown container found
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class ContainerUnneeded extends ContainerSubject implements Serializable
{
	/**
	 * Constructor with no related {@link AnywareBase} (set to <code>null</code>), but a related {@link Container}
	 * @param c the {@link Container} in relation
	 */
	public ContainerUnneeded(final Container c)
	{
		super(c);
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("ContainerUnneeded.Unneeded"), container.getType() == Container.Type.DIR ? Messages.getString("ContainerUnneeded.Directory") : Messages.getString("ContainerUnneeded.File"), container.getRelFile()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public Subject clone(final Set<FilterOptions> filterOptions)
	{
		return new ContainerUnneeded(container);
	}
	
	@Override
	public Stream<Note> stream(Set<FilterOptions> filterOptions)
	{
		return notes.stream();
	}
}
