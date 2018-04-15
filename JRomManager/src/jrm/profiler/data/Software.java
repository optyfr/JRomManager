package jrm.profiler.data;

import java.io.File;
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
	public Software getParent()
	{
		return getParent(Software.class);
	}

	@Override
	public String getName()
	{
		return name;
	}
	
	@Override
	public String getFullName()
	{
		return list.name + File.separator + name;
	}
	
	@Override
	public String getFullName(String filename)
	{
		return list.name + File.separator + filename;
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
