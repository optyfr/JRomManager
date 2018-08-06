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
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Machine}'s device
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Device implements Serializable
{
	/**
	 * type of device
	 */
	public String type;
	/**
	 * tag name used for device
	 */
	public String tag = null;
	/**
	 * the interface name associated
	 */
	public String intrface = null;
	/**
	 * is this device available as media switch? commonly "1" or null
	 */
	public String fixed_image = null;
	/**
	 * is this device mandatory? commonly "1" or null
	 */
	public String mandatory = null;
	
	/**
	 * The {@link Instance} associated with this {@link Device}
	 */
	public Instance instance = null;
	
	/**
	 * The {@link List} of file {@link Extension}s supported for this {@link Device}
	 */
	public List<Extension> extensions = new ArrayList<>();
	
	/**
	 * The Instance associated with this {@link Device} 
	 */
	public class Instance implements Serializable
	{
		/**
		 * the instance name
		 */
		public String name;
		/**
		 * the instance brief name
		 */
		public String briefname = null;
	}
	
	/**
	 * File {@link Extension} associated with this {@link Device}
	 */
	public class Extension implements Serializable
	{
		/**
		 * The extension name
		 */
		public String name;
	}
}
