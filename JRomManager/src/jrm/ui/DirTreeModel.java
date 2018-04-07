package jrm.ui;

import javax.swing.tree.DefaultTreeModel;

@SuppressWarnings("serial")
public class DirTreeModel extends DefaultTreeModel
{
	public DirTreeModel(DirNode root)
	{
		super(root);
	}
}
