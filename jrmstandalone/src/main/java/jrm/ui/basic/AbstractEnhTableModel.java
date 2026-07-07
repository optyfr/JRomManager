package jrm.ui.basic;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * Abstract base implementation of {@link EnhTableModel} that provides listener management functionality.
 * <p>
 * This class implements the {@link TableModel} listener registration and notification mechanism,
 * allowing subclasses to focus on data representation while inheriting event handling capabilities.
 * </p>
 *
 * @see EnhTableModel
 * @see TableModelListener
 */
public abstract class AbstractEnhTableModel implements EnhTableModel {
    /** The list of registered table model listeners. */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * {@inheritDoc}
     *
     * @param l the {@link TableModelListener} to add
     */
    @Override
    public void addTableModelListener(final TableModelListener l) {
        listenerList.add(TableModelListener.class, l);
    }

    /**
     * {@inheritDoc}
     *
     * @param l the {@link TableModelListener} to remove
     */
    @Override
    public void removeTableModelListener(final TableModelListener l) {
        listenerList.remove(TableModelListener.class, l);
    }

    /**
     * Sends TableChanged event to listeners
     * 
     * @param e the {@link TableModelEvent} to send
     */
    public void fireTableChanged(final TableModelEvent e) {
        final Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2)
            if (listeners[i] == TableModelListener.class)
                ((TableModelListener) listeners[i + 1]).tableChanged(e);
    }
}
