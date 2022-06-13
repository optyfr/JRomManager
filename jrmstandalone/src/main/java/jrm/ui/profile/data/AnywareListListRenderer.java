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
package jrm.ui.profile.data;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.locale.Messages;
import jrm.profile.data.MachineList;
import jrm.profile.data.SoftwareList;
import jrm.ui.MainFrame;
import jrm.ui.basic.CenteredTableCellRenderer;

/**
 * The Class AnywareListListRenderer.
 */
@SuppressWarnings("serial")
public final class AnywareListListRenderer
{

	/** The Constant columns. */
	protected static final String[] columns = new String[] { Messages.getString("SoftwareListListRenderer.Name"), Messages.getString("SoftwareListListRenderer.Description"), Messages.getString("SoftwareListListRenderer.Have") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/** The Constant columnsTypes. */
	protected static final Class<?>[] columnsTypes = new Class<?>[] { Object.class, String.class, String.class };

	/** The Constant columnsWidths. */
	protected static final int[] columnsWidths = new int[] { 70, 150, -80 };

	/** The Constant columnsRenderers. */
	protected static final TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{
		final ImageIcon diskMultipleGreen = MainFrame.getIcon("/jrm/resicons/disk_multiple_green.png"); //$NON-NLS-1$
		final ImageIcon diskMultipleOrange = MainFrame.getIcon("/jrm/resicons/disk_multiple_orange.png"); //$NON-NLS-1$
		final ImageIcon diskMultipleRed = MainFrame.getIcon("/jrm/resicons/disk_multiple_red.png"); //$NON-NLS-1$
		final ImageIcon diskMultipleGray = MainFrame.getIcon("/jrm/resicons/disk_multiple_gray.png"); //$NON-NLS-1$

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			if (value instanceof SoftwareList)
			{
				super.getTableCellRendererComponent(table, ((SoftwareList) value).getBaseName(), isSelected, hasFocus, row, column);
				switch (((SoftwareList) value).getStatus())
				{
					case COMPLETE:
						setIcon(diskMultipleGreen);
						break;
					case MISSING:
						setIcon(diskMultipleRed);
						break;
					case PARTIAL:
						setIcon(diskMultipleOrange);
						break;
					case UNKNOWN:
					default:
						setIcon(diskMultipleGray);
						break;

				}
				return this;
			}
			else if (value instanceof MachineList)
			{
				super.getTableCellRendererComponent(table, Messages.getString("MachineListListRenderer.*"), isSelected, hasFocus, row, column); //$NON-NLS-1$
				switch (((MachineList) value).getStatus())
				{
					case COMPLETE:
						setIcon(diskMultipleGreen);
						break;
					case MISSING:
						setIcon(diskMultipleRed);
						break;
					case PARTIAL:
						setIcon(diskMultipleOrange);
						break;
					case UNKNOWN:
					default:
						setIcon(diskMultipleGray);
						break;

				}
				return this;
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}, new DefaultTableCellRenderer(), new CenteredTableCellRenderer() };

	/**
	 * Instantiates a new anyware list list renderer.
	 */
	private AnywareListListRenderer()
	{
	}

}
