package jrm.profile.report;

import jrm.profile.data.Container;
import lombok.Getter;

abstract class ContainerSubject extends Subject
{
	private static final long serialVersionUID = 1L;
	/**
	 * The {@link Container} in relation
	 */
	protected @Getter final Container container;

	protected ContainerSubject(Container container)
	{
		super(null);
		this.container = container;
	}

	@Override
	public String getHTML()
	{
		return toString();
	}

	@Override
	public boolean equals(Object o)
	{
		return super.equals(o);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
	@Override
	public void updateStats()
	{
		// do nothing
	}

}
