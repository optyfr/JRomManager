package jrm.profiler.data;

import java.io.Serializable;

import jrm.profiler.scan.HashCollisionOptions;

@SuppressWarnings("serial")
public abstract class Entity implements Serializable
{
	protected String name;
	
	protected Machine parent;

	private transient boolean collision = false;
	
	public Entity(Machine parent)
	{
		this.parent = parent;
	}

	public void setCollisionMode()
	{
		if(Machine.hash_collision_mode==HashCollisionOptions.SINGLECLONE)
			parent.setCollisionMode(false);
		else if(Machine.hash_collision_mode==HashCollisionOptions.ALLCLONES)
			parent.setCollisionMode(true);
		collision = true;
	}
	
	public boolean isCollisionMode()
	{
		if(Machine.hash_collision_mode==HashCollisionOptions.SINGLECLONE)
			return parent.isCollisionMode();
		else if(Machine.hash_collision_mode==HashCollisionOptions.ALLCLONES)
			return parent.isCollisionMode();		
		else if(Machine.hash_collision_mode==HashCollisionOptions.DUMB)
			return true;
		return collision;
	}

	void resetCollisionMode()
	{
		collision = false;
	}
	

	public Machine getParent()
	{
		return parent;
	}
	
	public abstract String getName();
	public abstract void setName(String name);
	
	@Override
	public String toString()
	{
		return getName();
	}
}
