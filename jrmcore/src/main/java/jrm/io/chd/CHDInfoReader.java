/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.io.chd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;

/**
 * A reader class that gathers metadata and cryptographic checksum information from the headers of CHD (Compressed Hunks of Data)
 * files. It memory-maps the first 1KB of the target file to determine the header version (1-5) and delegates properties extraction
 * to the corresponding concrete header implementation.
 * 
 * @author optyfr
 */
public class CHDInfoReader implements CHDHeaderIntf {
    /**
     * The delegated version-specific CHD header implementation.
     */
    private CHDHeaderIntf header;

    /**
     * Constructs a new {@code CHDInfoReader} for the specified file and parses its header.
     *
     * @param chdfile the target CHD file to read and analyze
     * 
     * @throws IOException if an I/O error occurs while opening or mapping the file
     */
    public CHDInfoReader(final File chdfile) throws IOException {
        try (final var is = new FileInputStream(chdfile)) {
            // Memory maps a ByteBuffer of 1kB onto CHD file
            final MappedByteBuffer bb = is.getChannel().map(MapMode.READ_ONLY, 0, Math.min(1024, chdfile.length()));
            // Will read informations that are common to all CHD header versions (start tag
            // and version)
            final var hdr = new CHDHeader(bb);
            this.header = hdr;
            if (hdr.isValidTag()) {
                switch (hdr.getVersion()) {
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

    /**
     * Retrieves the SHA-1 digest of the raw, uncompressed data represented by this CHD, as specified in the header.
     * 
     * @return the SHA-1 hexadecimal string or {@code null} if not reported by this header version
     */
    @Override
    public String getSHA1() {
        return header.getSHA1();
    }

    /**
     * Retrieves the MD5 digest of the raw, uncompressed data represented by this CHD, as specified in the header.
     * 
     * @return the MD5 hexadecimal string or {@code null} if not reported by this header version
     */
    @Override
    public String getMD5() {
        return header.getMD5();
    }

    /**
     * Determines if the file has a valid CHD tag signature (i.e. 'MComprHD').
     * 
     * @return {@code true} if the header tag is valid, otherwise {@code false}
     */
    @Override
    public boolean isValidTag() {
        return header.isValidTag();
    }

    /**
     * Gets the length of the CHD header in bytes.
     * 
     * @return header length in bytes
     */
    @Override
    public int getLen() {
        return header.getLen();
    }

    /**
     * Gets the version number of the CHD file format.
     * 
     * @return CHD version number (e.g., 1, 2, 3, 4, 5)
     */
    @Override
    public int getVersion() {
        return header.getVersion();
    }

}
