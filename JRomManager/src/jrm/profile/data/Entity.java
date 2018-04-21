package jrm.profile.data;

import java.io.Serializable;

import jrm.profile.scan.options.HashCollisionOptions;

@SuppressWarnings("serial")
public abstract class Entity implements Serializable,Comparable<Entity>
{
	protected String name;	// required
	public long size = 0;
	public String crc = null;
	public String sha1 = null;
	public String md5 = null;
	public Status status = Status.good;
	public EntityStatus own_status = EntityStatus.UNKNOWN;

	public enum Status implements Serializable
	{
		baddump,
		nodump,
		good
	}

	protected Anyware parent;

	private transient boolean collision = false;

	public Entity(Anyware parent)
	{
		this.parent = parent;
	}

	public void setCollisionMode()
	{
		if(Machine.hash_collision_mode == HashCollisionOptions.SINGLECLONE)
			parent.setCollisionMode(false);
		else if(Machine.hash_collision_mode == HashCollisionOptions.ALLCLONES)
			parent.setCollisionMode(true);
		collision = true;
	}

	public boolean isCollisionMode()
	{
		if(Machine.hash_collision_mode == HashCollisionOptions.SINGLECLONE)
			return parent.isCollisionMode();
		else if(Machine.hash_collision_mode == HashCollisionOptions.ALLCLONES)
			return parent.isCollisionMode();
		else if(Machine.hash_collision_mode == HashCollisionOptions.DUMB)
			return true;
		return collision;
	}

	void resetCollisionMode()
	{
		collision = false;
	}

	public Anyware getParent()
	{
		return parent;
	}

	public abstract String getName();

	public abstract void setName(String name);
	
	public String getCRC()
	{
		return crc;
	}

	public String getMD5()
	{
		return md5;
	}

	public String getSHA1()
	{
		return sha1;
	}
	
	public long getSize()
	{
		return size;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public int compareTo(Entity o)
	{
		return this.name.compareTo(o.name);
	}
	
	public abstract EntityStatus getStatus();
}