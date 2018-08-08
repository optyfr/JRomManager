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

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

/**
 * The Class DirTreeCellEditor.
 *
 * @author optyfr
 */
// TODO: Auto-generated Javadoc
public class DirTreeCellEditor extends DefaultTreeCellEditor
{
	
	/**
	 * Instantiates a new dir tree cell editor.
	 *
	 * @param tree the tree
	 * @param renderer the renderer
	 */
	public DirTreeCellEditor(final JTree tree, final DefaultTreeCellRenderer renderer)
	{
		super(tree, renderer);
	}

	/**
	 * Instantiates a new dir tree cell editor.
	 *
	 * @param tree the tree
	 * @param renderer the renderer
	 * @param editor the editor
	 */
	public DirTreeCellEditor(final JTree tree, final DefaultTreeCellRenderer renderer, final TreeCellEditor editor)
	{
		super(tree, renderer, editor);
	}

	@Override
	public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected, final boolean expanded, final boolean leaf, final int row)
	{
		final TreePath path = tree.getPathForRow(row);
		if(path.getPathCount() > 1)
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		return renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, /* hasFocus */true);
	}
}
