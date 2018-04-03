package jrm.compressors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.misc.FindCmd;
import jrm.misc.Settings;

public class ZipArchive  implements Archive
{
	private File tempDir = null;
	private File archive;
	private String cmd;
	private boolean is_7z;

	private List<String> cmd_add = new ArrayList<>();

	public ZipArchive(File archive) throws IOException
	{
		this.archive = archive;
		this.cmd = Settings.getProperty("zip_cmd", FindCmd.find7z());
		if(!new File(this.cmd).exists() && !new File(this.cmd+".exe").exists())
			throw new IOException(this.cmd+" does not exists");
		this.is_7z = this.cmd.endsWith("7z")||this.cmd.endsWith("7z.exe");
	}

	@Override
	public void close() throws IOException
	{
		if(tempDir != null)
		{
			int err = 0;
			if(cmd_add.size()>0)
			{
				err = -1;
				Process process = new ProcessBuilder(cmd_add).directory(tempDir).start();
				try
				{
					err = process.waitFor();
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			FileUtils.deleteDirectory(tempDir);
			if(err!=0)
				throw new IOException("Process returned "+err);
		}
	}

	public File getTempDir() throws IOException
	{
		if(tempDir == null)
			this.tempDir = Files.createTempDirectory("JRM").toFile();
		return tempDir;
	}
	
	public File extract(String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>();
		if(is_7z)
		{
			Collections.addAll(cmd, this.cmd, "x", "-y", archive.getAbsolutePath(), entry);
			ProcessBuilder pb = new ProcessBuilder(cmd).directory(getTempDir());
			Process process = pb.start();
			try
			{
				process.waitFor();
				File result = new File(getTempDir(), entry);
				if (result.exists())
					return result;
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			try(FileSystem srcfs = FileSystems.newFileSystem(archive.toPath(), null);)
			{
				
				Path srcpath = srcfs.getPath(entry);
				return Files.copy(srcpath, new File(getTempDir(), entry).toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING).toFile();
			}
		}
		return null;
	}

	public InputStream extract_stdout(String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>();
		Collections.addAll(cmd, this.cmd, "x", "-y", "-so", archive.getAbsolutePath(), entry);
		return new ProcessBuilder(cmd).start().getInputStream();
	}

	public int add(String entry) throws IOException
	{
		return add(getTempDir(), entry);
	}

	public int add(File baseDir, String entry) throws IOException
	{
		if(cmd_add.size()==0)
		{
			if(is_7z)
			{				
				Collections.addAll(cmd_add, this.cmd, "a", "-y", "-r", "-tzip");
				Collections.addAll(cmd_add, Settings.getProperty("zip_args", ZipOptions.SEVENZIP_ULTRA.toString()).split("\\s"));
			}
			else
			{
				cmd_add.add(this.cmd);
				Collections.addAll(cmd_add, Settings.getProperty("zip_args", ZipOptions.ZIP_ULTRA.toString()).split("\\s"));
			}
			cmd_add.add(archive.getAbsolutePath());
		}
		cmd_add.add(entry);
		return 0;
/*		ProcessBuilder pb = new ProcessBuilder(cmd).directory(baseDir);
		Process process = pb.start();
		try
		{
			return process.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return -1;*/
	}

	
	public int add_stdin(InputStream src, String entry) throws IOException
	{
	//	List<String> cmd = new ArrayList<>();
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(),entry));
		if(cmd_add.size()==0)
		{
			if(is_7z)
			{				
				Collections.addAll(cmd_add, this.cmd, "a", "-y", "-r", "-tzip");
				Collections.addAll(cmd_add, Settings.getProperty("zip_args", ZipOptions.SEVENZIP_ULTRA.toString()).split("\\s"));
			}
			else
			{				
				Collections.addAll(cmd_add, this.cmd);
				Collections.addAll(cmd_add, Settings.getProperty("zip_args", ZipOptions.ZIP_ULTRA.toString()).split("\\s"));
			}
			cmd_add.add(archive.getAbsolutePath());
		}
		cmd_add.add(entry);
		return 0;
/*		ProcessBuilder pb = new ProcessBuilder(cmd).directory(getTempDir());
		Process process = pb.start();
		try
		{
			return process.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return -1;*/
	}

	public int delete(String entry) throws IOException
	{
		List<String> cmd = new ArrayList<>();
		if(is_7z)
		{
			Collections.addAll(cmd, this.cmd, "d");
			Collections.addAll(cmd, Settings.getProperty("zip_args", ZipOptions.SEVENZIP_ULTRA.toString()).split("\\s"));
			Collections.addAll(cmd, archive.getAbsolutePath(), entry);
		}
		else
		{
			Collections.addAll(cmd, this.cmd, archive.getAbsolutePath(), "-d", entry);
		}
		ProcessBuilder pb = new ProcessBuilder(cmd);
		Process process = pb.start();
		try
		{
			return process.waitFor();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	public int rename(String entry, String newname) throws IOException
	{
		List<String> cmd = new ArrayList<>();
		if(is_7z)
		{
			Collections.addAll(cmd, this.cmd, "rn");
			Collections.addAll(cmd, Settings.getProperty("zip_args", ZipOptions.SEVENZIP_ULTRA.toString()).split("\\s"));
			Collections.addAll(cmd, archive.getAbsolutePath(), entry, newname);
			ProcessBuilder pb = new ProcessBuilder(cmd);
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
		else
		{
			if(this.extract(entry)!=null)
			{
				if(this.delete(entry)==0)
				{
					FileUtils.moveFile(new File(getTempDir(),entry), new File(getTempDir(),newname));
					return this.add(newname);
				}
			}
		}
		return -1;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
