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
package jrm.profile.fix.actions;

import java.util.ArrayList;
import java.util.List;

import jrm.misc.HTMLRenderer;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;

/**
 * The base class for container operations
 * @author optyfr
 *
 */
abstract public class ContainerAction implements HTMLRenderer
{
	/**
	 * the container on which applying actions
	 */
	public final Container container;
	/**
	 * the desired container format
	 */
	public final FormatOptions format;
	/**
	 * the {@link ArrayList} of {@link EntryAction}s
	 */
	public final ArrayList<EntryAction> entry_actions = new ArrayList<>();

	/**
	 * Constructor
	 * @param container the container used for action
	 * @param format the desired format for container
	 */
	public ContainerAction(final Container container, final FormatOptions format)
	{
		this.container = container;
		this.format = format;
	}

	/**
	 * Add an {@link EntryAction} to this {@link ContainerAction}
	 * @param entryAction the {@link EntryAction} to add
	 */
	public void addAction(final EntryAction entryAction)
	{
		entry_actions.add(entryAction);
		entryAction.parent = this;
	}

	/**
	 * shortcut static method to add an action to an existing list of {@link ContainerAction}
	 * @param list the {@link List} of {@link ContainerAction}
	 * @param action the {@link ContainerAction} to add
	 */
	public static void addToList(final List<ContainerAction> list, final ContainerAction action)
	{
		if(action != null && action.entry_actions.size() > 0)
			list.add(action);
	}

	/**
	 * will do the determined action upon this container
	 * @param handler a {@link ProgressHandler} to show progression state
	 * @return true if successful, false otherwise
	 */
	public abstract boolean doAction(ProgressHandler handler);

}
