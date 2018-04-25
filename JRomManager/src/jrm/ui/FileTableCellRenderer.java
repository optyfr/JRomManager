package jrm.ui;

import java.awt.Color;
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
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		if(column>1)
			setHorizontalAlignment(CENTER);
		setForeground(Color.black);
		setBackground(isSelected?Color.decode("0xBBBBDD"):Color.white);
		return this;
	}
}
