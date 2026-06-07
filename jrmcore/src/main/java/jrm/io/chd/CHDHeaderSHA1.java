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
 * An intermediate representation of CHD file headers that include both MD5 and
 * SHA-1 checksums. Inherits MD5 storage from {@link CHDHeaderMD5} and adds
 * storage and retrieval methods for the SHA-1 digest.
 * 
 * @author optyfr
 */
class CHDHeaderSHA1 extends CHDHeaderMD5 {
    /**
     * The hexadecimal SHA-1 digest of the raw uncompressed data.
     */
    protected String sha1;

    /**
     * Constructs a CHD header parser supporting both MD5 and SHA-1 digest storage.
     *
     * @param bb the mapped byte buffer pointing to the start of the CHD file
     */
    public CHDHeaderSHA1(final MappedByteBuffer bb) {
        super(bb);
    }

    /**
     * Retrieves the SHA-1 digest of the raw, uncompressed data represented by this
     * CHD, as parsed from the header.
     *
     * @return the SHA-1 hexadecimal string representation
     */
    @Override
    public String getSHA1() {
        return sha1;
    }
}
