package jrm.profiler.data;

import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	private List<MachineList> ml_list = Collections.singletonList(new MachineList());
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
		columns = new String[] {"name","description"};
		columnsTypes = new Class<?>[] {Object.class,String.class};
		columnsWidths = new int[] {50, 150};
		columnsRenderers = new TableCellRenderer[] {
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					setIcon(null);
					return super.getTableCellRendererComponent(table, "*", isSelected, hasFocus, row, column);
				}
			},
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					setIcon(null);
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}
		}; 
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return columnsTypes[columnIndex];
	}

	public static TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return columnsRenderers[columnIndex]!=null?columnsRenderers[columnIndex]:new DefaultTableCellRenderer();
	}

	public static int getColumnWidth(int columnIndex)
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
			case 0: return ml_list.get(rowIndex);
			case 1: return "All Machines"; 
		}
		return null;
	}

	@Override
	protected List<MachineList> getList()
	{
		return ml_list;
	}

}
