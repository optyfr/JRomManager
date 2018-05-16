package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SystmStandard implements Systm, Serializable
{
	public final static SystmStandard STANDARD = new SystmStandard();

	public SystmStandard()
	{
	}

	@Override
	public Type getType()
	{
		return Type.STANDARD;
	}

	@Override
	public Systm getSystem()
	{
		return SystmStandard.STANDARD;
	}

	@Override
	public String toString()
	{
		return "["+getType()+"]";
	}

	@Override
	public String getName()
	{
		return "standard";
	}
}
