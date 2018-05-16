package jrm.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;

@SuppressWarnings("serial")
public final class ReportTreeModel extends DefaultTreeModel
{
	private final Report org_root;
	private List<FilterOptions> filterOptions = new ArrayList<>();

	public ReportTreeModel(final Report root)
	{
		super(root);
		org_root = root;
	}

	public void initClone()
	{
		setRoot(org_root.clone(filterOptions));
	}

	public void filter(final FilterOptions... filterOptions)
	{
		filter(Arrays.asList(filterOptions));
	}

	public void filter(final List<FilterOptions> filterOptions)
	{
		this.filterOptions = filterOptions;
		setRoot(org_root.clone(filterOptions));
	}

	public EnumSet<FilterOptions> getFilterOptions()
	{
		if(filterOptions.size()==0)
			return EnumSet.noneOf(FilterOptions.class);
		return EnumSet.copyOf(filterOptions);
	}

}
