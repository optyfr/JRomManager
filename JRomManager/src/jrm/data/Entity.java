package jrm.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class Entity implements Serializable
{
	protected String name;
	
	protected Machine parent;

	public Entity(Machine parent)
	{
		this.parent = parent;
	}

	public abstract String getName();
	public abstract void setName(String name);
}
