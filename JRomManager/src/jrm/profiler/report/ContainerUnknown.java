package jrm.profiler.report;

import java.util.List;

import jrm.Messages;
import jrm.profiler.data.Container;

public class ContainerUnknown extends Subject
{
	Container container;
	
	public ContainerUnknown(Container c)
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
	public Subject clone(List<FilterOptions> filterOptions)
	{
		return new ContainerUnknown(container);
	}

}
