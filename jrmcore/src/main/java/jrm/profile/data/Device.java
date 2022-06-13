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

import lombok.Getter;
import lombok.Setter;

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
	protected @Getter @Setter String type;
	/**
	 * tag name used for device
	 */
	protected @Getter @Setter String tag = null;
	/**
	 * the interface name associated
	 */
	protected @Getter @Setter String intrface = null;
	/**
	 * is this device available as media switch? commonly "1" or null
	 */
	protected @Getter @Setter String fixedImage = null;
	/**
	 * is this device mandatory? commonly "1" or null
	 */
	protected @Getter @Setter String mandatory = null;
	
	/**
	 * The {@link Instance} associated with this {@link Device}
	 */
	protected @Getter @Setter Instance instance = null;
	
	/**
	 * The {@link List} of file {@link Extension}s supported for this {@link Device}
	 */
	private @Getter @Setter List<Extension> extensions = new ArrayList<>();
	
	/**
	 * The Instance associated with this {@link Device} 
	 */
	public class Instance implements Serializable
	{
		/**
		 * the instance name
		 */
		protected @Getter @Setter String name;
		/**
		 * the instance brief name
		 */
		protected @Getter @Setter String briefname = null;
	}
	
	/**
	 * File {@link Extension} associated with this {@link Device}
	 */
	public class Extension implements Serializable
	{
		/**
		 * The extension name
		 */
		protected @Getter @Setter String name;
	}
}
