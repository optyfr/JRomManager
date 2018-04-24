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
	public StringBuffer manufacturer = new StringBuffer();
	public Driver driver = new Driver();
	public Input input = new Input();
	public DisplayOrientation orientation = DisplayOrientation.any;
	public CabinetType cabinetType = CabinetType.upright;
	
	public enum DisplayOrientation
	{
		any,
		horizontal,
		vertical;
	}

	public enum CabinetType
	{
		any,
		upright,
		cocktail;
	}

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
	public Systm getSystem()
	{
		switch(getType())
		{
			case BIOS:
				if(parent!=null)
					return parent.getSystem();
				return this;
			case DEVICE:
				return SystmDevice.DEVICE;
			case MECHANICAL:
				return SystmMechanical.MECHANICAL;
			case STANDARD:
			default:
				return SystmStandard.STANDARD;
		}
	}

	@Override
	public String toString()
	{
		return "["+getType()+"] "+description.toString();
	}
}
