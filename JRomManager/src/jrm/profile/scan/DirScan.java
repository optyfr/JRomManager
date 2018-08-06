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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystem;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;
import java.util.zip.ZipError;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import JTrrntzip.DummyLogCallback;
import JTrrntzip.SimpleTorrentZipOptions;
import JTrrntzip.TorrentZip;
import jrm.Messages;
import jrm.compressors.SevenZipArchive;
import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.io.CHDInfoReader;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profile.Profile;
import jrm.profile.data.Archive;
import jrm.profile.data.Container;
import jrm.profile.data.Container.Type;
import jrm.profile.data.Directory;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;
import jrm.profile.data.Rom;
import jrm.profile.scan.options.FormatOptions;
import jrm.ui.ProgressHandler;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

/**
 * Directories/Archives scanner
 * @author optyfr
 *
 */
public final class DirScan
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
	 * do we use parallelism (multithreading)
	 */
	private final boolean use_parallelism;
	/**
	 * The destination format
	 */
	private final FormatOptions format;

	/**
	 * the directory entry point
	 */
	private final File dir;
	/**
	 * is this a scan for a destination folder or a source folder (destination scans are optimized)
	 */
	private final boolean is_dest;
	/**
	 * {@link ProgressHandler} to show progression on UI
	 */
	private final ProgressHandler handler;
	/**
	 * The used {@link Profile}
	 */
	private final Profile profile;

	/**
	 * Initialization for SevenzipJBinding
	 */
	private void init7zJBinding()
	{
		if(!SevenZip.isInitializedSuccessfully())
		{
			try
			{
				SevenZip.initSevenZipFromPlatformJAR(Settings.getTmpPath(true).toFile());
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * The constructor<br>
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
		init7zJBinding();

		this.profile = profile;
		this.dir = dir;
		this.handler = handler;
		this.is_dest = is_dest;
		
		/*
		 * Profile options
		 */
		need_sha1_or_md5 = profile.getProperty("need_sha1_or_md5", false); //$NON-NLS-1$
		use_parallelism = profile.getProperty("use_parallelism", false); //$NON-NLS-1$
		format = FormatOptions.valueOf(profile.getProperty("format", FormatOptions.ZIP.toString())); //$NON-NLS-1$

		final Path path = Paths.get(dir.getAbsolutePath());

		/*
		 * Loading scan cache
		 */
		if(!Settings.getProperty("debug_nocache", false)) //$NON-NLS-1$
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
		handler.setInfos(Runtime.getRuntime().availableProcessors(),false);
		

		/*
		 * List files;
		 * We go up to 100 subdirs for src dir but 1 level for dest dir type, and we follow links
		 */

		try(Stream<Path> stream = Files.walk(path, is_dest ? 1 : 100, FileVisitOption.FOLLOW_LINKS))
		{
			final AtomicInteger i = new AtomicInteger();
			handler.setProgress(String.format(Messages.getString("DirScan.ListingFiles"), dir), 0); //$NON-NLS-1$
			StreamEx.of(StreamSupport.stream(stream.spliterator(), use_parallelism)).unordered().takeWhile((p) -> !handler.isCancel()).forEach(p -> {
				Container c = null;
				if(path.equals(p))
					return;
				final File file = p.toFile();
				try
				{
					final BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
					if(is_dest)
					{
						if(null == (c = containers_byname.get(file.getName())) /* new container */ || ((c.modified != attr.lastModifiedTime().toMillis() /* container date changed */ || (c instanceof Archive && c.size != attr.size()) /* container size changed */) && !c.up2date /* not up to date */))
						{
							if(attr.isRegularFile())
								containers.add(c = new Archive(file, attr));
							else
								containers.add(c = new Directory(file, attr));
							if(c != null)
							{
								// container listed
								c.up2date = true;
								containers_byname.put(file.getName(), c);
							}
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
							if(Container.getType(file) == Type.UNK)	// maybe we did found a potential directory container with unknown type files inside)
							{
								if(path.equals(file.getParentFile().toPath()))	// skip if parent is the entry point
									return;
								final File parent_dir = file.getParentFile();
								final BasicFileAttributes parent_attr = Files.readAttributes(p.getParent(), BasicFileAttributes.class);
								final Path relative  = path.relativize(p.getParent());
								if(null == (c = containers_byname.get(relative.toString())) || (c.modified != parent_attr.lastModifiedTime().toMillis() && !c.up2date))
								{
									containers.add(c = new Directory(parent_dir, attr));
									if(c != null)
									{
										c.up2date = true;
										containers_byname.put(relative.toString(), c);
									}
								}
								else if(!c.up2date)
								{
									c.up2date = true;
									containers.add(c);
								}
							}
							else	// otherwise it's an archive file
							{
								final Path relative  = path.relativize(p);
								if(null == (c = containers_byname.get(relative.toString())) || ((c.modified != attr.lastModifiedTime().toMillis() || c.size != attr.size()) && !c.up2date))
								{
									containers.add(c = new Archive(file, attr));
									if(c != null)
									{
										c.up2date = true;
										containers_byname.put(relative.toString(), c);
									}
								}
								else if(!c.up2date)
								{
									c.up2date = true;
									containers.add(c);
								}
							}
						}
					}
					handler.setProgress(String.format(Messages.getString("DirScan.ListingFiles2"), dir, i.incrementAndGet()) ); //$NON-NLS-1$
				}
				catch(final IOException e)
				{
					e.printStackTrace();
				}

			});
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
		catch(final Throwable e)
		{
			Log.err("Other Exception when listing", e); //$NON-NLS-1$
		}
		
		
		/*
		 * Initialize torrentzip if needed
		 */
		final TorrentZip torrentzip = (is_dest && format==FormatOptions.TZIP) ? new TorrentZip(new DummyLogCallback(), new SimpleTorrentZipOptions(false, true)) : null;
		
		/*
		 * Now read at least archives content, add eventually calculate checksum for each entries if needed
		 */
		final AtomicInteger i = new AtomicInteger(0);
		handler.setProgress(String.format(Messages.getString("DirScan.ScanningFiles"), dir) , i.get(), containers.size()); //$NON-NLS-1$
		StreamEx.of(use_parallelism ? containers.parallelStream().unordered() : containers.stream()).takeWhile((c) -> !handler.isCancel()).forEach(c -> {
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
							try(FileSystem fs = new ZipFileSystemProvider().newFileSystem(URI.create("zip:" + c.file.toURI()), env);) //$NON-NLS-1$
							{
								final Path root = fs.getPath("/"); //$NON-NLS-1$
								Files.walkFileTree(root, new SimpleFileVisitor<Path>()
								{
									@Override
									public FileVisitResult visitFile(final Path entry_path, final BasicFileAttributes attrs) throws IOException
									{
										update_entry(c.add(new Entry(entry_path.toString())), entry_path);
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
							catch (ZipError e) {
								System.err.println(c.file+" : "+e.getMessage());
							}
						}
						else
						{
							for(final Entry entry : c.getEntries())
								update_entry(entry);
						}
						if(is_dest && format==FormatOptions.TZIP && c.lastTZipCheck < c.modified)
						{
							c.lastTZipStatus = torrentzip.Process(c.file);
							c.lastTZipCheck = System.currentTimeMillis();
						}
						break;
					}
					case SEVENZIP:
					{
						try(SevenZUpdateEntries entries = new SevenZUpdateEntries(c))
						{
							entries.updateEntries();
						}
						break;
					}
					case DIR:
					{
						try
						{
							Files.walkFileTree(c.file.toPath(), new SimpleFileVisitor<Path>()
							{
								@Override
								public FileVisitResult visitFile(final Path entry_path, final BasicFileAttributes attrs) throws IOException
								{
									update_entry(c.add(new Entry(entry_path.toString(), attrs)), entry_path);
									return FileVisitResult.CONTINUE;
								}
	
								@Override
								public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException
								{
									return FileVisitResult.CONTINUE;
								}
							});
						}
						catch(AccessDeniedException e)
						{
							
						}
						c.loaded = need_sha1_or_md5 ? 2 : 1;
						break;
					}
					default:
						break;
				}
				handler.setProgress(String.format(Messages.getString("DirScan.Scanned"), c.file.getName()) , i.incrementAndGet(), null, String.format("%d/%d (%d%%)", i.get(), containers.size(), (int)(i.get() * 100.0 / containers.size()))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch(final IOException e)
			{
				Log.err("IOException when scanning", e); //$NON-NLS-1$
			}
			catch(final BreakException e)
			{
				handler.cancel();
			}
			catch(final Throwable e)
			{
				Log.err("Other Exception when listing", e); //$NON-NLS-1$
			}
		});

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
		 * The {@link RandomAccessFile} used to read 7z file
		 */
		private RandomAccessFile randomAccessFile = null;
		/**
		 * The container to read
		 */
		private final Container container;
		/**
		 * the algorithms requested for calculating message digest
		 */
		private final ArrayList<String> algorithms;
		/**
		 * The {@link MessageDigest} array (array size is equals to {@link #algorithms} size)
		 */
		private final MessageDigest[] digest;
		/**
		 * the interface to archive in case of sevenzipjbinding usage
		 */
		private IInArchive nArchive = null;
		/**
		 * the class to archive in case of org.apache.commons.compress usage
		 */
		private SevenZFile jArchive = null;
		/**
		 * the class to archive in case of external 7z cmd usage 
		 */
		private SevenZipArchive jArchive2 = null;

		/**
		 * The constructor
		 * @param container The {@link Container} from which to update entries
		 * @throws NoSuchAlgorithmException
		 */
		private SevenZUpdateEntries(final Container container) throws NoSuchAlgorithmException
		{
			this.container = container;
			algorithms = new ArrayList<>();
			if(profile.sha1_roms)
				algorithms.add("SHA-1"); //$NON-NLS-1$
			if(profile.md5_roms)
				algorithms.add("MD5"); //$NON-NLS-1$
			digest = new MessageDigest[algorithms.size()];
			for(int i = 0; i < algorithms.size(); i++)
				digest[i] = MessageDigest.getInstance(algorithms.get(i));
		}

		@Override
		public void close() throws IOException
		{
			if(jArchive2 != null)
				jArchive2.close();
			if(jArchive != null)
				jArchive.close();
			if(nArchive != null)
				nArchive.close();
			if(randomAccessFile != null)
				randomAccessFile.close();
		}

		/**
		 * get {@link IInArchive} interface from {@link Container#file}
		 * @return an {@link IInArchive}
		 * @throws IOException
		 */
		private IInArchive getNArchive() throws IOException
		{
			if(randomAccessFile == null)
				randomAccessFile = new RandomAccessFile(container.file, "r"); //$NON-NLS-1$
			if(nArchive == null)
				nArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
			return nArchive;
		}

		/**
		 * get {@link SevenZFile} from  {@link Container#file}
		 * @return {@link SevenZFile}
		 * @throws IOException
		 */
		private SevenZFile getJArchive() throws IOException
		{
			if(jArchive == null)
				jArchive = new SevenZFile(container.file);
			return jArchive;
		}

		/**
		 * get simplified interface from {@link IInArchive} 
		 * @return {@link ISimpleInArchive}
		 * @throws IOException
		 */
		private ISimpleInArchive getNInterface() throws IOException
		{
			return getNArchive().getSimpleInterface();
		}

		/**
		 * get {@link SevenZipArchive} from  {@link Container#file}
		 * @return {@link SevenZipArchive}
		 * @throws IOException
		 */
		private SevenZipArchive getJInterface() throws IOException
		{
			if(jArchive2 == null)
				jArchive2 = new SevenZipArchive(container.file);
			return jArchive2;
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
						updateEntry(container.add(new Entry(item.getPath())), entries, item);

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
			else
			{
				final HashSet<Entry> entries = new HashSet<>();
				if(container.loaded < 1 || (need_sha1_or_md5 && container.loaded < 2))
				{
					for(final SevenZArchiveEntry archive_entry : getJArchive().getEntries())
					{
						if(archive_entry.isDirectory())
							continue;
						updateEntry(container.add(new Entry(archive_entry.getName())), entries, archive_entry);
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
			if(entry.sha1 == null && entry.md5 == null && (need_sha1_or_md5 || entry.crc == null || profile.suspicious_crc.contains(entry.crc)))
			{
				if(item == null)
				{
					for(final ISimpleInArchiveItem itm : getNInterface().getArchiveItems())
					{
						if(entry.file.equals(itm.getPath()))
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
		private void updateEntry(final Entry entry, final HashSet<Entry> entries, final SevenZArchiveEntry archive_entry) throws IOException
		{
			if(entry.size == 0 && entry.crc == null && archive_entry != null)
			{
				entry.size = archive_entry.getSize();
				entry.crc = String.format("%08x", archive_entry.getCrcValue()); //$NON-NLS-1$
			}
			entries_bycrc.put(entry.crc + "." + entry.size, entry); //$NON-NLS-1$
			if(entry.sha1 == null && entry.md5 == null && (need_sha1_or_md5 || entry.crc == null || profile.suspicious_crc.contains(entry.crc)))
			{
				entries.add(entry);
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
				getNArchive().extract(IntStreamEx.of(entries.keySet()).toArray(), false, new IArchiveExtractCallback()
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
							for(final MessageDigest d : digest)
							{
								if(d.getAlgorithm().equals("SHA-1")) //$NON-NLS-1$
									entries_bysha1.put(entry.sha1 = Hex.encodeHexString(d.digest()), entry);
								if(d.getAlgorithm().equals("MD5")) //$NON-NLS-1$
									entries_bymd5.put(entry.md5 = Hex.encodeHexString(d.digest()), entry);
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
							for(final MessageDigest d : digest)
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
		private void computeHashes(final HashSet<Entry> entries) throws IOException
		{
			for(final Entry entry : entries)
			{
				for(final MessageDigest d : computeHash(getJInterface().extract_stdout(entry.getName())))
				{
					if(d.getAlgorithm().equals("SHA-1")) //$NON-NLS-1$
						entries_bysha1.put(entry.sha1 = Hex.encodeHexString(d.digest()), entry);
					if(d.getAlgorithm().equals("MD5")) //$NON-NLS-1$
						entries_bymd5.put(entry.md5 = Hex.encodeHexString(d.digest()), entry);
					d.reset();
				}
			}

		}

		/**
		 * Compute hash from an {@link InputStream}
		 * @param is the {@link InputStream} to read
		 * @return an array of {@link MessageDigest}
		 */
		private MessageDigest[] computeHash(final InputStream is)
		{
			try(final BufferedInputStream bis = new BufferedInputStream(is))
			{
				final byte[] buffer = new byte[8192];
				int len = is.read(buffer);
				while(len != -1)
				{
					for(final MessageDigest d : digest)
						d.update(buffer, 0, len);
					len = is.read(buffer);
				}
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
			return digest;
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
				Path path = entry_path;
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
		if(entry.sha1 == null && entry.md5 == null && (need_sha1_or_md5 || entry.crc == null || profile.suspicious_crc.contains(entry.crc)))
		{
			if(entry.type == Entry.Type.CHD)
			{
				if(profile.sha1_disks || profile.md5_disks)
				{
					Path path = entry_path;
					if(entry_path == null)
						path = getPath(entry);
					final CHDInfoReader chd_info = new CHDInfoReader(path.toFile());
					if(profile.sha1_disks)
						if(null != (entry.sha1 = chd_info.getSHA1()))
							entries_bysha1.put(entry.sha1, entry);
					if(profile.md5_disks)
						if(null != (entry.md5 = chd_info.getMD5()))
							entries_bymd5.put(entry.md5, entry);
					if(entry_path == null)
						path.getFileSystem().close();
				}
			}
			else
			{
				if(profile.sha1_roms || profile.md5_roms)
				{
					Path path = entry_path;
					if(entry_path == null)
						path = getPath(entry);
					if(profile.sha1_roms)
						if(null != (entry.sha1 = computeSHA1(path)))
							entries_bysha1.put(entry.sha1, entry);
					if(profile.md5_roms)
						if(null != (entry.md5 = computeMD5(path)))
							entries_bymd5.put(entry.md5, entry);
					if(entry_path == null)
						path.getFileSystem().close();
				}
			}
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
	 * Compute SHA-1 from entry {@link Path}
	 * @param entry_path the {@link Path} corresponding to the entry (can be null, it will then be retrieved from {@link Entry#parent}
	 * @return
	 */
	private String computeSHA1(final Path entry_path)
	{
		return computeHash(entry_path, "SHA-1"); //$NON-NLS-1$
	}

	/**
	 * Compute MD5 from entry {@link Path}
	 * @param entry_path the {@link Path} corresponding to the entry (can be null, it will then be retrieved from {@link Entry#parent}
	 * @return
	 */
	private String computeMD5(final Path entry_path)
	{
		return computeHash(entry_path, "MD5"); //$NON-NLS-1$
	}

	/**
	 * Compute Hash from entry {@link Path}
	 * @param entry_path the {@link Path} corresponding to the entry (can be null, it will then be retrieved from {@link Entry#parent}
	 * @param algorithm the desired hash algorithm
	 * @return
	 */
	private String computeHash(final Path entry_path, final String algorithm)
	{
		try(final InputStream is = new BufferedInputStream(Files.newInputStream(entry_path), 8192))
		{
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			final byte[] buffer = new byte[8192];
			int len = is.read(buffer);
			while(len != -1)
			{
				digest.update(buffer, 0, len);
				len = is.read(buffer);
			}
			return Hex.encodeHexString(digest.digest());
		}
		catch(final Exception e)
		{
			System.err.println(entry_path);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * get {@link Path} from {@link Entry} using FS deduced from {@link Entry#parent}
	 * @param entry the {@link Entry} to retrieve {@link Path}
	 * @return a {@link Path}
	 * @throws IOException
	 */
	private Path getPath(final Entry entry) throws IOException
	{
		final FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);
		return srcfs.getPath(entry.file);
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
			if(profile.suspicious_crc.contains(r.crc))
				return null;
		}
		if(r.md5 != null)
		{
			if(null != (entry = entries_bymd5.get(r.md5)))
				return entry;
			if(profile.suspicious_crc.contains(r.crc))
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

	/**
	 * get the scan cache File
	 * @param file the root dir {@link File} of the scan
	 * @return a {@link File} corresponding to the cache file
	 */
	private File getCacheFile(final File file)
	{
		final File workdir = Settings.getWorkPath().toFile(); //$NON-NLS-1$
		final File cachedir = new File(workdir, "cache"); //$NON-NLS-1$
		cachedir.mkdirs();
		final CRC32 crc = new CRC32();
		crc.update(file.getAbsolutePath().getBytes());
		return new File(cachedir, String.format("%08x", crc.getValue()) + (is_dest?".dcache":".scache")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Save the cache file
	 * @param file the root dir {@link File} of the scan
	 */
	private void save(final File file)
	{
		try(final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(file)))))
		{
			oos.writeObject(containers_byname);
		}
		catch(final Throwable e)
		{
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
		final File cachefile = getCacheFile(file);
		try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
		{
			handler.setProgress(String.format(Messages.getString("DirScan.LoadingScanCache"), file) , 0); //$NON-NLS-1$
			return (Map<String, Container>) ois.readObject();
		}
		catch(final Throwable e)
		{
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
