package jrm.profiler.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public final class MachineList implements Serializable, TableModel
{
	private EventListenerList listenerList = new EventListenerList();
	private String[] columns = { "name", "description", "cloneof"};
	private Class<?>[] columnsTypes = { String.class,String.class,String.class};
	public ArrayList<Machine> m_list = new ArrayList<>();
	public HashMap<String, Machine> m_byname = new HashMap<>();

	public MachineList()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getRowCount()
	{
		return m_list.size();
	}

	@Override
	public int getColumnCount()
	{
		return columns.length;
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
			case 0: return m_list.get(rowIndex).name;
			case 1: return m_list.get(rowIndex).description.toString();
			case 2: return m_list.get(rowIndex).cloneof;
		}
		return null;
	}

	public Anyware getWare(int rowIndex)
	{
		return m_list.get(rowIndex);
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
	
	public void sort()
	{
		m_list.sort(null);
	}

}
