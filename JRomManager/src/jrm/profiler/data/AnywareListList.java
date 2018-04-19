package jrm.profiler.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public abstract class AnywareListList<T extends AnywareList<? extends Anyware>> implements Serializable, TableModel, List<T>
{
	private transient EventListenerList listenerList;
	protected transient String[] columns;
	private transient List<T> filtered_list;

	public AnywareListList()
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
		listenerList = new EventListenerList();
		filtered_list = null;
	}
	
	protected List<T> getFilteredList()
	{
		if(filtered_list==null)
			filtered_list = getList().stream().sorted().collect(Collectors.toList());
		return filtered_list;
	}
	
	@Override
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return columns[columnIndex];
	}

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
		if(listenerList==null)
			listenerList = new EventListenerList();
		listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		if(listenerList==null)
			listenerList = new EventListenerList();
		listenerList.remove(TableModelListener.class, l);
	}

	public void fireTableChanged(TableModelEvent e)
	{
		if(listenerList==null)
			listenerList = new EventListenerList();
		Object[] listeners = listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

	protected abstract List<T> getList();

	@Override
	public T get(int index)
	{
		return getList().get(index);
	}
	
	@Override
	public boolean add(T list)
	{
		return getList().add(list);
	}
	
	@Override
	public void forEach(Consumer<? super T> action)
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
}
