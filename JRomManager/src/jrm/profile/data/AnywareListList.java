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
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jrm.profile.Profile;
import lombok.Getter;
import lombok.Setter;

/**
 *  A list of {@link Anyware} objects lists
 * @author optyfr
 *
 * @param <T> extends {@link AnywareList} (generally a {@link Machine} or a {@link Software})
 */
@SuppressWarnings("serial")
public abstract class AnywareListList<T extends AnywareList<? extends Anyware>> implements Serializable, List<T>
{
	protected @Getter @Setter Profile profile;

	/**
	 * {@link T} list cache (according current {@link Profile#filterListLists})
	 */
	protected transient List<T> filteredList;

	/**
	 * The constructor, will initialize transients fields
	 */
	protected AnywareListList(Profile profile)
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
	public abstract void resetCache();
	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 * @param filter the new {@link EnumSet} of {@link AnywareStatus} filter to apply
	 */
	public abstract void setFilterCache(final Set<AnywareStatus> filter);

	/**
	 * get a cached and filtered stream
	 * @return {@link Stream}&lt;{@link T}&gt;
	 */
	public abstract Stream<T> getFilteredStream();

	/**
	 * get a cached filtered list
	 * @return {@link List}&lt;{@link T}&gt;
	 */
	protected abstract List<T> getFilteredList();


	/**
	 * return a non filtered list of {@link T}
	 * @return {@link List}&lt;{@link T}&gt;
	 */
	public abstract List<T> getList();

	@Override
	public T get(final int index)
	{
		return getList().get(index);
	}

	@Override
	public boolean add(final T list)
	{
		return getList().add(list);
	}

	@Override
	public void forEach(final Consumer<? super T> action)
	{
		getList().forEach(action);
	}

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
	
	public abstract int count();
	
	public abstract AnywareList<? extends Anyware> getObject(int i);	//NOSONAR

	public abstract String getDescription(int i);

	public abstract String getHaveTot(int i);
}
