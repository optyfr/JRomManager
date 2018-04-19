package jrm.profiler.data;

import java.awt.Component;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.ui.ReportFrame;

@SuppressWarnings("serial")
public final class SoftwareListList extends AnywareListList<SoftwareList> implements Serializable
{
	private ArrayList<SoftwareList> sl_list = new ArrayList<>();
	public HashMap<String, SoftwareList> sl_byname = new HashMap<>();
	protected static transient Class<?>[] columnsTypes;
	protected static transient TableCellRenderer[] columnsRenderers;
	protected static transient int[] columnsWidths;

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
		columns = new String[] {"name","description"};
		columnsTypes = new Class<?>[] {Object.class,String.class};
		columnsWidths = new int[] {50, 150};
		columnsRenderers = new TableCellRenderer[] {
			new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					setIcon(new ImageIcon(ReportFrame.class.getResource("/jrm/resources/icons/disk_multiple.png")));
					return super.getTableCellRendererComponent(table, value instanceof SoftwareList?((SoftwareList)value).name:value, isSelected, hasFocus, row, column);
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
		return getFilteredList().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0: return getFilteredList().get(rowIndex);
			case 1: return getFilteredList().get(rowIndex).description.toString();
		}
		return null;
	}

	@Override
	protected List<SoftwareList> getList()
	{
		return sl_list;
	}
	

}
