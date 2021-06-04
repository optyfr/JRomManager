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

class CHDHeaderV5 extends CHDHeader implements CHDHeaderIntf
{
	private final String sha1;

	public CHDHeaderV5(final MappedByteBuffer bb, final CHDHeader header)
	{
		super();
		tag = header.tag;
		len = header.len;
		version = header.version;
		final var hash = new byte[20];
		bb.position(84);
		bb.get(hash);
		this.sha1 = CHDHeader.bytesToHex(hash);
	}

	@Override
	public String getSHA1()
	{
		return sha1;
	}

	@Override
	public String getMD5()
	{
		return null;
	}

}
