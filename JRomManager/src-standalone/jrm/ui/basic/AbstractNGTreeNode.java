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
package jrm.ui.basic;

import javax.swing.tree.TreeNode;

/**
 * The Class AbstractNGTreeNode.
 */
public abstract class AbstractNGTreeNode implements NGTreeNode
{
	@Override
	public boolean allChildrenSelected()
	{
		for(int i = 0; i < getChildCount(); i++)
		{
			if(!((NGTreeNode)getChildAt(i)).allChildrenSelected())
				return false;
		}
		return isSelected();
	}

	/**
	 * Returns the path from the root, to get to this node. The last element in the path is this node.
	 *
	 * @return an array of TreeNode objects giving the path, where the first element in the path is the root and the last element is this node.
	 */
	@SuppressWarnings("exports")
	@Override
	public TreeNode[] getPath()
	{
		return getPathToRoot(this, 0);
	}

	/**
	 * Builds the parents of node up to and including the root node, where the original node is the last element in the returned array. The length of the returned array gives the node's depth in the tree.
	 *
	 * @param aNode
	 *            the TreeNode to get the path for
	 * @param depth
	 *            an int giving the number of steps already taken towards the root (on recursive calls), used to size the returned array
	 * @return an array of TreeNodes giving the path from the root to the specified node
	 */
	protected TreeNode[] getPathToRoot(final TreeNode aNode, int depth)
	{
		TreeNode[] retNodes;

		/*
		 * Check for null, in case someone passed in a null node, or they passed in an element that isn't rooted at root.
		 */
		if(aNode == null)
		{
			if(depth == 0)
				return new TreeNode[0];
			else
				retNodes = new TreeNode[depth];
		}
		else
		{
			depth++;
			retNodes = getPathToRoot(aNode.getParent(), depth);
			retNodes[retNodes.length - depth] = aNode;
		}
		return retNodes;
	}

}
