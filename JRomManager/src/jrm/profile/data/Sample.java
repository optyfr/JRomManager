package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class Sample extends EntityBase implements Serializable
{
	public Sample(AnywareBase parent, String name)
	{
		super(parent);
		setName(name);
	}

	@Override
	public String getName()
	{
		return name + ".wav";
	}

	@Override
	public EntityStatus getStatus()
	{
		return own_status;
	}

	@Override
	public boolean equals(Object obj)
	{
		return this.toString().equals(obj.toString());
	}

	@Override
	public AnywareBase getParent()
	{
		return parent;
	}

}
