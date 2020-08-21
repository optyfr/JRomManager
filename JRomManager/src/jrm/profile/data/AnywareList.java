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
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

import jrm.profile.Profile;

/**
 * A list of {@link Anyware} objects
 * @author optyfr
 *
 * @param <T> extends {@link Anyware} (generally a {@link Machine} or a {@link Software})
 */
@SuppressWarnings("serial")
public abstract class AnywareList<T extends Anyware> extends NameBase implements Serializable, List<T>, ByName<T>
{
	Profile profile;

	/**
	 * {@link T} list cache (according current {@link Profile#filter_l})
	 */
	protected transient List<T> filtered_list;

	/**
	 * The constructor, will initialize transients fields
	 */
	public AnywareList(Profile profile)
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
		filtered_list = null;
	}

	/**
	 * return a non filtered list of {@link T}
	 * @return {@link List}&lt;{@link T}&gt;
	 */
	public abstract List<T> getList();

	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 */
	public void resetCache()
	{
		this.filtered_list = null;
	}

	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 * @param filter the new {@link EnumSet} of {@link AnywareStatus} filter to apply
	 */
	public void setFilterCache(final EnumSet<AnywareStatus> filter)
	{
		profile.filter_l = filter;
	}
	
	public EnumSet<AnywareStatus> getFilter()
	{
		return profile.filter_l;
	}

	/**
	 * get a cached and filtered stream
	 * @return {@link Stream}&lt;{@link T}&gt;
	 */
	public abstract Stream<T> getFilteredStream();

	/**
	 * get a cached filtered list
	 * @return {@link List}&lt;{@link T}&gt;
	 */
	public abstract List<T> getFilteredList();


	@Override
	public int size()
	{
		return getList().size();
	}

	@Override
	public boolean isEmpty()
	{
		return getList().isEmpty();
	}

	@Override
	public boolean contains(final Object o)
	{
		return getList().contains(o);
	}

	@Override
	public Iterator<T> iterator()
	{
		return getList().iterator();
	}

	@Override
	public Object[] toArray()
	{
		return getList().toArray();
	}

	@Override
	public <E> E[] toArray(final E[] a)
	{
		return getList().toArray(a);
	}

	@Override
	public boolean add(final T e)
	{
		return getList().add(e);
	}

	@Override
	public boolean remove(final Object o)
	{
		return getList().remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return getList().containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends T> c)
	{
		return getList().addAll(c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends T> c)
	{
		return getList().addAll(index, c);
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return getList().removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return getList().retainAll(c);
	}

	@Override
	public void clear()
	{
		getList().clear();
	}

	@Override
	public T get(final int index)
	{
		return getList().get(index);
	}

	@Override
	public T set(final int index, final T element)
	{
		return getList().set(index, element);
	}

	@Override
	public void add(final int index, final T element)
	{
		getList().add(index, element);
	}

	@Override
	public T remove(final int index)
	{
		return getList().remove(index);
	}

	@Override
	public int indexOf(final Object o)
	{
		return getList().indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o)
	{
		return getList().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return getList().listIterator();
	}

	@Override
	public ListIterator<T> listIterator(final int index)
	{
		return getList().listIterator(index);
	}

	@Override
	public List<T> subList(final int fromIndex, final int toIndex)
	{
		return getList().subList(fromIndex, toIndex);
	}

	/**
	 * get the overall current status according the status of all its currently filtered {@link Anyware}s
	 * @return an {@link AnywareStatus}
	 */
	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		boolean ok = false;
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
}
