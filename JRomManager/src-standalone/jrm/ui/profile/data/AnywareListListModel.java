package jrm.ui.profile.data;

import java.util.Set;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.AnywareStatus;
import jrm.ui.basic.AbstractEnhTableModel;

public abstract class AnywareListListModel extends AbstractEnhTableModel
{
	public abstract void reset();

	public abstract void setFilter(final Set<AnywareStatus> filter);
	
	@Override
	public TableCellRenderer[] getCellRenderers()
	{
		return AnywareListListRenderer.columnsRenderers;
	}

	@Override
	public int getColumnWidth(int columnIndex)
	{
		return AnywareListListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public String getColumnTT(int columnIndex)
	{
		return AnywareListListRenderer.columns[columnIndex];
	}

	@Override
	public int getColumnCount()
	{
		return AnywareListListRenderer.columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return AnywareListListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return AnywareListListRenderer.columnsTypes[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		// do nothing
	}

	@Override
	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return AnywareListListRenderer.columnsRenderers[columnIndex] != null ? AnywareListListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}


}
