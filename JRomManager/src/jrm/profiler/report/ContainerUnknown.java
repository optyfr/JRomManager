package jrm.profiler.report;

import java.util.List;

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
		return "Unknown " + (container.getType() == Container.Type.DIR ? "Directory" : "File") + " : " + container.file;
	}

	@Override
	public Subject clone(List<FilterOptions> filterOptions)
	{
		return new ContainerUnknown(container);
	}

}
