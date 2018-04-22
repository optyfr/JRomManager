package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.MachineListListRenderer;

@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	private List<MachineList> ml_list = Collections.singletonList(new MachineList());

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
		return MachineListListRenderer.columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return MachineListListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return MachineListListRenderer.columnsTypes[columnIndex];
	}

	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return MachineListListRenderer.columnsRenderers[columnIndex] != null ? MachineListListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	public int getColumnWidth(int columnIndex)
	{
		return MachineListListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return ml_list.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
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
		return null;
	}

	@Override
	protected List<MachineList> getList()
	{
		return ml_list;
	}

}
