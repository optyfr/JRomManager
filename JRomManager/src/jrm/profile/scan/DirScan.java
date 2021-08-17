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
package jrm.profile.scan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.ZipError;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FilenameUtils;

import JTrrntzip.DummyLogCallback;
import JTrrntzip.SimpleTorrentZipOptions;
import JTrrntzip.TorrentZip;
import jrm.aui.progress.ProgressHandler;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.digest.MDigest;
import jrm.digest.MDigest.Algo;
import jrm.io.chd.CHDInfoReader;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.MultiThreading;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Archive;
import jrm.profile.data.Container;
import jrm.profile.data.Container.Type;
import jrm.profile.data.Directory;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;
import jrm.profile.data.FakeDirectory;
import jrm.profile.data.Rom;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.FormatOptions.Ext;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import lombok.val;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import one.util.streamex.IntStreamEx;

/**
 * Directories/Archives scanner
 * @author optyfr
 *
 */
public final class DirScan extends PathAbstractor
{
	/**
	 * List of found {@link Container}s
	 */
	private final List<Container> containers = Collections.synchronizedList(new ArrayList<>());
	/**
	 * Map of {@link Container}s by name {@link String}.<br>Will be serialized for disk caching...
	 */
	private final Map<String, Container> containersByName;
	/**
	 * Map of {@link Entity} by CRC
	 */
	private final Map<String, Entry> entriesByCrc = Collections.synchronizedMap(new HashMap<>());
	/**
	 * Map of {@link Entity} by SHA1
	 */
	private final Map<String, Entry> entriesBySha1 = Collections.synchronizedMap(new HashMap<>());
	/**
	 * Map of {@link Entity} by MD5
	 */
	private final Map<String, Entry> entriesByMd5 = Collections.synchronizedMap(new HashMap<>());

	/**
	 * contains the detected suspicious CRCs from profile
	 */
	private Set<String> suspiciousCrc = null; 

	/**
	 * the current session
	 */
	private final Session session;
	
	/**
	 * the directory entry point
	 */
	private final File dir;
	/**
	 * {@link ProgressHandler} to show progression on UI
	 */
	private final ProgressHandler handler;

	private class ScanOptions
	{
		/**
		 * is sha1/md5 calculation is needed
		 */
		final boolean needSha1OrMd5;
		
		/**
		 * tell that md5 was described for disks in profile
		 */
		final boolean md5Disks;
		
		/**
		 * tell that md5 was described for roms in profile
		 */
		final boolean md5Roms;
		
		/**
		 * tell that sha1 was described for disks in profile
		 */
		final boolean sha1Disks;
		
		/**
		 * tell that sha1 was described for roms in profile
		 */
		final boolean sha1Roms;
		
		final boolean isDest;
		final boolean recurse;
		final boolean useParallelism;
		final boolean formatTZip;
		final boolean includeEmptyDirs;
		final boolean archivesAndChdAsRoms;
		
		final int nThreads;
		
		/*
		 * Initialize torrentzip if needed
		 */
		final TorrentZip torrentzip;

		public ScanOptions(Set<Options> options)
		{
			needSha1OrMd5 = options.contains(Options.NEED_SHA1_OR_MD5) || options.contains(Options.NEED_SHA1) || options.contains(Options.NEED_MD5);
			md5Disks = options.contains(Options.MD5_DISKS) || options.contains(Options.NEED_MD5);
			md5Roms = options.contains(Options.MD5_ROMS) || options.contains(Options.NEED_MD5);
			sha1Disks = options.contains(Options.SHA1_DISKS) || options.contains(Options.NEED_SHA1);
			sha1Roms = options.contains(Options.SHA1_ROMS) || options.contains(Options.NEED_SHA1);
			isDest = options.contains(Options.IS_DEST);
			recurse = options.contains(Options.RECURSE);
			useParallelism = options.contains(Options.USE_PARALLELISM);
			formatTZip = options.contains(Options.FORMAT_TZIP);
			includeEmptyDirs = options.contains(Options.EMPTY_DIRS);
			archivesAndChdAsRoms = options.contains(Options.ARCHIVES_AND_CHD_AS_ROMS);
			nThreads = useParallelism ? session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;
			torrentzip = (isDest && formatTZip) ? new TorrentZip(new DummyLogCallback(), new SimpleTorrentZipOptions(false, true)) : null;
		}
	}
	
	/**
	 * Initialization for SevenzipJBinding
	 */
	private void init7zJBinding()
	{
		if(!SevenZip.isInitializedSuccessfully())
		{
			try
			{
				SevenZip.initSevenZipFromPlatformJAR(session.getUser().getSettings().getTmpPath(true).toFile());
			}
			catch(final Exception e)
			{
				Log.err(e.getMessage(),e);
			}
		}
	}

	/**
	 * Options enumeration for directory scanning 
	 */
	public enum Options
	{
		IS_DEST,
		RECURSE,
		NEED_SHA1_OR_MD5,
		NEED_SHA1,
		NEED_MD5,
		USE_PARALLELISM,
		FORMAT_TZIP,
		MD5_ROMS,
		MD5_DISKS,
		SHA1_ROMS,
		SHA1_DISKS,
		EMPTY_DIRS,
		ARCHIVES_AND_CHD_AS_ROMS,
		JUNK_SUBFOLDERS,
		MATCH_PROFILE
	}
	
	/**
	 * convert options from profile and arguments to an {@link EnumSet} of {@link Options}
	 * @param profile the {@link Profile} to get informations
	 * @param is_dest an additional option
	 * @return the {@link EnumSet} of {@link Options}
	 */
	static EnumSet<Options> getOptions(Profile profile, final boolean is_dest)
	{
		EnumSet<Options> options = EnumSet.noneOf(Options.class);
		if (is_dest)
			options.add(Options.IS_DEST);
		/*
		 * Profile options
		 */
		if(profile==null)
			return options;
		if (profile.getProperty(jrm.misc.SettingsEnum.need_sha1_or_md5, false)) // $NON-NLS-1$
			options.add(Options.NEED_SHA1_OR_MD5);
		if (profile.getProperty(jrm.misc.SettingsEnum.use_parallelism, profile.getSession().isServer())) // $NON-NLS-1$
			options.add(Options.USE_PARALLELISM);
		if (profile.getProperty(jrm.misc.SettingsEnum.archives_and_chd_as_roms, false)) // $NON-NLS-1$
			options.add(Options.ARCHIVES_AND_CHD_AS_ROMS);
		final var format = FormatOptions.valueOf(profile.getProperty(jrm.misc.SettingsEnum.format, FormatOptions.ZIP.toString())); // $NON-NLS-1$
		if (FormatOptions.TZIP == format)
			options.add(Options.FORMAT_TZIP);
		else if (FormatOptions.DIR == format)
			options.add(Options.RECURSE);
		if (profile.isMd5Roms())
			options.add(Options.MD5_ROMS);
		if (profile.isMd5Disks())
			options.add(Options.MD5_DISKS);
		if (profile.isSha1Roms())
			options.add(Options.SHA1_ROMS);
		if (profile.isSha1Disks())
			options.add(Options.SHA1_DISKS);
		return options;
	}
	
	/**
	 * Test for suspicious crc (if information is available)
	 * @param crc the crc to test
	 * @return true if crc is suspicious, otherwise false (also false if it can't be tested because information is not available from profile)
	 */
	private boolean isSuspiciousCRC(String crc)
	{
		return suspiciousCrc!=null && suspiciousCrc.contains(crc);
	}
	
	/**
	 * The constructor used for regular scanning<br>
	 * Directory scanning consist of two phases :<br>
	 * - list files archives or directories and check what changed since previous scan (based on date+size)<br>
	 * - read archives content and eventually entries content, for all those that changed or because we need more informations since last scan
	 * @param profile The used {@link Profile}
	 * @param dir the directory entry point ({@link File})
	 * @param handler {@link ProgressHandler} to show progression on UI
	 * @param is_dest is this a scan for a destination folder or a source folder (destination scans are optimized)
	 * @throws BreakException in case user stopped processing thru {@link ProgressHandler}
	 */
	DirScan(final Profile profile, final File dir, final ProgressHandler handler, final boolean is_dest) throws BreakException
	{
		this(profile.getSession(), dir, handler, profile.getSuspiciousCRC(), getOptions(profile, is_dest));
	}
	
	/**
	 * The constructor used for dir2dat (no options or informations coming from profile)
	 * @param dir the directory entry point ({@link File})
	 * @param handler {@link ProgressHandler} to show progression on UI
	 * @param options an {@link EnumSet} of {@link Options}
	 * @throws BreakException in case user stopped processing thru {@link ProgressHandler}
	 */
	DirScan(final Session session, final File dir, final ProgressHandler handler, Set<Options> options) throws BreakException
	{
		this(session, dir, handler, null, options);
	}
	
	/**
	 * internal constructor
	 * @param dir the directory entry point ({@link File})
	 * @param handler {@link ProgressHandler} to show progression on UI
	 * @param suspiciousCrc the list of suspicious crc, can be null which mean non suspicious crc checking
	 * @param options an {@link EnumSet} of {@link Options}
	 * @throws BreakException in case user stopped processing thru {@link ProgressHandler}
	 */
	private DirScan(final Session session, final File dir, final ProgressHandler handler, final Set<String> suspiciousCrc, Set<Options> soptions) throws BreakException
	{
		super(session);
		this.session = session;

		init7zJBinding();

		this.dir = dir;
		this.handler = handler;
		this.suspiciousCrc = suspiciousCrc;
		
		final var options = new ScanOptions(soptions);
		final var path = Paths.get(dir.getAbsolutePath());

		/*
		 * Loading scan cache
		 */
		if(!session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.debug_nocache, false)) //$NON-NLS-1$
			containersByName = load(dir, soptions);
		else
			containersByName = Collections.synchronizedMap(new HashMap<>());

		/*
		 * Test if entry point is valid
		 */
		if(!Files.isDirectory(path))
			return;
		
		
		/*
		 * Initialize progression
		 */
		handler.clearInfos();
		handler.setInfos(options.nThreads,null);
		

		listFiles(dir, handler, path, options);
		
		
		
		/*
		 * Now read at least archives content, add eventually calculate checksum for each entries if needed
		 */
		final var i = new AtomicInteger(0);
		final var j = new AtomicInteger(0);
		final var max = new AtomicInteger(0);
		max.addAndGet(containers.size());
		containers.forEach(c->max.addAndGet((int)(c.getSize()>>20)));
		handler.clearInfos();
		handler.setInfos(options.nThreads,true);
		handler.setProgress(String.format(Messages.getString("DirScan.ScanningFiles"), getRelativePath(dir.toPath())) , -1); //$NON-NLS-1$
		handler.setProgress2("", j.get(), max.get()); //$NON-NLS-1$
		new MultiThreading<Container>(options.nThreads, c -> {
			if(handler.isCancel())
				return;
			try
			{
				scanContainer(c, handler, options);
				handler.setProgress(String.format(Messages.getString("DirScan.Scanned"), c.getFile().getName())); //$NON-NLS-1$
				handler.setProgress2(String.format("%d/%d (%d%%)", i.incrementAndGet(), containers.size(), (int)(j.addAndGet(1+(int)(c.getSize()>>20)) * 100.0 / max.get())), j.get()); //$NON-NLS-1$
			}
			catch(final IOException e)
			{
				c.setLoaded(0);
				Log.err("IOException when scanning", e); //$NON-NLS-1$
			}
			catch(final BreakException e)
			{
				c.setLoaded(0);
				handler.cancel();
			}
			catch(final Exception e)
			{
				c.setLoaded(0);
				Log.err("Other Exception when listing", e); //$NON-NLS-1$
			}
			return;
		}).start(containers.stream().sorted(Container.rcomparator()));

		if(!handler.isCancel())
			save(dir, soptions);

	}

	/**
	 * @param container
	 * @param torrentzip
	 * @param progress
	 * @param isDest
	 * @param recurse
	 * @param formatTZip
	 * @param archivesAndChdAsRoms
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	private void scanContainer(Container container, final ProgressHandler progress, ScanOptions options) throws IOException, NoSuchAlgorithmException
	{
		switch(container.getType())
		{
			case ZIP:
			{
				scanZip(container, options);
				break;
			}
			case RAR:
			case SEVENZIP:
			{
				try(final var entries = new SevenZUpdateEntries(container, options))
				{
					entries.updateEntries();
				}
				break;
			}
			case DIR:
			{
				scanDir(progress, container, options);
				break;
			}
			case FAKE:
			{
				scanFake(progress, container, options);
				break;
			}
			default:
				break;
		}
	}

	/**
	 * @param dir
	 * @param handler
	 * @param isDest
	 * @param includeEmptyDirs
	 * @param archivesAndChdAsRoms
	 * @param nThreads
	 * @param path
	 */
	private void listFiles(final File dir, final ProgressHandler handler, final Path path, final ScanOptions options)
	{
		/*
		 * List files
		 * We go up to 100 subdirs for src dir but 1 level for dest dir type, and we follow links
		 */

		try(Stream<Path> stream = Files.walk(path, options.isDest ? 1 : 100, FileVisitOption.FOLLOW_LINKS))
		{
			final var i = new AtomicInteger();
		//	handler.setProgress(String.format(Messages.getString("DirScan.ListingFiles"), getRelativePath(dir.toPath())), 0); //$NON-NLS-1$
		//	handler.setProgress2("", null); //$NON-NLS-1$
			handler.setProgress(null, -1);
			handler.setProgress2(String.format(Messages.getString("DirScan.ListingFiles"), getRelativePath(dir.toPath())), 0, 100); //$NON-NLS-1$
			
			new MultiThreading<Path>(options.nThreads, p -> {
				if(handler.isCancel())
					return;
				if(path.equals(p))
					return;
				final var file = p.toFile();
				try
				{
					final BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
					if(options.isDest)
						listFilesDest(file, attr);
					else
						listFilesSrc(path, p, file, attr, options);
					handler.setProgress(path.relativize(p).toString(), -1); //$NON-NLS-1$
					handler.setProgress2(String.format(Messages.getString("DirScan.ListingFiles2"), getRelativePath(dir.toPath()), i.incrementAndGet()), 0); //$NON-NLS-1$
				}
				catch(final IOException e)
				{
					Log.err(e.getMessage(),e);
				}
				catch(final BreakException e)
				{
					handler.cancel();
				}
				return;
			}).start(stream);
			
			/*
			 * Remove files from cache that are not in up2date state, because that mean that those files were removed from FS since the previous scan
			 */
			containersByName.entrySet().removeIf(entry -> !entry.getValue().isUp2date());
		}
		catch(final IOException e)
		{
			Log.err("IOException when listing", e); //$NON-NLS-1$
		}
		catch(final Exception e)
		{
			Log.err("Other Exception when listing", e); //$NON-NLS-1$
		}
	}

	/**
	 * @param includeEmptyDirs
	 * @param archivesAndChdAsRoms
	 * @param path
	 * @param p
	 * @param file
	 * @param attr
	 * @throws IOException
	 */
	private void listFilesSrc(final Path path, Path p, final File file, final BasicFileAttributes attr, ScanOptions options) throws IOException
	{
		/*
		 * With src type dir, we need to find each potential container end points while walking into the entire hierarchy   
		 */
		if(attr.isRegularFile()) // We test only regular files even for directory containers (must contains at least 1 file)
		{
			val type = Container.getType(file);
			if(type == Type.UNK || options.archivesAndChdAsRoms)	// maybe we did found a potential directory container with unknown type files inside)
			{
				if(path.equals(file.getParentFile().toPath()))	// skip if parent is the entry point
				{
					listFilesSrcUnknown(file, attr, type);
				}
				else
				{
					listFilesSrcParentDir(path, p, file, attr);
				}
			}
			else	// otherwise it's an archive file
			{
				listFilesSrcArchive(path, p, file, attr);
			}
		}
		else if(options.includeEmptyDirs)
		{
			listFilesSrcEmptyDir(path, p, file, attr);
		}
	}

	/**
	 * @param path
	 * @param p
	 * @param file
	 * @param attr
	 * @throws IOException
	 */
	private void listFilesSrcEmptyDir(final Path path, Path p, final File file, final BasicFileAttributes attr) throws IOException
	{
		Container c;
		try(DirectoryStream<Path> dirstream = Files.newDirectoryStream(p))
		{
			if(!dirstream.iterator().hasNext())
			{
				final var relative  = path.relativize(p);
				if(null == (c = containersByName.get(relative.toString())) || (c.getModified() != attr.lastModifiedTime().toMillis() && !c.isUp2date()))
				{
					c = new Directory(file, getRelativePath(file), attr);
					containers.add(c);
					c.setUp2date(true);
					containersByName.put(relative.toString(), c);
				}
				else if(!c.isUp2date())
				{
					c.setUp2date(true);
					containers.add(c);
				}
			}
		}
	}

	/**
	 * @param path
	 * @param p
	 * @param file
	 * @param attr
	 * @throws IOException
	 */
	private void listFilesSrcParentDir(final Path path, Path p, final File file, final BasicFileAttributes attr) throws IOException
	{
		Container c;
		final var parentDir = file.getParentFile();
		final var parentAttr = Files.readAttributes(p.getParent(), BasicFileAttributes.class);
		final var relative  = path.relativize(p.getParent());
		if(null == (c = containersByName.get(relative.toString())) || (c.getModified() != parentAttr.lastModifiedTime().toMillis() && !c.isUp2date()))
		{
			c = new Directory(parentDir, getRelativePath(parentDir), attr);
			containers.add(c);
			containersByName.put(relative.toString(), c);
			c.setUp2date(true);
		}
		else if(!c.isUp2date())
		{
			containers.add(c);
			c.setUp2date(true);
		}
	}

	/**
	 * @param file
	 * @param attr
	 * @param type
	 */
	private void listFilesSrcUnknown(final File file, final BasicFileAttributes attr, final jrm.profile.data.Container.Type type)
	{
		Container c;
		val fname = type == Type.UNK ? (FilenameUtils.getBaseName(file.getName()) + Ext.FAKE) : file.getName();
		if(null == (c = containersByName.get(fname)) || (c.getModified() != attr.lastModifiedTime().toMillis() && !c.isUp2date()))
		{
			c = new FakeDirectory(file, getRelativePath(file), attr);
			containers.add(c);
			containersByName.put(fname, c);
			c.setUp2date(true);
		}
		else if(!c.isUp2date())
		{
			containers.add(c);
			c.setUp2date(true);
		}
	}

	/**
	 * @param path
	 * @param p
	 * @param file
	 * @param attr
	 */
	private void listFilesSrcArchive(final Path path, Path p, final File file, final BasicFileAttributes attr)
	{
		Container c;
		final var relative  = path.relativize(p);
		if(null == (c = containersByName.get(relative.toString())) || ((c.getModified() != attr.lastModifiedTime().toMillis() || c.getSize() != attr.size()) && !c.isUp2date()))
		{
			c = new Archive(file, getRelativePath(file), attr);
			containers.add(c);
			c.setUp2date(true);
			containersByName.put(relative.toString(), c);
		}
		else if(!c.isUp2date())
		{
			c.setUp2date(true);
			containers.add(c);
		}
	}

	/**
	 * @param file
	 * @param attr
	 */
	private void listFilesDest(final File file, final BasicFileAttributes attr)
	{
		Container c;
		val type = attr.isRegularFile() ? Container.getType(file) : Type.DIR;
		val fname = type == Type.UNK ? (FilenameUtils.getBaseName(file.getName()) + Ext.FAKE) : file.getName();
		if(null == (c = containersByName.get(fname)) /* new container */ || ((c.getModified() != attr.lastModifiedTime().toMillis() /* container date changed */ || (c instanceof Archive && c.getSize() != attr.size()) /* container size changed */) && !c.isUp2date()))
		{
			if(attr.isRegularFile())
			{
				if (type != Container.Type.UNK)
					c = new Archive(file, getRelativePath(file), attr);
				else
					c = new FakeDirectory(file, getRelativePath(file), attr);
			}
			else
				c = new Directory(file, getRelativePath(file), attr);
			containers.add(c);
			containersByName.put(fname, c);
			c.setUp2date(true);
		}
		else if(!c.isUp2date())
		{
			// container listed but did not change
			c.setUp2date(true);
			containers.add(c);
		}
	}

	/**
	 * @param handler
	 * @param archivesAndChdAsRoms
	 * @param c
	 * @throws IOException
	 */
	private void scanFake(final ProgressHandler handler, Container c, ScanOptions options) throws IOException
	{
		if(c.getLoaded() < 1 || (options.needSha1OrMd5 && c.getLoaded() < 2))
		{
			final var entry = new Entry(c.getFile().getName(),c.getRelFile().getName(), c.getSize(), c.getModified());
			if(options.archivesAndChdAsRoms)
				entry.setType(Entry.Type.UNK);
			handler.setProgress(FilenameUtils.getBaseName(c.getFile().getName()) , -1, null, c.getFile().getName()); //$NON-NLS-1$ //$NON-NLS-2$
			updateEntry(c.add(entry), c.getFile().toPath(), options);
			c.setLoaded(options.needSha1OrMd5 ? 2 : 1);
		}
		else
		{
			for(final Entry entry : c.getEntries())
				updateEntry(entry, options);
		}
	}

	/**
	 * @param handler
	 * @param isDest
	 * @param recurse
	 * @param archivesAndChdAsRoms
	 * @param c
	 * @throws IOException
	 */
	private void scanDir(final ProgressHandler handler, Container c, ScanOptions options) throws IOException
	{
		if(c.getLoaded() < 1 || (options.needSha1OrMd5 && c.getLoaded() < 2))
		{
			scanDirNoCache(handler, c, options);
		}
		else
		{
			for(final Entry entry : c.getEntries())
				updateEntry(entry, options);
		}
	}

	/**
	 * @param handler
	 * @param c
	 * @param options
	 * @throws IOException
	 */
	private void scanDirNoCache(final ProgressHandler handler, Container c, ScanOptions options) throws IOException
	{
		try
		{
			Files.walkFileTree(c.getFile().toPath(), EnumSet.noneOf(FileVisitOption.class), (options.isDest&&options.recurse)?Integer.MAX_VALUE:1, new SimpleFileVisitor<Path>()
			{
				@Override
				public FileVisitResult visitFile(final Path entryPath, final BasicFileAttributes attrs) throws IOException
				{
					if(attrs.isRegularFile())
					{
						final var entry = new Entry(entryPath.toString(),getRelativePath(entryPath).toString(), attrs);
						if(options.archivesAndChdAsRoms)
							entry.setType(Entry.Type.UNK);
						handler.setProgress(c.getFile().getName() , -1, null, File.separator+c.getFile().toPath().relativize(entryPath).toString()); //$NON-NLS-1$ //$NON-NLS-2$
						updateEntry(c.add(entry), entryPath, options);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
				{
					return FileVisitResult.CONTINUE;
				}
			});
			c.setLoaded(options.needSha1OrMd5 ? 2 : 1);
		}
		catch(AccessDeniedException e)
		{
			// denied
		}
	}

	/**
	 * @param isDest
	 * @param formatTZip
	 * @param torrentzip
	 * @param c
	 * @throws IOException
	 */
	private void scanZip(Container c, ScanOptions options) throws IOException
	{
		if(c.getLoaded() < 1 || (options.needSha1OrMd5 && c.getLoaded() < 2))
		{
			final Map<String, Object> env = new HashMap<>();
			env.put("useTempFile", true); //$NON-NLS-1$
			env.put("readOnly", true); //$NON-NLS-1$
			try(final var fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + c.getFile().toURI()), env);) //$NON-NLS-1$
			{
				final var root = fs.getPath("/"); //$NON-NLS-1$
				Files.walkFileTree(root, new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile(final Path entryPath, final BasicFileAttributes attrs) throws IOException
					{
						updateEntry(c.add(new Entry(entryPath.toString(),getRelativePath(entryPath).toString())), entryPath, options);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
					{
						return FileVisitResult.CONTINUE;
					}
				});
				c.setLoaded(options.needSha1OrMd5 ? 2 : 1);
			}
			catch (ZipError | IOException e)
			{
				Log.err(() -> c.getRelFile() + " : " + e.getMessage());
			}
		}
		else
		{
			for(final Entry entry : c.getEntries())
				updateEntry(entry, options);
		}
		if(options.isDest && options.formatTZip && c.getLastTZipCheck() < c.getModified())
		{
			c.setLastTZipStatus(options.torrentzip.Process(c.getFile()));
			c.setLastTZipCheck(System.currentTimeMillis());
		}
	}

	/**
	 * Update SevenZip Entries
	 * @author optyfr
	 *
	 */
	private class SevenZUpdateEntries implements Closeable
	{
		private final class ComputeHashes7ZipCallback implements IArchiveExtractCallback
		{
			private final Map<Integer, Entry> entries;
			Entry entry;

			private ComputeHashes7ZipCallback(Map<Integer, Entry> entries)
			{
				this.entries = entries;
			}

			@Override
			public void setTotal(final long total) throws SevenZipException
			{
				// unused
			}

			@Override
			public void setCompleted(final long complete) throws SevenZipException
			{
				// unused
			}

			@Override
			public void setOperationResult(final ExtractOperationResult extractOperationResult) throws SevenZipException
			{
				if(extractOperationResult == ExtractOperationResult.OK)
				{
					for(final MDigest d : digest)
					{
						if(d.getAlgorithm()==Algo.SHA1) //$NON-NLS-1$
						{
							entry.setSha1(d.toString());
							entriesBySha1.put(entry.getSha1(), entry);
						}
						if(d.getAlgorithm()==Algo.MD5) //$NON-NLS-1$
						{
							entry.setMd5(d.toString());
							entriesByMd5.put(entry.getMd5(), entry);
						}
						d.reset();
					}
				}
			}

			@Override
			public void prepareOperation(final ExtractAskMode extractAskMode) throws SevenZipException
			{
				// unused
			}

			@Override
			public ISequentialOutStream getStream(final int index, final ExtractAskMode extractAskMode) throws SevenZipException
			{
				entry = entries.get(index);
				if(extractAskMode != ExtractAskMode.EXTRACT)
					return null;
				return data -> {
					for(final MDigest d : digest)
						d.update(data);
					return data.length; // Return amount of proceed data
				};
			}
		}

		/**
		 * The container to read
		 */
		private final Container container;
		/**
		 * the algorithms requested for calculating message digest
		 */
		private final ArrayList<Algo> algorithms;
		/**
		 * The {@link MessageDigest} array (array size is equals to {@link #algorithms} size)
		 */
		private final MDigest[] digest;
		/**
		 * the class to archive in case of org.apache.commons.compress usage
		 */
		private SevenZFile cArchive = null;
		/**
		 * the class to archive in case of external 7z cmd usage 
		 */
		private SevenZipArchive archive = null;

		private final ScanOptions options;
		
		/**
		 * The constructor
		 * @param container The {@link Container} from which to update entries
		 * @throws NoSuchAlgorithmException
		 */
		private SevenZUpdateEntries(final Container container, ScanOptions options) throws NoSuchAlgorithmException
		{
			this.container = container;
			this.options = options;
			algorithms = new ArrayList<>();
			if(options.sha1Roms)
				algorithms.add(Algo.SHA1); //$NON-NLS-1$
			if(options.md5Roms)
				algorithms.add(Algo.MD5); //$NON-NLS-1$
			digest = new MDigest[algorithms.size()];
			for(var i = 0; i < algorithms.size(); i++)
				digest[i] = MDigest.getAlgorithm(algorithms.get(i));
		}

		@Override
		public void close() throws IOException
		{
			if(archive != null)
				archive.close();
			if(cArchive != null)
				cArchive.close();
		}

		/**
		 * get {@link SevenZFile} from  {@link Container#file}
		 * @return {@link SevenZFile}
		 * @throws IOException
		 */
		private SevenZFile getCArchive() throws IOException
		{
			if(cArchive == null)
				cArchive = new SevenZFile(container.getFile());
			return cArchive;
		}

		/**
		 * get {@link SevenZipArchive} from  {@link Container#file}
		 * @return {@link SevenZipArchive}
		 * @throws IOException
		 */
		private SevenZipArchive getArchive() throws IOException
		{
			if(archive == null)
				archive = new SevenZipArchive(session, container.getFile());
			return archive;
		}

		/**
		 * get simplified interface from {@link IInArchive} 
		 * @return {@link ISimpleInArchive}
		 * @throws IOException
		 */
		private ISimpleInArchive getNInterface() throws IOException
		{
			return getArchive().getNative7Zip().getIInArchive().getSimpleInterface();
		}

		/**
		 * update the entries
		 * @throws IOException
		 * @throws NoSuchAlgorithmException
		 */
		private void updateEntries() throws IOException
		{
			if(SevenZip.isInitializedSuccessfully())
			{
				updateEntries7ZipJBindingMethod();
			}
			else	// in that case we support only sevenzip, not rar or whatever
			{
				updateEntriesFallbackMethod();
			}
		}

		/**
		 * @throws SevenZipException
		 * @throws IOException
		 */
		private void updateEntries7ZipJBindingMethod() throws IOException
		{
			final Map<Integer, Entry> entries = new HashMap<>();
			if(container.getLoaded() < 1 || (options.needSha1OrMd5 && container.getLoaded() < 2))
			{
				for(final ISimpleInArchiveItem item : getNInterface().getArchiveItems())
				{
					if(item.isFolder())
						continue;
					updateEntry(container.add(new Entry(item.getPath(),null)), entries, item);

				}
				container.setLoaded(options.needSha1OrMd5 ? 2 : 1);
			}
			else
			{
				for(final Entry entry : container.getEntries())
					updateEntry(entry, entries, null);
			}
			computeHashes(entries);
		}

		/**
		 * @throws IOException
		 */
		private void updateEntriesFallbackMethod() throws IOException
		{
			final HashMap<String, Entry> entries = new HashMap<>();
			if(container.getLoaded() < 1 || (options.needSha1OrMd5 && container.getLoaded() < 2))
			{
				for(final SevenZArchiveEntry archive_entry : getCArchive().getEntries())
				{
					if(archive_entry.isDirectory())
						continue;
					updateEntry(container.add(new Entry(archive_entry.getName(),null)), entries, archive_entry);
				}
				container.setLoaded(options.needSha1OrMd5 ? 2 : 1);
			}
			else
			{
				for(final Entry entry : container.getEntries())
					updateEntry(entry, entries, (SevenZArchiveEntry) null);
			}
			computeHashes(entries);
		}
		
		/**
		 * update an entry from an sevenzipjbinding {@link ISimpleInArchiveItem} item
		 * @param entry the entry to update
		 * @param entries the {@link Map} of {@link Entry} by {@link Integer} item index in archive, their hash will be computed
		 * @param item the {@link ISimpleInArchiveItem} in relation with {@link Entry}
		 * @throws IOException
		 */
		private void updateEntry(final Entry entry, final Map<Integer, Entry> entries, ISimpleInArchiveItem item) throws IOException
		{
			if(entry.getSize() == 0 && entry.getCrc() == null && item != null)
			{
				entry.setSize(item.getSize());
				entry.setCrc(String.format("%08x", item.getCRC())); //$NON-NLS-1$
			}
			entriesByCrc.put(entry.getCrc() + "." + entry.getSize(), entry); //$NON-NLS-1$
			if(entry.getSha1() == null && entry.getMd5() == null && (options.needSha1OrMd5 || entry.getCrc() == null || isSuspiciousCRC(entry.getCrc())))
			{
				updateEntryExt(entry, entries, item);
			}
			else
			{
				if(entry.getSha1() != null)
					entriesBySha1.put(entry.getSha1(), entry);
				if(entry.getMd5() != null)
					entriesByMd5.put(entry.getMd5(), entry);
			}
		}

		/**
		 * @param entry
		 * @param entries
		 * @param item
		 * @throws SevenZipException
		 * @throws IOException
		 */
		private void updateEntryExt(final Entry entry, final Map<Integer, Entry> entries, ISimpleInArchiveItem item) throws IOException
		{
			if(item == null)
			{
				for(final ISimpleInArchiveItem itm : getNInterface().getArchiveItems())
				{
					if(entry.getFile().equals(itm.getPath()))
					{
						item = itm;
						break;
					}
				}

			}
			if(item != null)
				entries.put(item.getItemIndex(), entry);
		}

		/**
		 * update an entry from a {@link SevenZArchiveEntry}
		 * @param entry the {@link Entry}
		 * @param entries the {@link HashSet} of {@link Entry}, their hash will be computed
		 * @param archiveEntry the {@link SevenZArchiveEntry} in relation with {@link Entry}
		 * @throws IOException
		 */
		private void updateEntry(final Entry entry, final Map<String,Entry> entries, final SevenZArchiveEntry archiveEntry)
		{
			if(entry.getSize() == 0 && entry.getCrc() == null && archiveEntry != null)
			{
				entry.setSize(archiveEntry.getSize());
				entry.setCrc(String.format("%08x", archiveEntry.getCrcValue())); //$NON-NLS-1$
			}
			entriesByCrc.put(entry.getCrc() + "." + entry.getSize(), entry); //$NON-NLS-1$
			if(entry.getSha1() == null && entry.getMd5() == null && (options.needSha1OrMd5 || entry.getCrc() == null || isSuspiciousCRC(entry.getCrc())))
			{
				entries.put(entry.getFile(), entry);
			}
			else
			{
				if(entry.getSha1() != null)
					entriesBySha1.put(entry.getSha1(), entry);
				if(entry.getMd5() != null)
					entriesByMd5.put(entry.getMd5(), entry);
			}
			
		}

		/**
		 * Extract and compute Hashes from items in a SevenzipJBinding archive
		 * @param entries the {@link Map} of {@link Entry} by {@link Integer} item index in archive
		 * @throws NoSuchAlgorithmException
		 * @throws IOException
		 */
		private void computeHashes(final Map<Integer, Entry> entries) throws IOException
		{
			if(entries.size() > 0)
			{
				getArchive().getNative7Zip().getIInArchive().extract(IntStreamEx.of(entries.keySet()).toArray(), false, new ComputeHashes7ZipCallback(entries));
			}
		}

		/**
		 * Extract and compute hashes from entries in a {@link SevenZipArchive}
		 * @param entries the {@link HashSet} of {@link Entry} to be computed
		 * @throws IOException
		 */
		private void computeHashes(final HashMap<String, Entry> entries) throws IOException
		{
			SevenZArchiveEntry entry7z;
			Entry entry;
			while (null != (entry7z = getCArchive().getNextEntry()))
			{
				if (null != (entry = entries.get(entry7z.getName())))
				{
					computeHashes(entry7z.getSize());
					for (MDigest d : digest)
					{
						if (d.getAlgorithm()==Algo.SHA1) //$NON-NLS-1$
						{
							entry.setSha1(d.toString());
							entriesBySha1.put(entry.getSha1(), entry);
						}
						if (d.getAlgorithm()==Algo.MD5) //$NON-NLS-1$
						{
							entry.setMd5(d.toString());
							entriesByMd5.put(entry.getMd5(), entry);
						}
						d.reset();
					}
				}
			}
		}

		/**
		 * @param size
		 * @throws IOException
		 */
		private void computeHashes(long size) throws IOException
		{
			final var buffer = new byte[8192];
			while (size > 0)
			{
				int read = getCArchive().read(buffer, 0, (int) Math.min(buffer.length, size));
				if(read == -1)
					break;
				for (MDigest d : digest)
					d.update(buffer, 0, read);
				size -= read;
			}
		}
	}

	/**
	 * Update an entry (zip or dir), FS and Path will be retrieved from {@link Entry#parent}
	 * @param entry the {@link Entry} to update
	 * @throws IOException
	 */
	private void updateEntry(final Entry entry, ScanOptions options) throws IOException
	{
		updateEntry(entry, (Path) null, options);
	}

	/**
	 * Update an entry (zip or dir)
	 * @param entry the {@link Entry} to update
	 * @param entryPath the {@link Path} corresponding to the entry (can be null, it will then be retrieved from {@link Entry#parent}
	 * @throws IOException
	 */
	private void updateEntry(final Entry entry, final Path entryPath, ScanOptions options) throws IOException
	{
		if(entry.getParent().getType() == Type.ZIP)
		{
			updatEntryZip(entry, entryPath);
		}
		if(entry.getType() == Entry.Type.CHD && entry.getSha1() == null && entry.getMd5() == null)
		{
			updateEntryCHD(entry, entryPath, options);
		}
		else if(entry.getType() != Entry.Type.CHD && (options.needSha1OrMd5 || entry.getCrc() == null || isSuspiciousCRC(entry.getCrc())))
		{
			updateEntryExt(entry, entryPath, options);
		}
		else
		{
			if(entry.getCrc() != null)
				entriesByCrc.put(entry.getCrc() + "." + entry.getSize(), entry); //$NON-NLS-1$
			if(entry.getSha1() != null)
				entriesBySha1.put(entry.getSha1(), entry);
			if(entry.getMd5() != null)
				entriesByMd5.put(entry.getMd5(), entry);
		}
	}

	/**
	 * @param entry
	 * @param entryPath
	 * @param options
	 * @throws IOException
	 */
	private void updateEntryExt(final Entry entry, final Path entryPath, ScanOptions options) throws IOException
	{
		List<Algo> algorithms = new ArrayList<>();
		if(entry.getCrc()==null)
			algorithms.add(Algo.CRC32); //$NON-NLS-1$
		if(entry.getMd5() == null && (options.md5Roms || options.needSha1OrMd5))
			algorithms.add(Algo.MD5); //$NON-NLS-1$
		if(entry.getSha1() == null && (options.sha1Roms || options.needSha1OrMd5))
			algorithms.add(Algo.SHA1); //$NON-NLS-1$
		updateEntryExt(entry, entryPath, algorithms);
		if(entry.getCrc() != null)
			entriesByCrc.put(entry.getCrc() + "." + entry.getSize(), entry); //$NON-NLS-1$
		if(entry.getSha1() != null)
			entriesBySha1.put(entry.getSha1(), entry);
		if(entry.getMd5() != null)
			entriesByMd5.put(entry.getMd5(), entry);
	}

	/**
	 * @param entry
	 * @param entryPath
	 * @param algorithms
	 * @throws IOException
	 */
	private void updateEntryExt(final Entry entry, final Path entryPath, List<Algo> algorithms) throws IOException
	{
		if(!algorithms.isEmpty()) try
		{
			var path = entryPath;
			if(entryPath == null)
				path = getPath(entry);
			MDigest[] digests = computeHash(path, algorithms);
			for(MDigest md : digests)
			{
				switch (md.getAlgorithm())
				{
					case CRC32: //$NON-NLS-1$
						entry.setCrc(md.toString());
						break;
					case MD5: //$NON-NLS-1$
						entry.setMd5(md.toString());
						break;
					case SHA1: //$NON-NLS-1$
						entry.setSha1(md.toString());
						break;
				}
			}
			if(entryPath == null)
				path.getFileSystem().close();
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	/**
	 * @param entry
	 * @param entryPath
	 * @param options
	 * @throws IOException
	 */
	private void updateEntryCHD(final Entry entry, final Path entryPath, ScanOptions options) throws IOException
	{
		var path = entryPath;
		if(entryPath == null)
			path = getPath(entry);
		final var chdInfo = new CHDInfoReader(path.toFile());
		if(options.sha1Disks)
		{
			entry.setSha1(chdInfo.getSHA1());
			if(null != entry.getSha1())
				entriesBySha1.put(entry.getSha1(), entry);
		}
		if(options.md5Disks)
		{
			entry.setMd5(chdInfo.getMD5());
			if(null != entry.getMd5())
				entriesByMd5.put(entry.getMd5(), entry);
		}
		if(entryPath == null)
			path.getFileSystem().close();
	}

	/**
	 * @param entry
	 * @param entryPath
	 * @throws IOException
	 */
	private void updatEntryZip(final Entry entry, final Path entryPath) throws IOException
	{
		if(entry.getSize() == 0 && entry.getCrc() == null)
		{
			var path = entryPath;
			if(entryPath == null)
				path = getPath(entry);
			final Map<String, Object> entryZipAttrs = Files.readAttributes(path, "zip:*"); //$NON-NLS-1$
			entry.setSize((Long) entryZipAttrs.get("size")); //$NON-NLS-1$
			entry.setCrc(String.format("%08x", entryZipAttrs.get("crc"))); //$NON-NLS-1$ //$NON-NLS-2$
			if(entryPath == null)
				path.getFileSystem().close();
		}
		entriesByCrc.put(entry.getCrc() + "." + entry.getSize(), entry); //$NON-NLS-1$
	}

	private MDigest[] computeHash(final Path entryPath, final List<Algo> algorithm) throws NoSuchAlgorithmException
	{
		return computeHash(entryPath, algorithm.toArray(new Algo[0]));
	}
	
	private MDigest[] computeHash(final Path entryPath, final Algo[] algorithm) throws NoSuchAlgorithmException
	{
		var md = new MDigest[algorithm.length];
		for(var i = 0; i < algorithm.length; i++)
			md[i] = MDigest.getAlgorithm(algorithm[i]);
		try
		{
			MDigest.computeHash(Files.newInputStream(entryPath), md);
		}
		catch(final Exception e)
		{
			Log.err(e.getMessage(),e);
		}
		return md;
	}
	

	/**
	 * get {@link Path} from {@link Entry} using FS deduced from {@link Entry#parent}
	 * @param entry the {@link Entry} to retrieve {@link Path}
	 * @return a {@link Path}
	 * @throws IOException
	 */
	private Path getPath(final Entry entry) throws IOException
	{
		try(final var srcfs = FileSystems.newFileSystem(entry.getParent().getFile().toPath(), (ClassLoader)null))
		{
			return srcfs.getPath(entry.getFile());
		}
	}

	/**
	 * Find an {@link Entry} by a {@link Rom} hash
	 * @param r the {@link Rom} from which to find {@link Entry} 
	 * @return {@link Entry} or null if not found
	 */
	Entry findByHash(final Rom r)
	{
		Entry entry = null;
		if(r.getSha1() != null)
		{
			if(null != (entry = entriesBySha1.get(r.getSha1())))
				return entry;
			if(isSuspiciousCRC(r.getCrc()))
				return null;
		}
		if(r.getMd5() != null)
		{
			if(null != (entry = entriesByMd5.get(r.getMd5())))
				return entry;
			if(isSuspiciousCRC(r.getCrc()))
				return null;
		}
		return entriesByCrc.get(r.getCrc() + "." + r.getSize()); //$NON-NLS-1$
	}

	/**
	 * Find an {@link Entry} by a {@link Disk} hash
	 * @param d the {@link Disk} from which to find {@link Entry}
	 * @return {@link Entry} or null if not found
	 */
	Entry findByHash(final Disk d)
	{
		Entry entry = null;
		if (d.getSha1() != null && null != (entry = entriesBySha1.get(d.getSha1())))
			return entry;
		return entriesByMd5.get(d.getMd5());
	}

	private static String getCacheExt(Set<Options> options)
	{
		if(options.contains(Options.IS_DEST))
		{
			return getCacheExtDest(options);
		}
		else
		{
			if(options.contains(Options.ARCHIVES_AND_CHD_AS_ROMS))
			{
				if(options.contains(Options.RECURSE))
					return ".rascache"; //$NON-NLS-1$
				return ".ascache"; //$NON-NLS-1$
			}
			if(options.contains(Options.RECURSE))
				return ".rscache"; //$NON-NLS-1$
			return ".scache"; //$NON-NLS-1$
		}
	}

	/**
	 * @param options
	 * @return
	 */
	private static String getCacheExtDest(Set<Options> options)
	{
		if(options.contains(Options.ARCHIVES_AND_CHD_AS_ROMS))
		{
			if(options.contains(Options.RECURSE))
				return ".radcache"; //$NON-NLS-1$
			return ".adcache"; //$NON-NLS-1$
		}
		if(options.contains(Options.RECURSE))
			return ".rdcache"; //$NON-NLS-1$
		return ".dcache"; //$NON-NLS-1$
	}
	
	/**
	 * get the scan cache File
	 * @param file the root dir {@link File} of the scan
	 * @param options {@link EnumSet} of {@link Options}
	 * @return a {@link File} corresponding to the cache file
	 */
	public static File getCacheFile(final Session session, final File file, Set<Options> options)
	{
		final var workdir = session.getUser().getSettings().getWorkPath().toFile(); //$NON-NLS-1$
		final var cachedir = new File(workdir, "cache"); //$NON-NLS-1$
		cachedir.mkdirs();
		final var crc = new CRC32();
		crc.update(file.getAbsolutePath().getBytes());
		return new File(cachedir, String.format("%08x", crc.getValue()) + getCacheExt(options)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Save the cache file
	 * @param file the root dir {@link File} of the scan
	 */
	private void save(final File file, Set<Options> options)
	{
		try(final var oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(session, file, options)))))
		{
			oos.writeObject(containersByName);
		}
		catch(final Exception e)
		{
			// ignore silently
		}
	}

	/**
	 * Load a cache file
	 * @param file the root dir {@link File} of the scan
	 * @return the resulting {@link Map}&lt;{@link String}, {@link Container}&gt; from cache
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Container> load(final File file, Set<Options> options)
	{
		final var cachefile = getCacheFile(session, file, options);
		try(final var ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
		{
			handler.clearInfos();
			handler.setProgress(String.format(Messages.getString("DirScan.LoadingScanCache"), getRelativePath(file.toPath())) , 0); //$NON-NLS-1$
			return (Map<String, Container>) ois.readObject();
		}
		catch(final Exception e)
		{
			// ignore silently
		}
		return new HashMap<>();
	}
	
	/**
	 * get the {@link Iterable} of {@link #containers}
	 * @return {@link Iterable}&lt;{@link Container}&gt;
	 */
	Iterable<Container> getContainersIterable()
	{
		return containers;
	}

	/**
	 * get a {@link Container} by its name from {@link #containersByName}
	 * @param name the name String of a container
	 * @return the {@link Container} or null
	 */
	Container getContainerByName(String name)
	{
		return containersByName.get(name);
	}
	
	/**
	 * get the root scan dir
	 * @return a {@link File} of the root scan dir
	 */
	File getDir()
	{
		return dir;
	}
	
}
