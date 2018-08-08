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
package jrm.ui.profile.manager;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import jrm.profile.manager.Dir;

// TODO: Auto-generated Javadoc
/**
 * The listener interface for receiving dirTreeSelection events.
 * The class that is interested in processing a dirTreeSelection
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addDirTreeSelectionListener<code> method. When
 * the dirTreeSelection event occurs, that object's appropriate
 * method is invoked.
 *
 * @see DirTreeSelectionEvent
 */
public class DirTreeSelectionListener implements TreeSelectionListener
{
	
	/** The profiles list. */
	JTable profilesList;

	/**
	 * Instantiates a new dir tree selection listener.
	 *
	 * @param profilesList the profiles list
	 */
	public DirTreeSelectionListener(final JTable profilesList)
	{
		this.profilesList = profilesList;
	}

	@Override
	public void valueChanged(final TreeSelectionEvent e)
	{
		final JTree tree = (JTree) e.getSource();
		final DirNode selectedNode = (DirNode) tree.getLastSelectedPathComponent();
		if(selectedNode != null)
		{
			((FileTableModel) profilesList.getModel()).populate((Dir) selectedNode.getUserObject());
			profilesList.getColumn(profilesList.getColumnName(0)).setCellRenderer(new FileTableCellRenderer());
		}
	}

}
