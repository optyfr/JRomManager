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
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import jrm.profile.Profile;
import jrm.ui.basic.EnhTableModel;

/**
 *  A list of {@link Anyware} objects lists
 * @author optyfr
 *
 * @param <T> extends {@link AnywareList} (generally a {@link Machine} or a {@link Software})
 */
@SuppressWarnings("serial")
public abstract class AnywareListList<T extends AnywareList<? extends Anyware>> implements Serializable, EnhTableModel, List<T>
{
	Profile profile;
	/**
	 * Event Listener list for firing events to Swing controls (Table)
	 */
	private static transient EventListenerList listenerList;
	/**
	 * {@link T} list cache (according current {@link Profile#filter_ll})
	 */
	protected transient List<T> filtered_list;

	/**
	 * The constructor, will initialize transients fields
	 */
	public AnywareListList(Profile profile)
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
		if(AnywareListList.listenerList == null)
			AnywareListList.listenerList = new EventListenerList();
		filtered_list = null;
	}

	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 */
	public abstract void reset();
	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 * @param filter the new {@link EnumSet} of {@link AnywareStatus} filter to apply
	 */
	public abstract void setFilter(final EnumSet<AnywareStatus> filter);

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
	 * get the declared renderer for a given column
	 * @param columnIndex the requested column index
	 * @return a {@link TableCellRenderer} associated with the given columnindex 
	 */
	@Override
	public abstract TableCellRenderer getColumnRenderer(int columnIndex);

	/**
	 * get the declared width for a given column
	 * @param columnIndex the requested column index
	 * @return a width in pixel (if negative then it's a fixed column width)
	 */
	@Override
	public abstract int getColumnWidth(int columnIndex);

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex)
	{
		return false;
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
	{
	}

	@Override
	public void addTableModelListener(final TableModelListener l)
	{
		if(AnywareListList.listenerList == null)
			AnywareListList.listenerList = new EventListenerList();
		AnywareListList.listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(final TableModelListener l)
	{
		if(AnywareListList.listenerList == null)
			AnywareListList.listenerList = new EventListenerList();
		AnywareListList.listenerList.remove(TableModelListener.class, l);
	}

	/**
	 * Sends TableChanged event to listeners
	 * @param e the {@link TableModelEvent} to send
	 */
	public void fireTableChanged(final TableModelEvent e)
	{
		if(AnywareListList.listenerList == null)
			AnywareListList.listenerList = new EventListenerList();
		final Object[] listeners = AnywareListList.listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

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
}
