package jrm.profile.report;

import java.util.List;

import jrm.Messages;
import jrm.profile.data.Container;

public class ContainerUnknown extends Subject
{
	final Container container;
	
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

	@Override
	public void updateStats()
	{
		// TODO Auto-generated method stub
		
	}

}
