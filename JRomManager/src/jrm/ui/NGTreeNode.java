package jrm.ui;

import javax.swing.tree.TreeNode;

public interface NGTreeNode extends TreeNode
{
	public TreeNode[] getPath();
	public Object getUserObject();
	public boolean isSelected();
	public void setSelected(boolean selected);
	public boolean allChildrenSelected();
	public void setAllChildrenSelected(boolean selected);
}
