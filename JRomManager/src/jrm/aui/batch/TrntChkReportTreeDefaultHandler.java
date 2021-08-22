package jrm.aui.batch;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jrm.batch.TrntChkReport;
import jrm.profile.report.FilterOptions;

public class TrntChkReportTreeDefaultHandler implements TrntChkReportTreeHandler
{

	/** The org root. */
	private TrntChkReport orgRoot;
	private TrntChkReport filteredRoot;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	public TrntChkReportTreeDefaultHandler(final TrntChkReport root)
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
	public TrntChkReport getFilteredReport()
	{
		return filteredRoot;
	}

	@Override
	public TrntChkReport getOriginalReport()
	{
		return orgRoot;
	}

}
