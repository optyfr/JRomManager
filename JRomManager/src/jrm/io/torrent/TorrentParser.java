/*
 * Copyright (C) 2015 Christophe De Troyer Copyright (C) 2018 Optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.io.torrent;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jrm.io.torrent.bencoding.Reader;
import jrm.io.torrent.bencoding.Utils;
import jrm.io.torrent.bencoding.types.BByteString;
import jrm.io.torrent.bencoding.types.BDictionary;
import jrm.io.torrent.bencoding.types.BInt;
import jrm.io.torrent.bencoding.types.BList;
import jrm.io.torrent.bencoding.types.IBencodable;
import jrm.misc.Log;
import lombok.experimental.UtilityClass;

/**
 * Created by christophe on 17.01.15.
 */
public @UtilityClass class TorrentParser
{
	private static final String PIECES = "pieces";
	private static final String LENGTH = "length";

	public static Torrent parseTorrent(String filePath) throws IOException, TorrentException
	{
		final var r = new Reader(new File(filePath));
		List<IBencodable> x = r.read();
		// A valid torrentfile should only return a single dictionary.
		if (x.size() != 1)
			throw new TorrentException("Parsing .torrent yielded wrong number of bencoding structs."); //$NON-NLS-1$
		try
		{
			return parseTorrent(x.get(0));
		}
		catch (ParseException e)
		{
			Log.err("Error parsing torrent!"); //$NON-NLS-1$
		}
		return null;
	}

	private static Torrent parseTorrent(Object o) throws ParseException, TorrentException
	{
		if (o instanceof BDictionary)
		{
			BDictionary torrentDictionary = (BDictionary) o;
			BDictionary infoDictionary = parseInfoDictionary(torrentDictionary);

			if (infoDictionary != null)
			{
				final var t = new Torrent();

				///////////////////////////////////
				//// OBLIGATED FIELDS /////////////
				///////////////////////////////////
				t.setAnnounce(parseAnnounce(torrentDictionary));
				t.setInfoHash(Utils.sumSHA(infoDictionary.bencode()));
				t.setName(parseTorrentLocation(infoDictionary));
				t.setPieceLength(parsePieceLength(infoDictionary));
				t.setPieces(parsePiecesHashes(infoDictionary));
				t.setPiecesBlob(parsePiecesBlob(infoDictionary));

				///////////////////////////////////
				//// OPTIONAL FIELDS //////////////
				///////////////////////////////////
				t.setFileList(parseFileList(infoDictionary));
				t.setComment(parseComment(torrentDictionary));
				t.setCreatedBy(parseCreatorName(torrentDictionary));
				t.setCreationDate(parseCreationDate(torrentDictionary));
				t.setAnnounceList(parseAnnounceList(torrentDictionary));
				t.setTotalSize(parseSingleFileTotalSize(infoDictionary));

				// Determine if torrent is a singlefile torrent.
				t.setSingleFileTorrent(null != infoDictionary.find(new BByteString(LENGTH))); // $NON-NLS-1$
				return t;
			}
			else
				throw new ParseException("Could not parse infoDictionary", 0); //$NON-NLS-1$
		}
		else
			throw new ParseException("Could not parse Object to BDictionary", 0); //$NON-NLS-1$
	}

	/**
	 * @param info
	 *            info dictionary
	 * @return length — size of the file in bytes (only when one file is being
	 *         shared)
	 */
	private static Long parseSingleFileTotalSize(BDictionary info)
	{
		if (null != info.find(new BByteString(LENGTH))) // $NON-NLS-1$
			return ((BInt) info.find(new BByteString(LENGTH))).getValue(); // $NON-NLS-1$
		return null;
	}

	/**
	 * @param dictionary
	 *            root dictionary of torrent
	 * @return info — this maps to a dictionary whose keys are dependent on whether
	 *         one or more files are being shared.
	 */
	private static BDictionary parseInfoDictionary(BDictionary dictionary)
	{
		if (null != dictionary.find(new BByteString("info"))) //$NON-NLS-1$
			return (BDictionary) dictionary.find(new BByteString("info")); //$NON-NLS-1$
		else
			return null;
	}

	/**
	 * @param dictionary
	 *            root dictionary of torrent
	 * @return creation date: (optional) the creation time of the torrent, in
	 *         standard UNIX epoch format (integer, seconds since 1-Jan-1970
	 *         00:00:00 UTC)
	 */
	private static Date parseCreationDate(BDictionary dictionary)
	{
		if (null != dictionary.find(new BByteString("creation date"))) //$NON-NLS-1$
			return new Date(Long.parseLong(dictionary.find(new BByteString("creation date")).toString())); //$NON-NLS-1$
		return null;
	}

	/**
	 * @param dictionary
	 *            root dictionary of torrent
	 * @return created by: (optional) name and version of the program used to create
	 *         the .torrent (string)
	 */
	private static String parseCreatorName(BDictionary dictionary)
	{
		if (null != dictionary.find(new BByteString("created by"))) //$NON-NLS-1$
			return dictionary.find(new BByteString("created by")).toString(); //$NON-NLS-1$
		return null;
	}

	/**
	 * @param dictionary
	 *            root dictionary of torrent
	 * @return comment: (optional) free-form textual comments of the author (string)
	 */
	private static String parseComment(BDictionary dictionary)
	{
		if (null != dictionary.find(new BByteString("comment"))) //$NON-NLS-1$
			return dictionary.find(new BByteString("comment")).toString(); //$NON-NLS-1$
		else
			return null;
	}

	/**
	 * @param info
	 *            infodictionary of torrent
	 * @return piece length — number of bytes per piece. This is commonly 28 KiB =
	 *         256 KiB = 262,144 B.
	 */
	private static Long parsePieceLength(BDictionary info)
	{
		if (null != info.find(new BByteString("piece length"))) //$NON-NLS-1$
			return ((BInt) info.find(new BByteString("piece length"))).getValue(); //$NON-NLS-1$
		else
			return null;
	}

	/**
	 * @param info
	 *            info dictionary of torrent
	 * @return name — suggested filename where the file is to be saved (if one
	 *         file)/suggested directory name where the files are to be saved (if
	 *         multiple files)
	 */
	private static String parseTorrentLocation(BDictionary info)
	{
		if (null != info.find(new BByteString("name"))) //$NON-NLS-1$
			return info.find(new BByteString("name")).toString(); //$NON-NLS-1$
		else
			return null;
	}

	/**
	 * @param dictionary
	 *            root dictionary of torrent
	 * @return announce — the URL of the tracke
	 */
	private static String parseAnnounce(BDictionary dictionary)
	{
		if (null != dictionary.find(new BByteString("announce"))) //$NON-NLS-1$
			return dictionary.find(new BByteString("announce")).toString(); //$NON-NLS-1$
		else
			return null;
	}

	/**
	 * @param info
	 *            info dictionary of .torrent file.
	 * @return pieces — a hash list, i.e., a concatenation of each piece's SHA-1
	 *         hash. As SHA-1 returns a 160-bit hash, pieces will be a string whose
	 *         length is a multiple of 160-bits.
	 * @throws TorrentException 
	 */
	private static byte[] parsePiecesBlob(BDictionary info) throws TorrentException
	{
		if (null != info.find(new BByteString(PIECES))) //$NON-NLS-1$
		{
			return ((BByteString) info.find(new BByteString(PIECES))).getData(); //$NON-NLS-1$
		}
		else
		{
			throw new TorrentException("Info dictionary does not contain pieces bytestring!"); //$NON-NLS-1$
		}
	}

	/**
	 * @param info
	 *            info dictionary of .torrent file.
	 * @return pieces — a hash list, i.e., a concatenation of each piece's SHA-1
	 *         hash. As SHA-1 returns a 160-bit hash, pieces will be a string whose
	 *         length is a multiple of 160-bits.
	 * @throws TorrentException 
	 */
	private static List<String> parsePiecesHashes(BDictionary info) throws TorrentException
	{
		if (null != info.find(new BByteString(PIECES))) //$NON-NLS-1$
		{
			final var sha1HexRenders = new ArrayList<String>();
			byte[] piecesBlob = ((BByteString) info.find(new BByteString(PIECES))).getData(); //$NON-NLS-1$
			// Split the piecesData into multiple hashes. 1 hash = 20 bytes.
			if (piecesBlob.length % 20 == 0)
			{
				int hashCount = piecesBlob.length / 20;
				for (var currHash = 0; currHash < hashCount; currHash++)
				{
					byte[] currHashByteBlob = Arrays.copyOfRange(piecesBlob, 20 * currHash, (20 * (currHash + 1)));
					String sha1 = Utils.bytesToHex(currHashByteBlob);
					sha1HexRenders.add(sha1);
				}
			}
			else
			{
				throw new TorrentException("Error parsing SHA1 piece hashes. Bytecount was not a multiple of 20."); //$NON-NLS-1$
			}
			return sha1HexRenders;
		}
		else
		{
			throw new TorrentException("Info dictionary does not contain pieces bytestring!"); //$NON-NLS-1$
		}
	}

	/**
	 * @param info
	 *            info dictionary of torrent
	 * @return files — a list of dictionaries each corresponding to a file (only
	 *         when multiple files are being shared).
	 */
	private static List<TorrentFile> parseFileList(BDictionary info)
	{
		final var fileList = new ArrayList<TorrentFile>();
		if (null != info.find(new BByteString("files"))) //$NON-NLS-1$
		{
			final var filesBList = (BList) info.find(new BByteString("files")); //$NON-NLS-1$

			Iterator<IBencodable> fileBDicts = filesBList.getIterator();
			while (fileBDicts.hasNext())
			{
				Object fileObject = fileBDicts.next();
				if (fileObject instanceof BDictionary)
				{
					BDictionary fileBDict = (BDictionary) fileObject;
					BList filePaths = (BList) fileBDict.find(new BByteString("path")); //$NON-NLS-1$
					BInt fileLength = (BInt) fileBDict.find(new BByteString(LENGTH)); // $NON-NLS-1$
					// Pick out each subdirectory as a string.
					final var paths = new LinkedList<String>();
					Iterator<IBencodable> filePathsIterator = filePaths.getIterator();
					while (filePathsIterator.hasNext())
						paths.add(filePathsIterator.next().toString());

					fileList.add(new TorrentFile(fileLength.getValue(), paths));
				}
			}
		}
		return fileList;
	}

	/**
	 * @param dictionary
	 *            root dictionary of torrent
	 * @return announce-list: (optional) this is an extention to the official
	 *         specification, offering backwards-compatibility. (list of lists of
	 *         strings).
	 */
	private static List<String> parseAnnounceList(BDictionary dictionary)
	{
		final var announceUrls = new LinkedList<String>();
		if (null != dictionary.find(new BByteString("announce-list"))) //$NON-NLS-1$
		{
			BList announceList = (BList) dictionary.find(new BByteString("announce-list")); //$NON-NLS-1$
			Iterator<IBencodable> subLists = announceList.getIterator();
			while (subLists.hasNext())
			{
				final var subList = (BList) subLists.next();
				Iterator<IBencodable> elements = subList.getIterator();
				while (elements.hasNext())
				{
					// Assume that each element is a BByteString
					BByteString tracker = (BByteString) elements.next();
					announceUrls.add(tracker.toString());
				}
			}
		}
		return announceUrls;
	}
}
