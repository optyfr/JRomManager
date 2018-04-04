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
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

public class SevenZipArchive implements Archive
{
	private File tempDir = null;
	private File archive;
	private String cmd;
	private boolean readonly;

	private static HashMap<String,File> archives = new HashMap<>();

	
	public SevenZipArchive(File archive) throws IOException
	{
		this(archive, false);
	}
	
	public SevenZipArchive(File archive, boolean readonly) throws IOException
	{
		if(!SevenZip.isInitializedSuccessfully())
		{
			try
			{
				SevenZip.initSevenZipFromPlatformJAR();
			}
			catch (SevenZipNativeInitializationException e)
			{
				e.printStackTrace();
			}
		}
		this.readonly = readonly;
		this.cmd = Settings.getProperty("7z_cmd", FindCmd.find7z());
		if (!new File(this.cmd).exists() && !new File(this.cmd + ".exe").exists())
			throw new IOException(this.cmd + " does not exists");
		if (null==(this.archive=archives.get(archive.getAbsolutePath())))
			archives.put(archive.getAbsolutePath(), this.archive = archive);
	}

	@Override
	public void close() throws IOException
	{
		if (tempDir != null)
		{
			if(readonly)
			{
				FileUtils.deleteDirectory(tempDir);
			}
			else
			{
				int err = -1;
				List<String> cmd_add = new ArrayList<>();
				Path tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z");
				tmpfile.toFile().delete();
				Collections.addAll(cmd_add, Settings.getProperty("7z_cmd", FindCmd.find7z()), "a", "-r", "-t7z");
				Collections.addAll(cmd_add, Settings.getProperty("7z_args", SevenZipOptions.SEVENZIP_ULTRA.toString()).split("\\s"));
				Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*");
				Process process = new ProcessBuilder(cmd_add).directory(tempDir).redirectErrorStream(true).start();
				try
				{
					err = process.waitFor();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				FileUtils.deleteDirectory(tempDir);
				if (err != 0)
				{
					Files.deleteIfExists(tmpfile);
					throw new IOException("Process returned " + err);
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
		if (tempDir == null)
		{
			tempDir = Files.createTempDirectory("JRM").toFile();
			if (archive.exists() && !readonly)
			{
				if(extract(tempDir,null) == 0)
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
		Collections.addAll(cmd, Settings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", archive.getAbsolutePath());
		if(entry!=null && !entry.isEmpty())
			cmd.add(entry);
		ProcessBuilder pb = new ProcessBuilder(cmd).directory(baseDir);
		synchronized(archive)
		{
			Process process = pb.start();
			try
			{
				 return process.waitFor();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return -1;
	}

	public File extract(String entry) throws IOException
	{
		if(readonly)
			extract(getTempDir(), entry);
		File result = new File(getTempDir(), entry);
		if (result.exists())
			return result;
		return null;
	}

	public InputStream extract_stdout(String entry) throws IOException
	{
		if(readonly)
			extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(),entry));
	}

	public int add(String entry) throws IOException
	{
		return add(getTempDir(), entry);
	}

	public int add(File baseDir, String entry) throws IOException
	{
		if(readonly)
			return -1;
		if (!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	public int add_stdin(InputStream src, String entry) throws IOException
	{
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		return 0;
	}

	public int delete(String entry) throws IOException
	{
		if(readonly)
			return -1;
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	public int rename(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
