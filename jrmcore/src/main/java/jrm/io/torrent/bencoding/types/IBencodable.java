/*
 * Copyright (C) 2015 Christophe De Troyer Copyright (C) 2018 Optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.io.torrent.bencoding.types;

/**
 * Created by christophe on 16.01.15.
 */
public interface IBencodable
{
	/**
	 * Returns the byte representation of the bencoded object.
	 * 
	 * @return byte representation of the bencoded object.
	 */
	byte[] bencode();

	/**
	 * Returns string representation of bencoded object.
	 * 
	 * @return string representation of bencoded object.
	 */
	String bencodedString();
}
