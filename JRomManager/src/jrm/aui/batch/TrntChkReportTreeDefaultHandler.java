package jrm.aui.batch;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import jrm.batch.TrntChkReport;
import jrm.profile.report.FilterOptions;

public class TrntChkReportTreeDefaultHandler implements TrntChkReportTreeHandler
{

	/** The org root. */
	private TrntChkReport org_root;
	private TrntChkReport filtered_root;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	public TrntChkReportTreeDefaultHandler(final TrntChkReport root)
	{
		this.org_root = root;
		root.setHandler(this);
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
	public TrntChkReport getFilteredReport()
	{
		return filtered_root;
	}

	@Override
	public TrntChkReport getOriginalReport()
	{
		return org_root;
	}

}
