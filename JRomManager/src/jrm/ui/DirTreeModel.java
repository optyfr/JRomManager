package jrm.ui;

import java.io.File;
import java.io.IOException;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.FileUtils;

@SuppressWarnings("serial")
public class DirTreeModel extends DefaultTreeModel implements TreeModelListener
{
	public DirTreeModel(DirNode root)
	{
		super(root);
		addTreeModelListener(this);
	}

	@Override
	public void treeNodesChanged(TreeModelEvent e)
	{
		DirNode node = (DirNode)e.getTreePath().getLastPathComponent();
		try
		{
			int index = e.getChildIndices()[0];
			DirNode child_node = (DirNode) node.getChildAt(index);
			if(child_node.getUserObject() instanceof String)
			{
				File newdir = new File(node.dir.getFile(), child_node.getUserObject().toString());
				File olddir = child_node.dir.getFile();
				if(olddir.renameTo(newdir))
					child_node.dir = new DirNode.Dir(newdir);
				child_node.setUserObject(child_node.dir);
			}
		}
		catch(NullPointerException exc)
		{
		}
	}

	@Override
	public void treeNodesInserted(TreeModelEvent e)
	{
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e)
	{
		try
		{
			int index = e.getChildIndices()[0];
			Object[] children = e.getChildren();
			DirNode child = (DirNode)children[index];
			FileUtils.deleteDirectory(child.dir.getFile());
		}
		catch(NullPointerException | IOException exc)
		{
		}
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e)
	{
	}
}
