package jrm.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import jrm.profile.ProfileNFO;

@SuppressWarnings("serial")
public class FileTableCellRenderer extends DefaultTableCellRenderer
{

	public FileTableCellRenderer()
	{
		super();
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if(value instanceof ProfileNFO)
		{
			ProfileNFO nfo = (ProfileNFO)value;
			super.getTableCellRendererComponent(table, nfo.name, isSelected, hasFocus, row, column);
			switch(nfo.mame.getStatus())
			{
				case UPTODATE:
					setForeground(Color.decode("0x00aa00"));
					setToolTipText(String.format("'%s' is up to date",nfo.name));
					break;
				case NEEDUPDATE:
					setForeground(Color.decode("0xcc8800"));
					setToolTipText(String.format("'%s' need to be updated from Mame executable",nfo.name));
					break;
				case NOTFOUND:
					setForeground(Color.decode("0xcc0000"));
					setToolTipText(String.format("'%s' update status is not known because Mame executable was not found",nfo.name));
					break;
				default:
					setForeground(Color.black);
					setToolTipText(getText());
					break;
			}
		}
		else
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setForeground(Color.black);
			if(column>1)
				setHorizontalAlignment(CENTER);
			else
				setToolTipText(getText());
		}
		setBackground(isSelected?Color.decode("0xBBBBDD"):Color.white);
		return this;
	}
}
