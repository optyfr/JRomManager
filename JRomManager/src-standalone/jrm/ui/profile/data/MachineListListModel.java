package jrm.ui.profile.data;

import java.util.Set;

import javax.swing.event.TableModelEvent;

import jrm.profile.data.AnywareStatus;
import jrm.profile.data.MachineListList;

public class MachineListListModel extends AnywareListListModel
{
	private MachineListList machineListList;
	
	private SoftwareListListModel sllmodel;
	
	public MachineListListModel(MachineListList machineListList)
	{
		this.machineListList = machineListList;
		sllmodel = new SoftwareListListModel(machineListList.getSoftwareListList());
	}

	@Override
	public int getRowCount()
	{
		return machineListList.getList().size() + sllmodel.getRowCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if(rowIndex < machineListList.getList().size())
		{
			switch(columnIndex)
			{
				case 0:
					return machineListList.getObject(rowIndex);
				case 1:
					return machineListList.getDescription(rowIndex);
				case 2:
					return machineListList.getHaveTot(rowIndex);
				default:
					return null;
			}
		}
		else
			return sllmodel.getValueAt(rowIndex - machineListList.getList().size(), columnIndex);
	}

	public void reset()
	{
		machineListList.resetCache();
		fireTableChanged(new TableModelEvent(this));
	}
	
	public void setFilter(final Set<AnywareStatus> filter)
	{
		machineListList.setFilterCache(filter);
		reset();
	}
}
