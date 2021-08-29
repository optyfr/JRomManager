/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui.profile.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;

import jrm.aui.profile.report.ReportTreeHandler;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;

/**
 * The Class ReportTreeModel.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public final class ReportTreeModel extends DefaultTreeModel implements ReportTreeHandler<Report>
{
	
	/** The org root. */
	private Report orgRoot;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	/**
	 * Instantiates a new report tree model.
	 *
	 * @param root the root
	 */
	private ReportTreeModel(final Report root)	//NOSONAR
	{
		super(new ReportNode(root));
		orgRoot = root;
		root.setHandler(this);
		initClone();
	}

	@SuppressWarnings("exports")
	public ReportTreeModel(final ReportTreeHandler<Report> handler)
	{
		super(new ReportNode(handler.getFilteredReport()));
		getFilteredReport().setHandler(this);
		orgRoot = handler.getOriginalReport();
		orgRoot.setHandler(this);
	}
	
	
	/**
	 * Inits the clone.
	 */
	public void initClone()
	{
		setRoot(new ReportNode(orgRoot.clone(filterOptions)));
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
		setRoot(new ReportNode(orgRoot.clone(filterOptions)));
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
	public Report getFilteredReport()
	{
		return ((ReportNode)getRoot()).getReport();
	}

	@SuppressWarnings("exports")
	@Override
	public Report getOriginalReport()
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
