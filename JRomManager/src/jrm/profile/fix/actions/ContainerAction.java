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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jrm.aui.progress.ProgressHandler;
import jrm.compressors.Archive;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.security.Session;

/**
 * The base class for container operations
 * @author optyfr
 *
 */
public abstract class ContainerAction implements HTMLRenderer, Comparable<ContainerAction>
{
	private static final String ACTION_TO_S_AT_S_FAILED = "action to %s@%s failed";
	private static final int COUNT = 0;
	private static final long ESTIMATED_SIZE = 0L;
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
	public final List<EntryAction> entryActions = new ArrayList<>();

	/**
	 * Constructor
	 * @param container the container used for action
	 * @param format the desired format for container
	 */
	protected ContainerAction(final Container container, final FormatOptions format)
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
		entryActions.add(entryAction);
		entryAction.parent = this;
	}

	/**
	 * shortcut static method to add an action to an existing list of {@link ContainerAction}
	 * @param list the {@link List} of {@link ContainerAction}
	 * @param action the {@link ContainerAction} to add
	 */
	public static void addToList(final List<ContainerAction> list, final ContainerAction action)
	{
		if(action != null && !action.entryActions.isEmpty())
			list.add(action);
	}

	/**
	 * will do the determined action upon this container
	 * @param handler a {@link ProgressHandler} to show progression state
	 * @return true if successful, false otherwise
	 */
	public abstract boolean doAction(final Session session, ProgressHandler handler);
	
	public long estimatedSize()
	{
		return ESTIMATED_SIZE;
	}
	
	public int count()
	{
		return COUNT;
	}
	
	@Override
	public int compareTo(ContainerAction o)
	{
		if (estimatedSize() < o.estimatedSize())
			return -1;
		if (estimatedSize() > o.estimatedSize())
			return 1;
		if (count() < o.count())
			return -1;
		if (count() > o.count())
			return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
	public static Comparator<ContainerAction> comparator()
	{
		return (o1, o2) -> o1.compareTo(o2);
	}
	
	public static Comparator<ContainerAction> rcomparator()
	{
		return (o1, o2) -> o2.compareTo(o1);
	}
	
	/**
	 * @param session
	 * @param handler
	 * @param archive
	 * @return
	 */
	protected boolean archiveAction(final Session session, final ProgressHandler handler, Archive archive) throws IOException
	{
		try(archive)
		{
			var i = 0;
			for (final EntryAction action : entryActions)
			{
				i++;
				if (!action.doAction(session, archive, handler, i, entryActions.size()))
				{
					Log.err(()->String.format(ACTION_TO_S_AT_S_FAILED, container.getFile().getName(), action.entry.getRelFile()));
					return false;
				}
			}
			return true;
		}
	}
	
	/**
	 * @param session
	 * @param handler
	 * @param fs
	 * @return
	 */
	protected boolean fsAction(final Session session, final ProgressHandler handler, final FileSystem fs)
	{
		var i = 0;
		for (final EntryAction action : entryActions)
		{
			i++;
			if (!action.doAction(session, fs, handler, i, entryActions.size() ))
			{
				Log.err(()->String.format(ACTION_TO_S_AT_S_FAILED, container.getFile().getName(), action.entry.getRelFile()));
				return false;
			}
		}
		return true;
	}

	/**
	 * @param session
	 * @param handler
	 * @param target
	 * @return
	 */
	protected boolean pathAction(final Session session, final ProgressHandler handler, final Path target)
	{
		var i = 0;
		for (final EntryAction action : entryActions)
		{
			i++;
			if (!action.doAction(session, target, handler, i, entryActions.size()))
			{
				Log.err(()->String.format(ACTION_TO_S_AT_S_FAILED, container.getFile().getName(), action.entry.getRelFile()));
				return false;
			}
		}
		return true;
	}

}
