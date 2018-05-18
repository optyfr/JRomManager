package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SystmDevice implements Systm, Serializable
{
	public final static SystmDevice DEVICE = new SystmDevice();

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
		return SystmDevice.DEVICE;
	}

	@Override
	public String toString()
	{
		return "["+getType()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getName()
	{
		return "device"; //$NON-NLS-1$
	}

}
