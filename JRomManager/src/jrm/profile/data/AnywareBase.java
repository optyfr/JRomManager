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

import java.io.Serializable;

import jrm.profile.Profile;

/**
 * The abstract base class for {@link Anyware}, its main purpose is to define parent relationship
 * @author optyfr
 *
 */
public abstract class AnywareBase extends NameBase implements Serializable
{
	private static final long serialVersionUID = 1L;

	/*
	private static final ObjectStreamField[] serialPersistentFields = {};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
//		final ObjectOutputStream.PutField fields = stream.putFields();
//		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
//		final ObjectInputStream.GetField fields = stream.readFields();
//		parent = null;
	}
*/
	
	/**
	 * {@link AnywareBase} can have parent of the same type
	 */
	protected transient AnywareBase parent = null;
	
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
	 * set the parent
	 * @param parent an object which is an instance of {@link AnywareBase}
	 * @param <T> a class which extends {@link AnywareBase}
	 */
	public <T extends AnywareBase> void setParent(T parent)
	{
		this.parent = parent;
	}

	/**
	 * get the parent
	 * @return the parent or null if not set
	 */
	public abstract AnywareBase getParent();

	/**
	 * get the name of this object, eventually concatenated with its list name
	 * @return the full name of this object as a {@link String}
	 */
	public abstract String getFullName();
	/**
	 * get the extended filename of this object, it will eventually concatenate with its list name
	 * @param filename the filename to use
	 * @return the full filename of this object as a {@link String}
	 */
	public abstract String getFullName(final String filename);
	/**
	 * get the description of this object
	 * @return a {@link CharSequence} containing the description
	 */
	public abstract CharSequence getDescription();
	/**
	 * get the status of this object
	 * @return an {@link AnywareStatus} describing the status of this object
	 */
	public abstract AnywareStatus getStatus();

	public abstract Profile getProfile();

}
