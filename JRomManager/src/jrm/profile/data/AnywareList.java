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
import java.util.stream.Stream;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import one.util.streamex.StreamEx;

/**
 * A list of {@link Anyware} objects
 * @author optyfr
 *
 * @param <T> extends {@link Anyware} (generally a {@link Machine} or a {@link Software})
 */
@SuppressWarnings("serial")
public abstract class AnywareList<T extends Anyware> extends NameBase implements Serializable, TableModel, List<T>, ByName<T>
{
	/**
	 * Event Listener list for firing events to Swing controls (Table)
	 */
	private static transient EventListenerList listenerList;
	/**
	 * Non permanent filter according scan status of anyware (machines, softwares)
	 */
	protected static transient EnumSet<AnywareStatus> filter = null;
	/**
	 * {@link T} list cache (according current {@link #filter})
	 */
	protected transient List<T> filtered_list;

	/**
	 * The constructor, will initialize transients fields
	 */
	public AnywareList()
	{
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
		if(AnywareList.listenerList == null)
			AnywareList.listenerList = new EventListenerList();
		if(AnywareList.filter == null)
			AnywareList.filter = EnumSet.allOf(AnywareStatus.class);
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
	public void reset()
	{
		this.filtered_list = null;
		fireTableChanged(new TableModelEvent(this));
	}

	/**
	 * resets {@link T} list cache and fire a TableChanged event to listeners
	 * @param filter the new {@link EnumSet} of {@link AnywareStatus} filter to apply
	 */
	public void setFilter(final EnumSet<AnywareStatus> filter)
	{
		AnywareList.filter = filter;
		reset();
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
	protected abstract List<T> getFilteredList();

	/**
	 * get the declared renderer for a given column
	 * @param columnIndex the requested column index
	 * @return a {@link TableCellRenderer} associated with the given columnindex 
	 */
	public abstract TableCellRenderer getColumnRenderer(int columnIndex);

	/**
	 * get the declared width for a given column
	 * @param columnIndex the requested column index
	 * @return a width in pixel (if negative then it's a fixed column width)
	 */
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
		AnywareList.listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(final TableModelListener l)
	{
		AnywareList.listenerList.remove(TableModelListener.class, l);
	}

	/**
	 * Sends TableChanged event to listeners
	 * @param e the {@link TableModelEvent} to send
	 */
	public void fireTableChanged(final TableModelEvent e)
	{
		final Object[] listeners = AnywareList.listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
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
		return find(StreamEx.of(getFilteredStream()).findFirst(s -> s.getName().toLowerCase().startsWith(search.toLowerCase())).orElse(null));
	}
}
