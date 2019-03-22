package jrm.ui.profile.data;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

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
				return softwareListList.getFilteredList().get(rowIndex);
			case 1:
				return softwareListList.getFilteredList().get(rowIndex).description.toString();
			case 2:
				return String.format("%d/%d", softwareListList.getFilteredList().get(rowIndex).countHave(), softwareListList.getFilteredList().get(rowIndex).countAll()); //$NON-NLS-1$
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
		softwareListList.reset();
		fireTableChanged(new TableModelEvent(this));
	}

}
