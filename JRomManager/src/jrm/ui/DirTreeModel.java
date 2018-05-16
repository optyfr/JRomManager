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
	public DirTreeModel(final DirNode root)
	{
		super(root);
		addTreeModelListener(this);
	}

	@Override
	public void treeNodesChanged(final TreeModelEvent e)
	{
		final DirNode node = (DirNode)e.getTreePath().getLastPathComponent();
		try
		{
			final int index = e.getChildIndices()[0];
			final DirNode child_node = (DirNode) node.getChildAt(index);
			if(child_node.getUserObject() instanceof String)
			{
				final File newdir = new File(node.dir.getFile(), child_node.getUserObject().toString());
				final File olddir = child_node.dir.getFile();
				if(olddir.renameTo(newdir))
					child_node.dir = new DirNode.Dir(newdir);
				child_node.setUserObject(child_node.dir);
			}
		}
		catch(final NullPointerException exc)
		{
		}
	}

	@Override
	public void treeNodesInserted(final TreeModelEvent e)
	{
	}

	@Override
	public void treeNodesRemoved(final TreeModelEvent e)
	{
		try
		{
			final int index = e.getChildIndices()[0];
			final Object[] children = e.getChildren();
			final DirNode child = (DirNode)children[index];
			FileUtils.deleteDirectory(child.dir.getFile());
		}
		catch(NullPointerException | IOException exc)
		{
		}
	}

	@Override
	public void treeStructureChanged(final TreeModelEvent e)
	{
	}
}
