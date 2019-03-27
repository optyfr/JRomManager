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
package jrm.profile.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import jrm.locale.Messages;
import lombok.Getter;

/**
 * nplayers.ini management class
 * @author optyfr
 */
public final class NPlayers implements Iterable<NPlayer>
{
	/**
	 * The {@link List} of {@link NPlayer} modes
	 */
	private final @Getter List<NPlayer> list_nplayers = new ArrayList<>();
	/**
	 * The location of the nplayers.ini {@link File}
	 */
	final public File file;

	/**
	 * The main constructor will read provided nplayers.ini and initialize list of nplayer/games
	 * @param file the nplayers.ini {@link File} to read
	 * @throws IOException
	 */
	private NPlayers(final File file) throws IOException
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(file));)
		{
			final Map<String, NPlayer> nplayers = new TreeMap<>();
			this.file = file;
			String line;
			boolean in_section = false;
			while(null != (line = reader.readLine()))
			{
				if(line.equalsIgnoreCase("[NPlayers]")) //$NON-NLS-1$
					in_section = true;
				else if(line.startsWith("[") && in_section) //$NON-NLS-1$
					break;
				else if(in_section)
				{
					final String[] kv = StringUtils.split(line, '=');
					if(kv.length == 2)
					{
						final String k = kv[0].trim();
						final String v = kv[1].trim();
						NPlayer nplayer;
						if(!nplayers.containsKey(v))
							nplayers.put(v, nplayer = new NPlayer(v));
						else
							nplayer = nplayers.get(v);
						nplayer.add(k);
					}
				}
			}
			list_nplayers.addAll(nplayers.values());
			if(list_nplayers.isEmpty())
				throw new IOException(Messages.getString("NPlayers.NoNPlayersData")); //$NON-NLS-1$
		}
	}

	/**
	 * static method shortcut to constructor
	 * @param file the nplayers.ini to read
	 * @return an initialized {@link NPlayers}
	 * @throws IOException
	 */
	public static NPlayers read(final File file) throws IOException
	{
		return new NPlayers(file);
	}

	@Override
	public Iterator<NPlayer> iterator()
	{
		return list_nplayers.iterator();
	}

}
