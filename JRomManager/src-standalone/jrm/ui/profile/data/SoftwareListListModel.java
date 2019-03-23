package jrm.ui.profile.data;

import java.util.EnumSet;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.AnywareStatus;
import jrm.profile.data.SoftwareListList;

public class SoftwareListListModel extends AnywareListListModel
{
	private SoftwareListList softwareListList;
	
	public SoftwareListListModel(SoftwareListList softwareListList)
	{
		this.softwareListList = softwareListList;
	}

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
	public int getRowCount()
	{
		return softwareListList.getFilteredList().size();
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
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				return softwareListList.getObject(rowIndex);
			case 1:
				return softwareListList.getDescription(rowIndex);
			case 2:
				return softwareListList.getHaveTot(rowIndex);
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
	}

	@Override
	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return AnywareListListRenderer.columnsRenderers[columnIndex] != null ? AnywareListListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}
	
	public void reset()
	{
		softwareListList.resetCache();
		fireTableChanged(new TableModelEvent(this));
	}

	public void setFilter(final EnumSet<AnywareStatus> filter)
	{
		softwareListList.setFilterCache(filter);
		reset();
	}
}
