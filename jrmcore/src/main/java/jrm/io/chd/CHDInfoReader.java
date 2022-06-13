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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

/**
 * Gatter CHD header informations
 * @author optyfr
 *
 */
public class CHDInfoReader implements CHDHeaderIntf
{
	CHDHeaderIntf header;

	public CHDInfoReader(final File chdfile) throws IOException
	{
		try(final var is = new FileInputStream(chdfile))
		{
			// Memory maps a ByteBuffer of 1kB onto CHD file
			final MappedByteBuffer bb = is.getChannel().map(MapMode.READ_ONLY, 0, Math.min(1024, chdfile.length()));
			// Will read informations that are common to all CHD header versions (start tag and version)
			final var hdr = new CHDHeader(bb);
			this.header = hdr;
			if(hdr.isValidTag())
			{
				switch(hdr.getVersion())
				{
					case 1:
						this.header = new CHDHeaderV1(bb, hdr);
						break;
					case 2:
						this.header = new CHDHeaderV2(bb, hdr);
						break;
					case 3:
						this.header = new CHDHeaderV3(bb, hdr);
						break;
					case 4:
						this.header = new CHDHeaderV4(bb, hdr);
						break;
					case 5:
						this.header = new CHDHeaderV5(bb, hdr);
						break;
					default:
						break;
				}
			}
		}
	}

	@Override
	public String getSHA1()
	{
		return header.getSHA1();
	}

	@Override
	public String getMD5()
	{
		return header.getMD5();
	}

	@Override
	public boolean isValidTag()
	{
		return header.isValidTag();
	}

	@Override
	public int getLen()
	{
		return header.getLen();
	}

	@Override
	public int getVersion()
	{
		return header.getVersion();
	}

}
