package jrm.batch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import jrm.aui.basic.ResultColUpdater;
import jrm.aui.basic.SrcDstResult;
import jrm.aui.progress.ProgressHandler;
import jrm.batch.TrntChkReport.Child;
import jrm.batch.TrntChkReport.Status;
import jrm.io.torrent.Torrent;
import jrm.io.torrent.TorrentException;
import jrm.io.torrent.TorrentFile;
import jrm.io.torrent.TorrentParser;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.misc.MultiThreading;
import jrm.misc.SettingsEnum;
import jrm.misc.UnitRenderer;
import jrm.security.PathAbstractor;
import jrm.security.Session;

public class TorrentChecker implements UnitRenderer, HTMLRenderer
{
	private static final String TORRENT_CHECKER_RESULT_COMPLETE = "TorrentChecker.ResultComplete";
	private final AtomicInteger processing = new AtomicInteger();
	private final AtomicInteger current = new AtomicInteger();
	private final Session session;

	public enum Options
	{
		REMOVEUNKNOWNFILES, REMOVEWRONGSIZEDFILES, DETECTARCHIVEDFOLDERS;
	}

	/**
	 * Check a dir versus torrent data
	 * 
	 * @param progress
	 *            the progressions handler
	 * @param sdrl
	 *            the data obtained from SDRTableModel
	 * @param mode
	 *            the check mode (see {@link TrntChkMode}
	 * @param updater
	 *            the result interface
	 * @throws IOException
	 */
	public TorrentChecker(final Session session, final ProgressHandler progress, List<SrcDstResult> sdrl, TrntChkMode mode, ResultColUpdater updater, Set<Options> options)
	{
		this.session = session;
		progress.setInfos(Math.min(Runtime.getRuntime().availableProcessors(), (int) sdrl.stream().filter(sdr -> sdr.selected).count()), true);
		progress.setProgress2("", 0, 1); //$NON-NLS-1$
		sdrl.stream().filter(sdr -> sdr.selected).forEach(sdr -> updater.updateResult(sdrl.indexOf(sdr), ""));
		final var use_parallelism = session.getUser().getSettings().getProperty(SettingsEnum.use_parallelism, true);
		final var nThreads = use_parallelism ? session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;
		new MultiThreading<SrcDstResult>(nThreads, sdr -> {
			if (progress.isCancel())
				return;
			try
			{
				final int row = sdrl.indexOf(sdr);
				updater.updateResult(row, "In progress...");
				final String result = check(progress, mode, sdr, options);
				updater.updateResult(row, result);
				progress.setProgress(null, -1, null, "");
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(), e);
			}
		}).start(sdrl.stream().filter(sdr -> sdr.selected));
	}

	/**
	 * @param progress
	 * @param mode
	 * @param sdr
	 * @return
	 * @throws IOException
	 * @throws TorrentException
	 */
	private String check(final ProgressHandler progress, final TrntChkMode mode, final SrcDstResult sdr, Set<Options> options) throws IOException, TorrentException
	{
		var result = ""; //$NON-NLS-1$
		if (sdr.src != null && sdr.dst != null)
		{
			final var src = PathAbstractor.getAbsolutePath(session, sdr.src).toFile();
			final var dst = PathAbstractor.getAbsolutePath(session, sdr.dst).toFile();
			if (src.exists() && dst.exists())
			{
				final var report = new TrntChkReport(src);

				final var torrent = TorrentParser.parseTorrent(src.getAbsolutePath());
				final List<TorrentFile> tfiles = torrent.getFileList();

				detectArchives(sdr, tfiles, options.contains(Options.DETECTARCHIVEDFOLDERS));

				if (mode != TrntChkMode.SHA1)
				{
					result = checkFiles(progress, mode, sdr, options, src, dst, report, tfiles);
				}
				else
				{
					result = checkBlocks(progress, sdr, options, src, dst, report, torrent, tfiles);
				}
				report.save(report.getReportFile(session));
			}
			else
				result = src.exists() ? session.msgs.getString("TorrentChecker.DstMustExist") : session.msgs.getString("TorrentChecker.SrcMustExist"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
			result = sdr.src == null ? session.msgs.getString("TorrentChecker.SrcNotDefined") : session.msgs.getString("TorrentChecker.DstNotDefined"); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

	/**
	 * @param progress
	 * @param mode
	 * @param sdr
	 * @param options
	 * @param src
	 * @param dst
	 * @param report
	 * @param tfiles
	 * @return
	 * @throws IOException
	 */
	private String checkFiles(final ProgressHandler progress, final TrntChkMode mode, final SrcDstResult sdr, Set<Options> options, final File src, final File dst, final TrntChkReport report, final List<TorrentFile> tfiles) throws IOException
	{
		String result;
		var ok = 0;
		var missingBytes = 0L;
		var missingFiles = 0;
		var wrongSizedFiles = 0;
		final var paths = new HashSet<Path>();
		final int total = tfiles.size();

		processing.addAndGet(total);
		for (var j = 0; j < total; j++)
		{
			current.incrementAndGet();
			TorrentFile tfile = tfiles.get(j);
			var file = dst.toPath();
			for (String path : tfile.getFileDirs())
				file = file.resolve(path);
			paths.add(file.toAbsolutePath());
			final var identity = Paths.get(".");
			final Child node = report.add(tfile.getFileDirs().stream().map(Paths::get).reduce(identity, (r, e) -> r.resolve(e)).toString());
			progress.setProgress(toHTML(toPurple(src.getAbsolutePath())), -1, null, file.toString());
			progress.setProgress2(current + "/" + processing, current.get(), processing.get()); //$NON-NLS-1$
			if (Files.exists(file))
			{
				if (mode == TrntChkMode.FILENAME || Files.size(file) == (node.data.length = tfile.getFileLength()))
				{
					ok++;
					node.setStatus(Status.OK);
				}
				else
				{
					if (options.contains(Options.REMOVEWRONGSIZEDFILES))
						Files.delete(file);
					wrongSizedFiles++;
					missingBytes += (node.data.length = tfile.getFileLength());
					node.setStatus(Status.SIZE);
				}
			}
			else
			{
				if (mode == TrntChkMode.FILENAME)
					missingFiles++;
				else
					missingBytes += (node.data.length = tfile.getFileLength());
				node.setStatus(Status.MISSING);
			}
			if (progress.isCancel())
				break;
		}
		int removedFiles = removeUnknownFiles(report, paths, sdr, options.contains(Options.REMOVEUNKNOWNFILES) && !progress.isCancel());
		if (ok == total)
		{
			if (removedFiles > 0)
				result = toHTML(toBold(toBlue(session.msgs.getString(TORRENT_CHECKER_RESULT_COMPLETE))));
			else
				result = toHTML(toBold(toGreen(session.msgs.getString(TORRENT_CHECKER_RESULT_COMPLETE))));
		}
		else if (mode == TrntChkMode.FILENAME)
			result = String.format(session.msgs.getString("TorrentChecker.ResultFileName"), ok * 100.0 / total, missingFiles, removedFiles); //$NON-NLS-1$
		else
			result = String.format(session.msgs.getString("TorrentChecker.ResultFileSize"), ok * 100.0 / total, humanReadableByteCount(missingBytes, false), wrongSizedFiles, removedFiles); //$NON-NLS-1$
		return result;
	}

	/**
	 * @param progress
	 * @param sdr
	 * @param options
	 * @param src
	 * @param dst
	 * @param report
	 * @param torrent
	 * @param tfiles
	 * @return
	 */
	private String checkBlocks(final ProgressHandler progress, final SrcDstResult sdr, Set<Options> options, final File src, final File dst, final TrntChkReport report, final Torrent torrent, final List<TorrentFile> tfiles)
	{
		String result;
		try
		{
			var missingBytes = 0L;
			var wrongSizedFiles = new AtomicInteger();
			final var paths = new HashSet<Path>();

			final long piece_length = torrent.getPieceLength();
			final List<String> pieces = torrent.getPieces();
			long toGo = piece_length;
			var pieceCnt = 0;
			var pieceValid = 0;
			processing.addAndGet(pieces.size());
			progress.setProgress(src.getAbsolutePath(), -1, null, ""); //$NON-NLS-1$
			progress.setProgress2(String.format(session.msgs.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), -1, processing.get()); //$NON-NLS-1$
			pieceCnt++;
			Child block = report.add(String.format("Piece %d", pieceCnt));
			block.data.length = piece_length;
			Child node = null;
			var valid = new AtomicBoolean(true);
			final var md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
			final var buffer = new byte[8192];
			for (TorrentFile tfile : tfiles)
			{
				var file = dst.toPath();
				for (String path : tfile.getFileDirs())
					file = file.resolve(path);
				paths.add(file.toAbsolutePath());
				final var identity = Paths.get(".");
				node = block.add(tfile.getFileDirs().stream().map(Paths::get).reduce(identity, (r, e) -> r.resolve(e)).toString());
				try (BufferedInputStream in = getFileStram(options, wrongSizedFiles, node, valid, tfile, file))
				{
					progress.setProgress(toHTML(toPurple(src.getAbsolutePath())), -1, null, file.toString());
					long flen = (node.data.length = tfile.getFileLength());
					while (flen >= toGo)
					{
						if (in != null)
						{
							long toRead = toGo;
							do
							{
								int len = in.read(buffer, 0, (int) (toRead < buffer.length ? toRead : buffer.length));
								md.update(buffer, 0, len);
								toRead -= len;
							}
							while (toRead > 0);
						}
						flen -= toGo;
						toGo = (int) piece_length;
						progress.setProgress2(String.format(session.msgs.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
						if (valid.get())
						{
							if (Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(pieceCnt - 1)))
							{
								pieceValid++;
								block.setStatus(Status.OK);
							}
							else
								block.setStatus(Status.SHA1);
						}
						else
						{
							missingBytes += piece_length;
							block.setStatus(Status.SKIPPED);
						}
						md.reset();
						pieceCnt++;
						block = report.add(String.format("Piece %d", pieceCnt));
						block.data.length = piece_length;
						node = block.add(node);
						current.incrementAndGet();
						valid.set(true);
						if (flen > 0)
						{
							if (!Files.exists(file))
							{
								valid.set(false);
								node.setStatus(Status.MISSING);
							}
							else if (Files.size(file) != tfile.getFileLength())
							{
								valid.set(false);
								node.setStatus(Status.SIZE);
							}
						}
					}
					if (in != null)
					{
						long toRead = flen;
						do
						{
							int len = in.read(buffer, 0, (int) (toRead < buffer.length ? toRead : buffer.length));
							md.update(buffer, 0, len);
							toRead -= len;
						}
						while (toRead > 0);
					}
					toGo -= flen;
				}
				if (progress.isCancel())
					break;
			}
			progress.setProgress2(String.format(session.msgs.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
			if (valid.get())
			{
				if (Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(pieceCnt - 1)))
				{
					pieceValid++;
					block.setStatus(Status.OK);
				}
				else
					block.setStatus(Status.SHA1);
			}
			else
			{
				missingBytes += piece_length - toGo;
				block.setStatus(Status.SKIPPED);
			}
			block.data.length = (piece_length - toGo);
			System.out.format("piece counted %d, given %d, valid %d, completion=%.02f%%%n", pieceCnt, pieces.size(), pieceValid, pieceValid * 100.0 / pieceCnt); //$NON-NLS-1$
			System.out.format("piece len : %d%n", piece_length); //$NON-NLS-1$
			System.out.format("last piece len : %d%n", piece_length - toGo); //$NON-NLS-1$
			int removedFiles = removeUnknownFiles(report, paths, sdr, options.contains(Options.REMOVEUNKNOWNFILES) && !progress.isCancel());
			if (pieceValid == pieceCnt)
			{
				if (removedFiles > 0)
					result = toHTML(toBold(toBlue(session.msgs.getString(TORRENT_CHECKER_RESULT_COMPLETE))));
				else
					result = toHTML(toBold(toGreen(session.msgs.getString(TORRENT_CHECKER_RESULT_COMPLETE))));
			}
			else
				result = String.format(session.msgs.getString("TorrentChecker.ResultSHA1"), pieceValid * 100.0 / pieceCnt, humanReadableByteCount(missingBytes, false), wrongSizedFiles.get(), removedFiles); //$NON-NLS-1$
		}
		catch (Exception ex)
		{
			result = ex.getMessage();
		}
		return result;
	}

	/**
	 * @param options
	 * @param wrongSizedFiles
	 * @param node
	 * @param valid
	 * @param tfile
	 * @param in
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private BufferedInputStream getFileStram(Set<Options> options, AtomicInteger wrongSizedFiles, Child node, AtomicBoolean valid, TorrentFile tfile, Path file) throws IOException
	{
		if (!Files.exists(file))
		{
			valid.set(false);
			node.setStatus(Status.MISSING);
		}
		else if (Files.size(file) != (node.data.length = tfile.getFileLength()))
		{
			if (options.contains(Options.REMOVEWRONGSIZEDFILES))
				Files.delete(file);
			wrongSizedFiles.incrementAndGet();
			node.setStatus(Status.SIZE);
			valid.set(false);
		}
		else
			return new BufferedInputStream(new FileInputStream(file.toFile()));
		return null;
	}

	private int removeUnknownFiles(final TrntChkReport report, final HashSet<Path> paths, final SrcDstResult sdr, final boolean remove) throws IOException
	{
		final var filesToRemove = new ArrayList<Path>();
		final var dst = PathAbstractor.getAbsolutePath(session, sdr.dst);
		Files.walkFileTree(dst, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException
			{
				if (!paths.contains(file.toAbsolutePath()))
					filesToRemove.add(file);
				return super.visitFile(file, attrs);
			}
		});
		final int count = filesToRemove.size();
		if (count > 0)
		{
			final Child lostfound = report.add("Unknown files");
			lostfound.data.length = 0L;
			for (final Path p : filesToRemove)
			{
				final Child entry = lostfound.add(Paths.get(".").resolve(dst.relativize(p)).toString());
				lostfound.data.length += (entry.data.length = Files.size(p));
			}
			if (remove)
			{
				filesToRemove.forEach(t -> {
					try
					{
						Files.delete(t);
					}
					catch (IOException e)
					{
						// ignore
					}
				});
			}
		}
		return count;
	}

	private void detectArchives(final SrcDstResult sdr, final List<TorrentFile> tfiles, final boolean unarchive)
	{
		final var components = new HashSet<String>();
		final var archives = new HashSet<Path>();
		final var dst = PathAbstractor.getAbsolutePath(session, sdr.dst);
		for (var j = 0; j < tfiles.size(); j++)
		{
			final TorrentFile tfile = tfiles.get(j);
			final List<String> filedirs = tfile.getFileDirs();
			if (tfile.getFileDirs().size() > 1)
			{
				final String path = filedirs.get(0);
				if (!components.contains(path))
				{
					components.add(path);

					Path file = dst;
					file = file.resolve(path);

					final Path parent = file.getParent();
					if (parent != null)
					{
						final Path filename = file.getFileName();
						if (filename != null)
						{
							final Path archive = parent.resolve(filename.toString() + ".zip");
							if (Files.exists(archive))
							{
								archives.add(archive);
							}
						}
					}
				}
			}
		}
		for (var j = 0; j < tfiles.size(); j++)
		{
			TorrentFile tfile = tfiles.get(j);
			Path file = dst;
			for (final String path : tfile.getFileDirs())
				file = file.resolve(path);
			if (archives.contains(file))
				archives.remove(file);
		}
		for (Path archive : archives)
		{
			if (unarchive)
			{
				try
				{
					Path parent = archive.getParent();
					if (parent != null)
					{
						Path filename = archive.getFileName();
						if (filename != null)
						{
							unzip(archive, parent.resolve(FilenameUtils.getBaseName(filename.toString())));
							// Files.delete(archive);
						}
					}
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(), e);
				}
			}
			else
				Log.debug(archive);
		}
	}

	private void unzip(final Path zipFile, final Path destDir) throws IOException
	{
		if (Files.notExists(destDir))
		{
			Files.createDirectories(destDir);
		}

		try (final var zipFileSystem = FileSystems.newFileSystem(zipFile, (ClassLoader) null))
		{
			Log.debug(() -> "unzipping : " + zipFile);
			final Path root = zipFileSystem.getRootDirectories().iterator().next();

			Files.walkFileTree(root, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					final var destFile = Paths.get(destDir.toString(), file.toString());
					try
					{
						Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
					}
					catch (DirectoryNotEmptyException ignore)
					{
						// ignore
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
				{
					final var dirToCreate = Paths.get(destDir.toString(), dir.toString());
					if (Files.notExists(dirToCreate))
					{
						Files.createDirectory(dirToCreate);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}
}
