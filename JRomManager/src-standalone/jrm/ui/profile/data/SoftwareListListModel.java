package jrm.ui.profile.data;

import java.util.Set;

import javax.swing.event.TableModelEvent;

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
	public int getRowCount()
	{
		return softwareListList.getFilteredList().size();
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
			default:
				return null;
		}
	}

	public void reset()
	{
		softwareListList.resetCache();
		fireTableChanged(new TableModelEvent(this));
	}

	public void setFilter(final Set<AnywareStatus> filter)
	{
		softwareListList.setFilterCache(filter);
		reset();
	}
}
