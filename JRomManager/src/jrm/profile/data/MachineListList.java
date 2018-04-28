package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.AnywareListListRenderer;

@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	private List<MachineList> ml_list = Collections.singletonList(new MachineList());

	public SoftwareListList softwarelist_list = new SoftwareListList();

	public MachineListList()
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

	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return AnywareListListRenderer.columnsRenderers[columnIndex] != null ? AnywareListListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	public int getColumnWidth(int columnIndex)
	{
		return AnywareListListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return ml_list.size() + softwarelist_list.getRowCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if(rowIndex < ml_list.size())
		{
			switch(columnIndex)
			{
				case 0:
					return ml_list.get(rowIndex);
				case 1:
					return "All Machines";
				case 2:
					return String.format("%d/%d", ml_list.get(rowIndex).countHave(), ml_list.get(rowIndex).countAll());
			}
		}
		else
			return softwarelist_list.getValueAt(rowIndex - ml_list.size(), columnIndex);
		return null;
	}

	@Override
	protected List<MachineList> getList()
	{
		return ml_list;
	}

	@Override
	public Stream<MachineList> getFilteredStream()
	{
		return getList().stream();
	}

	@Override
	protected List<MachineList> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(t -> filter.contains(t.getStatus())).sorted().collect(Collectors.toList());
		return filtered_list;
	}

}
