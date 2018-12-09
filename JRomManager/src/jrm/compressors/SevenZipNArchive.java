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
package jrm.compressors;

import java.io.File;
import java.io.IOException;

import jrm.security.Session;
import jrm.ui.progress.ProgressNarchiveCallBack;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * SevenZip native archive class, should not be used directly
 * @author optyfr
 */
class SevenZipNArchive extends NArchive
{

	public SevenZipNArchive(final Session session, final File archive) throws IOException, SevenZipNativeInitializationException
	{
		super(session, archive);
	}

	public SevenZipNArchive(final Session session, final File archive, final boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		super(session, archive, readonly, null);
	}

	public SevenZipNArchive(final Session session, final File archive, final boolean readonly, ProgressNarchiveCallBack cb) throws IOException, SevenZipNativeInitializationException
	{
		super(session, archive, readonly, cb);
	}

}
