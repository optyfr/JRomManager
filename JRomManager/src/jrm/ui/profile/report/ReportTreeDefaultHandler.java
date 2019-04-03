package jrm.ui.profile.report;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;

public class ReportTreeDefaultHandler implements ReportTreeHandler
{

	/** The org root. */
	private Report org_root;
	private Report filtered_root;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	public ReportTreeDefaultHandler(final Report root)
	{
		this.org_root = root;
		root.setModel(this);
		initClone();
	}

	@Override
	public void initClone()
	{
		filtered_root = org_root.clone(filterOptions);
	}

	@Override
	public void filter(List<FilterOptions> filterOptions)
	{
		this.filterOptions = filterOptions;
		initClone();
	}

	@Override
	public EnumSet<FilterOptions> getFilterOptions()
	{
		if(filterOptions.size()==0)
			return EnumSet.noneOf(FilterOptions.class);
		return EnumSet.copyOf(filterOptions);
	}

	@Override
	public Report getFilteredReport()
	{
		return filtered_root;
	}

	@Override
	public Report getOriginalReport()
	{
		return org_root;
	}

}
