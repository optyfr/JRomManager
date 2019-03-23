package jrm.ui.profile.data;

import java.util.EnumSet;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.AnywareStatus;
import jrm.ui.basic.EnhTableModel;

public abstract class AnywareListListModel implements EnhTableModel
{
	/**
	 * Event Listener list for firing events to Swing controls (Table)
	 */
	private static transient EventListenerList listenerList = new EventListenerList();

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
	public void addTableModelListener(TableModelListener l)
	{
		listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		listenerList.remove(TableModelListener.class, l);
	}
	
	/**
	 * Sends TableChanged event to listeners
	 * @param e the {@link TableModelEvent} to send
	 */
	public void fireTableChanged(final TableModelEvent e)
	{
		final Object[] listeners = listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

	public abstract void reset();

	public abstract void setFilter(final EnumSet<AnywareStatus> filter);
}
