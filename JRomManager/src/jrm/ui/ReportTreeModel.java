package jrm.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import jrm.profiler.report.FilterOptions;
import jrm.profiler.report.Report;

@SuppressWarnings("serial")
public final class ReportTreeModel extends DefaultTreeModel
{
	private Report org_root;
	private List<FilterOptions> filterOptions = new ArrayList<>();
	
	public ReportTreeModel(Report root)
	{
		super(root);
		org_root = root;
	}
	
	public void filter(FilterOptions... filterOptions)
	{
		this.filterOptions = Arrays.asList(filterOptions);
		setRoot(new Report(org_root, filterOptions));
	}
	
	public void filter(List<FilterOptions> filterOptions)
	{
		this.filterOptions = filterOptions;
		setRoot(new Report(org_root, filterOptions));
	}
	
	public EnumSet<FilterOptions> getFilterOptions()
	{
		if(filterOptions.size()==0)
			return EnumSet.noneOf(FilterOptions.class);
		return EnumSet.copyOf(filterOptions);
	}
	
	@Override
	public void reload(TreeNode node)
	{
		filter(filterOptions);
		//super.reload(node);
	}

}
