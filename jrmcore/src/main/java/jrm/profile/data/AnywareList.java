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

import java.io.IOException;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jrm.profile.Profile;

/**
 * A list of {@link Anyware} objects
 * @author optyfr
 *
 * @param <T> extends {@link Anyware} (generally a {@link Machine} or a {@link Software})
 */
@SuppressWarnings("serial")
public abstract class AnywareList<T extends Anyware> extends NameBase implements Serializable, AWList<T>, ByName<T>
{
	Profile profile;

	/**
	 * {@link T} list cache (according current {@link Profile#filterList})
	 */
	protected transient List<T> filteredList;

	/**
	 * The constructor, will initialize transients fields
	 */
	protected AnywareList(Profile profile)
	{
		this.profile = profile;
		initTransient();
	}

	/**
	 * the Serializable method for special serialization handling (in that case : initialize transient default values) 
	 * @param in the serialization inputstream
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	/**
	 * The method called to initialize transient and static fields
	 */
	protected void initTransient()
	{
		filteredList = null;
	}

	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 */
	public void resetCache()
	{
		this.filteredList = null;
	}

	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 * @param filter the new {@link EnumSet} of {@link AnywareStatus} filter to apply
	 */
	public void setFilterCache(final Set<AnywareStatus> filter)
	{
		profile.setFilterList(filter);
	}
	
	public Set<AnywareStatus> getFilter()
	{
		return profile.getFilterList();
	}

	/**
	 * get the overall current status according the status of all its currently filtered {@link Anyware}s
	 * @return an {@link AnywareStatus}
	 */
	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		var ok = false;
		for(final Iterator<T> iterator = getFilteredStream().iterator(); iterator.hasNext();)
		{
			final AnywareStatus estatus = iterator.next().getStatus();
			if(estatus == AnywareStatus.PARTIAL || estatus == AnywareStatus.MISSING)
				status = AnywareStatus.PARTIAL;
			else if(estatus == AnywareStatus.COMPLETE)
				ok = true;
			else if(estatus == AnywareStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		if(status == AnywareStatus.PARTIAL && !ok)
			status = AnywareStatus.MISSING;
		return status;
	}

	/**
	 * count the number of correct wares we have in this list
	 * @return an int which is the total counted
	 */
	public abstract long countHave();

	/**
	 * count the number of wares contained in this list, whether they are OK or not
	 * @return an int which is the sum of all the wares
	 */
	public abstract long countAll();

	/**
	 * Find the index of a given {@link Anyware} in the filetered list 
	 * @param anyware the given {@link Anyware}
	 * @return the int index or -1 if not found
	 */
	public int find(final Anyware anyware)
	{
		return getFilteredList().indexOf(anyware);
	}

	/**
	 * Find the first index of the {@link Anyware} for which its name starts with the search string
	 * @param search the {@link String} to search for
	 * @return the int index or -1 if not found
	 */
	public int find(final String search)
	{
		return find(getFilteredStream().filter(s -> s.getName().toLowerCase().startsWith(search.toLowerCase())).findFirst().orElse(null));
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
