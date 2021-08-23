package jrm.ui.batch;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
final class StatusCellRenderer extends DefaultTableCellRenderer
{
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		setBackground(Color.white);
		setHorizontalAlignment(TRAILING);
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}