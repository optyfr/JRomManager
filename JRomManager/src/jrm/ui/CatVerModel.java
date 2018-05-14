package jrm.ui;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

@SuppressWarnings("serial")
public class CatVerModel extends DefaultTreeModel
{

	public CatVerModel(NGTreeNode root)
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
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public int getIndex(TreeNode node)
			{
				return 0;
			}
			
			@Override
			public int getChildCount()
			{
				return 0;
			}
			
			@Override
			public TreeNode getChildAt(int childIndex)
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
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setSelected(boolean selected)
			{
				// TODO Auto-generated method stub
				
			}
		});
	}

}
