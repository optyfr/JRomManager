package jrm.ui.profile.data;

import java.util.Set;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import jrm.profile.data.AnywareStatus;
import jrm.ui.basic.EnhTableModel;

public abstract class AnywareListListModel implements EnhTableModel
{
	/**
	 * Event Listener list for firing events to Swing controls (Table)
	 */
	private static EventListenerList listenerList = new EventListenerList();

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

	public abstract void setFilter(final Set<AnywareStatus> filter);
}
