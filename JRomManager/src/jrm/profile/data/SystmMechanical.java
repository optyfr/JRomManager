package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SystmMechanical implements Systm, Serializable
{
	public static SystmMechanical MECHANICAL = new SystmMechanical();

	public SystmMechanical()
	{
	}

	@Override
	public Type getType()
	{
		return Type.MECHANICAL;
	}

	@Override
	public Systm getSystem()
	{
		return MECHANICAL;
	}

	@Override
	public String toString()
	{
		return "["+getType()+"]";
	}

	@Override
	public String getName()
	{
		return "mechanical";
	}
}
