package jrm.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class FileTableCellRenderer extends DefaultTableCellRenderer
{

	public FileTableCellRenderer()
	{
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
