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
 * A Sample as defined in Machines sets
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Sample extends EntityBase implements Serializable
{
	/**
	 * The Sample constructor
	 * @param parent the {@link Samples} parent
	 * @param name the name of the sample (with or without .wav extension)
	 */
	public Sample(AnywareBase parent, String name)
	{
		super(parent);
		setName(name);
	}

	@Override
	public String getName()
	{
		if(!name.endsWith(".wav")) //$NON-NLS-1$
			return name + ".wav"; //$NON-NLS-1$
		return name;
	}

	@Override
	public EntityStatus getStatus()
	{
		return ownStatus;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj==null)
			return false;
		return this.toString().equals(obj.toString());
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
	@Override
	public AnywareBase getParent()
	{
		return parent;
	}

}
