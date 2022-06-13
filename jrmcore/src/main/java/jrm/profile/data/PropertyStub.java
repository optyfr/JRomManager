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
 * interface definition for linking selectable data classes with profile properties getter/setter
 * @author optyfr
 *
 */
public interface PropertyStub extends Serializable
{
	/**
	 * get the defined property name of the current class
	 * @return the name of the property
	 */
	public String getPropertyName();

	/**
	 * get the selection state in profile properties according  {@link #getPropertyName()}
	 * @return true if selected
	 */
	public default boolean isSelected(final Profile profile)
	{
		return profile.getProperty(getPropertyName(), true);
	}

	/**
	 * set the selection state in profile properties according {@link #getPropertyName()}
	 * @param selected the selection state to set
	 */
	public default void setSelected(final Profile profile, final boolean selected)
	{
		profile.setProperty(getPropertyName(), selected);
	}

}
