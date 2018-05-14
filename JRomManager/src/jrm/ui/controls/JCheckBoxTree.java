package jrm.ui.controls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.EventObject;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import jrm.ui.NGTreeNode;

@SuppressWarnings("serial")
public class JCheckBoxTree extends JTree
{
	protected EventListenerList listenerList = new EventListenerList();

	public class CheckChangeEvent extends EventObject
	{
		public CheckChangeEvent(Object source)
		{
			super(source);
		}
	}

	public interface CheckChangeEventListener extends EventListener
	{
		public void checkStateChanged(CheckChangeEvent event);
	}

	public void addCheckChangeEventListener(CheckChangeEventListener listener)
	{
		listenerList.add(CheckChangeEventListener.class, listener);
	}

	public void removeCheckChangeEventListener(CheckChangeEventListener listener)
	{
		listenerList.remove(CheckChangeEventListener.class, listener);
	}

	void fireCheckChangeEvent(CheckChangeEvent evt)
	{
		Object[] listeners = listenerList.getListenerList();
		for(int i = 0; i < listeners.length; i++)
		{
			if(listeners[i] == CheckChangeEventListener.class)
			{
				((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
			}
		}
	}

	// Override
	public void setModel(TreeModel newModel)
	{
		super.setModel(newModel);
		setEnabled(newModel.getChildCount(newModel.getRoot()) > 0);
	}

	public boolean isSelectedPartially(TreePath path)
	{
		NGTreeNode cn = (NGTreeNode) path.getLastPathComponent();
		return cn.isSelected() && cn.getChildCount() > 0 && !cn.allChildrenSelected();
	}

	private class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer
	{
		JTristateCheckBox checkBox;

		public CheckBoxCellRenderer()
		{
			super();
			this.setLayout(new BorderLayout());
			checkBox = new JTristateCheckBox();
			checkBox.setOpaque(false);
			checkBox.setFont(getFont());
			add(checkBox, BorderLayout.CENTER);
			setOpaque(false);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			NGTreeNode node = (NGTreeNode) value;
			if(node == null)
				return this;
			Object obj = node.getUserObject();
			checkBox.setEnabled(tree.isEnabled());
			checkBox.setSelected(node.isSelected());
			checkBox.setText(obj != null ? obj.toString() : null);
			checkBox.setHalfSelected(node.isSelected() && node.getChildCount() > 0 && !node.allChildrenSelected());
			this.setPreferredSize(new Dimension(Math.max(this.getPreferredSize().width, checkBox.getFontMetrics(getFont()).stringWidth(checkBox.getText()) + 100), 20));
			return this;
		}
	}

	public JCheckBoxTree(TreeModel model)
	{
		super(model);
		this.setToggleClickCount(0);
		this.setCellRenderer(new CheckBoxCellRenderer());

		DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel()
		{
			@Override
			public void setSelectionPath(TreePath path)
			{
			}

			@Override
			public void addSelectionPath(TreePath path)
			{
			}

			@Override
			public void removeSelectionPath(TreePath path)
			{
			}

			@Override
			public void setSelectionPaths(TreePath[] pPaths)
			{
			}
		};
		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				if(!JCheckBoxTree.this.isEnabled())
					return;
				TreePath tp = JCheckBoxTree.this.getPathForLocation(arg0.getX(), arg0.getY());
				if(tp == null)
					return;
				boolean checkMode = !((NGTreeNode) tp.getLastPathComponent()).isSelected();
				checkSubTree(tp, checkMode);
				updatePredecessorsWithCheckMode(tp);
				fireCheckChangeEvent(new CheckChangeEvent(new Object()));
				JCheckBoxTree.this.repaint();
			}
		});
		this.setSelectionModel(dtsm);
	}

	protected void updatePredecessorsWithCheckMode(TreePath tp)
	{
		TreePath parentPath = tp.getParentPath();
		if(parentPath == null)
			return;
		NGTreeNode parentNode = (NGTreeNode) parentPath.getLastPathComponent();
		parentNode.setSelected(false);
		for(int i = 0; i < parentNode.getChildCount(); i++)
		{
			if(((NGTreeNode) parentNode.getChildAt(i)).isSelected())
				parentNode.setSelected(true);
		}
		updatePredecessorsWithCheckMode(parentPath);
	}

	protected void checkSubTree(TreePath tp, boolean check)
	{
		NGTreeNode node = (NGTreeNode) tp.getLastPathComponent();
		node.setSelected(check);
		for(int i = 0; i < node.getChildCount(); i++)
			checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
	}

}
