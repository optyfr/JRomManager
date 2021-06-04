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

class CHDHeaderV1 extends CHDHeader implements CHDHeaderIntf
{
	private final String md5;

	public CHDHeaderV1(final MappedByteBuffer bb, final CHDHeader header)
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		bb.position(44);
		final var hash = new byte[16];
		bb.get(hash);
		this.md5 = CHDHeader.bytesToHex(hash);
	}

	@Override
	public String getSHA1()
	{
		return null;
	}

	@Override
	public String getMD5()
	{
		return md5;
	}

}
