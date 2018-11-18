package jrm.batch;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileSystem;
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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import jrm.batch.TrntChkReport.Node;
import jrm.batch.TrntChkReport.Status;
import jrm.io.torrent.Torrent;
import jrm.io.torrent.TorrentFile;
import jrm.io.torrent.TorrentParser;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.HTMLRenderer;
import jrm.misc.UnitRenderer;
import jrm.security.Session;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.basic.SrcDstResult;
import jrm.ui.progress.ProgressHandler;
import one.util.streamex.StreamEx;

public class TorrentChecker implements UnitRenderer,HTMLRenderer
{
	AtomicInteger processing = new AtomicInteger();
	AtomicInteger current = new AtomicInteger();

	/**
	 * Check a dir versus torrent data 
	 * @param progress the progressions handler
	 * @param sdrl the data obtained from {@link SDRTableModel}
	 * @param mode the check mode (see {@link TrntChkMode}
	 * @param updater the result interface
	 * @throws IOException
	 */
	public TorrentChecker(final Session session, final ProgressHandler progress, List<SrcDstResult> sdrl, TrntChkMode mode, ResultColUpdater updater, boolean removeUnknownFiles, boolean removeWrongSizedFiles, boolean detectArchivedFolders) throws IOException
	{
		progress.setInfos(Math.min(Runtime.getRuntime().availableProcessors(),(int)sdrl.stream().filter(sdr->sdr.selected).count()), true);
		progress.setProgress2("", 0, 1); //$NON-NLS-1$
		StreamEx.of(sdrl).filter(sdr->sdr.selected).forEach(sdr->{
			updater.updateResult(sdrl.indexOf(sdr), "");
		});
		StreamEx.of(sdrl).filter(sdr->sdr.selected).parallel().takeWhile(sdr->!progress.isCancel()).forEach(sdr->{
			try
			{
				int row = sdrl.indexOf(sdr);
				updater.updateResult(row, "In progress...");
				final String result = check(session, progress, mode, sdr, removeUnknownFiles, removeWrongSizedFiles, detectArchivedFolders);
				updater.updateResult(row, result);
				progress.setProgress(null, -1, null, "");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		});
	}

	/**
	 * @param progress
	 * @param mode
	 * @param sdr
	 * @return
	 * @throws IOException
	 */
	private String check(final Session session, final ProgressHandler progress, TrntChkMode mode, SrcDstResult sdr, boolean removeUnknownFiles, boolean removeWrongSizedFiles, boolean detectArchivedFolders) throws IOException
	{
		String result = ""; //$NON-NLS-1$
		if (sdr.src != null && sdr.dst != null)
		{
			if (sdr.src.exists() && sdr.dst.exists())
			{
				TrntChkReport report = new TrntChkReport(sdr.src);
				
				Torrent torrent = TorrentParser.parseTorrent(sdr.src.getAbsolutePath());
				List<TorrentFile> tfiles = torrent.getFileList();
				int total = tfiles.size(), ok = 0;
				long missing_bytes = 0;
				int missing_files = 0;
				int wrong_sized_files = 0;
				HashSet<Path> paths = new HashSet<>();
				
				detectArchives(sdr, tfiles, detectArchivedFolders);
				
				if (mode != TrntChkMode.SHA1)
				{
					processing.addAndGet(total);
					for (int j = 0; j < total; j++)
					{
						current.incrementAndGet();
						TorrentFile tfile = tfiles.get(j);
						Path file = sdr.dst.toPath();
						for (String path : tfile.getFileDirs())
							file = file.resolve(path);
						paths.add(file.toAbsolutePath());
						final Path identity = Paths.get(".");
						final Node node = report.add(tfile.getFileDirs().stream().map(Paths::get).reduce(identity, (r,e)->r.resolve(e)).toString());
						progress.setProgress(toHTML(toPurple(sdr.src.getAbsolutePath())), -1, null, file.toString());
						progress.setProgress2(current + "/" + processing, current.get(), processing.get()); //$NON-NLS-1$
						if (Files.exists(file))
						{
							if(mode == TrntChkMode.FILENAME || Files.size(file) == (node.data.length=tfile.getFileLength()))
							{
								ok++;
								node.setStatus(Status.OK);
							}
							else
							{
								if(removeWrongSizedFiles)
									Files.delete(file);
								wrong_sized_files++;
								missing_bytes += (node.data.length=tfile.getFileLength());
								node.setStatus(Status.SIZE);
							}
						}
						else
						{
							if(mode == TrntChkMode.FILENAME)
								missing_files++;
							else
								missing_bytes += (node.data.length=tfile.getFileLength());
							node.setStatus(Status.MISSING);
						}
						if(progress.isCancel())
							break;
					}
					int removed_files = removeUnknownFiles(report, paths, sdr, removeUnknownFiles && !progress.isCancel());
					if(ok == total)
					{
						if(removed_files>0)
							result = toHTML(toBold(toBlue(Messages.getString("TorrentChecker.ResultComplete"))));
						else
							result = toHTML(toBold(toGreen(Messages.getString("TorrentChecker.ResultComplete"))));
					}
					else if(mode == TrntChkMode.FILENAME)
						result = String.format(Messages.getString("TorrentChecker.ResultFileName"), ok * 100.0 / total, missing_files, removed_files); //$NON-NLS-1$
					else
						result = String.format(Messages.getString("TorrentChecker.ResultFileSize"), ok * 100.0 / total, humanReadableByteCount(missing_bytes, false), wrong_sized_files, removed_files); //$NON-NLS-1$
				}
				else
				{
					try
					{
						long piece_length = torrent.getPieceLength();
						List<String> pieces = torrent.getPieces();
						long to_go = piece_length;
						int piece_cnt = 0, piece_valid = 0;
						processing.addAndGet(pieces.size());
						progress.setProgress(sdr.src.getAbsolutePath(), -1, null, ""); //$NON-NLS-1$
						progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), -1, processing.get()); //$NON-NLS-1$
						piece_cnt++;
						Node block = report.add(String.format("Piece %d", piece_cnt));
						block.data.length = piece_length;
						Node node = null;
						boolean valid = true;
						MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
						byte[] buffer = new byte[8192];
						for (TorrentFile tfile : tfiles)
						{
							BufferedInputStream in = null;
							Path file = sdr.dst.toPath();
							for (String path : tfile.getFileDirs())
								file = file.resolve(path);
							paths.add(file.toAbsolutePath());
							final Path identity = Paths.get(".");
							node = block.add(tfile.getFileDirs().stream().map(Paths::get).reduce(identity, (r,e)->r.resolve(e)).toString());
							if (!Files.exists(file))
							{
								valid = false;
								node.setStatus(Status.MISSING);
							}
							else if(Files.size(file) != (node.data.length = tfile.getFileLength()))
							{
								if(removeWrongSizedFiles)
									Files.delete(file);
								wrong_sized_files++;
								node.setStatus(Status.SIZE);
								valid = false;
							}
							else
								in = new BufferedInputStream(new FileInputStream(file.toFile()));
							progress.setProgress(toHTML(toPurple(sdr.src.getAbsolutePath())), -1, null, file.toString());
							long flen = (node.data.length = tfile.getFileLength());
							while (flen >= to_go)
							{
								if (in != null)
								{
									long to_read = to_go;
									do
									{
										int len = in.read(buffer, 0, (int) (to_read < buffer.length ? to_read : buffer.length));
										md.update(buffer, 0, len);
										to_read -= len;
									}
									while (to_read > 0);
								}
								flen -= to_go;
								to_go = (int) piece_length;
								progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
								if (valid)
								{
									if(Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(piece_cnt - 1)))
									{
										piece_valid++;
										block.setStatus(Status.OK);
									}
									else
										block.setStatus(Status.SHA1);
								}
								else
								{
									missing_bytes += piece_length;
									block.setStatus(Status.SKIPPED);
								}
								md.reset();
								piece_cnt++;
								block = report.add(String.format("Piece %d", piece_cnt));
								block.data.length = piece_length;
								node = block.add(node);
								current.incrementAndGet();
								valid = true;
								if (flen > 0)
								{
									if (!Files.exists(file))
									{
										valid = false;
										node.setStatus(Status.MISSING);
									}
									else if(Files.size(file) != tfile.getFileLength())
									{
										valid = false;
										node.setStatus(Status.SIZE);
									}
								}
							}
							if (in != null)
							{
								long to_read = flen;
								do
								{
									int len = in.read(buffer, 0, (int) (to_read < buffer.length ? to_read : buffer.length));
									md.update(buffer, 0, len);
									to_read -= len;
								}
								while (to_read > 0);
								in.close();
							}
							to_go -= flen;
							if(progress.isCancel())
								break;
						}
						progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), current.get(), processing.get()), current.get(), processing.get()); //$NON-NLS-1$
						if (valid)
						{
							if(Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(piece_cnt - 1)))
							{
								piece_valid++;
								block.setStatus(Status.OK);
							}
							else
								block.setStatus(Status.SHA1);
						}
						else
						{
							missing_bytes += piece_length - to_go;
							block.setStatus(Status.SKIPPED);
						}
						block.data.length = (piece_length - to_go);
						System.out.format("piece counted %d, given %d, valid %d, completion=%.02f%%\n", piece_cnt, pieces.size(), piece_valid, piece_valid * 100.0 / piece_cnt); //$NON-NLS-1$
						System.out.format("piece len : %d\n", piece_length); //$NON-NLS-1$
						System.out.format("last piece len : %d\n", piece_length - to_go); //$NON-NLS-1$
						int removed_files = removeUnknownFiles(report, paths, sdr, removeUnknownFiles && !progress.isCancel());
						if(piece_valid == piece_cnt)
						{
							if(removed_files>0)
								result = toHTML(toBold(toBlue(Messages.getString("TorrentChecker.ResultComplete"))));
							else
								result = toHTML(toBold(toGreen(Messages.getString("TorrentChecker.ResultComplete"))));
						}
						else
							result = String.format(Messages.getString("TorrentChecker.ResultSHA1"), piece_valid * 100.0 / piece_cnt, humanReadableByteCount(missing_bytes, false), wrong_sized_files, removed_files); //$NON-NLS-1$
					}
					catch (Exception ex)
					{
						result = ex.getMessage();
					}
				}
				report.save(report.getReportFile(session));
			}
			else
				result = sdr.src.exists() ? Messages.getString("TorrentChecker.DstMustExist") : Messages.getString("TorrentChecker.SrcMustExist"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
			result = sdr.src == null ? Messages.getString("TorrentChecker.SrcNotDefined") : Messages.getString("TorrentChecker.DstNotDefined"); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}
	
	private int removeUnknownFiles(TrntChkReport report, HashSet<Path> paths, SrcDstResult sdr, boolean remove) throws IOException
	{
		List<Path> files_to_remove = new ArrayList<>();
		Files.walkFileTree(sdr.dst.toPath(), new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
			{
				if (!paths.contains(file.toAbsolutePath()))
					files_to_remove.add(file);
				return super.visitFile(file, attrs);
			}
		});
		int count = files_to_remove.size();
		if (count > 0)
		{
			Node lostfound = report.add("Unknown files");
			lostfound.data.length = 0L;
			for (Path p : files_to_remove)
			{
				Node entry = lostfound.add(p.toString());
				lostfound.data.length += (entry.data.length = Files.size(p));
			}
			if (remove)
			{
				files_to_remove.forEach(t -> {
					try
					{
						Files.delete(t);
					}
					catch (IOException e)
					{
					}
				});
			}
		}
		return count;
	}

	private void detectArchives(SrcDstResult sdr, List<TorrentFile> tfiles, boolean unarchive)
	{
		HashSet<String> components = new HashSet<>();
		HashSet<Path> archives = new HashSet<>();
		for (int j = 0; j < tfiles.size(); j++)
		{
			TorrentFile tfile = tfiles.get(j);
			List<String> filedirs = tfile.getFileDirs();
			if (tfile.getFileDirs().size() > 1)
			{
				String path = filedirs.get(0);
				if(!components.contains(path))
				{
					components.add(path);
					
					Path file = sdr.dst.toPath();
					file = file.resolve(path);
					
					Path parent = file.getParent();
					if(parent!=null)
					{
						Path filename = file.getFileName();
						if(filename!=null)
						{
							Path archive = parent.resolve(filename.toString() + ".zip");
							if (Files.exists(archive))
							{
								archives.add(archive);
							}
						}
					}
				}
			}
		}
		for (int j = 0; j < tfiles.size(); j++)
		{
			TorrentFile tfile = tfiles.get(j);
			Path file = sdr.dst.toPath();
			for (String path : tfile.getFileDirs())
				file = file.resolve(path);
			if(archives.contains(file))
				archives.remove(file);
		}
		for(Path archive : archives)
		{
			if(unarchive)
			{
				try
				{
					Path parent = archive.getParent();
					if(parent!=null)
					{
						Path filename = archive.getFileName();
						if(filename!=null)
						{
							unzip(archive, parent.resolve(FilenameUtils.getBaseName(filename.toString())));
						//	Files.delete(archive);
						}
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			else
				System.out.println(archive);
		}
	}
	
	private void unzip(final Path zipFile, final Path destDir) throws IOException
	{
		if (Files.notExists(destDir))
		{
			Files.createDirectories(destDir);
		}

		try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipFile, null))
		{
			System.out.println("unzipping : "+zipFile);
			final Path root = zipFileSystem.getRootDirectories().iterator().next();

			Files.walkFileTree(root, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					final Path destFile = Paths.get(destDir.toString(), file.toString());
					try
					{
						Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
					}
					catch (DirectoryNotEmptyException ignore)
					{
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
				{
					final Path dirToCreate = Paths.get(destDir.toString(), dir.toString());
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
