package jrm.ui.basic;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public abstract class AbstractEnhTableModel implements EnhTableModel
{
	private final EventListenerList listenerList = new EventListenerList();

	@SuppressWarnings("exports")
	@Override
	public void addTableModelListener(final TableModelListener l)
	{
		listenerList.add(TableModelListener.class, l);
	}

	@SuppressWarnings("exports")
	@Override
	public void removeTableModelListener(final TableModelListener l)
	{
		listenerList.remove(TableModelListener.class, l);
	}

	/**
	 * Sends TableChanged event to listeners
	 * 
	 * @param e
	 *            the {@link TableModelEvent} to send
	 */
	@SuppressWarnings("exports")
	public void fireTableChanged(final TableModelEvent e)
	{
		final Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			if (listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}
}
