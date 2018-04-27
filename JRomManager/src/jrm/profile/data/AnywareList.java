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

@SuppressWarnings("serial")
public abstract class AnywareList<T extends Anyware> implements Serializable, TableModel, List<T>
{
	private static transient EventListenerList listenerList;
	protected static transient EnumSet<AnywareStatus> filter = null;
	protected transient List<T> filtered_list;

	public AnywareList()
	{
		initTransient();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	protected void initTransient()
	{
		if(listenerList == null)
			listenerList = new EventListenerList();
		if(filter == null)
			filter = EnumSet.allOf(AnywareStatus.class);
		filtered_list = null;
	}

	protected abstract List<T> getList();
	public abstract boolean containsName(String name);

	public void reset()
	{
		this.filtered_list = null;
		fireTableChanged(new TableModelEvent(this));
	}

	public void setFilter(EnumSet<AnywareStatus> filter)
	{
		AnywareList.filter = filter;
		reset();
	}

	public abstract Stream<T> getFilteredStream();

	protected abstract List<T> getFilteredList();

	public abstract TableCellRenderer getColumnRenderer(int columnIndex);

	public abstract int getColumnWidth(int columnIndex);

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
	}

	@Override
	public void addTableModelListener(TableModelListener l)
	{
		listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		listenerList.remove(TableModelListener.class, l);
	}

	public void fireTableChanged(TableModelEvent e)
	{
		Object[] listeners = listenerList.getListenerList();
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
	public boolean contains(Object o)
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
	public <E> E[] toArray(E[] a)
	{
		return getList().toArray(a);
	}

	@Override
	public boolean add(T e)
	{
		return getList().add(e);
	}

	@Override
	public boolean remove(Object o)
	{
		return getList().remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return getList().containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		return getList().addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		return getList().addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		return getList().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		return getList().retainAll(c);
	}

	@Override
	public void clear()
	{
		getList().clear();
	}

	@Override
	public T get(int index)
	{
		return getList().get(index);
	}

	@Override
	public T set(int index, T element)
	{
		return getList().set(index, element);
	}

	@Override
	public void add(int index, T element)
	{
		getList().add(index, element);
	}

	@Override
	public T remove(int index)
	{
		return getList().remove(index);
	}

	@Override
	public int indexOf(Object o)
	{
		return getList().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o)
	{
		return getList().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator()
	{
		return getList().listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index)
	{
		return getList().listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex)
	{
		return getList().subList(fromIndex, toIndex);
	}

	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		boolean ok = false;
		for(Iterator<T> iterator = getFilteredStream().iterator(); iterator.hasNext();)
		{
			AnywareStatus estatus = iterator.next().getStatus();
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

	public abstract long countHave();

	public abstract long countAll();

	public int find(Anyware anyware)
	{
		return getFilteredList().indexOf(anyware);
	}

	public int find(String search)
	{
		return find(StreamEx.of(getFilteredStream()).findFirst(s -> s.getName().toLowerCase().startsWith(search.toLowerCase())).orElse(null));
	}
}
