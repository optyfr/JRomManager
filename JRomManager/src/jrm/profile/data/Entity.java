package jrm.profile.data;

import java.io.Serializable;

import jrm.profile.scan.options.HashCollisionOptions;

/**
 * the common class for {@link Rom} and {@link Disk}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public abstract class Entity extends EntityBase implements Serializable
{
	/**
	 * the date size in bytes, 0 by default (always 0 for disks)
	 */
	public long size = 0;
	/**
	 * the crc value as a lowercase hex {@link String}, null if none defined (disks case)
	 */
	public String crc = null;
	/**
	 * the sha1 value as a lowercase hex {@link String}, null if none defined
	 */
	public String sha1 = null;
	/**
	 * the md5 value as a lowercase hex {@link String}, null if none defined
	 */
	public String md5 = null;
	/**
	 * the merge name of this Entity, null if no explicit merge defined
	 */
	public String merge = null;
	/**
	 * the dump status, default to good when not defined
	 */
	public Status status = Status.good;

	/**
	 * the dump status definition
	 */
	public enum Status implements Serializable
	{
		/**
		 * bad dump
		 */
		baddump,
		/**
		 * no dump known
		 */
		nodump,
		/**
		 * dump is good
		 */
		good,
		/**
		 * dump is good and has been verified (only logiqx)
		 */
		verified;

		/**
		 * status mapping according export format needed
		 * @param is_mame format is mame
		 * @return the mapped {@link Status}
		 */
		public Status getXML(final boolean is_mame)
		{
			return (Status.good == this || (is_mame && Status.verified == this)) ? null : this;
		}
	}

	/**
	 * collision state
	 */
	private transient boolean collision = false;

	/**
	 * The only one supported constructor
	 * @param parent the required {@link Anyware} parent (a Machine or a Software)
	 */
	protected Entity(final Anyware parent)
	{
		super(parent);
	}

	/**
	 * Enable collision mode
	 * Depending on the {@link Anyware#hash_collision_mode}, this may affect only parent, or all parent clones
	 */
	public void setCollisionMode()
	{
		if(Anyware.hash_collision_mode == HashCollisionOptions.SINGLECLONE)
			getParent().setCollisionMode(false);
		else if(Anyware.hash_collision_mode == HashCollisionOptions.ALLCLONES)
			getParent().setCollisionMode(true);
		collision = true;
	}

	/**
	 * are we in collision mode
	 * @param dumber special case for the dumb mode (always assume collision)
	 * @return true if we are in collision mode
	 */
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
			if(Anyware.hash_collision_mode == HashCollisionOptions.HALFDUMB)
				return true;
			else if(Anyware.hash_collision_mode == HashCollisionOptions.DUMB)
				return true;
			else if(Anyware.hash_collision_mode == HashCollisionOptions.DUMBER)
				return true;
		}
		return collision;
	}

	/**
	 * reset {@link #collision} mode and {@link EntityBase#own_status}
	 */
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

	/**
	 * get the crc string
	 * @return the crc {@link String} or null
	 */
	public String getCRC()
	{
		return crc;
	}

	/**
	 * get the md5 string
	 * @return the md5 {@link String} or null
	 */
	public String getMD5()
	{
		return md5;
	}

	/**
	 * get the sha1 string
	 * @return the sha1 {@link String} or null
	 */
	public String getSHA1()
	{
		return sha1;
	}

	/**
	 * get data size
	 * @return size in bytes or 0 if not defined
	 */
	public long getSize()
	{
		return size;
	}
}
