package jrm.ui;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class DirTreeSelectionListener implements TreeSelectionListener
{
	JTable profilesList;

	public DirTreeSelectionListener(final JTable profilesList)
	{
		this.profilesList = profilesList;
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e)
	{
		final JTree tree = (JTree) e.getSource();
		final DirNode selectedNode = (DirNode) tree.getLastSelectedPathComponent();
		if(selectedNode != null)
		{
			((FileTableModel) profilesList.getModel()).populate((DirNode.Dir) selectedNode.getUserObject());
			profilesList.getColumn(profilesList.getColumnName(0)).setCellRenderer(new FileTableCellRenderer());
		}
	}

}
