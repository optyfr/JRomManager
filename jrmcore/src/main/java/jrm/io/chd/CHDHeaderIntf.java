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
 * Unified interface for accessing metadata, structural details, and
 * cryptographic checksums extracted from different versions of CHD (Compressed
 * Hunks of Data) file headers.
 * 
 * @author optyfr
 */
interface CHDHeaderIntf {
    /**
     * Determines if the file has a valid CHD tag signature (i.e. 'MComprHD').
     * 
     * @return {@code true} if the header tag is valid, otherwise {@code false}
     */
    boolean isValidTag();

    /**
     * Gets the length of the CHD header in bytes.
     * 
     * @return header length in bytes
     */
    int getLen();

    /**
     * Gets the version number of the CHD file format.
     * 
     * @return CHD version number (e.g., 1, 2, 3, 4, 5)
     */
    int getVersion();

    /**
     * Retrieves the SHA-1 digest of the raw, uncompressed data represented by this
     * CHD, as specified in the header.
     * 
     * @return the SHA-1 hexadecimal string or {@code null} if not reported by this
     *         header version
     */
    public default String getSHA1() {
        return null;
    }

    /**
     * Retrieves the MD5 digest of the raw, uncompressed data represented by this
     * CHD, as specified in the header.
     * 
     * @return the MD5 hexadecimal string or {@code null} if not reported by this
     *         header version
     */
    public default String getMD5() {
        return null;
    }
}
