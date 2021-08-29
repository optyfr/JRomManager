package jrm.aui.profile.report;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jrm.profile.report.FilterOptions;

public interface ReportTreeHandler<T>
{
	/**
	 * Inits the clone.
	 */
	public void initClone();
	
	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	public default void filter(final FilterOptions... filterOptions)
	{
		filter(Arrays.asList(filterOptions));
	}
	
	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	public void filter(final List<FilterOptions> filterOptions);
	
	/**
	 * Gets the filter options.
	 *
	 * @return the filter options
	 */
	public Set<FilterOptions> getFilterOptions();
	
	public default boolean hasListeners()
	{
		return false;
	}
	
	public default void notifyInsertion(int[] childIndices, Object[] children)
	{
		
	}
	
	public T getFilteredReport();
	public T getOriginalReport();
	
}
