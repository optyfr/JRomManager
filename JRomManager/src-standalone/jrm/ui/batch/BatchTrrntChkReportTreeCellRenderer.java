/*
 * Copyright (C) 2018 optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.ui.batch;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import jrm.batch.TrntChkReport.Child;
import jrm.misc.Log;
import jrm.ui.MainFrame;
import jrm.ui.batch.BatchTrrntChkReportNode.ChildNode;

/**
 * The Class ReportTreeCellRenderer.
 *
 * @author optyfr
 */
@SuppressWarnings("serial")
public class BatchTrrntChkReportTreeCellRenderer extends DefaultTreeCellRenderer
{

	/**
	 * Instantiates a new report tree cell renderer.
	 */
	public BatchTrrntChkReportTreeCellRenderer()
	{
		super();
	}

	@SuppressWarnings("exports")
	@Override
	public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus)
	{
		try
		{
			if (!(value instanceof ChildNode))
			{
				super.getTreeCellRendererComponent(tree, "Root", sel, expanded, leaf, row, hasFocus);
				return this;
			}
			Child node = ((ChildNode) value).getChild();
			String title = node.data.title;
			if (node.data.length != null)
				title += " (" + node.data.length + ")";
			title += " [" + node.data.status + "]";
			super.getTreeCellRendererComponent(tree, title, sel, expanded, leaf, row, hasFocus);
			if (!leaf)
			{
				String icon = "/jrm/resicons/folder"; //$NON-NLS-1$
				if (expanded)
					icon += "_open"; //$NON-NLS-1$
				else
					icon += "_closed"; //$NON-NLS-1$
				switch (node.data.status)
				{
					case OK:
						icon += "_green";
						break;
					case MISSING:
						icon += "_red";
						break;
					case SHA1:
						icon += "_purple";
						break;
					case SIZE:
						icon += "_blue";
						break;
					case SKIPPED:
						icon += "_orange";
						break;
					case UNKNOWN:
						icon += "_gray";
						break;
					default:
						break;
				}
				icon += ".png"; //$NON-NLS-1$
				setIcon(MainFrame.getIcon(icon));
			}
			else
			{
				String icon = "/jrm/resicons/icons/bullet"; //$NON-NLS-1$
				switch (node.data.status)
				{
					case OK:
						icon += "_green";
						break;
					case MISSING:
						icon += "_red";
						break;
					case SHA1:
						icon += "_purple";
						break;
					case SIZE:
						icon += "_blue";
						break;
					case SKIPPED:
						icon += "_orange";
						break;
					case UNKNOWN:
						icon += "_black";
						break;
				}
				icon += ".png"; //$NON-NLS-1$
				setIcon(MainFrame.getIcon(icon));
			}
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
		}
		return this;
	}
}
