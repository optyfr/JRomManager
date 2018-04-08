package jrm.ui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

public class DirTreeCellEditor extends DefaultTreeCellEditor
{
	public DirTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer)
	{
		super(tree, renderer);
	}

	public DirTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer, TreeCellEditor editor)
	{
		super(tree, renderer, editor);
	}

	@Override
	public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row)
	{
		TreePath path = tree.getPathForRow(row);
		if(path.getPathCount() > 1)
			return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
		return renderer.getTreeCellRendererComponent(tree, value, isSelected, expanded, leaf, row, /* hasFocus */true);
	}
}
