package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
abstract class NameBase implements Serializable, Comparable<NameBase>
{
	protected String name = ""; // required //$NON-NLS-1$

	public abstract String getName();

	public final String getBaseName()
	{
		return name;
	}

	public final void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public final int compareTo(final NameBase o)
	{
		return name.compareTo(o.name);
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
