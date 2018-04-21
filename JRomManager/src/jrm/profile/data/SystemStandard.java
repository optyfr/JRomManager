package jrm.profile.data;

public class SystemStandard implements System
{
	public static SystemStandard STANDARD = new SystemStandard();
	
	public SystemStandard()
	{
	}

	@Override
	public Type getType()
	{
		return Type.STANDARD;
	}

	@Override
	public System getSystem()
	{
		return STANDARD;
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
