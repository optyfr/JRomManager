package jrm.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import jrm.Messages;
import jrm.profile.ProfileNFO;

@SuppressWarnings("serial")
public class FileTableCellRenderer extends DefaultTableCellRenderer
{

	public FileTableCellRenderer()
	{
		super();
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
	{
		if(column==0 && table.getModel() instanceof FileTableModel)
		{
			final ProfileNFO nfo = ((FileTableModel)table.getModel()).getNfoAt(row);
			super.getTableCellRendererComponent(table, nfo.name, isSelected, hasFocus, row, column);
			switch(nfo.mame.getStatus())
			{
				case UPTODATE:
					setForeground(Color.decode("0x00aa00")); //$NON-NLS-1$
					setToolTipText(String.format(Messages.getString("FileTableCellRenderer.IsUpToDate"),nfo.name)); //$NON-NLS-1$
					break;
				case NEEDUPDATE:
					setForeground(Color.decode("0xcc8800")); //$NON-NLS-1$
					setToolTipText(String.format(Messages.getString("FileTableCellRenderer.NeedUpdateFromMame"),nfo.name)); //$NON-NLS-1$
					break;
				case NOTFOUND:
					setForeground(Color.decode("0xcc0000")); //$NON-NLS-1$
					setToolTipText(String.format(Messages.getString("FileTableCellRenderer.StatusUnknownMameNotFound"),nfo.name)); //$NON-NLS-1$
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
				setHorizontalAlignment(SwingConstants.CENTER);
			else
				setToolTipText(getText());
		}
		setBackground(isSelected?Color.decode("0xBBBBDD"):Color.white); //$NON-NLS-1$
		return this;
	}
}
