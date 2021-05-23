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
	private final Map<String, Container> containers_byname;
	/**
	 * Map of {@link Entity} by CRC
	 */
	private final Map<String, Entry> entries_bycrc = Collections.synchronizedMap(new HashMap<>());
	/**
	 * Map of {@link Entity} by SHA1
	 */
	private final Map<String, Entry> entries_bysha1 = Collections.synchronizedMap(new HashMap<>());
	/**
	 * Map of {@link Entity} by MD5
	 */
	private final Map<String, Entry> entries_bymd5 = Collections.synchronizedMap(new HashMap<>());

	/**
	 * is sha1/md5 calculation is needed
	 */
	private final boolean need_sha1_or_md5;
	/**
	 * tell that md5 was described for disks in profile
	 */
	private final boolean md5_disks;
	/**
	 * tell that md5 was described for roms in profile
	 */
	private final boolean md5_roms;
	/**
	 * tell that sha1 was described for disks in profile
	 */
	private final boolean sha1_disks;
	/**
	 * tell that sha1 was described for roms in profile
	 */
	private final boolean sha1_roms;
	/**
	 * contains the detected suspicious CRCs from profile
	 */
	private Set<String> suspicious_crc = null; 

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
	/**
	 * other options
	 */
	private EnumSet<Options> options;

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
		if(profile!=null)
		{
			if (profile.getProperty(jrm.misc.SettingsEnum.need_sha1_or_md5, false)) //$NON-NLS-1$
				options.add(Options.NEED_SHA1_OR_MD5);
			if (profile.getProperty(jrm.misc.SettingsEnum.use_parallelism, profile.getSession().server)) //$NON-NLS-1$
				options.add(Options.USE_PARALLELISM);
			if (profile.getProperty(jrm.misc.SettingsEnum.archives_and_chd_as_roms, false)) //$NON-NLS-1$
				options.add(Options.ARCHIVES_AND_CHD_AS_ROMS);
			final var format = FormatOptions.valueOf(profile.getProperty(jrm.misc.SettingsEnum.format, FormatOptions.ZIP.toString())); //$NON-NLS-1$
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
		}
		return options;
	}
	
	/**
	 * Test for suspicious crc (if information is available)
	 * @param crc the crc to test
	 * @return true if crc is suspicious, otherwise false (also false if it can't be tested because information is not available from profile)
	 */
	private boolean isSuspiciousCRC(String crc)
	{
		return suspicious_crc!=null && suspicious_crc.contains(crc);
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
	DirScan(final Session session, final File dir, final ProgressHandler handler, EnumSet<Options> options) throws BreakException
	{
		this(session, dir, handler, null, options);
	}
	
	/**
	 * internal constructor
	 * @param dir the directory entry point ({@link File})
	 * @param handler {@link ProgressHandler} to show progression on UI
	 * @param suspicious_crc the list of suspicious crc, can be null which mean non suspicious crc checking
	 * @param options an {@link EnumSet} of {@link Options}
	 * @throws BreakException in case user stopped processing thru {@link ProgressHandler}
	 */
	private DirScan(final Session session, final File dir, final ProgressHandler handler, final Set<String> suspicious_crc, EnumSet<Options> options) throws BreakException
	{
		super(session);
		this.session = session;

		init7zJBinding();

		this.dir = dir;
		this.handler = handler;
		this.suspicious_crc = suspicious_crc;
		this.options = options;
		
		need_sha1_or_md5 = options.contains(Options.NEED_SHA1_OR_MD5) || options.contains(Options.NEED_SHA1) || options.contains(Options.NEED_MD5);
		md5_disks = options.contains(Options.MD5_DISKS) || options.contains(Options.NEED_MD5);
		md5_roms = options.contains(Options.MD5_ROMS) || options.contains(Options.NEED_MD5);
		sha1_disks = options.contains(Options.SHA1_DISKS) || options.contains(Options.NEED_SHA1);
		sha1_roms = options.contains(Options.SHA1_ROMS) || options.contains(Options.NEED_SHA1);
		final boolean is_dest = options.contains(Options.IS_DEST);
		final boolean recurse = options.contains(Options.RECURSE);
		final boolean use_parallelism = options.contains(Options.USE_PARALLELISM);
		final boolean format_tzip = options.contains(Options.FORMAT_TZIP);
		final boolean include_empty_dirs = options.contains(Options.EMPTY_DIRS);
		final boolean archives_and_chd_as_roms = options.contains(Options.ARCHIVES_AND_CHD_AS_ROMS);
		val nThreads = use_parallelism ? session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;
		
		final var path = Paths.get(dir.getAbsolutePath());

		/*
		 * Loading scan cache
		 */
		if(!session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.debug_nocache, false)) //$NON-NLS-1$
			containers_byname = load(dir);
		else
			containers_byname = Collections.synchronizedMap(new HashMap<>());

		/*
		 * Test if entry point is valid
		 */
		if(!Files.isDirectory(path))
			return;
		
		
		/*
		 * Initialize progression
		 */
		handler.clearInfos();
		handler.setInfos(nThreads,null);
		

		/*
		 * List files
		 * We go up to 100 subdirs for src dir but 1 level for dest dir type, and we follow links
		 */

		try(Stream<Path> stream = Files.walk(path, is_dest ? 1 : 100, FileVisitOption.FOLLOW_LINKS))
		{
			final var i = new AtomicInteger();
		//	handler.setProgress(String.format(Messages.getString("DirScan.ListingFiles"), getRelativePath(dir.toPath())), 0); //$NON-NLS-1$
		//	handler.setProgress2("", null); //$NON-NLS-1$
			handler.setProgress(null, -1);
			handler.setProgress2(String.format(Messages.getString("DirScan.ListingFiles"), getRelativePath(dir.toPath())), 0, 100); //$NON-NLS-1$
			
			new MultiThreading<Path>(nThreads, p -> {
				if(handler.isCancel())
					return;
				if(path.equals(p))
					return;
				Container c = null;
				final var file = p.toFile();
				try
				{
					final BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
					if(is_dest)
					{
						val type = attr.isRegularFile() ? Container.getType(file) : Type.DIR;
						val fname = type == Type.UNK ? (FilenameUtils.getBaseName(file.getName()) + Ext.FAKE) : file.getName();
						if(null == (c = containers_byname.get(fname)) /* new container */ || ((c.modified != attr.lastModifiedTime().toMillis() /* container date changed */ || (c instanceof Archive && c.size != attr.size()) /* container size changed */) && !c.up2date /* not up to date */))
						{
							if(attr.isRegularFile())
							{
								if (type != Container.Type.UNK)
									containers.add(c = new Archive(file, getRelativePath(file), attr));
								else
									containers.add(c = new FakeDirectory(file, getRelativePath(file), attr));
							}
							else
								containers.add(c = new Directory(file, getRelativePath(file), attr));
							containers_byname.put(fname, c);
							c.up2date = true;
						}
						else if(!c.up2date)
						{
							// container listed but did not change
							c.up2date = true;
							containers.add(c);
						}
					}
					else
					{
						/*
						 * With src type dir, we need to find each potential container end points while walking into the entire hierarchy   
						 */
						if(attr.isRegularFile()) // We test only regular files even for directory containers (must contains at least 1 file)
						{
							val type = Container.getType(file);
							if(type == Type.UNK || archives_and_chd_as_roms)	// maybe we did found a potential directory container with unknown type files inside)
							{
								if(path.equals(file.getParentFile().toPath()))	// skip if parent is the entry point
								{
									val fname = type == Type.UNK ? (FilenameUtils.getBaseName(file.getName()) + Ext.FAKE) : file.getName();
									if(null == (c = containers_byname.get(fname)) || (c.modified != attr.lastModifiedTime().toMillis() && !c.up2date))
									{
										containers.add(c = new FakeDirectory(file, getRelativePath(file), attr));
										containers_byname.put(fname, c);
										c.up2date = true;
									}
									else if(!c.up2date)
									{
										containers.add(c);
										c.up2date = true;
									}
								}
								else
								{
									final var parent_dir = file.getParentFile();
									final var parent_attr = Files.readAttributes(p.getParent(), BasicFileAttributes.class);
									final var relative  = path.relativize(p.getParent());
									if(null == (c = containers_byname.get(relative.toString())) || (c.modified != parent_attr.lastModifiedTime().toMillis() && !c.up2date))
									{
										containers.add(c = new Directory(parent_dir, getRelativePath(parent_dir), attr));
										c.up2date = true;
										containers_byname.put(relative.toString(), c);
									}
									else if(!c.up2date)
									{
										c.up2date = true;
										containers.add(c);
									}
								}
							}
							else	// otherwise it's an archive file
							{
								final var relative  = path.relativize(p);
								if(null == (c = containers_byname.get(relative.toString())) || ((c.modified != attr.lastModifiedTime().toMillis() || c.size != attr.size()) && !c.up2date))
								{
									containers.add(c = new Archive(file, getRelativePath(file), attr));
									c.up2date = true;
									containers_byname.put(relative.toString(), c);
								}
								else if(!c.up2date)
								{
									c.up2date = true;
									containers.add(c);
								}
							}
						}
						else if(include_empty_dirs)
						{
							try(DirectoryStream<Path> dirstream = Files.newDirectoryStream(p))
							{
								if(!dirstream.iterator().hasNext())
								{
									final var relative  = path.relativize(p);
									if(null == (c = containers_byname.get(relative.toString())) || (c.modified != attr.lastModifiedTime().toMillis() && !c.up2date))
									{
										containers.add(c = new Directory(file, getRelativePath(file), attr));
										c.up2date = true;
										containers_byname.put(relative.toString(), c);
									}
									else if(!c.up2date)
									{
										c.up2date = true;
										containers.add(c);
									}
								}
							}
						}
					}
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
			if(containers_byname.entrySet().removeIf(entry -> !entry.getValue().up2date))
				/*Log.info("Removed some scache elements")*/;
		}
		catch(final IOException e)
		{
			Log.err("IOException when listing", e); //$NON-NLS-1$
		}
		catch(final Exception e)
		{
			Log.err("Other Exception when listing", e); //$NON-NLS-1$
		}
		
		
		/*
		 * Initialize torrentzip if needed
		 */
		final TorrentZip torrentzip = (is_dest && format_tzip) ? new TorrentZip(new DummyLogCallback(), new SimpleTorrentZipOptions(false, true)) : null;
		
		/*
		 * Now read at least archives content, add eventually calculate checksum for each entries if needed
		 */
		final var i = new AtomicInteger(0);
		final var j = new AtomicInteger(0);
		final var max = new AtomicInteger(0);
		max.addAndGet(containers.size());
		containers.forEach(c->max.addAndGet((int)(c.size>>20)));
		handler.clearInfos();
		handler.setInfos(nThreads,true);
		handler.setProgress(String.format(Messages.getString("DirScan.ScanningFiles"), getRelativePath(dir.toPath())) , -1); //$NON-NLS-1$
		handler.setProgress2("", j.get(), max.get()); //$NON-NLS-1$
		new MultiThreading<Container>(nThreads, c -> {
			if(handler.isCancel())
				return;
			try
			{
				switch(c.getType())
				{
					case ZIP:
					{
						if(c.loaded < 1 || (need_sha1_or_md5 && c.loaded < 2))
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
									public FileVisitResult visitFile(final Path entry_path, final BasicFileAttributes attrs) throws IOException
									{
										update_entry(c.add(new Entry(entry_path.toString(),getRelativePath(entry_path).toString())), entry_path);
										return FileVisitResult.CONTINUE;
									}

									@Override
									public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
									{
										return FileVisitResult.CONTINUE;
									}
								});
								c.loaded = need_sha1_or_md5 ? 2 : 1;
							}
							catch (ZipError | IOException e)
							{
								System.err.println(c.getRelFile() + " : " + e.getMessage()); //$NON-NLS-1$
							}
						}
						else
						{
							for(final Entry entry : c.getEntries())
								update_entry(entry);
						}
						if(is_dest && format_tzip && c.lastTZipCheck < c.modified)
						{
							c.lastTZipStatus = torrentzip.Process(c.getFile());
							c.lastTZipCheck = System.currentTimeMillis();
						}
						break;
					}
					case RAR:
					case SEVENZIP:
					{
						try(final var entries = new SevenZUpdateEntries(c))
						{
							entries.updateEntries();
						}
						break;
					}
					case DIR:
					{
						if(c.loaded < 1 || (need_sha1_or_md5 && c.loaded < 2))
						{
							try
							{
								Files.walkFileTree(c.getFile().toPath(), EnumSet.noneOf(FileVisitOption.class), (is_dest&&recurse)?Integer.MAX_VALUE:1, new SimpleFileVisitor<Path>()
								{
									@Override
									public FileVisitResult visitFile(final Path entry_path, final BasicFileAttributes attrs) throws IOException
									{
										if(attrs.isRegularFile())
										{
											final var entry = new Entry(entry_path.toString(),getRelativePath(entry_path).toString(), attrs);
											if(archives_and_chd_as_roms)
												entry.type = Entry.Type.UNK;
											handler.setProgress(c.getFile().getName() , -1, null, File.separator+c.getFile().toPath().relativize(entry_path).toString()); //$NON-NLS-1$ //$NON-NLS-2$
											update_entry(c.add(entry), entry_path);
										}
										return FileVisitResult.CONTINUE;
									}
		
									@Override
									public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
									{
										return FileVisitResult.CONTINUE;
									}
								});
								c.loaded = need_sha1_or_md5 ? 2 : 1;
							}
							catch(AccessDeniedException e)
							{
								
							}
						}
						else
						{
							for(final Entry entry : c.getEntries())
								update_entry(entry);
						}
						break;
					}
					case FAKE:
					{
						if(c.loaded < 1 || (need_sha1_or_md5 && c.loaded < 2))
						{
							final var entry = new Entry(c.getFile().getName(),c.getRelFile().getName(), c.getSize(), c.getModified());
							if(archives_and_chd_as_roms)
								entry.type = Entry.Type.UNK;
							handler.setProgress(FilenameUtils.getBaseName(c.getFile().getName()) , -1, null, c.getFile().getName()); //$NON-NLS-1$ //$NON-NLS-2$
							update_entry(c.add(entry), c.getFile().toPath());
							c.loaded = need_sha1_or_md5 ? 2 : 1;
						}
						else
						{
							for(final Entry entry : c.getEntries())
								update_entry(entry);
						}
						break;
					}
					default:
						break;
				}
				handler.setProgress(String.format(Messages.getString("DirScan.Scanned"), c.getFile().getName())); //$NON-NLS-1$
				handler.setProgress2(String.format("%d/%d (%d%%)", i.incrementAndGet(), containers.size(), (int)(j.addAndGet(1+(int)(c.size>>20)) * 100.0 / max.get())), j.get()); //$NON-NLS-1$
			}
			catch(final IOException e)
			{
				c.loaded = 0;
				Log.err("IOException when scanning", e); //$NON-NLS-1$
			}
			catch(final BreakException e)
			{
				c.loaded = 0;
				handler.cancel();
			}
			catch(final Exception e)
			{
				c.loaded = 0;
				Log.err("Other Exception when listing", e); //$NON-NLS-1$
			}
			return;
		}).start(containers.stream().sorted(Container.rcomparator()));

		if(!handler.isCancel())
			save(dir);

	}

	/**
	 * Update SevenZip Entries
	 * @author optyfr
	 *
	 */
	private class SevenZUpdateEntries implements Closeable
	{
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

		/**
		 * The constructor
		 * @param container The {@link Container} from which to update entries
		 * @throws NoSuchAlgorithmException
		 */
		private SevenZUpdateEntries(final Container container) throws NoSuchAlgorithmException
		{
			this.container = container;
			algorithms = new ArrayList<>();
			if(sha1_roms)
				algorithms.add(Algo.SHA1); //$NON-NLS-1$
			if(md5_roms)
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
		private void updateEntries() throws IOException, NoSuchAlgorithmException
		{
			if(SevenZip.isInitializedSuccessfully())
			{
				final Map<Integer, Entry> entries = new HashMap<>();
				if(container.loaded < 1 || (need_sha1_or_md5 && container.loaded < 2))
				{
					for(final ISimpleInArchiveItem item : getNInterface().getArchiveItems())
					{
						if(item.isFolder())
							continue;
						updateEntry(container.add(new Entry(item.getPath(),null)), entries, item);

					}
					container.loaded = need_sha1_or_md5 ? 2 : 1;
				}
				else
				{
					for(final Entry entry : container.getEntries())
						updateEntry(entry, entries, null);
				}
				computeHashes(entries);
			}
			else	// in that case we support only sevenzip, not rar or whatever
			{
				final HashMap<String, Entry> entries = new HashMap<>();
				if(container.loaded < 1 || (need_sha1_or_md5 && container.loaded < 2))
				{
					for(final SevenZArchiveEntry archive_entry : getCArchive().getEntries())
					{
						if(archive_entry.isDirectory())
							continue;
						updateEntry(container.add(new Entry(archive_entry.getName(),null)), entries, archive_entry);
					}
					container.loaded = need_sha1_or_md5 ? 2 : 1;
				}
				else
				{
					for(final Entry entry : container.getEntries())
						updateEntry(entry, entries, (SevenZArchiveEntry) null);
				}
				computeHashes(entries);
			}
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
			if(entry.size == 0 && entry.crc == null && item != null)
			{
				entry.size = item.getSize();
				entry.crc = String.format("%08x", item.getCRC()); //$NON-NLS-1$
			}
			entries_bycrc.put(entry.crc + "." + entry.size, entry); //$NON-NLS-1$
			if(entry.sha1 == null && entry.md5 == null && (need_sha1_or_md5 || entry.crc == null || isSuspiciousCRC(entry.crc)))
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
			else
			{
				if(entry.sha1 != null)
					entries_bysha1.put(entry.sha1, entry);
				if(entry.md5 != null)
					entries_bymd5.put(entry.md5, entry);
			}
		}

		/**
		 * update an entry from a {@link SevenZArchiveEntry}
		 * @param entry the {@link Entry}
		 * @param entries the {@link HashSet} of {@link Entry}, their hash will be computed
		 * @param archive_entry the {@link SevenZArchiveEntry} in relation with {@link Entry}
		 * @throws IOException
		 */
		private void updateEntry(final Entry entry, final Map<String,Entry> entries, final SevenZArchiveEntry archive_entry) throws IOException
		{
			if(entry.size == 0 && entry.crc == null && archive_entry != null)
			{
				entry.size = archive_entry.getSize();
				entry.crc = String.format("%08x", archive_entry.getCrcValue()); //$NON-NLS-1$
			}
			entries_bycrc.put(entry.crc + "." + entry.size, entry); //$NON-NLS-1$
			if(entry.sha1 == null && entry.md5 == null && (need_sha1_or_md5 || entry.crc == null || isSuspiciousCRC(entry.crc)))
			{
				entries.put(entry.getFile(), entry);
			}
			else
			{
				if(entry.sha1 != null)
					entries_bysha1.put(entry.sha1, entry);
				if(entry.md5 != null)
					entries_bymd5.put(entry.md5, entry);
			}
			
		}

		/**
		 * Extract and compute Hashes from items in a SevenzipJBinding archive
		 * @param entries the {@link Map} of {@link Entry} by {@link Integer} item index in archive
		 * @throws NoSuchAlgorithmException
		 * @throws IOException
		 */
		private void computeHashes(final Map<Integer, Entry> entries) throws NoSuchAlgorithmException, IOException
		{
			if(entries.size() > 0)
			{
				getArchive().getNative7Zip().getIInArchive().extract(IntStreamEx.of(entries.keySet()).toArray(), false, new IArchiveExtractCallback()
				{
					Entry entry;

					@Override
					public void setTotal(final long total) throws SevenZipException
					{
					}

					@Override
					public void setCompleted(final long complete) throws SevenZipException
					{
					}

					@Override
					public void setOperationResult(final ExtractOperationResult extractOperationResult) throws SevenZipException
					{
						if(extractOperationResult == ExtractOperationResult.OK)
						{
							for(final MDigest d : digest)
							{
								if(d.getAlgorithm()==Algo.SHA1) //$NON-NLS-1$
									entries_bysha1.put(entry.sha1 = d.toString(), entry);
								if(d.getAlgorithm()==Algo.MD5) //$NON-NLS-1$
									entries_bymd5.put(entry.md5 = d.toString(), entry);
								d.reset();
							}
						}
					}

					@Override
					public void prepareOperation(final ExtractAskMode extractAskMode) throws SevenZipException
					{
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
				});
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
					long size = entry7z.getSize();
					final var buffer = new byte[8192];
					while (size > 0)
					{
						int read = getCArchive().read(buffer, 0, (int) Math.min((long) buffer.length, size));
						if(read == -1)
							break;
						for (MDigest d : digest)
							d.update(buffer, 0, read);
						size -= read;
					}
					for (MDigest d : digest)
					{
						if (d.getAlgorithm()==Algo.SHA1) //$NON-NLS-1$
							entries_bysha1.put(entry.sha1 = d.toString(), entry);
						if (d.getAlgorithm()==Algo.MD5) //$NON-NLS-1$
							entries_bymd5.put(entry.md5 = d.toString(), entry);
						d.reset();
					}
				}
			}
		}
	}

	/**
	 * Update an entry (zip or dir), FS and Path will be retrieved from {@link Entry#parent}
	 * @param entry the {@link Entry} to update
	 * @throws IOException
	 */
	private void update_entry(final Entry entry) throws IOException
	{
		update_entry(entry, (Path) null);
	}

	/**
	 * Update an entry (zip or dir)
	 * @param entry the {@link Entry} to update
	 * @param entry_path the {@link Path} corresponding to the entry (can be null, it will then be retrieved from {@link Entry#parent}
	 * @throws IOException
	 */
	private void update_entry(final Entry entry, final Path entry_path) throws IOException
	{
		if(entry.parent.getType() == Type.ZIP)
		{
			if(entry.size == 0 && entry.crc == null)
			{
				var path = entry_path;
				if(entry_path == null)
					path = getPath(entry);
				final Map<String, Object> entry_zip_attrs = Files.readAttributes(path, "zip:*"); //$NON-NLS-1$
				entry.size = (Long) entry_zip_attrs.get("size"); //$NON-NLS-1$
				entry.crc = String.format("%08x", entry_zip_attrs.get("crc")); //$NON-NLS-1$ //$NON-NLS-2$
				if(entry_path == null)
					path.getFileSystem().close();
			}
			entries_bycrc.put(entry.crc + "." + entry.size, entry); //$NON-NLS-1$
		}
		if(entry.type == Entry.Type.CHD && entry.sha1 == null && entry.md5 == null)
		{

				var path = entry_path;
				if(entry_path == null)
					path = getPath(entry);
				final var chd_info = new CHDInfoReader(path.toFile());
				if(sha1_disks)
					if(null != (entry.sha1 = chd_info.getSHA1()))
						entries_bysha1.put(entry.sha1, entry);
				if(md5_disks)
					if(null != (entry.md5 = chd_info.getMD5()))
						entries_bymd5.put(entry.md5, entry);
				if(entry_path == null)
					path.getFileSystem().close();
		}
		else if(entry.type != Entry.Type.CHD && (need_sha1_or_md5 || entry.crc == null || isSuspiciousCRC(entry.crc)))
		{
			List<Algo> algorithms = new ArrayList<>();
			if(entry.crc==null)
				algorithms.add(Algo.CRC32); //$NON-NLS-1$
			if(entry.md5 == null && (md5_roms || need_sha1_or_md5))
				algorithms.add(Algo.MD5); //$NON-NLS-1$
			if(entry.sha1 == null && (sha1_roms || need_sha1_or_md5))
				algorithms.add(Algo.SHA1); //$NON-NLS-1$
			if(!algorithms.isEmpty()) try
			{
				var path = entry_path;
				if(entry_path == null)
					path = getPath(entry);
				MDigest[] digests = computeHash(path, algorithms);
				for(MDigest md : digests)
				{
					switch (md.getAlgorithm())
					{
						case CRC32: //$NON-NLS-1$
							entry.crc = md.toString();
							break;
						case MD5: //$NON-NLS-1$
							entry.md5 = md.toString();
							break;
						case SHA1: //$NON-NLS-1$
							entry.sha1 = md.toString();
							break;
					}
				}
				if(entry_path == null)
					path.getFileSystem().close();
			}
			catch (NoSuchAlgorithmException e)
			{
				Log.err(e.getMessage(),e);
			}
			if(entry.crc != null)
				entries_bycrc.put(entry.crc + "." + entry.size, entry); //$NON-NLS-1$
			if(entry.sha1 != null)
				entries_bysha1.put(entry.sha1, entry);
			if(entry.md5 != null)
				entries_bymd5.put(entry.md5, entry);
		}
		else
		{
			if(entry.crc != null)
				entries_bycrc.put(entry.crc + "." + entry.size, entry); //$NON-NLS-1$
			if(entry.sha1 != null)
				entries_bysha1.put(entry.sha1, entry);
			if(entry.md5 != null)
				entries_bymd5.put(entry.md5, entry);
		}
	}

	private MDigest[] computeHash(final Path entry_path, final List<Algo> algorithm) throws NoSuchAlgorithmException
	{
		return computeHash(entry_path, algorithm.toArray(new Algo[0]));
	}
	
	private MDigest[] computeHash(final Path entry_path, final Algo[] algorithm) throws NoSuchAlgorithmException
	{
		var md = new MDigest[algorithm.length];
		for(var i = 0; i < algorithm.length; i++)
			md[i] = MDigest.getAlgorithm(algorithm[i]);
		try
		{
			MDigest.computeHash(Files.newInputStream(entry_path), md);
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
		try(final var srcfs = FileSystems.newFileSystem(entry.parent.getFile().toPath(), (ClassLoader)null))
		{
			return srcfs.getPath(entry.getFile());
		}
	}

	/**
	 * Find an {@link Entry} by a {@link Rom} hash
	 * @param r the {@link Rom} from which to find {@link Entry} 
	 * @return {@link Entry} or null if not found
	 */
	Entry find_byhash(final Rom r)
	{
		Entry entry = null;
		if(r.sha1 != null)
		{
			if(null != (entry = entries_bysha1.get(r.sha1)))
				return entry;
			if(isSuspiciousCRC(r.crc))
				return null;
		}
		if(r.md5 != null)
		{
			if(null != (entry = entries_bymd5.get(r.md5)))
				return entry;
			if(isSuspiciousCRC(r.crc))
				return null;
		}
		return entries_bycrc.get(r.crc + "." + r.size); //$NON-NLS-1$
	}

	/**
	 * Find an {@link Entry} by a {@link Disk} hash
	 * @param d the {@link Disk} from which to find {@link Entry}
	 * @return {@link Entry} or null if not found
	 */
	Entry find_byhash(final Disk d)
	{
		Entry entry = null;
		if(d.sha1 != null)
			if(null != (entry = entries_bysha1.get(d.sha1)))
				return entry;
		return entries_bymd5.get(d.md5);
	}

	private static String getCacheExt(Set<Options> options)
	{
		if(options.contains(Options.IS_DEST))
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
	private void save(final File file)
	{
		try(final var oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(session, file, options)))))
		{
			oos.writeObject(containers_byname);
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
	private Map<String, Container> load(final File file)
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
	 * get a {@link Container} by its name from {@link #containers_byname}
	 * @param name the name String of a container
	 * @return the {@link Container} or null
	 */
	Container getContainerByName(String name)
	{
		return containers_byname.get(name);
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
