package jrm.profiler.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public final class SoftwareListList implements Serializable, TableModel
{
	private transient EventListenerList listenerList = new EventListenerList();
	private transient String[] columns = { "name", "description" };
	private transient Class<?>[] columnsTypes = { String.class, String.class };
	
	public ArrayList<SoftwareList> sl_list = new ArrayList<>();
	public HashMap<String, SoftwareList> sl_byname = new HashMap<>();

	public SoftwareListList()
	{
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		listenerList = new EventListenerList();
		columns = new String[] {"name","description"};
		columnsTypes = new Class<?>[] {String.class,String.class};
	}
	
	@Override
	public int getRowCount()
	{
		return sl_list.size();
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
	public Class<?> getColumnClass(int columnIndex)
	{
		return columnsTypes[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0: return sl_list.get(rowIndex).name;
			case 1: return sl_list.get(rowIndex).description.toString();
		}
		return null;
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
	
	public void sort()
	{
		sl_list.sort(null);
	}

}
