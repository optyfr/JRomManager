package jrm.ui.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import jrm.batch.TrntChkReport;
import jrm.profile.report.FilterOptions;
import jrm.ui.profile.report.ReportNode;

@SuppressWarnings("serial")
public class BatchTrrntChkReportTreeModel extends DefaultTreeModel implements TrntChkReportTreeHandler
{
	/** The org root. */
	private final TrntChkReport org_root;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	public BatchTrrntChkReportTreeModel(final TrntChkReport root)
	{
		super(new BatchTrrntChkReportNode(root));
		org_root = root;
		root.setHandler(this);
		initClone();
	}

	public BatchTrrntChkReportTreeModel(final TrntChkReportTreeHandler handler)
	{
		super(new BatchTrrntChkReportNode(handler.getFilteredReport()));
		getFilteredReport().setHandler(this);
		org_root = handler.getOriginalReport();
		org_root.setHandler(this);
	}

	/**
	 * Inits the clone.
	 */
	public void initClone()
	{
		setRoot(new BatchTrrntChkReportNode(org_root.clone(filterOptions)));
	}

	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	public void filter(final FilterOptions... filterOptions)
	{
		filter(Arrays.asList(filterOptions));
	}

	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	public void filter(final List<FilterOptions> filterOptions)
	{
		this.filterOptions = filterOptions;
		setRoot(new BatchTrrntChkReportNode(org_root.clone(filterOptions)));
	}

	/**
	 * Gets the filter options.
	 *
	 * @return the filter options
	 */
	public EnumSet<FilterOptions> getFilterOptions()
	{
		if(filterOptions.size()==0)
			return EnumSet.noneOf(FilterOptions.class);
		return EnumSet.copyOf(filterOptions);
	}
	
	@Override
	public TrntChkReport getFilteredReport()
	{
		return ((BatchTrrntChkReportNode)getRoot()).getReport();
	}

	@Override
	public TrntChkReport getOriginalReport()
	{
		return org_root;
	}

	@Override
	public boolean hasListeners()
	{
		return getTreeModelListeners().length>0;
	}
	
	@Override
	public void notifyInsertion(int[] childIndices, Object[] children)
	{
		if(getTreeModelListeners().length>0)
		{
			final TreeModelEvent event = new TreeModelEvent(this, getPathToRoot((ReportNode)getRoot()), childIndices, children);
			for(final TreeModelListener l : getTreeModelListeners())
				l.treeNodesInserted(event);
		}
	}

}
