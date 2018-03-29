package data;

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class Entity implements Serializable
{
	protected String name;

	public Entity()
	{
		// TODO Auto-generated constructor stub
	}

	public abstract String getName();
	public abstract void setName(String name);
}
