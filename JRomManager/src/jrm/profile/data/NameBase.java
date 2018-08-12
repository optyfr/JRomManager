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

/**
 * The base class for named data entities
 * @author optyfr
 */
@SuppressWarnings("serial")
abstract class NameBase implements Serializable, Comparable<NameBase>
{
	/**
	 * The name of the entity
	 */
	protected String name = ""; // required //$NON-NLS-1$

	/**
	 * get the name of the entity, may be forged depending on its extending class
	 * @return the name of the entity
	 */
	public abstract String getName();

	/**
	 * get the untouched (non forged) name of the entity
	 * @return the name as defined initially by {@link #setName(String)}
	 */
	public final String getBaseName()
	{
		return name;
	}

	public final String getNormalizedName()
	{
		return getName().replace('\\', '/');
	}
	
	/**
	 * set the name of the entity
	 * @param name the name to set
	 */
	public final void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public final int compareTo(final NameBase o)
	{
		return name.compareTo(o.name);
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
