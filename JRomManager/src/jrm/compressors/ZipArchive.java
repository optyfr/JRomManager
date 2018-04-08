package jrm.compressors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.misc.FindCmd;
import jrm.misc.Settings;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

public class ZipArchive implements Archive
{
	private File tempDir = null;
	private File archive;
	private String cmd;
	private boolean readonly;

	private boolean is_7z;

	private static HashMap<String, File> archives = new HashMap<>();

	private ZipNArchive native_zip = null;

	public ZipArchive(File archive) throws IOException
	{
		this(archive, false);
	}

	public ZipArchive(File archive, boolean readonly) throws IOException
	{
		try
		{
			native_zip = new ZipNArchive(archive, readonly);
		}
		catch(SevenZipNativeInitializationException e)
		{
			this.readonly = readonly;
			this.archive = archive;
			this.cmd = Settings.getProperty("zip_cmd", FindCmd.find7z());
			if(!new File(this.cmd).exists() && !new File(this.cmd + ".exe").exists())
				throw new IOException(this.cmd + " does not exists");
			if(null == (this.archive = archives.get(archive.getAbsolutePath())))
				archives.put(archive.getAbsolutePath(), this.archive = archive);
			this.is_7z = this.cmd.endsWith("7z") || this.cmd.endsWith("7z.exe");
		}
	}

	@Override
	public void close() throws IOException
	{
		if(native_zip != null)
			native_zip.close();
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
				Path tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z");
				tmpfile.toFile().delete();
				if(is_7z)
				{
					Collections.addAll(cmd_add, this.cmd, "a", "-r", "-t7z");
					Collections.addAll(cmd_add, "-mx=" + Settings.getProperty("zip_level", ZipOptions.NORMAL.toString()));
					Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*");
				}
				else
				{
					Collections.addAll(cmd_add, this.cmd, "-r");
					Collections.addAll(cmd_add, "-" + Settings.getProperty("zip_level", ZipOptions.NORMAL.toString()));
					Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*");
				}
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
		if(native_zip != null)
			return native_zip.getTempDir();
		if(tempDir == null)
		{
			tempDir = Files.createTempDirectory("JRM").toFile();
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
		if(is_7z)
		{
			Collections.addAll(cmd, Settings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", archive.getAbsolutePath());
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
		}
		else
		{
			try(FileSystem srcfs = FileSystems.newFileSystem(archive.toPath(), null);)
			{
				if(entry != null && !entry.isEmpty())
					Files.copy(srcfs.getPath(entry), baseDir.toPath().resolve(entry));
				else
				{
					Path sourcePath = srcfs.getPath("/");
					Path targetPath = baseDir.toPath();
					Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>()
					{
						@Override
						public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
						{
							Files.createDirectories(targetPath.resolve(sourcePath.relativize(dir)));
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
						{
							Files.copy(file, targetPath.resolve(sourcePath.relativize(file)));
							return FileVisitResult.CONTINUE;
						}
					});
					return 0;
				}
			}

		}
		return -1;
	}

	public File extract(String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.extract(entry);
		if(readonly)
			extract(getTempDir(), entry);
		File result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	public InputStream extract_stdout(String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.extract_stdout(entry);
		if(readonly)
			extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	public int add(String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.add(entry);
		return add(getTempDir(), entry);
	}

	public int add(File baseDir, String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.add(baseDir, entry);
		if(readonly)
			return -1;
		if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	public int add_stdin(InputStream src, String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.add_stdin(src, entry);
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		return 0;
	}

	public int delete(String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.delete(entry);
		if(readonly)
			return -1;
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	public int rename(String entry, String newname) throws IOException
	{
		if(native_zip != null)
			return native_zip.rename(entry, newname);
		if(readonly)
			return -1;
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		if(native_zip != null)
			return native_zip.duplicate(entry, newname);
		if(readonly)
			return -1;
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
