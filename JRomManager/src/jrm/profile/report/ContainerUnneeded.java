package jrm.profile.report;

import java.util.List;

import jrm.Messages;
import jrm.profile.data.Container;

public class ContainerUnneeded extends Subject
{
	final Container container;
	
	public ContainerUnneeded(Container c)
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
	public Subject clone(List<FilterOptions> filterOptions)
	{
		return new ContainerUnneeded(container);
	}

	@Override
	public void updateStats()
	{
	}

}
