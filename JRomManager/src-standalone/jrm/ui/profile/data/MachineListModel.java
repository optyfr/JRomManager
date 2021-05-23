package jrm.ui.profile.data;

import java.util.EnumSet;

import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.AnywareStatus;
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
				return machine.getCloneof() != null ? (machineList.containsName(machine.getCloneof()) ? machineList.getByName(machine.getCloneof()) : machine.getCloneof()) : null;
			case 5:
				return machine.getRomof() != null && !machine.getRomof().equals(machine.getCloneof()) ? (machineList.containsName(machine.getRomof()) ? machineList.getByName(machine.getRomof()) : machine.getRomof()) : null;
			case 6:
				return machine.getSampleof() != null ? (machineList.samplesets.containsName(machine.getSampleof()) ? machineList.samplesets.getByName(machine.getSampleof()) : machine.getSampleof()) : null;
			case 7:
				return machine.isSelected();
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		if(columnIndex==7 && aValue instanceof Boolean)
		{
			final Machine machine = machineList.getFilteredList().get(rowIndex);
			machine.setSelected((Boolean)aValue);
		}
	}

	@Override
	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return MachineListRenderer.columnsRenderers[columnIndex];
	}

	public void reset()
	{
		machineList.resetCache();
		fireTableChanged(new TableModelEvent(this));
	}

	@Override
	public MachineList getList()
	{
		return machineList;
	}

	@Override
	public void setFilter(EnumSet<AnywareStatus> filter)
	{
		machineList.setFilterCache(filter);
		reset();
	}
}
