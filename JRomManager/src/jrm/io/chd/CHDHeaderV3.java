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

import java.nio.MappedByteBuffer;

class CHDHeaderV3 extends CHDHeader implements CHDHeaderIntf
{
	private final String md5;
	private final String sha1;

	public CHDHeaderV3(final MappedByteBuffer bb, final CHDHeader header)
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		md5 = getHash(bb, 44, 16);
		sha1 = getHash(bb, 80, 20);
	}

	@Override
	public String getSHA1()
	{
		return sha1;
	}

	@Override
	public String getMD5()
	{
		return md5;
	}

}
