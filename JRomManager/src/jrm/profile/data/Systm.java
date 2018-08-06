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
 * This interface define System types
 * @author optyfr
 */
public interface Systm extends Serializable, PropertyStub
{
	/**
	 * The types definitions 
	 */
	public enum Type
	{
		/**
		 * Standard machine
		 */
		STANDARD,
		/**
		 * Electro-Mechanical machine
		 */
		MECHANICAL,
		/**
		 * Device pseudo-machine
		 */
		DEVICE,
		/**
		 * BIOS
		 */
		BIOS,
		/**
		 * Software list
		 */
		SOFTWARELIST
	}

	/**
	 * get the type of system
	 * @return return {@link Type}
	 */
	public Type getType();

	/**
	 * get the System
	 * @return {@link Systm}
	 */
	public Systm getSystem();

	/**
	 * get the name of the system
	 * @return the name of the system
	 */
	public String getName();

	@Override
	public default String getPropertyName()
	{
		if(getType()==Type.SOFTWARELIST)
			return "filter.systems.swlist." + getName();
		return "filter.systems." + getName();
	}
}
