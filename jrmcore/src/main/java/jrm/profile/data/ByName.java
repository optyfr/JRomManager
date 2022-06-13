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
 * Interface describing methods related to entity name manipulation
 * @author optyfr
 *
 * @param <T> any class that extends {@link NameBase}
 */
public interface ByName<T extends NameBase> extends Serializable
{
	/**
	 * tells reevaluate the named map filtered cache
	 */
	public void resetFilteredName();
	/**
	 * Ask if this name is contained in the named map filtered cache
	 * @param name the name to search
	 * @return true if it was found
	 */
	public boolean containsFilteredName(String name);
	/**
	 * Ask if this name is contained in the named map non-filtered cache
	 * @param name the name to search
	 * @return true if it was found
	 */
	public boolean containsName(String name);
	/**
	 * get the {@link T} by its name from the named map filtered cache
	 * @param name the name to search
	 * @return {@link T} or null
	 */
	public T getFilteredByName(String name);
	/**
	 * get the {@link T} by its name from the named map non-filtered cache
	 * @param name the name to search
	 * @return {@link T} or null
	 */
	public T getByName(String name);
	/**
	 * add {@link T} to the named map non-filtered cache
	 * @param t the {@link T} to add
	 * @return the previous {@link T} value stored with this name or null
	 */
	public abstract T putByName(T t);
}
