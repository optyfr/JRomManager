package jrm.profile.data;

import java.io.Serializable;

import jrm.profile.scan.options.HashCollisionOptions;

@SuppressWarnings("serial")
public abstract class Entity extends EntityBase implements Serializable
{
	public long size = 0;
	public String crc = null;
	public String sha1 = null;
	public String md5 = null;
	public String merge = null;
	public Status status = Status.good;

	public enum Status implements Serializable
	{
		baddump,
		nodump,
		good,
		verified;

		public Status getXML(final boolean is_mame)
		{
			return (Status.good == this || (is_mame && Status.verified == this)) ? null : this;
		}
	}


	private transient boolean collision = false;

	public Entity(final Anyware parent)
	{
		super(parent);
	}

	public void setCollisionMode()
	{
		if(Anyware.hash_collision_mode == HashCollisionOptions.SINGLECLONE)
			getParent().setCollisionMode(false);
		else if(Anyware.hash_collision_mode == HashCollisionOptions.ALLCLONES)
			getParent().setCollisionMode(true);
		collision = true;
	}

	public boolean isCollisionMode(boolean dumber)
	{
		if(Anyware.hash_collision_mode == HashCollisionOptions.SINGLECLONE)
			return getParent().isCollisionMode();
		else if(Anyware.hash_collision_mode == HashCollisionOptions.ALLCLONES)
			return getParent().isCollisionMode();
		else if(dumber)
		{
			if(Anyware.hash_collision_mode == HashCollisionOptions.DUMBER)
				return true;
		}
		else 
		{
			if(Anyware.hash_collision_mode == HashCollisionOptions.DUMB)
				return true;
			else if(Anyware.hash_collision_mode == HashCollisionOptions.DUMBER)
				return true;
		}
		return collision;
	}

	void resetCollisionMode()
	{
		collision = false;
		own_status = EntityStatus.UNKNOWN;
	}

	@Override
	public Anyware getParent()
	{
		return getParent(Anyware.class);
	}

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
}
