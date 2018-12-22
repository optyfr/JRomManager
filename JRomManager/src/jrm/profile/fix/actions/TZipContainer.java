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
import java.util.EnumSet;

import org.apache.commons.text.StringEscapeUtils;

import JTrrntzip.SimpleTorrentZipOptions;
import JTrrntzip.TorrentZip;
import JTrrntzip.TrrntZipStatus;
import jrm.misc.Log;
import jrm.profile.data.Container;
import jrm.profile.scan.options.FormatOptions;
import jrm.security.Session;
import jrm.ui.progress.ProgressHandler;
import jrm.ui.progress.ProgressTZipCallBack;

/**
 * The specialized container action for trrntzipping zip containers
 * @author optyfr
 *
 */
public class TZipContainer extends ContainerAction
{

	/**
	 * Constructor
	 * @param container the container to tzip
	 * @param format the desired format (should be always {@link FormatOptions#TZIP} otherwise nothing will happen)
	 */
	public TZipContainer(final Container container, final FormatOptions format)
	{
		super(container, format);
	}

	@Override
	public boolean doAction(final Session session, final ProgressHandler handler)
	{
		if(container.getType() == Container.Type.ZIP)
		{
			if(format == FormatOptions.TZIP)
			{
				handler.setProgress(toHTML(toNoBR(String.format(StringEscapeUtils.escapeHtml4("TorrentZipping %s [%s]"), toBlue(container.m.getFullName(container.file.getName())), toPurple(container.m.getDescription()))))); //$NON-NLS-1$
				try
				{
					if(container.file.exists())
					{
						final EnumSet<TrrntZipStatus> status = new TorrentZip(new ProgressTZipCallBack(handler), new SimpleTorrentZipOptions()).Process(container.file);
						if(!status.contains(TrrntZipStatus.ValidTrrntzip))
							System.out.format("%-64s => %s\n", container.file, status.toString()); //$NON-NLS-1$
					}
					handler.setProgress(""); //$NON-NLS-1$
					return true;
				}
				catch(/*InterruptedException |*/ final IOException e)
				{
					System.err.println(container.file);
					Log.err(e.getMessage(),e);
				}
				handler.setProgress(""); //$NON-NLS-1$
			}
		}
		return false;
	}

}
