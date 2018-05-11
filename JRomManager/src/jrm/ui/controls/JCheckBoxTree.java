package jrm.ui.controls;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import jrm.ui.NGTreeNode;

@SuppressWarnings("serial")
public class JCheckBoxTree extends JTree
{

	JCheckBoxTree selfPointer = this;

	HashMap<TreePath, NGTreeNode> nodesCheckingState;
	HashSet<TreePath> checkedPaths = new HashSet<TreePath>();

	// Defining a new event type for the checking mechanism and preparing event-handling mechanism
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
		resetCheckingState();
	}

	// New method that returns only the checked paths (totally ignores original "selection" mechanism)
	public TreePath[] getCheckedPaths()
	{
		return checkedPaths.toArray(new TreePath[checkedPaths.size()]);
	}

	// Returns true in case that the node is selected, has children but not all of them are selected
	public boolean isSelectedPartially(TreePath path)
	{
		NGTreeNode cn = nodesCheckingState.get(path);
		return cn.isSelected() && cn.getChildCount()>0 && !cn.allChildrenSelected();
	}

	private void resetCheckingState()
	{
		nodesCheckingState = new HashMap<TreePath, NGTreeNode>();
		checkedPaths = new HashSet<TreePath>();
		NGTreeNode node = (NGTreeNode) getModel().getRoot();
		if(node == null)
		{
			return;
		}
		addSubtreeToCheckingStateTracking(node);
	}

	// Creating data structure of the current model for the checking mechanism
	private void addSubtreeToCheckingStateTracking(NGTreeNode node)
	{
		TreeNode[] path = node.getPath();
		TreePath tp = new TreePath(path);
		nodesCheckingState.put(tp, node);
		for(int i = 0; i < node.getChildCount(); i++)
		{
			addSubtreeToCheckingStateTracking((NGTreeNode) tp.pathByAddingChild(node.getChildAt(i)).getLastPathComponent());
		}
	}

	// Overriding cell renderer by a class that ignores the original "selection" mechanism
	// It decides how to show the nodes due to the checking-mechanism
	private class CheckBoxCellRenderer extends JPanel implements TreeCellRenderer
	{
		JTristateCheckBox checkBox;

		public CheckBoxCellRenderer()
		{
			super();
			this.setLayout(new BorderLayout());
			checkBox = new JTristateCheckBox();
			checkBox.setOpaque(false);
			add(checkBox, BorderLayout.CENTER);
			setOpaque(false);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			NGTreeNode node = (NGTreeNode) value;
			Object obj = node.getUserObject();
			TreePath tp = new TreePath(node.getPath());
			NGTreeNode cn = nodesCheckingState.get(tp);
			if(cn == null)
			{
				return this;
			}
			checkBox.setSelected(cn.isSelected());
			checkBox.setText(obj!=null?obj.toString():null);
			checkBox.setHalfSelected(cn.isSelected() && cn.getChildCount()>0 && !cn.allChildrenSelected());
			this.setPreferredSize(new Dimension(checkBox.getPreferredSize().width, 20));
			return this;
		}
	}

	public JCheckBoxTree(TreeModel model)
	{
		super(model);
		// Disabling toggling by double-click
		this.setToggleClickCount(0);
		// Overriding cell renderer by new one defined above
		CheckBoxCellRenderer cellRenderer = new CheckBoxCellRenderer();
		this.setCellRenderer(cellRenderer);

		// Overriding selection model by an empty one
		DefaultTreeSelectionModel dtsm = new DefaultTreeSelectionModel()
		{
			// Totally disabling the selection mechanism
			public void setSelectionPath(TreePath path)
			{
			}

			public void addSelectionPath(TreePath path)
			{
			}

			public void removeSelectionPath(TreePath path)
			{
			}

			public void setSelectionPaths(TreePath[] pPaths)
			{
			}
		};
		// Calling checking mechanism on mouse click
		this.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent arg0)
			{
				TreePath tp = selfPointer.getPathForLocation(arg0.getX(), arg0.getY());
				if(tp == null)
				{
					return;
				}
				boolean checkMode = !nodesCheckingState.get(tp).isSelected();
				checkSubTree(tp, checkMode);
				updatePredecessorsWithCheckMode(tp, checkMode);
				// Firing the check change event
				fireCheckChangeEvent(new CheckChangeEvent(new Object()));
				// Repainting tree after the data structures were updated
				selfPointer.repaint();
			}

			public void mouseEntered(MouseEvent arg0)
			{
			}

			public void mouseExited(MouseEvent arg0)
			{
			}

			public void mousePressed(MouseEvent arg0)
			{
			}

			public void mouseReleased(MouseEvent arg0)
			{
			}
		});
		this.setSelectionModel(dtsm);
	}

	// When a node is checked/unchecked, updating the states of the predecessors
	protected void updatePredecessorsWithCheckMode(TreePath tp, boolean check)
	{
		TreePath parentPath = tp.getParentPath();
		// If it is the root, stop the recursive calls and return
		if(parentPath == null)
		{
			return;
		}
		NGTreeNode parentNode = (NGTreeNode) parentPath.getLastPathComponent();
		parentNode.setAllChildrenSelected(true);
		parentNode.setSelected(false);
		for(int i = 0; i < parentNode.getChildCount(); i++)
		{
			TreePath childPath = parentPath.pathByAddingChild(parentNode.getChildAt(i));
			NGTreeNode childCheckedNode = nodesCheckingState.get(childPath);
			// It is enough that even one subtree is not fully selected
			// to determine that the parent is not fully selected
			if(!childCheckedNode.allChildrenSelected())
			{
				parentNode.setAllChildrenSelected(false);
			}
			// If at least one child is selected, selecting also the parent
			if(childCheckedNode.isSelected())
			{
				parentNode.setSelected(true);
			}
		}
		if(parentNode.isSelected())
		{
			checkedPaths.add(parentPath);
		}
		else
		{
			checkedPaths.remove(parentPath);
		}
		// Go to upper predecessor
		updatePredecessorsWithCheckMode(parentPath, check);
	}

	// Recursively checks/unchecks a subtree
	protected void checkSubTree(TreePath tp, boolean check)
	{
		NGTreeNode cn = nodesCheckingState.get(tp);
		cn.setSelected(check);
		NGTreeNode node = (NGTreeNode) tp.getLastPathComponent();
		for(int i = 0; i < node.getChildCount(); i++)
		{
			checkSubTree(tp.pathByAddingChild(node.getChildAt(i)), check);
		}
		cn.setAllChildrenSelected(check);
		if(check)
		{
			checkedPaths.add(tp);
		}
		else
		{
			checkedPaths.remove(tp);
		}
	}

}
