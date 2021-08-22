package jrm.aui.profile.report;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;

public class ReportTreeDefaultHandler implements ReportTreeHandler
{

	/** The org root. */
	private Report orgRoot;
	private Report filteredRoot;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	public ReportTreeDefaultHandler(final Report root)
	{
		this.orgRoot = root;
		root.setHandler(this);
		initClone();
	}

	@Override
	public void initClone()
	{
		filteredRoot = orgRoot.clone(filterOptions);
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
		if(filterOptions.isEmpty())
			return EnumSet.noneOf(FilterOptions.class);
		return EnumSet.copyOf(filterOptions);
	}

	@Override
	public Report getFilteredReport()
	{
		return filteredRoot;
	}

	@Override
	public Report getOriginalReport()
	{
		return orgRoot;
	}

}
