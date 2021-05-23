package jrm.ui.basic;

import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class CenteredTableCellRenderer extends DefaultTableCellRenderer
{

	public CenteredTableCellRenderer()
	{
		setHorizontalAlignment(SwingConstants.CENTER);
	}

}
