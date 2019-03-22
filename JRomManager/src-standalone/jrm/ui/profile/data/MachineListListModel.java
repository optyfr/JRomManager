package jrm.ui.profile.data;

import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.MachineListList;

public class MachineListListModel extends AnywareListListModel
{
	private MachineListList machineListList;
	
	private SoftwareListListModel sllmodel;
	
	public MachineListListModel(MachineListList machineListList)
	{
		this.machineListList = machineListList;
		sllmodel = new SoftwareListListModel(machineListList.softwarelist_list);
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
		return machineListList.getList().size() + sllmodel.getRowCount();
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
		if(rowIndex < machineListList.getList().size())
		{
			switch(columnIndex)
			{
				case 0:
					return machineListList.getList().get(rowIndex);
				case 1:
					return machineListList.profile.session.msgs.getString("MachineListList.AllMachines"); //$NON-NLS-1$
				case 2:
					return String.format("%d/%d", machineListList.getList().get(rowIndex).countHave(), machineListList.getList().get(rowIndex).countAll()); //$NON-NLS-1$
			}
		}
		else
			return sllmodel.getValueAt(rowIndex - machineListList.getList().size(), columnIndex);
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
		machineListList.reset();
		fireTableChanged(new TableModelEvent(this));
	}
	
}
