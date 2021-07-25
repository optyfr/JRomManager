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
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;

import jrm.aui.progress.ProgressHandler;
import jrm.locale.Messages;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.security.Session;
import lombok.val;

/**
 * Delete a container (will all its entries)
 * @author optyfr
 *
 */
public class DeleteContainer extends ContainerAction
{

	/**
	 * constructor
	 * @param container to delete
	 * @param format format of the container
	 */
	public DeleteContainer(final Container container, final FormatOptions format)
	{
		super(container, format);
	}

	/**
	 * shortcut static method to get an instance of {@link DeleteContainer}
	 * @param action the potentially already existing {@link DeleteContainer} 
	 * @param container the container to backup
	 * @param format the format of the container
	 * @return a {@link DeleteContainer}
	 */
	public static DeleteContainer getInstance(DeleteContainer action, final Container container, final FormatOptions format)
	{
		if(action == null)
			action = new DeleteContainer(container, format);
		return action;
	}

	@Override
	public boolean doAction(final Session session, final ProgressHandler handler)
	{
		handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4(session.getMsgs().getString("DeleteContainer.Deleting")), toBlue(container.getFile().getName()))))); //$NON-NLS-1$
		if(container.getType() == Container.Type.ZIP)
			return container.getFile().delete();
		else if(container.getType() == Container.Type.SEVENZIP)
			return container.getFile().delete();
		else if(container.getType() == Container.Type.DIR)
		{
			try
			{
				FileUtils.deleteDirectory(container.getFile());
				return true;
			}
			catch(final IOException e)
			{
				System.err.println("failed to delete " + container.getRelFile() + " ("+e.getMessage()+")"); //$NON-NLS-1$
				if(container.getFile().exists())
					FileUtils.deleteQuietly(container.getFile());
				return true;
			}
		}
		else if(container.getType() == Container.Type.FAKE)
		{
			for(val entry : container.getEntries())
			{
				try
				{
					Files.deleteIfExists(container.getFile().getParentFile().toPath().resolve(entry.getFile())); 
					return true;
				}
				catch(final IOException e)
				{
					System.err.println("failed to delete " + container.getRelFile()); //$NON-NLS-1$
					return false;
				}
			}
		}
		else if(container.getType() == Container.Type.UNK)
			return container.getFile().delete();
		return false;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("DeleteContainer.Delete"), container); //$NON-NLS-1$
	}
}
