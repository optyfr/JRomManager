package jrm.profile.report;

import java.util.List;

import jrm.profile.data.Container;

public class ContainerTZip extends Subject
{
	final Container container;

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
