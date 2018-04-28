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

import jrm.ui.AnywareListListRenderer;

@SuppressWarnings("serial")
public final class SoftwareListList extends AnywareListList<SoftwareList> implements Serializable
{
	private ArrayList<SoftwareList> sl_list = new ArrayList<>();
	public HashMap<String, SoftwareList> sl_byname = new HashMap<>();

	public SoftwareListList()
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
		return getFilteredList().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				return getFilteredList().get(rowIndex);
			case 1:
				return getFilteredList().get(rowIndex).description.toString();
			case 2:
				return String.format("%d/%d", getFilteredList().get(rowIndex).countHave(), getFilteredList().get(rowIndex).countAll());
		}
		return null;
	}

	@Override
	protected List<SoftwareList> getList()
	{
		return sl_list;
	}

	public Stream<SoftwareList> getFilteredStream()
	{
		return getList().stream().filter(t -> {
			if(!t.getSystem().isSelected())
				return false;
			return true;
		});
	}

	@Override
	protected List<SoftwareList> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(t -> filter.contains(t.getStatus())).sorted().collect(Collectors.toList());
		return filtered_list;
	}

}
