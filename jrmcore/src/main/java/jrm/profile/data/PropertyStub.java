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
 * Interface definition for linking selectable data classes with profile properties getter/setter.
 * Provides a standardized mechanism to query and mutate boolean preferences associated with specific
 * profiles.
 * 
 * @author optyfr
 * @since 1.0
 */
public interface PropertyStub extends Serializable
{
	/**
	 * Get the defined property name of the current class.
	 * 
	 * @return the name of the property as a String
	 */
	public String getPropertyName();

	/**
	 * Get the selection state in profile properties according to {@link #getPropertyName()}.
	 * 
	 * @param profile the profile to read the property from
	 * @return true if selected, false otherwise
	 */
	public default boolean isSelected(final Profile profile)
	{
		return profile.getProperty(getPropertyName(), true);
	}

	/**
	 * Set the selection state in profile properties according to {@link #getPropertyName()}.
	 * 
	 * @param profile the profile to set the property in
	 * @param selected the selection state to set
	 */
	public default void setSelected(final Profile profile, final boolean selected)
	{
		profile.setProperty(getPropertyName(), selected);
	}

}
