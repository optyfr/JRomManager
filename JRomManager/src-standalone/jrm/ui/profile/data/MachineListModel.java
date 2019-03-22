package jrm.ui.profile.data;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.Machine;
import jrm.profile.data.MachineList;

public class MachineListModel extends AnywareListModel
{
	MachineList machineList;

	public MachineListModel(MachineList machineList)
	{
		this.machineList = machineList;
	}

	@Override
	public TableCellRenderer[] getCellRenderers()
	{
		return MachineListRenderer.columnsRenderers;
	}

	@Override
	public int getColumnWidth(int columnIndex)
	{
		return MachineListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public String getColumnTT(int columnIndex)
	{
		return MachineListRenderer.columns[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return machineList.getFilteredList().size();
	}

	@Override
	public int getColumnCount()
	{
		return MachineListRenderer.columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return MachineListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return MachineListRenderer.columnsTypes[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex==7;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		final Machine machine = machineList.getFilteredList().get(rowIndex);
		switch(columnIndex)
		{
			case 0:
				return machine;
			case 1:
				return machine;
			case 2:
				return machine.description.toString();
			case 3:
				return String.format("%d/%d", machine.countHave(), machine.countAll()); //$NON-NLS-1$
			case 4:
				return machine.cloneof != null ? (machineList.containsName(machine.cloneof) ? machineList.getByName(machine.cloneof) : machine.cloneof) : null;
			case 5:
				return machine.romof != null && !machine.romof.equals(machine.cloneof) ? (machineList.containsName(machine.romof) ? machineList.getByName(machine.romof) : machine.romof) : null;
			case 6:
				return machine.sampleof != null ? (machineList.samplesets.containsName(machine.sampleof) ? machineList.samplesets.getByName(machine.sampleof) : machine.sampleof) : null;
			case 7:
				return machine.selected;
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(columnIndex==7 && aValue instanceof Boolean)
		{
			final Machine machine = machineList.getFilteredList().get(rowIndex);
			machine.selected = (Boolean)aValue;
		}
	}

	@Override
	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return MachineListRenderer.columnsRenderers[columnIndex];
	}

	public void reset()
	{
		machineList.reset();
		fireTableChanged(new TableModelEvent(this));
	}

	@Override
	public MachineList getList()
	{
		return machineList;
	}
}
