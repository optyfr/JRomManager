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

import jrm.misc.Log;

/**
 * The abstract base class for {@link Entity} and {@link Sample}, main purpose is to define parent relationship and scan status
 * @author optyfr
 */
public abstract class EntityBase extends NameBase implements Serializable
{
	private static final String OWN_STATUS = "own_status";
	private static final long serialVersionUID = 1L;
	/**
	 * The scan status, defaulting to {@link EntityStatus#UNKNOWN}
	 */
	protected EntityStatus ownStatus = EntityStatus.UNKNOWN;
	/**
	 * The parent {@link AnywareBase}
	 */
	protected transient AnywareBase parent;

	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
		new ObjectStreamField(OWN_STATUS, EntityStatus.class)
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final var fields = stream.putFields();
		fields.put(OWN_STATUS, ownStatus);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final var fields = stream.readFields();
		ownStatus = (EntityStatus)fields.get(OWN_STATUS, EntityStatus.UNKNOWN);
	}

	/**
	 * The constructor with its required parent
	 * @param parent the required {@link AnywareBase} parent 
	 */
	protected EntityBase(AnywareBase parent)
	{
		this.parent = parent;
	}

	/**
	 * get the entity status
	 * @return an {@link EntityStatus} value
	 */
	public abstract EntityStatus getStatus();

	/**
	 * set the {@link Entity} status
	 * @param status the {@link EntityStatus} to set
	 */
	public void setStatus(EntityStatus status)
	{
		this.ownStatus = status;
	}

	/**
	 * get the parent casted according the given class
	 * @param type the class to cast, must extends {@link AnywareBase}
	 * @param <T> a class which extends {@link AnywareBase}
	 * @return the type casted parent
	 */
	protected <T extends AnywareBase> T getParent(final Class<T> type)
	{
		return type.cast(parent);
	}

	/**
	 * get the parent
	 * @return the parent (can't be null)
	 */
	public abstract AnywareBase getParent();

	/**
	 * special method to get the value of a field outside its scope<br>
	 * <b style='color:red'>*** USE WITH CAUTION ***</b> 
	 * @param name the property name as a string (case sensitive)
	 * @return the value as an {@link Object} or null;
	 */
	public Object getProperty(String name)
	{
		try
		{
			final var field  = this.getClass().getField(name);
			field.setAccessible(true);	//NOSONAR
			return field.get(this);
		}
		catch(NoSuchFieldException e)
		{
			return null;
		}
		catch (SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			Log.err(e.getMessage(),e);
		}
		return null;
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
