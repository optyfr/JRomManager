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
import java.io.InputStream;

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.misc.Log;
import jrm.security.Session;
import lombok.Getter;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * The external SevenZip archive class.<br>
 * If possible, wrap over {@link NArchive} via {@link SevenZipNArchive} to use SevenZipJBinding.<br>
 * Otherwise will try to use external 7z executable if available...<br>
 * If command line is used, the archive will be extracted to temporary directory upon first write operation,
 * then entirely recreated from temporary directory upon archive's {@link #close()} operation 
 * @author optyfr
 *
 */
public class SevenZipArchive implements Archive
{
	private @Getter SevenZipNArchive native7Zip = null;

	public SevenZipArchive(final Session session, final File archive) throws IOException
	{
		this(session, archive, false, null);
	}

	public SevenZipArchive(final Session session, final File archive, ProgressNarchiveCallBack cb) throws IOException
	{
		this(session, archive, false, cb);
	}

	public SevenZipArchive(final Session session, final File archive, final boolean readonly, ProgressNarchiveCallBack cb) throws IOException
	{
		try
		{
			native7Zip = new SevenZipNArchive(session, archive, readonly, cb);
		}
		catch(final SevenZipNativeInitializationException e)
		{
			Log.err(e.getMessage(), e);
			throw new IOException("7zip not supported on that platform"); //$NON-NLS-1$
		}
	}

	@Override
	public void close() throws IOException
	{
		native7Zip.close();
	}

	@Override
	public File getTempDir() throws IOException
	{
		return native7Zip.getTempDir();
	}

	@Override
	public int extract() throws IOException
	{
		return native7Zip.extract();
	}
	
	@Override
	public File extract(final String entry) throws IOException
	{
		return native7Zip.extract(entry);
	}

	@Override
	public InputStream extractStdOut(final String entry) throws IOException
	{
		return native7Zip.extractStdOut(entry);
	}

	@Override
	public int add(final String entry) throws IOException
	{
		return native7Zip.add(entry);
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		return native7Zip.add(baseDir, entry);
	}

	@Override
	public int addStdIn(final InputStream src, final String entry) throws IOException
	{
		return native7Zip.addStdIn(src, entry);
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		return native7Zip.delete(entry);
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		return native7Zip.rename(entry, newname);
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		return native7Zip.duplicate(entry, newname);
	}
}
