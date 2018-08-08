/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui.profile.manager;

import java.io.File;
import java.io.IOException;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.io.FileUtils;

import jrm.profile.manager.Dir;

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
				final File newdir = new File(node.getDir().getFile(), child_node.getUserObject().toString());
				final File olddir = child_node.getDir().getFile();
				if(olddir.renameTo(newdir))
					child_node.setDir(new Dir(newdir));
				child_node.setUserObject(child_node.getDir());
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
			FileUtils.deleteDirectory(child.getDir().getFile());
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
