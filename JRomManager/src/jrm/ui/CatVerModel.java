package jrm.ui;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

@SuppressWarnings("serial")
public class CatVerModel extends DefaultTreeModel
{

	public CatVerModel(final NGTreeNode root)
	{
		super(root);
	}

	public CatVerModel()
	{
		super(new AbstractNGTreeNode()
		{

			@Override
			public boolean isLeaf()
			{
				return true;
			}

			@Override
			public TreeNode getParent()
			{
				return null;
			}

			@Override
			public int getIndex(final TreeNode node)
			{
				return 0;
			}

			@Override
			public int getChildCount()
			{
				return 0;
			}

			@Override
			public TreeNode getChildAt(final int childIndex)
			{
				return null;
			}

			@Override
			public boolean getAllowsChildren()
			{
				return false;
			}

			@Override
			public Enumeration<?> children()
			{
				return null;
			}

			@Override
			public Object getUserObject()
			{
				return "Add a catver.ini first";
			}

			@Override
			public boolean isSelected()
			{
				return false;
			}

			@Override
			public void setSelected(final boolean selected)
			{
			}
		});
	}

}
