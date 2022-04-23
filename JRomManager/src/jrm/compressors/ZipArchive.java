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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.misc.Log;
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
public class ZipArchive implements Archive
{
	private ProgressNarchiveCallBack cb;
	private File archive;

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
		this.cb = cb;
		this.archive = archive;
		try
		{
			nativeZip = new ZipNArchive(session, archive, readonly, cb);
		}
		catch(final SevenZipNativeInitializationException e)
		{
			throw new IOException("not supported on that platform"); //$NON-NLS-1$
		}
	}

	@Override
	public void close() throws IOException
	{
		nativeZip.close();
	}


	@Override
	public File getTempDir() throws IOException
	{
		return nativeZip.getTempDir();
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
		return nativeZip.extract();
	}
	
	@Override
	public File extract(final String entry) throws IOException
	{
		return nativeZip.extract(entry);
	}

	@Override
	public InputStream extractStdOut(final String entry) throws IOException
	{
		return nativeZip.extractStdOut(entry);
	}

	@Override
	public int add(final String entry) throws IOException
	{
		return nativeZip.add(entry);
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		return nativeZip.add(baseDir, entry);
	}

	@Override
	public int addStdIn(final InputStream src, final String entry) throws IOException
	{
		return nativeZip.addStdIn(src, entry);
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		return nativeZip.delete(entry);
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		return nativeZip.rename(entry, newname);
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		return nativeZip.duplicate(entry, newname);
	}
}
