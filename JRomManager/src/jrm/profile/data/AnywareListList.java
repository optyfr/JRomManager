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
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public abstract class AnywareListList<T extends AnywareList<? extends Anyware>> implements Serializable, TableModel, List<T>
{
	private static transient EventListenerList listenerList;
	protected static transient EnumSet<AnywareStatus> filter = null;
	protected transient List<T> filtered_list;

	public AnywareListList()
	{
		initTransient();
	}

	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	protected void initTransient()
	{
		if(AnywareListList.listenerList == null)
			AnywareListList.listenerList = new EventListenerList();
		if(AnywareListList.filter == null)
			AnywareListList.filter = EnumSet.allOf(AnywareStatus.class);
		filtered_list = null;
	}

	public abstract void reset();
	public abstract void setFilter(final EnumSet<AnywareStatus> filter);

	public abstract Stream<T> getFilteredStream();

	protected abstract List<T> getFilteredList();

	public abstract TableCellRenderer getColumnRenderer(int columnIndex);

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

	public void fireTableChanged(final TableModelEvent e)
	{
		if(AnywareListList.listenerList == null)
			AnywareListList.listenerList = new EventListenerList();
		final Object[] listeners = AnywareListList.listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

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
