package jrm.profiler.report;

import jrm.profiler.data.Container;

public class ContainerUnneeded extends Note
{
	private Container container;

	public ContainerUnneeded(Container container)
	{
		this.container = container;
	}

	@Override
	public String toString()
	{
		return "[" + parent.machine.name + "] " + container.file.getName() + " is unneeded";
	}

}
