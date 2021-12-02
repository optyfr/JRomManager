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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.misc.FindCmd;
import jrm.misc.IOUtils;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
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
public class ZipArchive extends AbstractArchive
{
	private Session session;
	private String cmd;
	private boolean readonly;
	private ProgressNarchiveCallBack cb;

	private boolean is7z;

	private static final HashMap<String, File> archives = new HashMap<>();

	private ZipNArchive nativeZip = null;

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
			nativeZip = new ZipNArchive(session, archive, readonly, cb);
		}
		catch(final SevenZipNativeInitializationException e)
		{
			this.readonly = readonly;
			cmd = session.getUser().getSettings().getProperty(SettingsEnum.zip_cmd, FindCmd.find7z()); //$NON-NLS-1$
			if(!new File(cmd).exists() && !new File(cmd + ".exe").exists()) //$NON-NLS-1$
				throw new IOException(cmd + " does not exists"); //$NON-NLS-1$
			if(null == (this.archive = ZipArchive.archives.get(archive.getAbsolutePath())))
			{
				this.archive = archive;
				ZipArchive.archives.put(archive.getAbsolutePath(), archive);
			}
			is7z = cmd.endsWith("7z") || cmd.endsWith("7z.exe"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	public void close() throws IOException
	{
		if(nativeZip != null)
		{
			nativeZip.close();
			return;
		}
		if(tempDir == null)
			return;
		if(readonly)
		{
			FileUtils.deleteDirectory(tempDir);
			tempDir = null;
			return;
		}
		final List<String> cmdAdd = new ArrayList<>();
		final Path tmpfile = IOUtils.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z"); //$NON-NLS-1$ //$NON-NLS-2$
		Files.delete(tmpfile);
		if(is7z)
		{
			Collections.addAll(cmdAdd, cmd, "a", "-r", "-t7z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			Collections.addAll(cmdAdd, "-mx=" + ZipOptions.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.zip_level, ZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
			Collections.addAll(cmdAdd, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
		}
		else
		{
			Collections.addAll(cmdAdd, cmd, "-r"); //$NON-NLS-1$
			Collections.addAll(cmdAdd, "-" + ZipOptions.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.zip_level, ZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
			Collections.addAll(cmdAdd, tmpfile.toFile().getAbsolutePath(), "*"); //$NON-NLS-1$
		}
		close(cmdAdd, tmpfile);
		tempDir = null;
}


	@Override
	public File getTempDir() throws IOException
	{
		if(nativeZip != null)
			return nativeZip.getTempDir();
		if(tempDir == null)
		{
			tempDir = IOUtils.createTempDirectory("JRM").toFile(); //$NON-NLS-1$
			if(archive.exists() && !readonly)
			{
				if(extract(tempDir, null) == 0)	//NOSONAR
					return tempDir;
				FileUtils.deleteDirectory(tempDir);
				tempDir = null;
			}
		}
		return tempDir;
	}

	private int extract(final File baseDir, final String entry) throws IOException
	{
		try (final var srcfs = FileSystems.newFileSystem(archive.toPath(), (ClassLoader) null);)
		{
			if (entry != null && !entry.isEmpty())
				Files.copy(srcfs.getPath(entry), baseDir.toPath().resolve(entry));
			else
			{
				final var sourcePath = srcfs.getPath("/"); //$NON-NLS-1$
				final var targetPath = baseDir.toPath();
				if (cb != null)
					try (final var stream = Files.walk(sourcePath))
					{
						cb.setTotal(stream.filter(Files::isRegularFile).count());
					}
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
						if (cb != null)
							cb.setCompleted(++cnt);
						return FileVisitResult.CONTINUE;
					}
				});
				return 0;
			}
		}
		return -1;
	}

	private final class CustomVisitorCB extends SimpleFileVisitor<Path>
	{
		private final CustomVisitor sfv;
		long cnt = 0;

		private CustomVisitorCB(CustomVisitor sfv)
		{
			this.sfv = sfv;
		}

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
		}

		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
		{
			sfv.visitFile(file, attrs);
			if(cb != null)
				cb.setCompleted(++cnt);
			return FileVisitResult.CONTINUE;
		}
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
			this.fs = fs;
		}

		public FileSystem getFileSystem()
		{
			return fs;
		}
}
	
	public int extractCustom(CustomVisitor sfv)
	{
		try(final var srcfs = FileSystems.newFileSystem(archive.toPath(), (ClassLoader)null);)
		{
			sfv.setFileSystem(srcfs);
			sfv.setSourcePath(srcfs.getPath("/"));
			if(cb != null)
				try(final var stream = Files.walk(sfv.getSourcePath()))
				{
					cb.setTotal(stream.filter(Files::isRegularFile).count());
				}
			Files.walkFileTree(sfv.getSourcePath(), new CustomVisitorCB(sfv));
			return 0;
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
		if(nativeZip != null)
			return nativeZip.extract();
		return extract(getTempDir(), null);
	}
	
	@Override
	public File extract(final String entry) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.extract(entry);
		if(readonly)
			extract(getTempDir(), entry);
		final var result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	@Override
	public InputStream extractStdOut(final String entry) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.extractStdOut(entry);
		if(readonly)
			extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	@Override
	public int add(final String entry) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.add(entry);
		return add(getTempDir(), entry);
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.add(baseDir, entry);
		if(readonly)
			return -1;
		if(baseDir.isFile())
			FileUtils.copyFile(baseDir, new File(getTempDir(), entry));
		else if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int addStdIn(final InputStream src, final String entry) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.addStdIn(src, entry);
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.delete(entry);
		if(readonly)
			return -1;
		FileUtils.deleteQuietly(new File(getTempDir(), entry));
		return 0;
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.rename(entry, newname);
		if(readonly)
			return -1;
		FileUtils.moveFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		if(nativeZip != null)
			return nativeZip.duplicate(entry, newname);
		if(readonly)
			return -1;
		FileUtils.copyFile(new File(getTempDir(), entry), new File(getTempDir(), newname));
		return 0;
	}
}
