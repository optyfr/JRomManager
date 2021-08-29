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
package jrm.io.chd;

/**
 * Interface to get informations from CHD headers
 * @author optyfr
 *
 */
interface CHDHeaderIntf
{
	/**
	 * Does the file has a valid CHD header?
	 * @return true for valid, otherwise false
	 */
	boolean isValidTag();
	
	/**
	 * Header length
	 * @return length in bytes
	 */
	int getLen();
	
	/**
	 * Header version
	 * @return version number
	 */
	int getVersion();
	
	/**
	 * get the SHA1 (for uncompressed data), null if not reported by header
	 * @return the SHA1 string or null
	 */
	public default String getSHA1()
	{
		return null;
	}

	/**
	 * get the MD5 (for uncompressed data), null if not reported by header
	 * @return the MD5 string or null
	 */
	public default String getMD5()
	{
		return null;
	}
}
