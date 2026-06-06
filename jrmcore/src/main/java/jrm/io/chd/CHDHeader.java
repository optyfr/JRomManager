package jrm.io.chd;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.charset.StandardCharsets;

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
 * Base implementation of a CHD (Compressed Hunks of Data) file header parser.
 * It reads the basic signature tag, length, and version, and provides utility
 * methods for hex conversion and hash extraction from byte buffers.
 * 
 * @author optyfr
 */
class CHDHeader implements CHDHeaderIntf
{
	/**
	 * The 8-byte tag/signature indicating a CHD format (typically "MComprHD").
	 */
	protected String tag = "";

	/**
	 * The total length of the CHD header in bytes.
	 */
	protected int len = 0;

	/**
	 * The version number of the CHD file format.
	 */
	protected int version = 0;

	/**
	 * Constructs a CHD header by parsing basic info from a mapped byte buffer.
	 *
	 * @param bb the mapped byte buffer pointing to the start of the CHD file
	 */
	public CHDHeader(final MappedByteBuffer bb)
	{
		if (bb.remaining() >= 16)
		{
			final var t = new byte[8];
			bb.get(t);
			this.tag = new String(t, StandardCharsets.US_ASCII); //$NON-NLS-1$
			len = bb.getInt();
			version = bb.getInt();
		}
	}

	/**
	 * Default no-argument constructor for subclassing.
	 */
	protected CHDHeader() {}

	/**
	 * Determines if the file has a valid CHD tag signature ("MComprHD").
	 *
	 * @return {@code true} if the tag matches "MComprHD", otherwise {@code false}
	 */
	@Override
	public boolean isValidTag()
	{
		return tag.equals("MComprHD"); //$NON-NLS-1$
	}

	/**
	 * Gets the length of the CHD header in bytes.
	 *
	 * @return header length in bytes
	 */
	@Override
	public int getLen()
	{
		return len;
	}

	/**
	 * Gets the version number of the CHD file format.
	 *
	 * @return CHD version number (e.g., 1, 2, 3, 4, 5)
	 */
	@Override
	public int getVersion()
	{
		return version;
	}

	/**
	 * Helper array of hexadecimal characters in lowercase.
	 */
	private static final char[] hexArray = "0123456789abcdef".toCharArray(); //$NON-NLS-1$

	/**
	 * Utility method to convert a byte array to its lowercase hexadecimal string representation.
	 *
	 * @param bytes the array of bytes to convert
	 * @return the converted lowercase hexadecimal string
	 */
	protected static String bytesToHex(final byte[] bytes)
	{
		final var hexChars = new char[bytes.length * 2];
		for (var j = 0; j < bytes.length; j++)
		{
			final int v = bytes[j] & 0xFF;
			hexChars[j * 2] = CHDHeader.hexArray[v >>> 4];
			hexChars[j * 2 + 1] = CHDHeader.hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Retrieves the SHA-1 digest of the raw, uncompressed data.
	 * Default implementation returns {@code null} as base CHD header may not report it.
	 *
	 * @return {@code null} or SHA-1 hash if available
	 */
	@Override
	public String getSHA1()
	{
		return null;
	}

	/**
	 * Retrieves the MD5 digest of the raw, uncompressed data.
	 * Default implementation returns {@code null} as base CHD header may not report it.
	 *
	 * @return {@code null} or MD5 hash if available
	 */
	@Override
	public String getMD5()
	{
		return null;
	}
	
	/**
	 * Reads a hash sequence of a specified size from the given byte buffer at the specified position.
	 *
	 * @param bb the byte buffer to read from
	 * @param position the 0-based offset position in the buffer to start reading the hash
	 * @param size the size of the hash in bytes
	 * @return the hexadecimal string representation of the parsed hash
	 */
	protected String getHash(ByteBuffer bb, int position, int size)
	{
		bb.position(position);
		final var hash = new byte[size];
		bb.get(hash);
		return CHDHeader.bytesToHex(hash);
	}
}
