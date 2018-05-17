package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AnywareBase implements Serializable, Comparable<Anyware>
{
	public String name; // required
	public AnywareBase parent = null;

	public AnywareBase()
	{
	}

	public <T extends AnywareBase> T getParent(final Class<T> type)
	{
		return type.cast(parent);
	}
	
	public <T extends AnywareBase> void setParent(T parent)
	{
		this.parent = parent;
	}

	public abstract AnywareBase getParent();

	public abstract String getName();
	public abstract String getFullName();
	public abstract String getFullName(final String filename);
	public abstract CharSequence getDescription();

	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public int compareTo(final Anyware o)
	{
		return name.compareTo(o.name);
	}

}
