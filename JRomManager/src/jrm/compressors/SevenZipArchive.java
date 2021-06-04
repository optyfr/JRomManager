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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.misc.FindCmd;
import jrm.misc.IOUtils;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
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
	private Session session;
	private File tempDir = null;
	private File archive = null;
	private String cmd = null;
	private boolean readonly = false;

	private static final HashMap<String, File> archives = new HashMap<>();

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
		this.session = session;
		try
		{
			native7Zip = new SevenZipNArchive(session, archive, readonly, cb);
		}
		catch(final SevenZipNativeInitializationException e)
		{
			Log.err(e.getMessage(), e);
			this.readonly = readonly;
			cmd = session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_cmd, FindCmd.find7z()); //$NON-NLS-1$
			if(!new File(cmd).exists() && !new File(cmd + ".exe").exists()) //$NON-NLS-1$
				throw new IOException(cmd + " does not exists"); //$NON-NLS-1$
			if(null == (this.archive = SevenZipArchive.archives.get(archive.getAbsolutePath())))
			{
				this.archive = archive;
				SevenZipArchive.archives.put(archive.getAbsolutePath(), archive);
			}
		}
	}

	@Override
	public void close() throws IOException
	{
		if(native7Zip != null)
			native7Zip.close();
		else if(tempDir != null)
		{
			if(readonly)
			{
				FileUtils.deleteDirectory(tempDir);
			}
			else
			{
				int err = -1;
				final var cmdAdd = new ArrayList<String>();
				final Path tmpfile = IOUtils.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z"); //$NON-NLS-1$ //$NON-NLS-2$
				Files.delete(tmpfile);
				Collections.addAll(cmdAdd, session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_cmd, FindCmd.find7z()), "a", "-r", "-t7z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				Collections.addAll(cmdAdd, "-ms=" + (session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_solid, false) ? "on" : "off"), "-mx=" + SevenZipOptions.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_level, SevenZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				Collections.addAll(cmdAdd, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
				final var process = new ProcessBuilder(cmdAdd).directory(tempDir).redirectErrorStream(true).start();
				try
				{
					err = process.waitFor();
				}
				catch(InterruptedException e)
				{
					Log.err(e.getMessage(),e);
				}
				FileUtils.deleteDirectory(tempDir);
				if(err != 0)
				{
					Files.deleteIfExists(tmpfile);
					throw new IOException("Process returned " + err); //$NON-NLS-1$
				}
				else
				{
					synchronized(archive)
					{
						Files.move(tmpfile, archive.toPath(), StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
			tempDir = null;
		}
	}

	@Override
	public File getTempDir() throws IOException
	{
		if(native7Zip != null)
			return native7Zip.getTempDir();
		if(tempDir == null)
		{
			tempDir = IOUtils.createTempDirectory("JRM").toFile(); //$NON-NLS-1$
			if(archive.exists() && !readonly)
			{
				if(extract(tempDir, null) == 0)
					return tempDir;
				FileUtils.deleteDirectory(tempDir);
				tempDir = null;
			}
		}
		return tempDir;
	}

	private int extract(final File baseDir, final String entry) throws IOException
	{
		final var command = new ArrayList<String>();
		Collections.addAll(command, session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_cmd, FindCmd.find7z()), "x", "-y", archive.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(entry != null && !entry.isEmpty())
			command.add(entry);
		final ProcessBuilder pb = new ProcessBuilder(command).directory(baseDir);
		synchronized(archive)
		{
			final var process = pb.start();
			try
			{
				return process.waitFor();
			}
			catch(InterruptedException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		return -1;
	}

	@Override
	public int extract() throws IOException
	{
		if(native7Zip != null)
			return native7Zip.extract();
		return extract(getTempDir(), null);
	}
	
	@Override
	public File extract(final String entry) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.extract(entry);
		if(readonly)
			extract(getTempDir(), entry);
		final var result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	@Override
	public InputStream extract_stdout(final String entry) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.extract_stdout(entry);
		if(readonly)
			extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	@Override
	public int add(final String entry) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.add(entry);
		return add(getTempDir(), entry);
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.add(baseDir, entry);
		if(readonly)
			return -1;
		if(baseDir.isFile())
			FileUtils.copyFile(baseDir, new File(getTempDir(), entry));
		else if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int add_stdin(final InputStream src, final String entry) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.add_stdin(src, entry);
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.delete(entry);
		if(readonly)
			return -1;
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.rename(entry, newname);
		if(readonly)
			return -1;
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		if(native7Zip != null)
			return native7Zip.duplicate(entry, newname);
		if(readonly)
			return -1;
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
