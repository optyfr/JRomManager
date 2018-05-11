package jrm.ui;

import javax.swing.tree.TreeNode;

public interface NGTreeNode extends TreeNode
{
	public TreeNode[] getPath();
	public Object getUserObject();

}
