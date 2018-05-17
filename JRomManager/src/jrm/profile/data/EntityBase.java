package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class EntityBase implements Serializable, Comparable<Entity>
{
	protected String name; // required
	protected EntityStatus own_status = EntityStatus.UNKNOWN;
	protected final AnywareBase parent;

	public EntityBase(AnywareBase parent)
	{
		this.parent = parent;
	}

	public abstract String getName();

	public String getOriginalName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}
	public abstract EntityStatus getStatus();
	
	public void setStatus(EntityStatus status)
	{
		this.own_status = status;
	}

	public <T extends AnywareBase> T getParent(final Class<T> type)
	{
		return type.cast(parent);
	}

	public abstract AnywareBase getParent();

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public int compareTo(final Entity o)
	{
		return name.compareTo(o.name);
	}

}
