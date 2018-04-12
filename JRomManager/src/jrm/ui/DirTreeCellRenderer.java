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
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		return super.getTreeCellRendererComponent(tree, value, sel, expanded, false, row, hasFocus);
	}
}
