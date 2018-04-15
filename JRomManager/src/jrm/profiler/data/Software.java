package jrm.profiler.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Software extends Anyware implements Serializable
{
	public Supported supported = Supported.yes;

	SoftwareList list = null;
	
	public enum Supported implements Serializable
	{
		yes,
		partial,
		no;
	};
	
	public Software()
	{
	}

	@Override
	public void setCollisionMode(boolean parent)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCollisionMode()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClone()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Software getParent()
	{
		return getParent(Software.class);
	}

	@Override
	public boolean isBios()
	{
		return false;
	}

	@Override
	public boolean isRomOf()
	{
		return false;
	}

}
