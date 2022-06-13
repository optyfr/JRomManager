package jrm.ui.profile.report;

import javax.swing.tree.TreeNode;

import jrm.profile.report.ReportIntf;
import lombok.Getter;

public abstract class ReportNodeGeneric<T extends ReportIntf<T>> implements TreeNode
{
	protected final @Getter T report;
	
	protected ReportNodeGeneric(final T report)
	{
		this.report = report;
	}

	@SuppressWarnings("exports")
	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}
}
