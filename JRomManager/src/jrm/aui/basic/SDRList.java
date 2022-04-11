package jrm.aui.basic;

import java.util.ArrayList;

import lombok.Getter;

@SuppressWarnings("serial")
public class SDRList<T> extends ArrayList<T>
{
	@Getter boolean needSave = false;
	
	public SDRList()
	{
		super();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return super.equals(o);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}