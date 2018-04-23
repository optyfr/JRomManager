package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.Profile;
import jrm.profile.data.Driver.StatusType;
import jrm.ui.MachineListRenderer;

@SuppressWarnings("serial")
public final class MachineList extends AnywareList<Machine> implements Serializable
{
	private ArrayList<Machine> m_list = new ArrayList<>();
	public HashMap<String, Machine> m_byname = new HashMap<>();

	public MachineList()
	{
		initTransient();
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	protected void initTransient()
	{
		super.initTransient();
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

	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return MachineListRenderer.columnsRenderers[columnIndex] != null ? MachineListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	public int getColumnWidth(int columnIndex)
	{
		return MachineListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return getFilteredList().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Machine machine = getFilteredList().get(rowIndex);
		switch(columnIndex)
		{
			case 0:
				return machine;
			case 1:
				return machine;
			case 2:
				return machine.description.toString();
			case 3:
				return String.format("%d/%d", machine.countHave(), machine.roms.size() + machine.disks.size());
			case 4:
				return machine.cloneof != null ? m_byname.get(getFilteredList().get(rowIndex).cloneof) : null;
			case 5:
				return machine.romof != null && !machine.romof.equals(machine.cloneof) ? m_byname.get(machine.romof) : null;
			case 6:
				return machine.sampleof;
		}
		return null;
	}

	@Override
	protected List<Machine> getList()
	{
		return m_list;
	}

	public Stream<Machine> getFilteredStream()
	{
		final boolean filterIncludeClones = Profile.curr_profile.getProperty("filter.InclClones", true);
		final boolean filterIncludeDisks = Profile.curr_profile.getProperty("filter.InclDisks", true);
		final Driver.StatusType filterMinDriverStatus = Driver.StatusType.valueOf(Profile.curr_profile.getProperty("filter.DriverStatus", Driver.StatusType.preliminary.toString()));
		return getList().stream().filter(t -> {
			if(filterMinDriverStatus==StatusType.imperfect && t.driver.getStatus()==StatusType.preliminary)
				return false;
			if(filterMinDriverStatus==StatusType.good && t.driver.getStatus()!=StatusType.good)
				return false;
			if(!filterIncludeClones && t.isClone())
				return false;
			if(!filterIncludeDisks && t.disks.size()>0)
				return false;
			if(!t.getSystem().isSelected())
				return false;
			return true;
		});
	}
	
	@Override
	protected List<Machine> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(t -> {
				return filter.contains(t.getStatus());
			}).sorted().collect(Collectors.toList());
		return filtered_list;
	}

	@Override
	public long countAll()
	{
		return getFilteredStream().count();
	}

	@Override
	public long countHave()
	{
		return getFilteredStream().filter(t -> {
			return t.getStatus() == AnywareStatus.COMPLETE;
		}).count();
	}

}
