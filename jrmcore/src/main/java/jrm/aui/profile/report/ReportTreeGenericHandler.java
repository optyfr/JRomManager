package jrm.aui.profile.report;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import jrm.profile.report.FilterOptions;
import jrm.profile.report.ReportIntf;

public abstract class ReportTreeGenericHandler<T extends ReportIntf<T>> implements ReportTreeHandler<T>
{

	/** The org root. */
	protected T orgRoot;
	protected T filteredRoot;

	
	/** The filter options. */
	protected Set<FilterOptions> filterOptions = new HashSet<>();

	protected ReportTreeGenericHandler(final T root)
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
	public void filter(Set<FilterOptions> filterOptions)
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
	public T getFilteredReport()
	{
		return filteredRoot;
	}

	@Override
	public T getOriginalReport()
	{
		return orgRoot;
	}
}
