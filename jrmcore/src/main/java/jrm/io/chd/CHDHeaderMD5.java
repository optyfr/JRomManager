package jrm.io.chd;

import java.nio.MappedByteBuffer;

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

/**
 * An intermediate representation of CHD file headers that include an MD5
 * checksum. Provides storage and retrieval methods for the MD5 digest of the
 * raw, uncompressed data.
 * 
 * @author optyfr
 */
class CHDHeaderMD5 extends CHDHeader {
    /**
     * The hexadecimal MD5 digest of the raw uncompressed data.
     */
    protected String md5;

    /**
     * Constructs a CHD header parser supporting MD5 digest storage.
     *
     * @param bb the mapped byte buffer pointing to the start of the CHD file
     */
    public CHDHeaderMD5(final MappedByteBuffer bb) {
        super(bb);
    }

    /**
     * Retrieves the MD5 digest of the raw, uncompressed data represented by this
     * CHD, as parsed from the header.
     *
     * @return the MD5 hexadecimal string representation
     */
    @Override
    public String getMD5() {
        return md5;
    }
}
