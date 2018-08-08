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

// TODO: Auto-generated Javadoc
/**
 * The Class JCheckBoxTree.
 */
@SuppressWarnings("serial")
public class JCheckBoxTree extends JTree
{
	
	/** The listener list. */
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * The Class CheckChangeEvent.
	 */
	public class CheckChangeEvent extends EventObject
	{
		
		/**
		 * Instantiates a new check change event.
		 *
		 * @param source the source
		 */
		public CheckChangeEvent(final Object source)
		{
			super(source);
		}
	}

	/**
	 * The listener interface for receiving checkChangeEvent events.
	 */
	public interface CheckChangeEventListener extends EventListener
	{
		
		/**
		 * Check state changed.
		 *
		 * @param event the event
		 */
		public void checkStateChanged(CheckChangeEvent event);
	}

	/**
	 * Adds the check change event listener.
	 *
	 * @param listener the listener
	 */
	public void addCheckChangeEventListener(final CheckChangeEventListener listener)
	{
		listenerList.add(CheckChangeEventListener.class, listener);
	}

	/**
	 * Removes the check change event listener.
	 *
	 * @param listener the listener
	 */
	public void removeCheckChangeEventListener(final CheckChangeEventListener listener)
	{
		listenerList.remove(CheckChangeEventListener.class, listener);
	}

	/**
	 * Fire check change event.
	 *
	 * @param evt the evt
	 */
	void fireCheckChangeEvent(final CheckChangeEvent evt)
	{
		final Object[] listeners = listenerList.getListenerList();
		for(int i = 0; i < listeners.length; i++)
		{
			if(listeners[i] == CheckChangeEventListener.class)
			{
				((CheckChangeEventListener) listeners[i + 1]).checkStateChanged(evt);
			}
		}
	}

	// Override
	@Override
	public void setModel(final TreeModel newModel)
	{
		super.setModel(newModel);
		setEnabled(newModel.getChildCount(newModel.getRoot()) > 0);
	}

	/**
	 * Checks if is selected partially.
	 *
	 * @param path the path
	 * @return true, if is selected partially
	 */
	public boolean isSelectedPartially(final TreePath path)
	{
		final NGTreeNode cn = (NGTreeNode) path.getLastPathComponent();
		return cn.isSelected() && cn.getChildCount() > 0 && !cn.allChildrenSelected();
	}

	/**
	 * The Class CheckBoxCellRenderer.
	 */
	private class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer
	{
		
		/** The check box. */
		JTristateCheckBox checkBox;

		/**
		 * Instantiates a new check box cell renderer.
		 */
		public CheckBoxCellRenderer()
		{
			super();
			setLayout(new BorderLayout());
			checkBox = new JTristateCheckBox();
			checkBox.setOpaque(false);
			checkBox.setFont(getFont());
			add(checkBox, BorderLayout.CENTER);
			setOpaque(false);
		}

		@Override
		public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
		{
			final NGTreeNode node = (NGTreeNode) value;
			if(node == null)
				return this;
			final Object obj = node.getUserObject();
			checkBox.setEnabled(tree.isEnabled());
			checkBox.setSelected(node.isSelected());
			checkBox.setText(obj != null ? obj.toString() : null);
			checkBox.setHalfSelected(node.isSelected() && node.getChildCount() > 0 && !node.allChildrenSelected());
			setPreferredSize(new Dimension(Math.max(getPreferredSize().width, checkBox.getFontMetrics(getFont()).stringWidth(checkBox.getText()) + 100), 20));
			return this;
		}
	}

	/**
	 * Instantiates a new j check box tree.
	 *
	 * @param model the model
	 */
	public JCheckBoxTree(final TreeModel model)
	{
		super(model);
		setToggleClickCount(0);
		setCellRenderer(new CheckBoxCellRenderer());

		final DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel()
		{
			@Override
			public void setSelectionPath(final TreePath path)
			{
			}

			@Override
			public void addSelectionPath(final TreePath path)
			{
			}

			@Override
			public void removeSelectionPath(final TreePath path)
			{
			}

			@Override
			public void setSelectionPaths(final TreePath[] pPaths)
			{
			}
		};
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(final MouseEvent e)
			{
				if(!e.isPopupTrigger())
				{
					if(!JCheckBoxTree.this.isEnabled())
						return;
					final TreePath tp = JCheckBoxTree.this.getPathForLocation(e.getX(), e.getY());
					if(tp == null)
						return;
					final boolean checkMode = !((NGTreeNode) tp.getLastPathComponent()).isSelected();
					checkSubTree(tp, checkMode);
					updatePredecessorsWithCheckMode(tp);
					fireCheckChangeEvent(new CheckChangeEvent(new Object()));
					JCheckBoxTree.this.repaint();
				}
				else
					super.mouseClicked(e);
			}
		});
		setSelectionModel(dtsm);
	}

	/**
	 * Sets the selected.
	 *
	 * @param selected the selected
	 * @param nodes the nodes
	 */
	public void setSelected(final boolean selected, final NGTreeNode... nodes)
	{
		for(final NGTreeNode node : nodes)
		{
			final TreePath tp = new TreePath(node.getPath());
			checkSubTree(tp, selected);
			updatePredecessorsWithCheckMode(tp);
		}
		fireCheckChangeEvent(new CheckChangeEvent(new Object()));
		JCheckBoxTree.this.repaint();
	}

	/**
	 * Select.
	 *
	 * @param nodes the nodes
	 */
	public void select(final NGTreeNode... nodes)
	{
		setSelected(true, nodes);
	}

	/**
	 * Unselect.
	 *
	 * @param nodes the nodes
	 */
	public void unselect(final NGTreeNode... nodes)
	{
		setSelected(false, nodes);
	}

	/**
	 * Select all.
	 */
	public void selectAll()
	{
		select((NGTreeNode)getModel().getRoot());
	}

	/**
	 * Select none.
	 */
	public void selectNone()
	{
		unselect((NGTreeNode)getModel().getRoot());
	}

	/**
	 * Update predecessors with check mode.
	 *
	 * @param tp the tp
	 */
	protected void updatePredecessorsWithCheckMode(final TreePath tp)
	{
		final TreePath parentPath = tp.getParentPath();
		if(parentPath == null)
			return;
		final NGTreeNode parentNode = (NGTreeNode) parentPath.getLastPathComponent();
		parentNode.setSelected(false);
		for(int i = 0; i < parentNode.getChildCount(); i++)
		{
			if(((NGTreeNode) parentNode.getChildAt(i)).isSelected())
				parentNode.setSelected(true);
		}
		updatePredecessorsWithCheckMode(parentPath);
	}

	/**
	 * Check sub tree.
	 *
	 * @param tp the tp
	 * @param check the check
	 */
	protected void checkSubTree(final TreePath tp, final boolean check)
	{
		final NGTreeNode node = (NGTreeNode) tp.getLastPathComponent();
		node.setSelected(check);
		for(int i = 0; i < node.getChildCount(); i++)
			checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
	}

}
