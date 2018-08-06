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

import java.nio.file.FileSystem;
import java.nio.file.Path;

import jrm.compressors.Archive;
import jrm.misc.HTMLRenderer;
import jrm.profile.data.Entry;
import jrm.ui.ProgressHandler;

/**
 * the base class for entry actions
 * @author optyfr
 *
 */
abstract public class EntryAction implements HTMLRenderer
{
	/**
	 * the entry on which we should apply an action
	 */
	final Entry entry;
	/**
	 * the parent {@link ContainerAction}
	 */
	ContainerAction parent;

	/**
	 * constructor
	 * @param entry the {@link Entry} on which to apply action
	 */
	public EntryAction(final Entry entry)
	{
		this.entry = entry;
	}

	/**
	 * do action on entry in an {@link Archive}  
	 * @param archive the compressed {@link Archive} provided by {@link ContainerAction#doAction(ProgressHandler)} in which we should apply entry action
	 * @param handler the {@link ProgressHandler} to show progression state
	 * @param i the progression level
	 * @param max the progression maximum
	 * @return true if successful, otherwise false
	 */
	public abstract boolean doAction(Archive archive, ProgressHandler handler, int i, int max);

	/**
	 * do action on entry on a {@link FileSystem}
	 * @param fs the {@link FileSystem} provided by {@link ContainerAction#doAction(ProgressHandler)} in which we should apply entry action
	 * @param handler handler the {@link ProgressHandler} to show progression state
	 * @param i the progression level
	 * @param max the progression maximum
	 * @return true if successful, otherwise false
	 */
	public abstract boolean doAction(FileSystem fs, ProgressHandler handler, int i, int max);

	/**
	 * 
	 * @param target the Path provided by {@link ContainerAction#doAction(ProgressHandler)} in which we should apply entry action
	 * @param handler handler the {@link ProgressHandler} to show progression state
	 * @param i the progression level
	 * @param max the progression maximum
	 * @return true if successful, otherwise false
	 */
	public abstract boolean doAction(Path target, ProgressHandler handler, int i, int max);
}
