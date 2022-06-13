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
package jrm.io.torrent.bencoding.types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by christophe on 15.01.15.
 */
public class BList implements IBencodable
{
	private final List<IBencodable> list;

	public BList()
	{
		this.list = new LinkedList<>();
	}

	////////////////////////////////////////////////////////////////////////////
	//// LOGIC METHODS /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	public Iterator<IBencodable> getIterator()
	{
		return list.iterator();
	}

	public void add(IBencodable o)
	{
		this.list.add(o);
	}

	////////////////////////////////////////////////////////////////////////////
	//// BENCODING /////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	public String bencodedString()
	{
		final var sb = new StringBuilder();

		sb.append("l"); //$NON-NLS-1$

		for (final var entry : this.list)
			sb.append(entry.bencodedString());

		sb.append("e"); //$NON-NLS-1$

		return sb.toString();
	}

	public byte[] bencode()
	{
		// Get the total size of the keys and values.
		final var bytes = new ArrayList<Byte>();
		bytes.add((byte) 'l');
		for (final var entry : this.list)
			for (byte b : entry.bencode())
				bytes.add(b);
		bytes.add((byte) 'e');

		final var bencoded = new byte[bytes.size()];

		for (var i = 0; i < bytes.size(); i++)
			bencoded[i] = bytes.get(i);

		return bencoded;
	}
	////////////////////////////////////////////////////////////////////////////
	//// OVERRIDDEN METHODS ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////

	@Override
	public String toString()
	{
		final var sb = new StringBuilder();
		sb.append("("); //$NON-NLS-1$
		for (Object entry : this.list)
		{
			sb.append(entry.toString());
		}
		sb.append(") "); //$NON-NLS-1$

		return sb.toString();
	}

}
