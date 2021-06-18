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

	private static final ObjectStreamField[] serialPersistentFields = {
		new ObjectStreamField("size", long.class),
		new ObjectStreamField("crc", String.class),
		new ObjectStreamField("sha1", String.class),
		new ObjectStreamField("md5", String.class),
		new ObjectStreamField("merge", String.class),
		new ObjectStreamField("status", Status.class)
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final var fields = stream.putFields();
		fields.put("size", size);
		fields.put("crc", crc);
		fields.put("sha1", sha1);
		fields.put("md5", md5);
		fields.put("merge", merge);
		fields.put("status", dumpStatus);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final var fields = stream.readFields();
		size = fields.get("size", 0L);
		crc = (String)fields.get("crc", null);
		sha1 = (String)fields.get("sha1", null);
		md5 = (String)fields.get("md5", null);
		merge = (String)fields.get("merge", null);
		dumpStatus = (Status)fields.get("status", Status.good);
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
			if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.HALFDUMB)
				return true;
			else if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.DUMB)
				return true;
			else if(getParent().profile.getSettings().getHashCollisionMode() == HashCollisionOptions.DUMBER)
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

}
