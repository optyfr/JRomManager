package jrm.batch;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;

import JTrrntzip.SimpleTorrentZipOptions;
import JTrrntzip.TorrentZip;
import JTrrntzip.TrrntZipStatus;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.compressors.zipfs.ZipLevel;
import jrm.compressors.zipfs.ZipTempThreshold;
import jrm.misc.HTMLRenderer;
import jrm.security.Session;
import jrm.ui.progress.ProgressHandler;
import jrm.ui.progress.ProgressTZipCallBack;

public class Compressor implements HTMLRenderer
{
	Session session;
	AtomicInteger cnt;
	int total;
	ProgressHandler progress;
	
	public static class FileResult
	{
		public File file;
		public String result = "";
		
		public FileResult(File file)
		{
			this.file = file;
		}
	}

	public Compressor(Session session, AtomicInteger cnt, int total, ProgressHandler progress)
	{
		this.session = session;
		this.cnt = cnt;
		this.total = total;
		this.progress = progress;
	}
	
	private static long size(Path path)
	{
		final AtomicLong size = new AtomicLong(0);
		try
		{
			Files.walkFileTree(path, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				{
					size.addAndGet(attrs.size());
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc)
				{
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				{
					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch (IOException e)
		{
			throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
		}
		return size.get();
	}
	
	public interface UpdResultCallBack
	{
		public abstract void apply(String txt);
	}

	public interface UpdSrcCallBack
	{
		public abstract void apply(File file);
	}

	public File sevenZip2SevenZip(File file, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		try
		{
			File tmpfile = Files.createTempFile(file.getParentFile().toPath(), "JRM", ".7z").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".7z");
			try(SevenZipArchive archive = new SevenZipArchive(session, file))
			{
				progress.setProgress(toHTML("extracting " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
				if(archive.extract()==0)
				{
					try(SevenZipArchive newarchive = new SevenZipArchive(session, tmpfile))
					{
						Path basedir = archive.getTempDir().toPath();
						Files.walkFileTree(basedir, new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
							{
								newarchive.add_stdin(Files.newInputStream(file), basedir.relativize(file).toString());
								return FileVisitResult.CONTINUE;
							}
							@Override
							public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
							{
								return FileVisitResult.CONTINUE;
							};
						});
						progress.setProgress(toHTML("Crunching " + toItalic(StringEscapeUtils.escapeHtml4(newfile.getName()))), cnt.get(), total);
					}
				}
				else
				{
					tmpfile.delete();
					cb.apply("extract failed");
					return null;
				}
			}
			catch(Exception e)
			{
				tmpfile.delete();
				cb.apply("7z creation failed");
				return null;
			}
			if(tmpfile.exists())
			{
				file.delete();
				tmpfile.renameTo(newfile);
				scb.apply(newfile);
				cb.apply("OK");
				return newfile;
			}
		}
		catch(IOException e)
		{
			cb.apply("Failed");
		}
		return null;
	}

	public File sevenZip2Zip(File file, boolean tzip, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		try
		{
			File tmpfile = Files.createTempFile(file.getParentFile().toPath(), "JRM", ".zip").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".zip");
			try(SevenZipArchive archive = new SevenZipArchive(session, file))
			{
				progress.setProgress(toHTML("extracting " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
				if(archive.extract()==0)
				{
					File basedir = archive.getTempDir();
					final Map<String, Object> env = new HashMap<>();
					env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
					env.put("useTempFile", FileUtils.sizeOf(basedir) > ZipTempThreshold.valueOf(session.getUser().settings.getProperty("zip_temp_threshold", ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
					env.put("compressionLevel", tzip ? 1 :  ZipLevel.valueOf(session.getUser().settings.getProperty("zip_compression_level", ZipLevel.DEFAULT.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
					FileUtils.forceMkdirParent(tmpfile);
					progress.setProgress(toHTML("creating " + toItalic(StringEscapeUtils.escapeHtml4(newfile.getName()))), cnt.get(), total);
					try (FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + tmpfile.toURI()), env);) //$NON-NLS-1$
					{
						Files.walkFileTree(basedir.toPath(), new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
							{
								Path dst = fs.getPath(basedir.toPath().relativize(file).toString());
								if(dst.getParent()!=null)
									Files.createDirectories(dst.getParent());
								Files.copy(file, dst, StandardCopyOption.REPLACE_EXISTING);
								return FileVisitResult.CONTINUE;
							}
							@Override
							public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
							{
								Path dst = fs.getPath(basedir.toPath().relativize(dir).toString());
								Files.createDirectories(dst);
								return FileVisitResult.CONTINUE;
							};
						});
					}
				}
				else
				{
					tmpfile.delete();
					cb.apply("extract failed");
					return null;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				tmpfile.delete();
				cb.apply("zip creation failed");
				return null;
			}
			if(tmpfile.exists())
			{
				file.delete();
				tmpfile.renameTo(newfile);
				scb.apply(newfile);
				cb.apply("OK");
				return newfile;
			}
		}
		catch (IOException e)
		{
			cb.apply("Failed");
		}
		return null;
	}

	public File zip2Zip(File file, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		try
		{
			File tmpfile = Files.createTempFile(file.getParentFile().toPath(), "JRM", ".zip").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".zip");
			try (FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + file.toURI()), new HashMap<>());) //$NON-NLS-1$
			{
				Path basedir = fs.getPath("/");
				final Map<String, Object> env = new HashMap<>();
				env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				env.put("useTempFile", size(basedir) > ZipTempThreshold.valueOf(session.getUser().settings.getProperty("zip_temp_threshold", ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
				env.put("compressionLevel", ZipLevel.valueOf(session.getUser().settings.getProperty("zip_compression_level", ZipLevel.DEFAULT.toString())).getLevel()); //$NON-NLS-1$
				progress.setProgress(toHTML("Crunching " + toItalic(StringEscapeUtils.escapeHtml4(newfile.getName()))), cnt.get(), total);
				try (FileSystem newfs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + tmpfile.toURI()), env);) //$NON-NLS-1$
				{
					Files.walkFileTree(basedir, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
						{
							Path dst = newfs.getPath(basedir.relativize(file).toString());
							if(dst.getParent()!=null)
								Files.createDirectories(dst.getParent());
							Files.copy(file, dst, StandardCopyOption.REPLACE_EXISTING);
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
						{
							Path dst = newfs.getPath(basedir.relativize(dir).toString());
							Files.createDirectories(dst);
							return FileVisitResult.CONTINUE;
						};
					});
				}
			}
			if(tmpfile.exists())
			{
				file.delete();
				tmpfile.renameTo(newfile);
				scb.apply(newfile);
				cb.apply("OK");
				return newfile;
			}
		}
		catch(IOException e)
		{
			cb.apply("failed");
		}
		return null;
	}
	
	public File zip2SevenZip(File file, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		try
		{
			File tmpfile = Files.createTempFile(file.getParentFile().toPath(), "JRM", ".7z").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".7z");
			try(SevenZipArchive archive = new SevenZipArchive(session, tmpfile))
			{
				progress.setProgress(toHTML("extracting " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
				try (FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + file.toURI()), new HashMap<>())) //$NON-NLS-1$
				{
					Path basedir = fs.getPath("/");
					Files.walkFileTree(basedir, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
						{
							archive.add_stdin(Files.newInputStream(file), basedir.relativize(file).toString());
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
						{
							return FileVisitResult.CONTINUE;
						};
					});
				}
				progress.setProgress(toHTML("Crunching " + toItalic(StringEscapeUtils.escapeHtml4(newfile.getName()))), cnt.get(), total);
			}
			catch (Exception e)
			{
				cb.apply("7z creation failed");
				return null;
			}
			if(tmpfile.exists())
			{
				file.delete();
				tmpfile.renameTo(newfile);
				scb.apply(newfile);
				cb.apply("OK");
				return newfile;
			}
		}
		catch(IOException e)
		{
			cb.apply("failed");
		}
		return null;
	}

	public File zip2TZip(File file, boolean force, UpdResultCallBack cb)
	{
		try
		{
			progress.setProgress(toHTML("Crunching " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
			cb.apply("Crunching to "+file.getName());
			final EnumSet<TrrntZipStatus> status = new TorrentZip(new ProgressTZipCallBack(progress), new SimpleTorrentZipOptions(force,false)).Process(file);
			if(status.contains(TrrntZipStatus.ValidTrrntzip))
			{
				cb.apply("OK");
				return file;
			}
			cb.apply(status.toString());
		}
		catch(IOException e)
		{
			cb.apply("failed");
		}
		return null;
	}
}
