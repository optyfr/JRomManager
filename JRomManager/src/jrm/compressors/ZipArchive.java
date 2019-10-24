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
import java.net.URI;
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
import java.util.Map;

import org.apache.commons.io.FileUtils;

import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.misc.FindCmd;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import jrm.ui.progress.ProgressNarchiveCallBack;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * The external Zip archive class.<br>
 * If possible, wrap over {@link NArchive} via {@link ZipNArchive} to use SevenZipJBinding.<br>
 * Otherwise will try to use external zip executable if available...<br>
 * If command line is used, the archive will be extracted to temporary directory upon first write operation,
 * then entirely recreated from temporary directory upon archive's {@link #close()} operation 
 * @author optyfr
 *
 */
public class ZipArchive implements Archive
{
	private Session session;
	private File tempDir = null;
	private File archive;
	private String cmd;
	private boolean readonly;
	private ProgressNarchiveCallBack cb;

	private boolean is_7z;

	private final static HashMap<String, File> archives = new HashMap<>();

	private ZipNArchive native_zip = null;

	public ZipArchive(final Session session, final File archive) throws IOException
	{
		this(session, archive, false, null);
	}

	public ZipArchive(final Session session, final File archive, ProgressNarchiveCallBack cb) throws IOException
	{
		this(session, archive, false, cb);
	}

	public ZipArchive(final Session session, final File archive, final boolean readonly, ProgressNarchiveCallBack cb) throws IOException
	{
		this.session = session;
		this.cb = cb;
		this.archive = archive;
		try
		{
			native_zip = new ZipNArchive(session, archive, readonly, cb);
		}
		catch(final SevenZipNativeInitializationException e)
		{
			this.readonly = readonly;
			cmd = session.getUser().settings.getProperty(SettingsEnum.zip_cmd, FindCmd.find7z()); //$NON-NLS-1$
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
					Collections.addAll(cmd_add, "-mx=" + ZipOptions.valueOf(session.getUser().settings.getProperty(SettingsEnum.zip_level, ZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
					Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
				}
				else
				{
					Collections.addAll(cmd_add, cmd, "-r"); //$NON-NLS-1$
					Collections.addAll(cmd_add, "-" + ZipOptions.valueOf(session.getUser().settings.getProperty(SettingsEnum.zip_level, ZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
					Collections.addAll(cmd_add, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
				}
				final Process process = new ProcessBuilder(cmd_add).directory(tempDir).redirectErrorStream(true).start();
				try
				{
					err = process.waitFor();
				}
				catch(final InterruptedException e)
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
/*		final List<String> cmd = new ArrayList<>();
		if(is_7z)
		{
			Collections.addAll(cmd, session.getUser().settings.getProperty("7z_cmd", FindCmd.find7z()), "x", "-y", archive.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
					Log.err(e.getMessage(),e);
				}
			}
		}
		else*/
		{
			try(FileSystem srcfs = FileSystems.newFileSystem(archive.toPath(), null);)
			{
				if(entry != null && !entry.isEmpty())
					Files.copy(srcfs.getPath(entry), baseDir.toPath().resolve(entry));
				else
				{
					final Path sourcePath = srcfs.getPath("/"); //$NON-NLS-1$
					final Path targetPath = baseDir.toPath();
					if(cb != null)
						cb.setTotal(Files.walk(sourcePath).filter(p->Files.isRegularFile(p)).count());
					Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>()
					{
						long cnt = 0;

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
							if(cb != null)
								cb.setCompleted(++cnt);
							return FileVisitResult.CONTINUE;
						}
					});
					return 0;
				}
			}

		}
		return -1;
	}

	public static class CustomVisitor extends SimpleFileVisitor<Path>
	{
		private Path sourcePath = null;
		private FileSystem fs = null;

		public CustomVisitor()
		{
		}
		
		public CustomVisitor(Path sourcePath)
		{
			setSourcePath(sourcePath);
		}
		
		public Path getSourcePath()
		{
			return sourcePath;
		}

		private void setSourcePath(Path sourcePath)
		{
			this.sourcePath = sourcePath;
		}
		
		private void setFileSystem(FileSystem fs)
		{
			this.fs = fs;;
		}

		public FileSystem getFileSystem()
		{
			return fs;
		}
}
	
	public int extract_custom(CustomVisitor sfv)
	{
		try(FileSystem srcfs = FileSystems.newFileSystem(archive.toPath(), null);)
		{
			sfv.setFileSystem(srcfs);
			sfv.setSourcePath(srcfs.getPath("/"));
			if(cb != null)
				cb.setTotal(Files.walk(sfv.getSourcePath()).filter(p->Files.isRegularFile(p)).count());
			Files.walkFileTree(sfv.getSourcePath(), new SimpleFileVisitor<Path>()
			{
				long cnt = 0;

				@Override
				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
				{
					return sfv.preVisitDirectory(dir, attrs);
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
				{
					sfv.postVisitDirectory(dir, exc);
					return FileVisitResult.CONTINUE;
				};

				@Override
				public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
				{
					sfv.visitFile(file, attrs);
					if(cb != null)
						cb.setCompleted(++cnt);
					return FileVisitResult.CONTINUE;
				}
			});
			return 0;
		}
		catch(IOException ex)
		{
			Log.err(ex.getMessage(),ex);
		}
		return -1;
	}
	
	public int compress_custom(CustomVisitor sfv, Map<String, Object> env)
	{
		try (FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + archive.toURI()), env);) //$NON-NLS-1$
		{
			sfv.setFileSystem(fs);
			if(cb != null)
				cb.setTotal(Files.walk(sfv.getSourcePath()).filter(p->Files.isRegularFile(p)).count());
			Files.walkFileTree(sfv.getSourcePath(), new SimpleFileVisitor<Path>()
			{
				long cnt = 0;

				@Override
				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
				{
					return sfv.preVisitDirectory(dir, attrs);
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
				{
					sfv.postVisitDirectory(dir, exc);
					return FileVisitResult.CONTINUE;
				};

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
				{
					sfv.visitFile(file, attr);
					if(cb != null)
						cb.setCompleted(++cnt);
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch(IOException ex)
		{
			Log.err(ex.getMessage(),ex);
		}
		return -1;
	}
	
	@Override
	public int extract() throws IOException
	{
		if(native_zip != null)
			return native_zip.extract();
		return extract(getTempDir(), null);
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
		if(baseDir.isFile())
			FileUtils.copyFile(baseDir, new File(getTempDir(), entry));
		else if(!baseDir.equals(getTempDir()))
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
