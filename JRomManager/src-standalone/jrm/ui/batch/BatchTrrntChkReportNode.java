package jrm.ui.batch;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.TreeNode;

import jrm.batch.TrntChkReport;
import jrm.batch.TrntChkReport.Child;
import lombok.Getter;

public class BatchTrrntChkReportNode implements TreeNode
{
	private @Getter TrntChkReport report;
	
	private Map<Long,ChildNode> nodeCache = new HashMap<>();

	@SuppressWarnings("exports")
	public BatchTrrntChkReportNode(final TrntChkReport report)
	{
		this.report = report;
	}

	@SuppressWarnings("exports")
	public ChildNode getNode(Child child)
	{
		if(child==null)
			return null;
		ChildNode node;
		if(null==(node=nodeCache.get(child.getUid())))
		{
			node = new ChildNode(child);
			nodeCache.put(child.getUid(), node);
		}
		return node;
	}
	
	@SuppressWarnings("exports")
	@Override
	public TreeNode getChildAt(int childIndex)
	{
		return getNode(report.getNodes().get(childIndex));
	}

	@Override
	public int getChildCount()
	{
		return report.getNodes().size();
	}

	@SuppressWarnings("exports")
	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@SuppressWarnings("exports")
	@Override
	public int getIndex(TreeNode node)
	{
		return report.getNodes().indexOf(((ChildNode)node).child);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return report.getNodes().isEmpty();
	}

	@Override
	public Enumeration<ChildNode> children()
	{
		return new Enumeration<ChildNode>()
		{
			private final Iterator<Child> i = report.getNodes().iterator();

			public boolean hasMoreElements()
			{
				return i.hasNext();
			}

			public ChildNode nextElement()
			{
				return getNode(i.next());
			}
		};
	}
	
	public class ChildNode implements TreeNode
	{
		private final @Getter Child child;
		
		@SuppressWarnings("exports")
		public ChildNode(final Child child)
		{
			this.child = child;
		}
		
		@SuppressWarnings("exports")
		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return getNode(child.getChildren()==null?null:child.getChildren().get(childIndex));
		}

		@Override
		public int getChildCount()
		{
			return child.getChildren()==null?0:child.getChildren().size();
		}

		@SuppressWarnings("exports")
		@Override
		public TreeNode getParent()
		{
			return BatchTrrntChkReportNode.this;
		}

		@SuppressWarnings("exports")
		@Override
		public int getIndex(TreeNode node)
		{
			return child.getChildren()==null?-1:child.getChildren().indexOf(((ChildNode)node).child);
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return child.getChildren()==null||child.getChildren().isEmpty();
		}

		@Override
		public Enumeration<ChildNode> children()
		{
			return child.getChildren()==null?null:new Enumeration<ChildNode>()
			{
				private final Iterator<Child> i = child.getChildren().iterator();

				public boolean hasMoreElements()
				{
					return i.hasNext();
				}

				public ChildNode nextElement()
				{
					return getNode(i.next());
				}
			};
		}
		
	}

}
