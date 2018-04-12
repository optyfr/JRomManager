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
import jrm.misc.Settings;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

public class SevenZipArchive implements Archive
{
	private File tempDir = null;
	private File archive = null;
	private String cmd = null;
	private boolean readonly = false;

	private static HashMap<String, File> archives = new HashMap<>();

	private SevenZipNArchive native_7zip = null;

	public SevenZipArchive(File archive) throws IOException
	{
		this(archive, false);
	}

	public SevenZipArchive(File archive, boolean readonly) throws IOException
	{
		try
		{
			native_7zip = new SevenZipNArchive(archive, readonly);
		}
		catch(SevenZipNativeInitializationException e)
		{
			this.readonly = readonly;
			this.cmd = Settings.getProperty("7z_cmd", FindCmd.find7z()); //$NON-NLS-1$
			if(!new File(this.cmd).exists() && !new File(this.cmd + ".exe").exists()) //$NON-NLS-1$
				throw new IOException(this.cmd + " does not exists"); //$NON-NLS-1$
			if(null == (this.archive = archives.get(archive.getAbsolutePath())))
				archives.put(archive.getAbsolutePath(), this.archive = archive);
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
				List<String> cmd_add = new ArrayList<>();
				Path tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z"); //$NON-NLS-1$ //$NON-NLS-2$
				tmpfile.toFile().delete();
				Collections.addAll(cmd_add, Settings.getProperty("7z_cmd", FindCmd.find7z()), "a", "-r", "-t7z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				Collections.addAll(cmd_add, "-ms=" + (Settings.getProperty("7z_solid", false) ? "on" : "off"), "-mx=" + Settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
				Process process = new ProcessBuilder(cmd_add).directory(tempDir).redirectErrorStream(true).start();
				try
				{
					err = process.waitFor();
				}
				catch(InterruptedException e)
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

	private int extract(File baseDir, String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>();
		Collections.addAll(cmd, Settings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", archive.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if(entry != null && !entry.isEmpty())
			cmd.add(entry);
		ProcessBuilder pb = new ProcessBuilder(cmd).directory(baseDir);
		synchronized(archive)
		{
			Process process = pb.start();
			try
			{
				return process.waitFor();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return -1;
	}

	public File extract(String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.extract(entry);
		if(readonly)
			extract(getTempDir(), entry);
		File result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	public InputStream extract_stdout(String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.extract_stdout(entry);
		if(readonly)
			extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	public int add(String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.add(entry);
		return add(getTempDir(), entry);
	}

	public int add(File baseDir, String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.add(baseDir, entry);
		if(readonly)
			return -1;
		if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	public int add_stdin(InputStream src, String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.add_stdin(src, entry);
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		return 0;
	}

	public int delete(String entry) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.delete(entry);
		if(readonly)
			return -1;
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	public int rename(String entry, String newname) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.rename(entry, newname);
		if(readonly)
			return -1;
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		if(native_7zip != null)
			return native_7zip.duplicate(entry, newname);
		if(readonly)
			return -1;
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
