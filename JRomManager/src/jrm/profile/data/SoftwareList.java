package jrm.profile.data;

import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.ReportFrame;

@SuppressWarnings("serial")
public class SoftwareList extends AnywareList<Software> implements System, Serializable, Comparable<SoftwareList>
{
	public String name; // required
	public StringBuffer description = new StringBuffer();

	private List<Software> s_list = new ArrayList<>();
	public Map<String, Software> s_byname = new HashMap<>();

	protected static transient String[] columns;
	protected static transient Class<?>[] columnsTypes;
	protected static transient TableCellRenderer[] columnsRenderers;
	protected static transient int[] columnsWidths;

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
		if(columns == null)
			columns = new String[] { "", "name", "description", "have", "cloneof" };
		if(columnsTypes == null)
			columnsTypes = new Class<?>[] { Object.class, Object.class, String.class, String.class, Object.class };
		if(columnsWidths == null)
			columnsWidths = new int[] { -20, 40, 200, -45, 40 };
		if(columnsRenderers == null)
			columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
			{
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Software)
					{
						super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
						switch(((Software) value).getStatus())
						{
							case COMPLETE:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_green.png")));
								break;
							case PARTIAL:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_orange.png")));
								break;
							case MISSING:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_red.png")));
								break;
							case UNKNOWN:
							default:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_gray.png")));
								break;
						}
						return this;
					}
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}, new DefaultTableCellRenderer()
			{
				{
					setIcon(null);
				}

				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Software)
					{
						super.getTableCellRendererComponent(table, ((Software) value).name, isSelected, hasFocus, row, column);
						return this;
					}
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}, null, new DefaultTableCellRenderer()
			{
				{
					setHorizontalAlignment(CENTER);
				}
			}, new DefaultTableCellRenderer()
			{
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof Software)
					{
						super.getTableCellRendererComponent(table, ((Software) value).name, isSelected, hasFocus, row, column);
						switch(((Software) value).getStatus())
						{
							case COMPLETE:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_green.png")));
								break;
							case PARTIAL:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_orange.png")));
								break;
							case MISSING:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_red.png")));
								break;
							case UNKNOWN:
							default:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/folder_closed_gray.png")));
								break;
						}
						return this;
					}
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			} };
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
		return columns.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return columnsTypes[columnIndex];
	}

	public TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return columnsRenderers[columnIndex] != null ? columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	public int getColumnWidth(int columnIndex)
	{
		return columnsWidths[columnIndex];
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
	public System getSystem()
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
