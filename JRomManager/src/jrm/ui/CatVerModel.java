package jrm.ui;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

@SuppressWarnings("serial")
public class CatVerModel extends DefaultTreeModel
{

	public CatVerModel(TreeNode root)
	{
		super(root);
	}

	public CatVerModel(TreeNode root, boolean asksAllowsChildren)
	{
		super(root, asksAllowsChildren);
	}

}
