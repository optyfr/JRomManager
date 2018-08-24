package jrm.batch;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

import jrm.io.torrent.Torrent;
import jrm.io.torrent.TorrentFile;
import jrm.io.torrent.TorrentParser;
import jrm.io.torrent.options.TrntChkMode;
import jrm.locale.Messages;
import jrm.misc.UnitRenderer;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.basic.SDRTableModel.SrcDstResult;
import jrm.ui.progress.Progress;

public class TorrentChecker implements UnitRenderer
{

	/**
	 * Check a dir versus torrent data 
	 * @param progress the progressions handler
	 * @param sdrl the data obtained from {@link SDRTableModel}
	 * @param mode the check mode (see {@link TrntChkMode}
	 * @param updater the result interface
	 * @throws IOException
	 */
	public TorrentChecker(final Progress progress, List<SrcDstResult> sdrl, TrntChkMode mode, ResultColUpdater updater) throws IOException
	{
		for (int i = 0; i < sdrl.size(); i++)
		{
			SrcDstResult sdr = sdrl.get(i);
			final String result = check(progress, mode, sdr);
			updater.updateResult(i, result);
		}
	}

	/**
	 * @param progress
	 * @param mode
	 * @param sdr
	 * @return
	 * @throws IOException
	 */
	private String check(final Progress progress, TrntChkMode mode, SrcDstResult sdr) throws IOException
	{
		String result = ""; //$NON-NLS-1$
		if (sdr.src != null && sdr.dst != null)
		{
			if (sdr.src.exists() && sdr.dst.exists())
			{
				Torrent torrent = TorrentParser.parseTorrent(sdr.src.getAbsolutePath());
				List<TorrentFile> tfiles = torrent.getFileList();
				int total = tfiles.size(), ok = 0;
				long missing_bytes = 0;
				int missing_files = 0;
				if (mode != TrntChkMode.SHA1)
				{
					for (int j = 0; j < total; j++)
					{
						TorrentFile tfile = tfiles.get(j);
						Path file = sdr.dst.toPath();
						for (String path : tfile.getFileDirs())
							file = file.resolve(path);
						progress.setProgress(sdr.src.getAbsolutePath(), -1, null, file.toString());
						progress.setProgress2(j + "/" + total, j, total); //$NON-NLS-1$
						if (Files.exists(file) && (mode == TrntChkMode.FILENAME || Files.size(file) == tfile.getFileLength()))
							ok++;
						else if(mode == TrntChkMode.FILENAME)
							missing_files++;
						else
							missing_bytes += tfile.getFileLength();
					}
					if(mode == TrntChkMode.FILENAME)
						result = String.format(Messages.getString("TorrentChecker.ResultFileName"), (float) ok * 100.0 / (float) total, missing_files); //$NON-NLS-1$
					else
						result = String.format(Messages.getString("TorrentChecker.ResultFileSize"), (float) ok * 100.0 / (float) total, humanReadableByteCount(missing_bytes, false)); //$NON-NLS-1$
				}
				else
				{
					try
					{
						long piece_length = torrent.getPieceLength();
						List<String> pieces = torrent.getPieces();
						long to_go = piece_length;
						int piece_cnt = 0, piece_valid = 0;
						progress.setProgress(sdr.src.getAbsolutePath(), -1, null, ""); //$NON-NLS-1$
						progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), piece_cnt, pieces.size()), piece_cnt, pieces.size()); //$NON-NLS-1$
						piece_cnt++;
						boolean valid = true;
						MessageDigest md = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
						byte[] buffer = new byte[8192];
						for (TorrentFile tfile : tfiles)
						{
							BufferedInputStream in = null;
							Path file = sdr.dst.toPath();
							for (String path : tfile.getFileDirs())
								file = file.resolve(path);
							if (!Files.exists(file) || Files.size(file) != tfile.getFileLength())
								valid = false;
							else
								in = new BufferedInputStream(new FileInputStream(file.toFile()));
							progress.setProgress(sdr.src.getAbsolutePath(), -1, null, file.toString());
							long flen = tfile.getFileLength();
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
								progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), piece_cnt, pieces.size()), piece_cnt, pieces.size()); //$NON-NLS-1$
								if (valid && Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(piece_cnt - 1)))
									piece_valid++;
								else
									missing_bytes += piece_length;
								md.reset();
								piece_cnt++;
								valid = true;
								if (flen > 0)
								{
									if (!Files.exists(file) || Files.size(file) != tfile.getFileLength())
										valid = false;
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
						}
						progress.setProgress2(String.format(Messages.getString("TorrentChecker.PieceProgression"), piece_cnt, pieces.size()), piece_cnt, pieces.size()); //$NON-NLS-1$
						if (valid && Hex.encodeHexString(md.digest()).equalsIgnoreCase(pieces.get(piece_cnt - 1)))
							piece_valid++;
						else
							missing_bytes += piece_length - to_go;
						System.out.format("piece counted %d, given %d, valid %d, completion=%.02f%%\n", piece_cnt, pieces.size(), piece_valid, (double) piece_valid * 100.0 / (double) piece_cnt); //$NON-NLS-1$
						System.out.format("piece len : %d\n", piece_length); //$NON-NLS-1$
						System.out.format("last piece len : %d\n", piece_length - to_go); //$NON-NLS-1$
						result = String.format(Messages.getString("TorrentChecker.ResultSHA1"), (double) piece_valid * 100.0 / (double) piece_cnt, humanReadableByteCount(missing_bytes, false)); //$NON-NLS-1$
					}
					catch (Exception ex)
					{
						result = ex.getMessage();
					}
				}
			}
			else
				result = sdr.src.exists() ? Messages.getString("TorrentChecker.DstMustExist") : Messages.getString("TorrentChecker.SrcMustExist"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else
			result = sdr.src == null ? Messages.getString("TorrentChecker.SrcNotDefined") : Messages.getString("TorrentChecker.DstNotDefined"); //$NON-NLS-1$ //$NON-NLS-2$
		return result;
	}

}
