package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Machine extends Anyware implements Serializable
{
	public String romof = null;
	public String sampleof = null;
	public boolean isbios = false;
	public boolean ismechanical = false;
	public boolean isdevice = false;

	public Machine()
	{
	}

	@Override
	public Machine getParent()
	{
		return getParent(Machine.class);
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getFullName()
	{
		return name;
	}

	@Override
	public String getFullName(String filename)
	{
		return filename;
	}

	@Override
	public boolean isBios()
	{
		return isbios;
	}

	@Override
	public boolean isRomOf()
	{
		return romof != null;
	}

	@Override
	public Type getType()
	{
		if(parent!=null)
			return ((Machine)parent).getType();
		if(isbios)
			return Type.BIOS;
		if(ismechanical)
			return Type.MECHANICAL;
		if(isdevice)
			return Type.DEVICE;
		return Type.STANDARD;
	}


	@Override
	public System getSystem()
	{
		switch(getType())
		{
			case BIOS:
				if(parent!=null)
					return parent.getSystem();
				return this;
			case DEVICE:
				return SystemDevice.DEVICE;
			case MECHANICAL:
				return SystemMechanical.MECHANICAL;
			case STANDARD:
			default:
				return SystemStandard.STANDARD;
		}
	}

	@Override
	public String toString()
	{
		return "["+getType()+"] "+description.toString();
	}
}
