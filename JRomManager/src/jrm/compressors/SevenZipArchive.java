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
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.misc.FindCmd;
import jrm.misc.GlobalSettings;
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
	private File tempDir = null;
	private File archive = null;
	private String cmd = null;
	private boolean readonly = false;

	private final static HashMap<String, File> archives = new HashMap<>();

	private SevenZipNArchive native_7zip = null;

	public SevenZipArchive(final File archive) throws IOException
	{
		this(archive, false);
	}

	public SevenZipArchive(final File archive, final boolean readonly) throws IOException
	{
		try
		{
			native_7zip = new SevenZipNArchive(archive, readonly);
		}
		catch(final SevenZipNativeInitializationException e)
		{
			this.readonly = readonly;
			cmd = GlobalSettings.getProperty("7z_cmd", FindCmd.find7z()); //$NON-NLS-1$
			if(!new File(cmd).exists() && !new File(cmd + ".exe").exists()) //$NON-NLS-1$
				throw new IOException(cmd + " does not exists"); //$NON-NLS-1$
			if(null == (this.archive = SevenZipArchive.archives.get(archive.getAbsolutePath())))
				SevenZipArchive.archives.put(archive.getAbsolutePath(), this.archive = archive);
		}
	}

	@Override
	public void close() throws IOException
	{
		if(native_7zip != null)
			native_7zip.close();
		else if(tempDir != null)
		{
			if(readonly)
			{
				FileUtils.deleteDirectory(tempDir);
			}
			else
			{
				int err = -1;
				final List<String> cmd_add = new ArrayList<>();
				final Path tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z"); //$NON-NLS-1$ //$NON-NLS-2$
				tmpfile.toFile().delete();
				Collections.addAll(cmd_add, GlobalSettings.getProperty("7z_cmd", FindCmd.find7z()), "a", "-r", "-t7z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				Collections.addAll(cmd_add, "-ms=" + (GlobalSettings.getProperty("7z_solid", false) ? "on" : "off"), "-mx=" + GlobalSettings.getProperty("7z_level", SevenZipOptions.NORMAL.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
				final Process process = new ProcessBuilder(cmd_add).directory(tempDir).redirectErrorStream(true).start();
				try
				{
					err = process.waitFor();
				}
				catch(final InterruptedException e)
				{
					e.printStackTrace();
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
		if(native_7zip != null)
			return native_7zip.getTempDir();
		if(tempDir == null)
		{
			tempDir = Files.createTempDirectory("JRM").toFile(); //$NON-NLS-1$
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
		final List<String> cmd = new ArrayList<>();
		Collections.addAll(cmd, GlobalSettings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", archive.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(entry != null && !entry.isEmpty())
			cmd.add(entry);
		final ProcessBuilder pb = new ProcessBuilder(cmd).directory(baseDir);
		synchronized(archive)
		{
			final Process process = pb.start();
			try
			{
				return process.waitFor();
			}
			catch(final InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return -1;
	}

	@Override
	public File extract(final String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.extract(entry);
		if(readonly)
			extract(getTempDir(), entry);
		final File result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	@Override
	public InputStream extract_stdout(final String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.extract_stdout(entry);
		if(readonly)
			extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	@Override
	public int add(final String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.add(entry);
		return add(getTempDir(), entry);
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.add(baseDir, entry);
		if(readonly)
			return -1;
		if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int add_stdin(final InputStream src, final String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.add_stdin(src, entry);
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.delete(entry);
		if(readonly)
			return -1;
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.rename(entry, newname);
		if(readonly)
			return -1;
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.duplicate(entry, newname);
		if(readonly)
			return -1;
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
