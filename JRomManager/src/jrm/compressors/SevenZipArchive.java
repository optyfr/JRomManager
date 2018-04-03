package jrm.compressors;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.misc.FindCmd;
import jrm.misc.Settings;

public class SevenZipArchive implements Archive
{
	private File tempDir = null;
	private File archive;
	private String cmd;


	public SevenZipArchive(File archive) throws IOException
	{
		this.archive = archive;
		this.cmd = Settings.getProperty("7z_cmd", FindCmd.find7z());
		if(!new File(this.cmd).exists() && !new File(this.cmd+".exe").exists())
			throw new IOException(this.cmd+" does not exists");
	}

	@Override
	public void close() throws IOException
	{
		if(tempDir != null)
		{
			int err = -1;
			List<String> cmd_add = new ArrayList<>();
			Path tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z");
			tmpfile.toFile().delete();
			Collections.addAll(cmd_add, Settings.getProperty("7z_cmd", FindCmd.find7z()), "a", "-r", "-t7z");
			Collections.addAll(cmd_add, Settings.getProperty("7z_args", SevenZipOptions.SEVENZIP_ULTRA.toString()).split("\\s"));
			Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*");
			Process process = new ProcessBuilder(cmd_add).directory(tempDir).redirectErrorStream(true).start();
			BufferedInputStream is = new BufferedInputStream(process.getInputStream());
			StringBuffer lines = new StringBuffer();
			try
			{
				new Thread(new Runnable(){
				    public void run(){
				    	   try {
				               InputStreamReader isr = new InputStreamReader(is);
				               BufferedReader br = new BufferedReader(isr);
				               String line = null;
				               while ((line = br.readLine()) != null)
				                   lines.append(line).append("\n");
				           }
				           catch (IOException ioe) {
				               ioe.printStackTrace();
				           }
				    }
				}).start();
				err = process.waitFor();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			FileUtils.deleteDirectory(tempDir);
			if(err!=0)
			{
				Files.deleteIfExists(tmpfile);
				System.out.println(lines);
				throw new IOException("Process returned "+err);
			}
			else
			{
				Files.deleteIfExists(archive.toPath());
				Files.move(tmpfile, archive.toPath());
			}
		}
	}

	public File getTempDir() throws IOException
	{
		if(tempDir == null)
		{
			if(archive.exists())
			{
				this.tempDir = Files.createTempDirectory("JRM").toFile();
				List<String> cmd = new ArrayList<>();
				Collections.addAll(cmd, Settings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", archive.getAbsolutePath());
				ProcessBuilder pb = new ProcessBuilder(cmd).directory(getTempDir());
				Process process = pb.start();
				try
				{
					if(process.waitFor()==0)
						return tempDir;
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				FileUtils.deleteDirectory(tempDir);
				tempDir = null;
			}
		}
		return tempDir;
	}
	
	public File extract(String entry) throws IOException
	{
		File result = new File(getTempDir(),entry);
		if(result.exists())
			return result;
		return null;
	}

	public InputStream extract_stdout(String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>();
		Collections.addAll(cmd, Settings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", "-so", archive.getAbsolutePath(), entry);
		return new ProcessBuilder(cmd).start().getInputStream();
	}

	public int add(String entry) throws IOException
	{
		return add(getTempDir(), entry);
	}

	public int add(File baseDir, String entry) throws IOException
	{
		if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	public int add_stdin(InputStream src, String entry) throws IOException
	{
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(),entry));
		return 0;
	}

	public int delete(String entry) throws IOException
	{
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	public int rename(String entry, String newname) throws IOException
	{
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
