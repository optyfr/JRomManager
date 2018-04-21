package jrm.profile.data;

public class SystemDevice implements System
{
	public static SystemDevice DEVICE = new SystemDevice();

	public SystemDevice()
	{
	}

	@Override
	public Type getType()
	{
		return Type.DEVICE;
	}

	@Override
	public System getSystem()
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
