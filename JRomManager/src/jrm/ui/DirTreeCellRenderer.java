package jrm.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

@SuppressWarnings("serial")
public class DirTreeCellRenderer extends DefaultTreeCellRenderer
{

	public DirTreeCellRenderer()
	{
		super();
		setOpenIcon(new ImageIcon(getClass().getResource("/jrm/resources/folder_open.png"))); //$NON-NLS-1$
		setClosedIcon(new ImageIcon(getClass().getResource("/jrm/resources/folder_closed.png"))); //$NON-NLS-1$
	}

	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
	{
		return super.getTreeCellRendererComponent(tree, value, sel, expanded, false, row, hasFocus);
	}
}
