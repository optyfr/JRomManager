package jrm.profile.report;

import java.util.List;

import jrm.profile.data.AnywareBase;
import jrm.profile.data.Container;
/**
 * Subject about container that need to be torrentzipped
 * @author optyfr
 *
 */
public class ContainerTZip extends Subject
{
	/**
	 * The {@link Container} in relation
	 */
	final Container container;

	/**
	 * Constructor with no related {@link AnywareBase} (set to <code>null</code>), but a related {@link Container}
	 * @param c the {@link Container} in relation
	 */
	public ContainerTZip(final Container c)
	{
		super(null);
		container = c;
	}

	@Override
	public String toString()
	{
		return String.format("File %s need to be torrentzipped", container.file);
	}

	@Override
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		return new ContainerTZip(container);
	}

	@Override
	public void updateStats()
	{
	}

}
