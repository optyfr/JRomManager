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
package jrm.xml;

// TODO: Auto-generated Javadoc
/**
 * A simple attribute only with {@link #name} and {@link #value}.
 *
 * @author optyfr
 */
public final class SimpleAttribute
{
	
	/** The name. */
	final String name;
	
	/** The value. */
	final Object value;

	/**
	 * Instantiates a new simple attribute.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public SimpleAttribute(final String name, final Object value)
	{
		this.name = name;
		this.value = value;
	}
}