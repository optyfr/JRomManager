package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class AnywareBase extends NameBase implements Serializable
{
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

	public abstract String getFullName();
	public abstract String getFullName(final String filename);
	public abstract CharSequence getDescription();
	public abstract AnywareStatus getStatus();


}
