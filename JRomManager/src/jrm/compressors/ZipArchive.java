package jrm.compressors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
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

	public ZipArchive(final File archive) throws IOException
	{
		this(archive, false);
	}

	public ZipArchive(final File archive, final boolean readonly) throws IOException
	{
		try
		{
			native_zip = new ZipNArchive(archive, readonly);
		}
		catch(final SevenZipNativeInitializationException e)
		{
			this.readonly = readonly;
			this.archive = archive;
			cmd = Settings.getProperty("zip_cmd", FindCmd.find7z()); //$NON-NLS-1$
			if(!new File(cmd).exists() && !new File(cmd + ".exe").exists()) //$NON-NLS-1$
				throw new IOException(cmd + " does not exists"); //$NON-NLS-1$
			if(null == (this.archive = ZipArchive.archives.get(archive.getAbsolutePath())))
				ZipArchive.archives.put(archive.getAbsolutePath(), this.archive = archive);
			is_7z = cmd.endsWith("7z") || cmd.endsWith("7z.exe"); //$NON-NLS-1$ //$NON-NLS-2$
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
				final List<String> cmd_add = new ArrayList<>();
				final Path tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z"); //$NON-NLS-1$ //$NON-NLS-2$
				tmpfile.toFile().delete();
				if(is_7z)
				{
					Collections.addAll(cmd_add, cmd, "a", "-r", "-t7z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					Collections.addAll(cmd_add, "-mx=" + Settings.getProperty("zip_level", ZipOptions.NORMAL.toString())); //$NON-NLS-1$ //$NON-NLS-2$
					Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
				}
				else
				{
					Collections.addAll(cmd_add, cmd, "-r"); //$NON-NLS-1$
					Collections.addAll(cmd_add, "-" + Settings.getProperty("zip_level", ZipOptions.NORMAL.toString())); //$NON-NLS-1$ //$NON-NLS-2$
					Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
				}
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
		if(native_zip != null)
			return native_zip.getTempDir();
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
		if(is_7z)
		{
			Collections.addAll(cmd, Settings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", archive.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		}
		else
		{
			try(FileSystem srcfs = FileSystems.newFileSystem(archive.toPath(), null);)
			{
				if(entry != null && !entry.isEmpty())
					Files.copy(srcfs.getPath(entry), baseDir.toPath().resolve(entry));
				else
				{
					final Path sourcePath = srcfs.getPath("/"); //$NON-NLS-1$
					final Path targetPath = baseDir.toPath();
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

	@Override
	public File extract(final String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.extract(entry);
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
		if(native_zip != null)
			return native_zip.extract_stdout(entry);
		if(readonly)
			extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	@Override
	public int add(final String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.add(entry);
		return add(getTempDir(), entry);
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.add(baseDir, entry);
		if(readonly)
			return -1;
		if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int add_stdin(final InputStream src, final String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.add_stdin(src, entry);
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		if(native_zip != null)
			return native_zip.delete(entry);
		if(readonly)
			return -1;
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		if(native_zip != null)
			return native_zip.rename(entry, newname);
		if(readonly)
			return -1;
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		if(native_zip != null)
			return native_zip.duplicate(entry, newname);
		if(readonly)
			return -1;
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
