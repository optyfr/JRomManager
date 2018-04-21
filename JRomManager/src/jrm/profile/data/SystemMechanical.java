package jrm.profile.data;

public class SystemMechanical implements System
{
	public static SystemMechanical MECHANICAL = new SystemMechanical();

	public SystemMechanical()
	{
	}

	@Override
	public Type getType()
	{
		return Type.MECHANICAL;
	}

	@Override
	public System getSystem()
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
