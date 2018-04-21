package jrm.profile.data;

import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.ReportFrame;

@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	private List<MachineList> ml_list = Collections.singletonList(new MachineList());
	protected static transient String[] columns;
	protected static transient Class<?>[] columnsTypes;
	protected static transient TableCellRenderer[] columnsRenderers;
	protected static transient int[] columnsWidths;

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
		if(columns == null)
			columns = new String[] { "name", "description", "have" };
		if(columnsTypes == null)
			columnsTypes = new Class<?>[] { Object.class, String.class, String.class };
		if(columnsWidths == null)
			columnsWidths = new int[] { 70, 150, -80 };
		if(columnsRenderers == null)
			columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
			{
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof MachineList)
					{
						super.getTableCellRendererComponent(table, "*", isSelected, hasFocus, row, column);
						switch(((MachineList) value).getStatus())
						{
							case COMPLETE:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/disk_multiple_green.png")));
								break;
							case MISSING:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/disk_multiple_red.png")));
								break;
							case PARTIAL:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/disk_multiple_orange.png")));
								break;
							case UNKNOWN:
							default:
								setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/disk_multiple_gray.png")));
								break;

						}
						return this;
					}
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}, new DefaultTableCellRenderer()
			{
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}, new DefaultTableCellRenderer()
			{
				{
					setHorizontalAlignment(CENTER);
				}
			} };
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
