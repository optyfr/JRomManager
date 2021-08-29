package jrm.ui.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import jrm.aui.profile.report.ReportTreeHandler;
import jrm.batch.TrntChkReport;
import jrm.profile.report.FilterOptions;
import jrm.ui.profile.report.ReportNode;

@SuppressWarnings("serial")
public class BatchTrrntChkReportTreeModel extends DefaultTreeModel implements ReportTreeHandler<TrntChkReport>
{
	/** The org root. */
	private final TrntChkReport orgRoot;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	@SuppressWarnings("exports")
	public BatchTrrntChkReportTreeModel(final TrntChkReport root)
	{
		super(new BatchTrrntChkReportNode(root));
		orgRoot = root;
		root.setHandler(this);
		initClone();
	}

	@SuppressWarnings("exports")
	public BatchTrrntChkReportTreeModel(final ReportTreeHandler<TrntChkReport> handler)
	{
		super(new BatchTrrntChkReportNode(handler.getFilteredReport()));
		getFilteredReport().setHandler(this);
		orgRoot = handler.getOriginalReport();
		orgRoot.setHandler(this);
	}

	/**
	 * Inits the clone.
	 */
	public void initClone()
	{
		setRoot(new BatchTrrntChkReportNode(orgRoot.clone(filterOptions)));
	}

	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	@SuppressWarnings("exports")
	@Override
	public void filter(final FilterOptions... filterOptions)
	{
		filter(Arrays.asList(filterOptions));
	}

	/**
	 * Filter.
	 *
	 * @param filterOptions the filter options
	 */
	@SuppressWarnings("exports")
	public void filter(final List<FilterOptions> filterOptions)
	{
		this.filterOptions = filterOptions;
		setRoot(new BatchTrrntChkReportNode(orgRoot.clone(filterOptions)));
	}

	/**
	 * Gets the filter options.
	 *
	 * @return the filter options
	 */
	@SuppressWarnings("exports")
	public EnumSet<FilterOptions> getFilterOptions()
	{
		if(filterOptions.isEmpty())
			return EnumSet.noneOf(FilterOptions.class);
		return EnumSet.copyOf(filterOptions);
	}
	
	@SuppressWarnings("exports")
	@Override
	public TrntChkReport getFilteredReport()
	{
		return ((BatchTrrntChkReportNode)getRoot()).getReport();
	}

	@SuppressWarnings("exports")
	@Override
	public TrntChkReport getOriginalReport()
	{
		return orgRoot;
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
