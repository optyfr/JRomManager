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
import java.security.NoSuchAlgorithmException;
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
	private static final String TORRENT_CHECKER_PIECE_PROGRESSION = "TorrentChecker.PieceProgression";
	private static final String TORRENT_CHECKER_RESULT_COMPLETE = "TorrentChecker.ResultComplete";
	private final AtomicInteger processing = new AtomicInteger();
	private final AtomicInteger current = new AtomicInteger();
	private final Session session;
	private final Set<Options> options;
	private final TrntChkMode mode;

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
		this.options = options;
		this.mode = mode;
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
				final String result = check(progress, sdr);
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
	private String check(final ProgressHandler progress, final SrcDstResult sdr) throws IOException, TorrentException
	{
		if (sdr.src == null || sdr.dst == null)
			return sdr.src == null ? session.getMsgs().getString("TorrentChecker.SrcNotDefined") : session.getMsgs().getString("TorrentChecker.DstNotDefined"); //$NON-NLS-1$ //$NON-NLS-2$
		var result = ""; //$NON-NLS-1$
		final var src = PathAbstractor.getAbsolutePath(session, sdr.src).toFile();
		final var dst = PathAbstractor.getAbsolutePath(session, sdr.dst).toFile();
		if (!src.exists() || !dst.exists())
			return src.exists() ? session.getMsgs().getString("TorrentChecker.DstMustExist") : session.getMsgs().getString("TorrentChecker.SrcMustExist"); //$NON-NLS-1$ //$NON-NLS-2$

		final var report = new TrntChkReport(src);
		final var torrent = TorrentParser.parseTorrent(src.getAbsolutePath());
		final List<TorrentFile> tfiles = torrent.getFileList();
		detectArchives(sdr, tfiles, options.contains(Options.DETECTARCHIVEDFOLDERS));
		if (mode != TrntChkMode.SHA1)
			result = checkFiles(progress, sdr, src, dst, report, tfiles);
		else
			result = checkBlocks(progress, sdr, src, dst, report, torrent, tfiles);
		report.save(report.getReportFile(session));
		return result;
	}

	private class CheckFilesData
	{
		int ok = 0;
		long missingBytes = 0L;
		int missingFiles = 0;
		int wrongSizedFiles = 0;
		final Set<Path> paths = new HashSet<>();
		final int total;

		public CheckFilesData(final List<TorrentFile> tfiles)
		{
			total = tfiles.size();
		}
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
	private String checkFiles(final ProgressHandler progress, final SrcDstResult sdr, final File src, final File dst, final TrntChkReport report, final List<TorrentFile> tfiles) throws IOException
	{
		String result;
		CheckFilesData data = new CheckFilesData(tfiles);

		processing.addAndGet(data.total);
		for (var j = 0; j < data.total; j++)
		{
			TorrentFile tfile = tfiles.get(j);
			checkFilesFile(data, src, dst, tfile, report, progress);
			if (progress.isCancel())
				break;
		}
		int removedFiles = removeUnknownFiles(report, data.paths, sdr, options.contains(Options.REMOVEUNKNOWNFILES) && !progress.isCancel());
		if (data.ok == data.total)
		{
			if (removedFiles > 0)
				result = toHTML(toBold(toBlue(session.getMsgs().getString(TORRENT_CHECKER_RESULT_COMPLETE))));
			else
				result = toHTML(toBold(toGreen(session.getMsgs().getString(TORRENT_CHECKER_RESULT_COMPLETE))));
		}
		else if (mode == TrntChkMode.FILENAME)
			result = String.format(session.getMsgs().getString("TorrentChecker.ResultFileName"), data.ok * 100.0 / data.total, data.missingFiles, removedFiles); //$NON-NLS-1$
		else
			result = String.format(session.getMsgs().getString("TorrentChecker.ResultFileSize"), data.ok * 100.0 / data.total, humanReadableByteCount(data.missingBytes, false), data.wrongSizedFiles, removedFiles); //$NON-NLS-1$
		return result;
	}

	/**
	 * @param data
	 * @param src
	 * @param dst
	 * @param tfile
	 * @param report
	 * @param progress
	 * @throws IOException
	 */
	private void checkFilesFile(CheckFilesData data, final File src, final File dst, TorrentFile tfile, final TrntChkReport report, final ProgressHandler progress) throws IOException
	{
		current.incrementAndGet();
		var file = dst.toPath();
		for (String path : tfile.getFileDirs())
			file = file.resolve(path);
		data.paths.add(file.toAbsolutePath());
		final var identity = Paths.get(".");
		final Child node = report.add(tfile.getFileDirs().stream().map(Paths::get).reduce(identity, (r, e) -> r.resolve(e)).toString());
		progress.setProgress(toHTML(toPurple(src.getAbsolutePath())), -1, null, file.toString());
		progress.setProgress2(current + "/" + processing, current.get(), processing.get()); //$NON-NLS-1$
		if (Files.exists(file))
		{
			if (mode == TrntChkMode.FILENAME || Files.size(file) == (node.getData().setLength(tfile.getFileLength()).getLength()))
			{
				data.ok++;
				node.setStatus(Status.OK);
			}
			else
			{
				if (options.contains(Options.REMOVEWRONGSIZEDFILES))
					Files.delete(file);
				data.wrongSizedFiles++;
				data.missingBytes += (node.getData().setLength(tfile.getFileLength()).getLength());
				node.setStatus(Status.SIZE);
			}
		}
		else
		{
			if (mode == TrntChkMode.FILENAME)
				data.missingFiles++;
			else
				data.missingBytes += (node.getData().setLength(tfile.getFileLength()).getLength());
			node.setStatus(Status.MISSING);
		}
	}

	private class CheckBlocksData
	{
		Child block;
		Child node = null;
		long missingBytes = 0L;
		long toGo;
		int pieceCnt = 0;
		int pieceValid = 0;
		final byte[] buffer = new byte[8192];
		final MessageDigest md;
		final AtomicBoolean valid = new AtomicBoolean(true);
		final AtomicInteger wrongSizedFiles = new AtomicInteger();
		final Set<Path> paths = new HashSet<>();
		final long pieceLength;
		final List<String> pieces;
		
		public CheckBlocksData(Torrent torrent) throws NoSuchAlgorithmException
		{
			md = MessageDigest.getInstance("SHA-1");	//NOSONAR
			pieceLength = torrent.getPieceLength();
			pieces = torrent.getPieces();
		}
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
	private String checkBlocks(final ProgressHandler progress, final SrcDstResult sdr, final File src, final File dst, final TrntChkReport report, final Torrent torrent, final List<TorrentFile> tfiles)
	{
		String result;
		try
		{
			final var data = new CheckBlocksData(torrent);
			

			data.toGo = data.pieceLength;
			processing.addAndGet(data.pieces.size());
			progress.setProgress(src.getAbsolutePath(), -1, null, ""); //$NON-NLS-1$
			progress.setProgress2(String.format(session.getMsgs().getString(TORRENT_CHECKER_PIECE_PROGRESSION), current.get(), processing.get()), -1, processing.get()); //$NON-NLS-1$
			data.pieceCnt++;
			data.block = report.add(String.format("Piece %d", data.pieceCnt));
			data.block.getData().setLength(data.pieceLength);
			for (TorrentFile tfile : tfiles)
			{
				checkBlocksFile(data, src, dst, tfile, report, progress);
				if (progress.isCancel())
					break;
			}
			progress.setProgress2(String.format(session.getMsgs().getString(TORRENT_CHECKER_PIECE_PROGRESSION), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
			if (data.valid.get())
			{
				if (Hex.encodeHexString(data.md.digest()).equalsIgnoreCase(data.pieces.get(data.pieceCnt - 1)))
				{
					data.pieceValid++;
					data.block.setStatus(Status.OK);
				}
				else
					data.block.setStatus(Status.SHA1);
			}
			else
			{
				data.missingBytes += data.pieceLength - data.toGo;
				data.block.setStatus(Status.SKIPPED);
			}
			data.block.getData().setLength(data.pieceLength - data.toGo);
			Log.info(String.format("piece counted %d, given %d, valid %d, completion=%.02f%%%n", data.pieceCnt, data.pieces.size(), data.pieceValid, data.pieceValid * 100.0 / data.pieceCnt)); //$NON-NLS-1$
			Log.info(String.format("piece len : %d%n", data.pieceLength)); //$NON-NLS-1$
			Log.info(String.format("last piece len : %d%n", data.pieceLength - data.toGo)); //$NON-NLS-1$
			int removedFiles = removeUnknownFiles(report, data.paths, sdr, options.contains(Options.REMOVEUNKNOWNFILES) && !progress.isCancel());
			if (data.pieceValid == data.pieceCnt)
			{
				if (removedFiles > 0)
					result = toHTML(toBold(toBlue(session.getMsgs().getString(TORRENT_CHECKER_RESULT_COMPLETE))));
				else
					result = toHTML(toBold(toGreen(session.getMsgs().getString(TORRENT_CHECKER_RESULT_COMPLETE))));
			}
			else
				result = String.format(session.getMsgs().getString("TorrentChecker.ResultSHA1"), data.pieceValid * 100.0 / data.pieceCnt, humanReadableByteCount(data.missingBytes, false), data.wrongSizedFiles.get(), removedFiles); //$NON-NLS-1$
		}
		catch (Exception ex)
		{
			result = ex.getMessage();
		}
		return result;
	}

	/**
	 * @param data
	 * @param src
	 * @param dst
	 * @param tfile
	 * @param report
	 * @param progress
	 * @throws IOException
	 */
	private void checkBlocksFile(final CheckBlocksData data, final File src, final File dst, TorrentFile tfile, final TrntChkReport report, final ProgressHandler progress) throws IOException
	{
		var file = dst.toPath();
		for (String path : tfile.getFileDirs())
			file = file.resolve(path);
		data.paths.add(file.toAbsolutePath());
		final var identity = Paths.get(".");
		data.node = data.block.add(tfile.getFileDirs().stream().map(Paths::get).reduce(identity, (r, e) -> r.resolve(e)).toString());
		try (BufferedInputStream in = getFileStram(options, data.wrongSizedFiles, data.node, data.valid, tfile, file))
		{
			progress.setProgress(toHTML(toPurple(src.getAbsolutePath())), -1, null, file.toString());
			long flen = (data.node.getData().setLength(tfile.getFileLength()).getLength());
			while (flen >= data.toGo)
			{
				hashStream(data.md, data.buffer, in, data.toGo);
				flen -= data.toGo;
				data.toGo = data.pieceLength;
				progress.setProgress2(String.format(session.getMsgs().getString(TORRENT_CHECKER_PIECE_PROGRESSION), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
				if (data.valid.get())
				{
					if (Hex.encodeHexString(data.md.digest()).equalsIgnoreCase(data.pieces.get(data.pieceCnt - 1)))
					{
						data.pieceValid++;
						data.block.setStatus(Status.OK);
					}
					else
						data.block.setStatus(Status.SHA1);
				}
				else
				{
					data.missingBytes += data.pieceLength;
					data.block.setStatus(Status.SKIPPED);
				}
				data.md.reset();
				data.pieceCnt++;
				data.block = report.add(String.format("Piece %d", data.pieceCnt));
				data.block.getData().setLength(data.pieceLength);
				data.node = data.block.add(data.node);
				current.incrementAndGet();
				data.valid.set(true);
				if (flen > 0)
				{
					if (!Files.exists(file))
					{
						data.valid.set(false);
						data.node.setStatus(Status.MISSING);
					}
					else if (Files.size(file) != tfile.getFileLength())
					{
						data.valid.set(false);
						data.node.setStatus(Status.SIZE);
					}
				}
			}
			hashStream(data.md, data.buffer, in, flen);
			data.toGo -= flen;
		}
	}

	/**
	 * @param md
	 * @param buffer
	 * @param in
	 * @param toRead
	 * @throws IOException
	 */
	private void hashStream(final MessageDigest md, final byte[] buffer, BufferedInputStream in, long toRead) throws IOException
	{
		if (in != null)
		{
			do
			{
				int len = in.read(buffer, 0, (int) (toRead < buffer.length ? toRead : buffer.length));
				md.update(buffer, 0, len);
				toRead -= len;
			}
			while (toRead > 0);
		}
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
		else if (Files.size(file) != (node.getData().setLength(tfile.getFileLength())).getLength())
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

	private int removeUnknownFiles(final TrntChkReport report, final Set<Path> paths, final SrcDstResult sdr, final boolean remove) throws IOException
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
			lostfound.getData().setLength(0L);
			for (final Path p : filesToRemove)
			{
				final Child entry = lostfound.add(Paths.get(".").resolve(dst.relativize(p)).toString());
				lostfound.getData().setLength(lostfound.getData().getLength() + (entry.getData().setLength(Files.size(p)).getLength()));
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
			if (filedirs.size() > 1)
			{
				final String path = filedirs.get(0);
				if (!components.contains(path))
				{
					components.add(path);

					Path file = dst;
					file = file.resolve(path);

					isArchive(archives, file);
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
				unarchive(archive);
			}
			else
				Log.debug(archive);
		}
	}

	/**
	 * @param archives
	 * @param file
	 */
	private void isArchive(final HashSet<Path> archives, Path file)
	{
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

	/**
	 * @param archive
	 */
	private void unarchive(Path archive)
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
				}
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
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
