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
 * Concrete implementation of the CHD file header parser for CHD version 3.
 * Extracts both MD5 and SHA-1 checksums at the offsets specified by version 3
 * of the format.
 * 
 * @author optyfr
 */
class CHDHeaderV3 extends CHDHeaderSHA1 {
    /**
     * Constructs a CHD version 3 header parser, copying core tag/length/version
     * properties from a base parsed CHD header and extracting its MD5 and SHA-1
     * checksums.
     *
     * @param bb  the mapped byte buffer pointing to the start of the CHD file
     * @param hdr the base CHD header containing generic parsed properties
     */
    public CHDHeaderV3(final MappedByteBuffer bb, final CHDHeader hdr) {
        super(bb);
        this.tag = hdr.tag;
        this.len = hdr.len;
        this.version = hdr.version;

        if (bb.remaining() >= getLen() - 16) {
            this.md5 = getHash(bb, 56, 16);
            this.sha1 = getHash(bb, 72, 20);
        }
    }
}
