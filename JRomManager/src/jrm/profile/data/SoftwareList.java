package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.SoftwareListRenderer;

@SuppressWarnings("serial")
public class SoftwareList extends AnywareList<Software> implements Systm, Serializable, Comparable<SoftwareList>
{
	public String name; // required
	public StringBuffer description = new StringBuffer();

	private List<Software> s_list = new ArrayList<>();
	public Map<String, Software> s_byname = new HashMap<>();

	public SoftwareList()
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

	public boolean add(Software software)
	{
		software.sl = this;
		s_byname.put(software.name, software);
		return s_list.add(software);
	}

	@Override
	public int compareTo(SoftwareList o)
	{
		return this.name.compareTo(o.name);
	}

	@Override
	public int getColumnCount()
	{
		return SoftwareListRenderer.columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return SoftwareListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return SoftwareListRenderer.columnsTypes[columnIndex];
	}

	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return SoftwareListRenderer.columnsRenderers[columnIndex] != null ? SoftwareListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	public int getColumnWidth(int columnIndex)
	{
		return SoftwareListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return getFilteredList().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Software software = getFilteredList().get(rowIndex);
		switch(columnIndex)
		{
			case 0:
				return software;
			case 1:
				return software;
			case 2:
				return software.description.toString();
			case 3:
				return String.format("%d/%d", software.countHave(), software.roms.size() + software.disks.size());
			case 4:
				return software.cloneof != null ? s_byname.get(software.cloneof) : null;
		}
		return null;
	}

	@Override
	protected List<Software> getList()
	{
		return s_list;
	}

	@Override
	public Type getType()
	{
		return Type.SOFTWARELIST;
	}

	@Override
	public Systm getSystem()
	{
		return this;
	}
	
	@Override
	public String toString()
	{
		return "["+getType()+"] "+description.toString();
	}

	@Override
	public String getName()
	{
		return name;
	}

}
