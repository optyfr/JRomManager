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

import jrm.aui.progress.ProgressHandler;
import jrm.aui.status.StatusRendererFactory;
import jrm.compressors.Archive;
import jrm.profile.data.Entry;
import jrm.security.Session;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

/**
 * the base class for entry actions
 * @author optyfr
 *
 */
public abstract class EntryAction implements StatusRendererFactory
{
	private static final long ESTIMATED_SIZE = 0L;
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
	protected EntryAction(final Entry entry)
	{
		this.entry = entry;
	}

	/**
	 * do action on entry in an {@link Archive}
	 * @param session the current {@link Session}  
	 * @param archive the compressed {@link Archive} provided by {@link ContainerAction#doAction(Session, ProgressHandler)} in which we should apply entry action
	 * @param handler the {@link ProgressHandler} to show progression state
	 * @param i the progression level
	 * @param max the progression maximum
	 * @return true if successful, otherwise false
	 */
	public abstract boolean doAction(final Session session, Archive archive, ProgressHandler handler, int i, int max);

	/**
	 * 
	 * @param target the Path provided by {@link ContainerAction#doAction(Session, ProgressHandler)} in which we should apply entry action
	 * @param handler handler the {@link ProgressHandler} to show progression state
	 * @param i the progression level
	 * @param max the progression maximum
	 * @return true if successful, otherwise false
	 */
	public abstract boolean doAction(final Session session, Path target, ProgressHandler handler, int i, int max);

	/**
	 * do action on entry on a {@link FileSystem}
	 * @param session the current {@link Session}  
	 * @param zipf the {@link ZipFile} provided by {@link ContainerAction#doAction(Session, ProgressHandler)} in which we should apply entry action
	 * @param zipp the {@link ZipParameters} for compression settings
	 * @param handler handler the {@link ProgressHandler} to show progression state
	 * @param i the progression level
	 * @param max the progression maximum
	 * @return true if successful, otherwise false
	 */
	@SuppressWarnings("exports")
	public abstract boolean doAction(final Session session, final ZipFile zipf, final ZipParameters zipp, ProgressHandler handler, int i, int max);

	public long estimatedSize()
	{
		return ESTIMATED_SIZE;
	}

}
