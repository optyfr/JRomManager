/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.data;

import java.io.IOException;
import java.io.ObjectStreamField;
import java.io.Serializable;

import jrm.misc.ProfileSettings;
import jrm.profile.scan.options.HashCollisionOptions;
import lombok.Getter;
import lombok.Setter;

/**
 * the common class for {@link Rom} and {@link Disk}
 * @author optyfr
 *
 */
public abstract class Entity extends EntityBase implements Serializable
{
	protected static final String STATUS_STR = "status";
	protected static final String MERGE_STR = "merge";
	protected static final String MD5_STR = "md5";
	protected static final String SHA1_STR = "sha1";
	protected static final String CRC_STR = "crc";
	protected static final String SIZE_STR = "size";

	private static final long serialVersionUID = 1L;

	/**
	 * the date size in bytes, 0 by default (always 0 for disks)
	 */
	protected @Getter @Setter long size = 0;
	/**
	 * the crc value as a lowercase hex {@link String}, null if none defined (disks case)
	 */
	protected @Getter @Setter String crc = null;
	/**
	 * the sha1 value as a lowercase hex {@link String}, null if none defined
	 */
	protected @Getter @Setter String sha1 = null;
	/**
	 * the md5 value as a lowercase hex {@link String}, null if none defined
	 */
	protected @Getter @Setter String md5 = null;
	/**
	 * the merge name of this Entity, null if no explicit merge defined
	 */
	protected @Getter @Setter String merge = null;
	/**
	 * the dump status, default to good when not defined
	 */
	protected @Getter @Setter Status dumpStatus = Status.good;

	/**
	 * the dump status definition
	 */
	public enum Status implements Serializable
	{
		/**
		 * bad dump
		 */
		baddump,	//NOSONAR
		/**
		 * no dump known
		 */
		nodump,	//NOSONAR
		/**
		 * dump is good
		 */
		good,	//NOSONAR
		/**
		 * dump is good and has been verified (only logiqx)
		 */
		verified;	//NOSONAR

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

	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
		new ObjectStreamField(SIZE_STR, long.class),
		new ObjectStreamField(CRC_STR, String.class),
		new ObjectStreamField(SHA1_STR, String.class),
		new ObjectStreamField(MD5_STR, String.class),
		new ObjectStreamField(MERGE_STR, String.class),
		new ObjectStreamField(STATUS_STR, Status.class)
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final var fields = stream.putFields();
		fields.put(SIZE_STR, size);
		fields.put(CRC_STR, crc);
		fields.put(SHA1_STR, sha1);
		fields.put(MD5_STR, md5);
		fields.put(MERGE_STR, merge);
		fields.put(STATUS_STR, dumpStatus);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final var fields = stream.readFields();
		size = fields.get(SIZE_STR, 0L);
		crc = (String)fields.get(CRC_STR, null);
		sha1 = (String)fields.get(SHA1_STR, null);
		md5 = (String)fields.get(MD5_STR, null);
		merge = (String)fields.get(MERGE_STR, null);
		dumpStatus = (Status)fields.get(STATUS_STR, Status.good);
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
	 * Depending on the {@link ProfileSettings#hashCollisionMode}, this may affect only parent, or all parent clones
	 */
	public void setCollisionMode()
	{
		if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.SINGLECLONE)
			getParent().setCollisionMode(false);
		else if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.ALLCLONES)
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
		if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.SINGLECLONE)
			return getParent().isCollisionMode();
		else if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.ALLCLONES)
			return getParent().isCollisionMode();
		else if(dumber)
		{
			if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.DUMBER)
				return true;
		}
		else 
		{
			if (getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.HALFDUMB
				|| getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.DUMB
				|| getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.DUMBER)
				return true;
		}
		return collision;
	}

	/**
	 * reset {@link #collision} mode and {@link EntityBase#ownStatus}
	 */
	void resetCollisionMode()
	{
		collision = false;
		ownStatus = EntityStatus.UNKNOWN;
	}

	@Override
	public Anyware getParent()
	{
		return getParent(Anyware.class);
	}

	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
}
