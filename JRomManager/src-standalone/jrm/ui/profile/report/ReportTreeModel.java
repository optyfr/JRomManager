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

import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;

// TODO: Auto-generated Javadoc
/**
 * The Class ReportTreeModel.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public final class ReportTreeModel extends DefaultTreeModel implements ReportTreeHandler
{
	
	/** The org root. */
	private Report org_root;
	
	/** The filter options. */
	private List<FilterOptions> filterOptions = new ArrayList<>();

	/**
	 * Instantiates a new report tree model.
	 *
	 * @param root the root
	 */
	private ReportTreeModel(final Report root)
	{
		super(root);
		org_root = root;
		root.setHandler(this);
		initClone();
	}

	public ReportTreeModel(final ReportTreeHandler handler)
	{
		super(handler.getFilteredReport());
		getFilteredReport().setHandler(this);
		org_root = handler.getOriginalReport();
		org_root.setHandler(this);
	}
	
	
	/**
	 * Inits the clone.
	 */
	public void initClone()
	{
		setRoot(org_root.clone(filterOptions));
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
		setRoot(org_root.clone(filterOptions));
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
	public Report getFilteredReport()
	{
		return (Report)getRoot();
	}

	@Override
	public Report getOriginalReport()
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
			final TreeModelEvent event = new TreeModelEvent(this, getPathToRoot(getFilteredReport()), childIndices, children);
			for(final TreeModelListener l : getTreeModelListeners())
				l.treeNodesInserted(event);
		}
	}
	
}
