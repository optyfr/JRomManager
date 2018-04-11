package jrm.profiler.report;

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
}
