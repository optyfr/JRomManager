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

import jrm.misc.Log;
import jrm.profile.manager.Dir;

/**
 * The Class DirTreeModel.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public class DirTreeModel extends DefaultTreeModel implements TreeModelListener
{
	
	/**
	 * Instantiates a new dir tree model.
	 *
	 * @param root the root
	 */
	public DirTreeModel(final DirNode root)
	{
		super(root);
		addTreeModelListener(this);
	}

	@SuppressWarnings("exports")
	@Override
	public void treeNodesChanged(final TreeModelEvent e)
	{
		final DirNode node = (DirNode)e.getTreePath().getLastPathComponent();
		try
		{
			final int index = e.getChildIndices()[0];
			final DirNode childNode = (DirNode) node.getChildAt(index);
			if(childNode.getUserObject() instanceof String)
			{
				final File newdir = new File(node.getDir().getFile(), childNode.getUserObject().toString());
				final File olddir = childNode.getDir().getFile();
				if(olddir.renameTo(newdir))
					childNode.setDir(new Dir(newdir));
				childNode.setUserObject(childNode.getDir());
			}
		}
		catch(final NullPointerException exc)
		{
			Log.err(exc.getMessage(), exc);
		}
	}

	@SuppressWarnings("exports")
	@Override
	public void treeNodesInserted(final TreeModelEvent e)
	{
		// do nothing
	}

	@SuppressWarnings("exports")
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
			Log.err(exc.getMessage(), exc);
		}
	}

	@SuppressWarnings("exports")
	@Override
	public void treeStructureChanged(final TreeModelEvent e)
	{
		// do nothing
	}
}
