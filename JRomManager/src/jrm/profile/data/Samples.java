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
import java.util.Map;

import jrm.profile.Profile;
import lombok.Getter;

/**
 * Samples is a set of unique {@link Samples}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Samples extends AnywareBase implements Serializable, Iterable<Sample>
{
	/**
	 * the {@link HashMap} of {@link Sample} with {@link NameBase#name} as key
	 */
	private @Getter Map<String, Sample> samplesMap = new HashMap<>();

	/**
	 * The constructor
	 * @param name the set name
	 */
	public Samples(String name)
	{
		setName(name);
	}

	/**
	 * add a unique sample, only add if not already existing
	 * @param sample the {@link Sample} to add
	 * @return return the added {@link Sample}, or the already existing one
	 */
	public Sample add(Sample sample)
	{
		if (!samplesMap.containsKey(sample.name))
		{
			samplesMap.put(sample.name, sample);
			return sample;
		}
		return samplesMap.get(sample.name);
	}

	@Override
	public AnywareBase getParent()
	{
		return parent;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getFullName()
	{
		return name;
	}

	@Override
	public String getFullName(String filename)
	{
		return filename;
	}

	@Override
	public CharSequence getDescription()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public Iterator<Sample> iterator()
	{
		return samplesMap.values().iterator();
	}

	@Override
	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		boolean ok = false;
		for(final Sample sample : this)
		{
			final EntityStatus estatus = sample.getStatus();
			if(estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if(estatus == EntityStatus.OK)
				ok = true;
			else if(estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		if(status == AnywareStatus.PARTIAL && !ok)
			status = AnywareStatus.MISSING;
		return status;
	}

	@Override
	public Profile getProfile()
	{
		return null;
	}
}
