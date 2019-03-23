package jrm.ui.profile.data;

import java.util.EnumSet;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.Anyware;
import jrm.profile.data.EntityStatus;
import jrm.ui.basic.EnhTableModel;

public class AnywareModel implements EnhTableModel
{
	private Anyware anyware;

	/**
	 * Event Listener list for firing events to Swing controls (Table)
	 */
	private static transient EventListenerList listenerList = new EventListenerList();

	public AnywareModel(Anyware anyware)
	{
		this.anyware = anyware;
	}

	@Override
	public int getRowCount()
	{
		return anyware.getEntities().size();
	}

	@Override
	public int getColumnCount()
	{
		return AnywareRenderer.columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return AnywareRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return AnywareRenderer.columnsTypes[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch (columnIndex)
		{
			case 0:
				return anyware.getEntities().get(rowIndex);
			case 1:
				return anyware.getEntities().get(rowIndex);
			case 2:
				return anyware.getEntities().get(rowIndex).getProperty("size"); //$NON-NLS-1$
			case 3:
				return anyware.getEntities().get(rowIndex).getProperty("crc"); //$NON-NLS-1$
			case 4:
				return anyware.getEntities().get(rowIndex).getProperty("md5"); //$NON-NLS-1$
			case 5:
				return anyware.getEntities().get(rowIndex).getProperty("sha1"); //$NON-NLS-1$
			case 6:
				return anyware.getEntities().get(rowIndex).getProperty("merge"); //$NON-NLS-1$
			case 7:
				return anyware.getEntities().get(rowIndex).getProperty("status"); //$NON-NLS-1$
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
		listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(TableModelListener l)
	{
		listenerList.remove(TableModelListener.class, l);
	}

	@Override
	public TableCellRenderer[] getCellRenderers()
	{
		return AnywareRenderer.columnsRenderers;
	}

	@Override
	public int getColumnWidth(int columnIndex)
	{
		return AnywareRenderer.columnsWidths[columnIndex];
	}

	@Override
	public String getColumnTT(int columnIndex)
	{
		return AnywareRenderer.columns[columnIndex];
	}
	
	/**
	 * get the declared renderer for a given column
	 * @param columnIndex the requested column index
	 * @return a {@link TableCellRenderer} associated with the given columnindex 
	 */
	@Override
	public TableCellRenderer getColumnRenderer(final int columnIndex)
	{
		return columnIndex < AnywareRenderer.columnsRenderers.length && AnywareRenderer.columnsRenderers[columnIndex] != null ? AnywareRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	/**
	 * Sends TableChanged event to listeners
	 * @param e the {@link TableModelEvent} to send
	 */
	public void fireTableChanged(final TableModelEvent e)
	{
		final Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2)
			if (listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

	/**
	 * resets entities list cache and fire a TableChanged event to listeners
	 */
	public void reset()
	{
		anyware.resetCache();
		fireTableChanged(new TableModelEvent(this));
	}
	
	/**
	 * Set a new Entity status set filter and reset list cache
	 * @param filter the new entity status set filter to apply
	 */
	public void setFilter(final EnumSet<EntityStatus> filter)
	{
		anyware.setFilterCache(filter);
		reset();
	}

}
