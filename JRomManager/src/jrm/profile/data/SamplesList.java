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
package jrm.profile.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Set of unique {@link Samples} sets
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class SamplesList implements Serializable, ByName<Samples>, Iterable<Samples>
{
	/**
	 * {@link HashMap} of {@link Samples} set with {@link Samples#name} as key
	 */
	protected final HashMap<String, Samples> samplesets = new HashMap<>();

	@Override
	public boolean containsName(String name)
	{
		return samplesets.containsKey(name);
	}

	@Override
	public Samples getByName(String name)
	{
		return samplesets.get(name);
	}

	@Override
	public Samples putByName(Samples t)
	{
		return samplesets.put(t.name, t);
	}

	@Override
	public Iterator<Samples> iterator()
	{
		return samplesets.values().iterator();
	}

	public int size()
	{
		return samplesets.size();
	}

	@Override
	public void resetFilteredName()
	{
	}

	@Override
	public boolean containsFilteredName(String name)
	{
		return containsName(name);
	}

	@Override
	public Samples getFilteredByName(String name)
	{
		return getByName(name);
	}
}
