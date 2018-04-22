package jrm.profile.data;

import java.io.File;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Software extends Anyware implements Serializable
{
	public Supported supported = Supported.yes;

	SoftwareList sl = null;
	
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
		return sl.name + File.separator + name;
	}
	
	@Override
	public String getFullName(String filename)
	{
		return sl.name + File.separator + filename;
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

	@Override
	public Type getType()
	{
		return Type.SOFTWARELIST;
	}

	@Override
	public Systm getSystem()
	{
		return sl;
	}
}
