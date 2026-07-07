/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.ui.basic;

import javax.swing.tree.TreeNode;

/**
 * Interface for tree nodes that support selection state and path operations.
 * <p>
 * Extends {@link TreeNode} to add selection management capabilities for tree-based UI components.
 * Nodes can track their selection state and verify if all descendants are selected.
 * </p>
 *
 * @see AbstractNGTreeNode
 * @see TreeNode
 */
public interface NGTreeNode extends TreeNode {

    /**
     * Returns the path from the root to this node.
     *
     * @return an array of {@link TreeNode} objects representing the path, where the first element is the root
     *         and the last element is this node
     */
    public TreeNode[] getPath();

    /**
     * Returns the user object associated with this node.
     *
     * @return the user object, or {@code null} if none is set
     */
    public Object getUserObject();

    /**
     * Checks whether this node is currently selected.
     *
     * @return {@code true} if this node is selected, {@code false} otherwise
     */
    public boolean isSelected();

    /**
     * Sets the selection state of this node.
     *
     * @param selected {@code true} to select this node, {@code false} to deselect it
     */
    public void setSelected(boolean selected);

    /**
     * Checks whether this node and all its descendants are selected.
     *
     * @return {@code true} if this node and all children are selected, {@code false} otherwise
     */
    public boolean allChildrenSelected();
}
