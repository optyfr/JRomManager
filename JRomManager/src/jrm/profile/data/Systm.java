package jrm.profile.data;

import java.io.Serializable;

public interface Systm extends Serializable, PropertyStub
{
	public enum Type
	{
		STANDARD,
		MECHANICAL,
		DEVICE,
		BIOS,
		SOFTWARELIST
	}

	public Type getType();

	public Systm getSystem();

	public String getName();

	@Override
	public default String getPropertyName()
	{
		return "filter.systems." + getName();
	}
}
