/*
 * Copyright (C) 2015 Christophe De Troyer Copyright (C) 2018 Optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.io.torrent.bencoding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import jrm.io.torrent.TorrentException;
import jrm.io.torrent.bencoding.types.BByteString;
import jrm.io.torrent.bencoding.types.BDictionary;
import jrm.io.torrent.bencoding.types.BInt;
import jrm.io.torrent.bencoding.types.BList;
import jrm.io.torrent.bencoding.types.IBencodable;

public class Reader
{
	private int currentByteIndex;
	private byte[] datablob;

	////////////////////////////////////////////////////////////////////////////
	//// CONSTRUCTORS //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	public Reader(File file) throws IOException
	{
		datablob = IOUtils.toByteArray(new FileInputStream(file));
	}

	public Reader(String s)
	{
		datablob = s.getBytes();
	}

	////////////////////////////////////////////////////////////////////////////
	//// PARSER ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Starts reading from the beginning of the file. Keeps reading single types and
	 * adds them to the list to finally return them.
	 *
	 * @return {@link List} of {@link IBencodable} containing all the parsed
	 *         bencoded objects.
	 * @throws TorrentException 
	 */
	public synchronized List<IBencodable> read() throws TorrentException
	{
		this.currentByteIndex = 0;
		long fileSize = datablob.length;

		final var dataTypes = new ArrayList<IBencodable>();
		while (currentByteIndex < fileSize)
			dataTypes.add(readSingleType());

		return dataTypes;
	}

	/**
	 * Tries to read in an object starting at the current byte index. If not
	 * possible throws an exception.
	 *
	 * @return Returns an Object that represents either BByteString, BDictionary,
	 *         BInt or BList.
	 * @throws TorrentException 
	 */
	private IBencodable readSingleType() throws TorrentException
	{
		// Read in the byte at current position and dispatch over it.
		byte current = datablob[currentByteIndex];
		switch (current)
		{
			case '0','1','2','3','4','5','6','7','8','9':
				return readByteString();
			case 'd':
				return readDictionary();
			case 'i':
				return readInteger();
			case 'l':
				return readList();
			default:
				break;
		}
		throw new TorrentException("Parser in invalid state at byte " + currentByteIndex); //$NON-NLS-1$
	}

	////////////////////////////////////////////////////////////////////////////
	//// BENCODING READ TYPES //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Reads in a list starting from the current byte index. Throws an error if not
	 * called on an appropriate index. A list of values is encoded as
	 * l&lt;contents&gt;e . The contents consist of the bencoded elements of the
	 * list, in order, concatenated. A list consisting of the string "spam" and the
	 * number 42 would be encoded as: l4:spami42ee. Note the absence of separators
	 * between elements.
	 *
	 * @return BList object.
	 * @throws TorrentException 
	 */
	private BList readList() throws TorrentException
	{
		// If we got here, the current byte is an 'l'.
		if (readCurrentByte() != 'l')
			throw new TorrentException("Error parsing list. Was expecting a 'l' but got " + readCurrentByte()); //$NON-NLS-1$
		currentByteIndex++; // Skip over the 'l'

		final var list = new BList();
		while (readCurrentByte() != 'e')
			list.add(readSingleType());

		currentByteIndex++; // Skip the 'e'
		return list;
	}

	/**
	 * Reads in a bytestring strating at the current position. Throws an error if
	 * not possible. A byte string (a sequence of bytes, not necessarily characters)
	 * is encoded as &lt;length&gt;:&lt;contents&gt;. The length is encoded in base
	 * 10, like integers, but must be non-negative (zero is allowed); the contents
	 * are just the bytes that make up the string. The string "spam" would be
	 * encoded as 4:spam. The specification does not deal with encoding of
	 * characters outside the ASCII set; to mitigate this, some BitTorrent
	 * applications explicitly communicate the encoding (most commonly UTF-8) in
	 * various non-standard ways. This is identical to how netstrings work, except
	 * that netstrings additionally append a comma suffix after the byte sequence.
	 *
	 * @return BByteString
	 * @throws TorrentException 
	 */
	private BByteString readByteString() throws TorrentException
	{
		var lengthAsString = new StringBuilder(); //$NON-NLS-1$
		int lengthAsInt;
		byte[] bsData;

		// Build up a string of ascii chars representing the size.
		var current = readCurrentByte();
		while (current >= 48 && current <= 57)
		{
			lengthAsString.append(Character.toString((char) current));
			currentByteIndex++;
			current = readCurrentByte();
		}
		lengthAsInt = Integer.parseInt(lengthAsString.toString());

		if (readCurrentByte() != ':')
			throw new TorrentException("Read length of byte string and was expecting ':' but got " + readCurrentByte()); //$NON-NLS-1$
		currentByteIndex++; // Skip over the ':'.

		// Read the actual data
		bsData = new byte[lengthAsInt];
		for (var i = 0; i < lengthAsInt; i++)
		{
			bsData[i] = readCurrentByte();
			currentByteIndex++;
		}

		return new BByteString(bsData);
	}

	/**
	 * Reads in a dictionary. Each dictionary consists of N bytestrings mapped to
	 * any other value. Example: d3:foo3:bare == ({foo, bar}) A dictionary is
	 * encoded as d&lt;contents&gt;e. The elements of the dictionary are encoded
	 * each key immediately followed by its value. All keys must be byte strings and
	 * must appear in lexicographical order. A dictionary that associates the values
	 * 42 and "spam" with the keys "foo" and "bar", respectively (in other words,
	 * {"bar": "spam", "foo": 42}), would be encoded as follows:
	 * d3:bar4:spam3:fooi42ee. (This might be easier to read by inserting some
	 * spaces: d 3:bar 4:spam 3:foo i42e e.)
	 *
	 * @return BDictionary representing the dictionary.
	 * @throws TorrentException 
	 */
	private BDictionary readDictionary() throws TorrentException
	{
		// If we got here, the current byte is an 'd'.
		if (readCurrentByte() != 'd')
			throw new TorrentException("Error parsing dictionary. Was expecting a 'd' but got " + readCurrentByte()); //$NON-NLS-1$
		currentByteIndex++; // Skip over the 'd'

		final var dict = new BDictionary();
		while (readCurrentByte() != 'e')
		{
			// Each dictionary *must* map BByteStrings to any other value.
			BByteString key = (BByteString) readSingleType();
			IBencodable value = readSingleType();

			dict.add(key, value);
		}
		currentByteIndex++; // Skip the 'e'

		return dict;
	}

	/**
	 * Parses an integer in Bencode fromat. Example: 123 == i123e An integer is
	 * encoded as i&lt;integer encoded in base ten ASCII&gt;e. Leading zeros are not
	 * allowed (although the number zero is still represented as "0"). Negative
	 * values are encoded by prefixing the number with a minus sign. The number 42
	 * would thus be encoded as i42e, 0 as i0e, and -42 as i-42e. Negative zero is
	 * not permitted.
	 *
	 * @return BInt representing the value of the parsed integer.
	 * @throws TorrentException 
	 */
	private BInt readInteger() throws TorrentException
	{
		// If we got here, the current byte is an 'i'.
		if (readCurrentByte() != 'i')
			throw new TorrentException("Error parsing integer. Was expecting an 'i' but got " + readCurrentByte()); //$NON-NLS-1$
		currentByteIndex++;// Skip the 'i'.

		// Read in the integer number by number.
		// They are represented as ASCII numbers.
		var intString = new StringBuilder(); //$NON-NLS-1$
		var current = readCurrentByte();
		// 45 negative mark
		while (current >= 48 && current <= 57 || current == 45)
		{
			intString.append(Character.toString((char) current));
			currentByteIndex++;
			current = readCurrentByte();
		}

		if (readCurrentByte() != 'e')
			throw new TorrentException("Error parsing integer. Was expecting 'e' at end but got " + readCurrentByte()); //$NON-NLS-1$

		currentByteIndex++; // Skip past 'e'
		return new BInt(Long.parseLong(intString.toString()));
	}

	////////////////////////////////////////////////////////////////////////////
	//// HELPERS ///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the byte in the current position of the file.
	 *
	 * @return byte
	 */
	private byte readCurrentByte()
	{
		return datablob[currentByteIndex];
	}
}
