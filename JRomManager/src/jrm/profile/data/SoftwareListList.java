package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.SoftwareListListRenderer;

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
		return SoftwareListListRenderer.columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return SoftwareListListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return SoftwareListListRenderer.columnsTypes[columnIndex];
	}

	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return SoftwareListListRenderer.columnsRenderers[columnIndex] != null ? SoftwareListListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	public int getColumnWidth(int columnIndex)
	{
		return SoftwareListListRenderer.columnsWidths[columnIndex];
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

}
