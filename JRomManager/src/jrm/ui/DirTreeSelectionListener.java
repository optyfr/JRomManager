package jrm.ui;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class DirTreeSelectionListener implements TreeSelectionListener
{
	JTable profilesList;

	public DirTreeSelectionListener(JTable profilesList)
	{
		this.profilesList = profilesList;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		JTree tree = (JTree) e.getSource();
		DirNode selectedNode = (DirNode) tree.getLastSelectedPathComponent();
		if(selectedNode != null)
		{
			((FileTableModel) profilesList.getModel()).populate((DirNode.Dir) selectedNode.getUserObject());
			profilesList.getColumn("Profile").setCellRenderer(new FileTableCellRenderer());
		}
	}

}
