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
 * The Interface NGTreeNode.
 */
public interface NGTreeNode extends TreeNode
{
	
	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	@SuppressWarnings("exports")
	public TreeNode[] getPath();
	
	/**
	 * Gets the user object.
	 *
	 * @return the user object
	 */
	public Object getUserObject();
	
	/**
	 * Checks if is selected.
	 *
	 * @return true, if is selected
	 */
	public boolean isSelected();
	
	/**
	 * Sets the selected.
	 *
	 * @param selected the new selected
	 */
	public void setSelected(boolean selected);
	
	/**
	 * All children selected.
	 *
	 * @return true, if successful
	 */
	public boolean allChildrenSelected();
}
