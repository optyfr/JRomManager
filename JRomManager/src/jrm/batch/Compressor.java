package jrm.batch;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
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
import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.aui.progress.ProgressTZipCallBack;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.ZipArchive;
import jrm.compressors.ZipArchive.CustomVisitor;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.compressors.zipfs.ZipLevel;
import jrm.compressors.zipfs.ZipTempThreshold;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.security.Session;

public class Compressor implements HTMLRenderer
{
	Session session;
	AtomicInteger cnt;
	int total;
	ProgressHandler progress;
	
	public static final String[] extensions = new String[] { "zip", "7z", "rar", "arj", "tar", "lzh", "lha", "tgz", "tbz", "tbz2", "rpm", "iso", "deb", "cab" };
	
	public static class FileResult
	{
		public Path file;
		public String result = "";
		
		public FileResult(Path file)
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
			cb.apply("Processing "+file.getName());
			File tmpfile = Files.createTempFile("JRM", ".7z").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".7z");
			try(SevenZipArchive archive = new SevenZipArchive(session, file, true, new ProgressNarchiveCallBack(progress)))
			{
				progress.setProgress(toHTML("extracting " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
				if(archive.extract()==0)
				{
					try(SevenZipArchive newarchive = new SevenZipArchive(session, tmpfile, new ProgressNarchiveCallBack(progress)))
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
				if(FileUtils.deleteQuietly(file))
				{
					FileUtils.moveFile(tmpfile, newfile);
					scb.apply(newfile);
					cb.apply("OK");
					return newfile;
				}
				else
					cb.apply("Failed");
			}
		}
		catch(IOException e)
		{
			cb.apply("Failed");
		}
		finally
		{
//			progress.setProgress("", null, null, "");
		}
		return null;
	}

	public File sevenZip2Zip(File file, boolean tzip, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		try
		{
			cb.apply("Processing "+file.getName());
			File tmpfile = Files.createTempFile("JRM", ".zip").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".zip");
			try(SevenZipArchive archive = new SevenZipArchive(session, file, false, new ProgressNarchiveCallBack(progress)))
			{
				progress.setProgress(toHTML("extracting " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
				if(archive.extract()==0)
				{
					File basedir = archive.getTempDir();
					final Map<String, Object> env = new HashMap<>();
					env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
					env.put("useTempFile", FileUtils.sizeOf(basedir) > ZipTempThreshold.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_temp_threshold, ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
					env.put("compressionLevel", tzip ? 1 :  ZipLevel.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_compression_level, ZipLevel.DEFAULT.toString())).getLevel()); //$NON-NLS-1$ //$NON-NLS-2$
					FileUtils.forceMkdirParent(tmpfile);
					progress.setProgress(toHTML("creating " + toItalic(StringEscapeUtils.escapeHtml4(newfile.getName()))), cnt.get(), total);
					try(ZipArchive dstarchive = new ZipArchive(session, tmpfile, new ProgressNarchiveCallBack(progress)))
					{
						dstarchive.compress_custom(new CustomVisitor(basedir.toPath()) {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
							{
								Path dst = getFileSystem().getPath(basedir.toPath().relativize(file).toString());
								if(dst.getParent()!=null)
									Files.createDirectories(dst.getParent());
								Files.copy(file, dst, StandardCopyOption.REPLACE_EXISTING);
								return FileVisitResult.CONTINUE;
							}
							@Override
							public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
							{
								Path dst = getFileSystem().getPath(basedir.toPath().relativize(dir).toString());
								Files.createDirectories(dst);
								return FileVisitResult.CONTINUE;
							};
						}, env);
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
				Log.err(e.getMessage(),e);
				tmpfile.delete();
				cb.apply("zip creation failed");
				return null;
			}
			if(tmpfile.exists())
			{
				if(FileUtils.deleteQuietly(file))
				{
					FileUtils.moveFile(tmpfile, newfile);
					scb.apply(newfile);
					cb.apply("OK");
					return newfile;
				}
				else
					cb.apply("Failed");
			}
		}
		catch (IOException e)
		{
			cb.apply("Failed");
		}
		finally
		{
			progress.setProgress("", null, null, "");
		}
		return null;
	}

	public File zip2Zip(File file, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		try
		{
			cb.apply("Processing "+file.getName());
			File tmpfile = Files.createTempFile("JRM", ".zip").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".zip");
			try (FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + file.toURI()), new HashMap<>());) //$NON-NLS-1$
			{
				Path basedir = fs.getPath("/");
				final Map<String, Object> env = new HashMap<>();
				env.put("create", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				env.put("useTempFile", size(basedir) > ZipTempThreshold.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_temp_threshold, ZipTempThreshold._10MB.toString())).getThreshold()); //$NON-NLS-1$ //$NON-NLS-2$
				env.put("compressionLevel", ZipLevel.valueOf(session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.zip_compression_level, ZipLevel.DEFAULT.toString())).getLevel()); //$NON-NLS-1$
				progress.setProgress(toHTML("Crunching " + toItalic(StringEscapeUtils.escapeHtml4(newfile.getName()))), cnt.get(), total);
				try (ZipArchive newarchive = new ZipArchive(session, tmpfile, new ProgressNarchiveCallBack(progress)))
				{
					newarchive.compress_custom(new CustomVisitor(basedir) {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
						{
							Path dst = getFileSystem().getPath(basedir.relativize(file).toString());
							if(dst.getParent()!=null)
								Files.createDirectories(dst.getParent());
							Files.copy(file, dst, StandardCopyOption.REPLACE_EXISTING);
							return FileVisitResult.CONTINUE;
						}
						@Override
						public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
						{
							Path dst = getFileSystem().getPath(basedir.relativize(dir).toString());
							Files.createDirectories(dst);
							return FileVisitResult.CONTINUE;
						};
					}, env);
				}
			}
			if(tmpfile.exists())
			{
				if(FileUtils.deleteQuietly(file))
				{
					FileUtils.moveFile(tmpfile, newfile);
					scb.apply(newfile);
					cb.apply("OK");
					return newfile;
				}
				else
					cb.apply("Failed");
			}
		}
		catch(IOException e)
		{
			cb.apply("failed");
		}
		finally
		{
			progress.setProgress("", null, null, "");
		}
		return null;
	}
	
	public File zip2SevenZip(File file, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		try
		{
			cb.apply("Processing "+file.getName());
			File tmpfile = Files.createTempFile("JRM", ".7z").toFile();
			tmpfile.delete();
			File newfile = new File(file.getParentFile(),FilenameUtils.getBaseName(file.getName())+".7z");
			try(SevenZipArchive archive = new SevenZipArchive(session, tmpfile, new ProgressNarchiveCallBack(progress)))
			{
				progress.setProgress(toHTML("extracting " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
				try(ZipArchive srcarchive = new ZipArchive(session, file, true, new ProgressNarchiveCallBack(progress));)
				{
					srcarchive.extract_custom(new CustomVisitor() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException
						{
							archive.add_stdin(Files.newInputStream(file), getSourcePath().relativize(file).toString());
							return FileVisitResult.CONTINUE;
						}
					});
				}
				progress.setProgress(toHTML("Crunching " + toItalic(StringEscapeUtils.escapeHtml4(newfile.getName()))), cnt.get(), total);
			}
			catch (Exception e)
			{
				Log.err(e.getMessage(),e);
				if(tmpfile.exists())
					tmpfile.delete();
				cb.apply("7z creation failed");
				return null;
			}
			if(tmpfile.exists())
			{
				if(FileUtils.deleteQuietly(file))
				{
					FileUtils.moveFile(tmpfile, newfile);
					scb.apply(newfile);
					cb.apply("OK");
					return newfile;
				}
				else
					cb.apply("Failed to replace original file");
			}
		}
		catch(IOException e)
		{
			cb.apply("failed");
		}
		finally
		{
			progress.setProgress("", null, null, "");
		}
		return null;
	}

	public File zip2TZip(File file, boolean force, UpdResultCallBack cb)
	{
		try
		{
			progress.setProgress(toHTML("TorrentZipping " + toItalic(StringEscapeUtils.escapeHtml4(file.getName()))), cnt.get(), total);
			cb.apply("Processing "+file.getName());
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
		finally
		{
			progress.setProgress("", null, null, "");
		}
		return null;
	}
	
	public void compress(CompressorFormat format, File file, boolean force, UpdResultCallBack cb, UpdSrcCallBack scb)
	{
		switch (format)
		{
			case SEVENZIP:
			{
				switch (FilenameUtils.getExtension(file.getName()))
				{
					case "zip":
						zip2SevenZip(file, cb, scb);
						break;
					case "7z":
						if (force)
							sevenZip2SevenZip(file, cb, scb);
						else
							cb.apply("Skipped");
						break;
					default:
						sevenZip2SevenZip(file, cb, scb);
						break;
				}
				break;
			}
			case ZIP:
			{
				switch (FilenameUtils.getExtension(file.getName()))
				{
					case "zip":
						if (force)
							zip2Zip(file, cb, scb);
						else
							cb.apply("Skipped");
						break;
					default:
						sevenZip2Zip(file, false, cb, scb);
						break;
				}
				break;
			}
			case TZIP:
			{
				switch (FilenameUtils.getExtension(file.getName()))
				{
					case "zip":
						zip2TZip(file, force, cb);
						break;
					default:
						file = sevenZip2Zip(file, true, cb, scb);
						if (file != null && file.exists())
							zip2TZip(file, force, cb);
						break;
				}
				break;
			}
		}
	}
}
