package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SystmDevice implements Systm, Serializable
{
	public static SystmDevice DEVICE = new SystmDevice();

	public SystmDevice()
	{
	}

	@Override
	public Type getType()
	{
		return Type.DEVICE;
	}

	@Override
	public Systm getSystem()
	{
		return DEVICE;
	}

	@Override
	public String toString()
	{
		return "["+getType()+"]";
	}

	@Override
	public String getName()
	{
		return "device";
	}
	
}
