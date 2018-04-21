package jrm.profile.scan;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import jrm.Messages;
import jrm.compressors.SevenZipArchive;
import jrm.io.CHDInfoReader;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profile.Profile;
import jrm.profile.data.*;
import jrm.profile.data.Container.Type;
import jrm.ui.ProgressHandler;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import one.util.streamex.IntStreamEx;
import one.util.streamex.StreamEx;

public final class DirScan
{
	List<Container> containers = Collections.synchronizedList(new ArrayList<>());
	Map<String, Container> containers_byname = Collections.synchronizedMap(new HashMap<>());
	Map<String, Entry> entries_bycrc = Collections.synchronizedMap(new HashMap<>());
	Map<String, Entry> entries_bysha1 = Collections.synchronizedMap(new HashMap<>());
	Map<String, Entry> entries_bymd5 = Collections.synchronizedMap(new HashMap<>());

	boolean need_sha1_or_md5 = true;
	boolean use_parallelism = true;
	
	File dir;

	private DirScan()
	{
		if(!SevenZip.isInitializedSuccessfully())
		{
			try
			{
				SevenZip.initSevenZipFromPlatformJAR();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public DirScan(Profile profile, File dir, ProgressHandler handler, boolean is_dest) throws BreakException
	{
		this();
		
		this.dir = dir;
		this.need_sha1_or_md5 = profile.getProperty("need_sha1_or_md5", false); //$NON-NLS-1$
		this.use_parallelism = profile.getProperty("use_parallelism", false); //$NON-NLS-1$

		Path path = Paths.get(dir.getAbsolutePath());

		/*
		 * Loading scan cache
		 */
		if(!Settings.getProperty("debug_nocache", false)) //$NON-NLS-1$
			containers_byname = load(dir, handler);

		/*
		 * List files;
		 */

		try(Stream<Path> stream = Files.walk(path, is_dest ? 1 : 100, FileVisitOption.FOLLOW_LINKS))
		{
			AtomicInteger i = new AtomicInteger();
			handler.setProgress(String.format(Messages.getString("DirScan.ListingFiles"), dir), 0); //$NON-NLS-1$
			StreamEx.of(StreamSupport.stream(stream.spliterator(), use_parallelism)).unordered().takeWhile((p) -> !handler.isCancel()).forEach(p -> {
				Container c = null;
				if(path.equals(p))
					return;
				File file = p.toFile();
				try
				{
					BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
					if(is_dest)
					{
						if(null == (c = containers_byname.get(file.getName())) || ((c.modified != attr.lastModifiedTime().toMillis() || (c instanceof Archive && c.size != attr.size())) && !c.up2date))
						{
							if(attr.isRegularFile())
								containers.add(c = new Archive(file, attr));
							else
								containers.add(c = new Directory(file, attr));
							if(c != null)
							{
								c.up2date = true;
								containers_byname.put(file.getName(), c);
							}
						}
						else if(!c.up2date)
						{
							c.up2date = true;
							containers.add(c);
						}
					}
					else
					{
						if(attr.isRegularFile())
						{
							if(Container.getType(file) == Type.UNK)
							{
								if(path.equals(file.getParentFile().toPath()))
									return;
								File parent_dir = file.getParentFile();
								BasicFileAttributes parent_attr = Files.readAttributes(p.getParent(), BasicFileAttributes.class);
								Path relative  = path.relativize(p.getParent());
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
							else
							{
								Path relative  = path.relativize(p);
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
				catch(IOException e)
				{
					e.printStackTrace();
				}

			});
			if(containers_byname.entrySet().removeIf(entry -> !entry.getValue().up2date))
				Log.info("Removed some scache elements"); //$NON-NLS-1$
		}
		catch(IOException e)
		{
			Log.err("IOException when listing", e); //$NON-NLS-1$
		}
		catch(Throwable e)
		{
			Log.err("Other Exception when listing", e); //$NON-NLS-1$
		}
		AtomicInteger i = new AtomicInteger(0);
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
							Map<String, Object> env = new HashMap<>();
							env.put("useTempFile", Boolean.TRUE); //$NON-NLS-1$
							try(FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + c.file.toURI()), env);) //$NON-NLS-1$
							{
								final Path root = fs.getPath("/"); //$NON-NLS-1$
								Files.walkFileTree(root, new SimpleFileVisitor<Path>()
								{
									@Override
									public FileVisitResult visitFile(Path entry_path, BasicFileAttributes attrs) throws IOException
									{
										update_entry(profile, c.add(new Entry(entry_path.toString())), entry_path);
										return FileVisitResult.CONTINUE;
									}

									@Override
									public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
									{
										return FileVisitResult.CONTINUE;
									}
								});
								c.loaded = need_sha1_or_md5 ? 2 : 1;
							}
						}
						else
						{
							for(Entry entry : c.getEntries())
								update_entry(profile, entry);
						}
						break;
					}
					case SEVENZIP:
					{
						try(SevenZUpdateEntries entries = new SevenZUpdateEntries(profile, c))
						{
							entries.updateEntries();
						}
						break;
					}
					case DIR:
					{
						Files.walkFileTree(c.file.toPath(), new SimpleFileVisitor<Path>()
						{
							@Override
							public FileVisitResult visitFile(Path entry_path, BasicFileAttributes attrs) throws IOException
							{
								update_entry(profile, c.add(new Entry(entry_path.toString(), attrs)), entry_path);
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
							{
								return FileVisitResult.CONTINUE;
							}
						});
						c.loaded = need_sha1_or_md5 ? 2 : 1;
						break;
					}
					default:
						break;
				}
				handler.setProgress(String.format(Messages.getString("DirScan.Scanned"), c.file.getName()) , i.incrementAndGet(), null, String.format("%d/%d (%d%%)", i.get(), containers.size(), (int)(i.get() * 100.0 / containers.size()))); //$NON-NLS-1$ //$NON-NLS-2$
			}
			catch(IOException e)
			{
				Log.err("IOException when scanning", e); //$NON-NLS-1$
			}
			catch(BreakException e)
			{
				handler.cancel();
			}
			catch(Throwable e)
			{
				Log.err("Other Exception when listing", e); //$NON-NLS-1$
			}
		});

		if(!handler.isCancel())
			save(dir, containers_byname);

	}

	class SevenZUpdateEntries implements Closeable
	{
		RandomAccessFile randomAccessFile = null;
		Profile profile;
		Container container;
		ArrayList<String> algorithms;
		MessageDigest[] digest;
		IInArchive nArchive = null;
		SevenZFile jArchive = null;
		SevenZipArchive jArchive2 = null;

		public SevenZUpdateEntries(Profile profile, Container container) throws NoSuchAlgorithmException
		{
			this.profile = profile;
			this.container = container;
			this.algorithms = new ArrayList<>();
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

		private IInArchive getNArchive() throws IOException
		{
			if(randomAccessFile == null)
				randomAccessFile = new RandomAccessFile(container.file, "r"); //$NON-NLS-1$
			if(nArchive == null)
				nArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile));
			return nArchive;
		}

		private SevenZFile getJArchive() throws IOException
		{
			if(jArchive == null)
				jArchive = new SevenZFile(container.file);
			return jArchive;
		}

		private ISimpleInArchive getNInterface() throws IOException
		{
			return getNArchive().getSimpleInterface();
		}

		private SevenZipArchive getJInterface() throws IOException
		{
			if(jArchive2 == null)
				jArchive2 = new SevenZipArchive(container.file);
			return jArchive2;
		}

		public void updateEntries() throws IOException, NoSuchAlgorithmException
		{
			if(SevenZip.isInitializedSuccessfully())
			{
				Map<Integer, Entry> entries = new HashMap<>();
				if(container.loaded < 1 || (need_sha1_or_md5 && container.loaded < 2))
				{
					for(ISimpleInArchiveItem item : getNInterface().getArchiveItems())
					{
						if(item.isFolder())
							continue;
						updateEntry(container.add(new Entry(item.getPath())), entries, item);

					}
					container.loaded = need_sha1_or_md5 ? 2 : 1;
				}
				else
				{
					for(Entry entry : container.getEntries())
						updateEntry(entry, entries, null);
				}
				computeHashes(entries);
			}
			else
			{
				HashSet<Entry> entries = new HashSet<>();
				if(container.loaded < 1 || (need_sha1_or_md5 && container.loaded < 2))
				{
					for(SevenZArchiveEntry archive_entry : getJArchive().getEntries())
					{
						if(archive_entry.isDirectory())
							continue;
						updateEntry(container.add(new Entry(archive_entry.getName())), entries, archive_entry);
					}
					container.loaded = need_sha1_or_md5 ? 2 : 1;
				}
				else
				{
					for(Entry entry : container.getEntries())
						updateEntry(entry, entries, (SevenZArchiveEntry) null);
				}
				computeHashes(entries);
			}
		}

		private void updateEntry(Entry entry, Map<Integer, Entry> entries, ISimpleInArchiveItem item) throws IOException
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
					for(ISimpleInArchiveItem itm : getNInterface().getArchiveItems())
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

		private void updateEntry(Entry entry, HashSet<Entry> entries, SevenZArchiveEntry archive_entry) throws IOException
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

		private void computeHashes(Map<Integer, Entry> entries) throws NoSuchAlgorithmException, IOException
		{
			if(entries.size() > 0)
			{
				getNArchive().extract(IntStreamEx.of(entries.keySet()).toArray(), false, new IArchiveExtractCallback()
				{
					Entry entry;

					@Override
					public void setTotal(long total) throws SevenZipException
					{
					}

					@Override
					public void setCompleted(long complete) throws SevenZipException
					{
					}

					@Override
					public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException
					{
						if(extractOperationResult == ExtractOperationResult.OK)
						{
							for(MessageDigest d : digest)
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
					public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException
					{
					}

					@Override
					public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException
					{
						entry = entries.get(index);
						if(extractAskMode != ExtractAskMode.EXTRACT)
							return null;
						return new ISequentialOutStream()
						{
							@Override
							public int write(byte[] data) throws SevenZipException
							{
								for(MessageDigest d : digest)
									d.update(data);
								return data.length; // Return amount of proceed data
							}
						};
					}
				});
			}
		}

		private void computeHashes(HashSet<Entry> entries) throws IOException
		{
			for(Entry entry : entries)
			{
				for(MessageDigest d : computeHash(getJInterface().extract_stdout(entry.getName())))
				{
					if(d.getAlgorithm().equals("SHA-1")) //$NON-NLS-1$
						entries_bysha1.put(entry.sha1 = Hex.encodeHexString(d.digest()), entry);
					if(d.getAlgorithm().equals("MD5")) //$NON-NLS-1$
						entries_bymd5.put(entry.md5 = Hex.encodeHexString(d.digest()), entry);
					d.reset();
				}
			}

		}

		private MessageDigest[] computeHash(InputStream is)
		{
			try(BufferedInputStream bis = new BufferedInputStream(is))
			{
				byte[] buffer = new byte[8192];
				int len = is.read(buffer);
				while(len != -1)
				{
					for(MessageDigest d : digest)
						d.update(buffer, 0, len);
					len = is.read(buffer);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			return digest;
		}
	}

	private void update_entry(Profile profile, Entry entry) throws IOException
	{
		update_entry(profile, entry, (Path) null);
	}

	private void update_entry(Profile profile, Entry entry, Path entry_path) throws IOException
	{
		if(entry.parent.getType() == Type.ZIP)
		{
			if(entry.size == 0 && entry.crc == null)
			{
				if(entry_path == null)
					entry_path = getPath(entry);
				Map<String, Object> entry_zip_attrs = Files.readAttributes(entry_path, "zip:*"); //$NON-NLS-1$
				entry.size = (Long) entry_zip_attrs.get("size"); //$NON-NLS-1$
				entry.crc = String.format("%08x", entry_zip_attrs.get("crc")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			entries_bycrc.put(entry.crc + "." + entry.size, entry); //$NON-NLS-1$
		}
		if(entry.sha1 == null && entry.md5 == null && (need_sha1_or_md5 || entry.crc == null || profile.suspicious_crc.contains(entry.crc)))
		{
			if(entry.type == Entry.Type.CHD)
			{
				if(profile.sha1_disks || profile.md5_disks)
				{
					if(entry_path == null)
						entry_path = getPath(entry);
					CHDInfoReader chd_info = new CHDInfoReader(entry_path.toFile());
					if(profile.sha1_disks)
						if(null != (entry.sha1 = chd_info.getSHA1()))
							entries_bysha1.put(entry.sha1, entry);
					if(profile.md5_disks)
						if(null != (entry.md5 = chd_info.getMD5()))
							entries_bymd5.put(entry.md5, entry);
				}
			}
			else
			{
				if(profile.sha1_roms || profile.md5_roms)
				{
					if(entry_path == null)
						entry_path = getPath(entry);
					if(profile.sha1_roms)
						if(null != (entry.sha1 = computeSHA1(entry_path)))
							entries_bysha1.put(entry.sha1, entry);
					if(profile.md5_roms)
						if(null != (entry.md5 = computeMD5(entry_path)))
							entries_bymd5.put(entry.md5, entry);
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

	private String computeSHA1(Path entry_path)
	{
		return computeHash(entry_path, "SHA-1"); //$NON-NLS-1$
	}

	private String computeMD5(Path entry_path)
	{
		return computeHash(entry_path, "MD5"); //$NON-NLS-1$
	}

	private String computeHash(Path entry_path, String algorithm)
	{
		try(InputStream is = new BufferedInputStream(Files.newInputStream(entry_path), 8192))
		{
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			byte[] buffer = new byte[8192];
			int len = is.read(buffer);
			while(len != -1)
			{
				digest.update(buffer, 0, len);
				len = is.read(buffer);
			}
			return Hex.encodeHexString(digest.digest());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private Path getPath(Entry entry) throws IOException
	{
		try(FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
		{
			return srcfs.getPath(entry.file);
		}
	}

	public Entry find_byhash(Profile profile, Rom r)
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

	public Entry find_byhash(Disk d)
	{
		Entry entry = null;
		if(d.sha1 != null)
			if(null != (entry = entries_bysha1.get(d.sha1)))
				return entry;
		return entries_bymd5.get(d.md5);
	}

	private static File getCacheFile(File file)
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
		File cachedir = new File(workdir, "cache"); //$NON-NLS-1$
		cachedir.mkdirs();
		CRC32 crc = new CRC32();
		crc.update(file.getAbsolutePath().getBytes());
		return new File(cachedir, String.format("%08x", crc.getValue()) + ".scache"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void save(File file, Object obj)
	{
		try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(file)))))
		{
			oos.writeObject(obj);
		}
		catch(Throwable e)
		{
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Container> load(File file, ProgressHandler handler)
	{
		File cachefile = getCacheFile(file);
		try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
		{
			handler.setProgress(String.format(Messages.getString("DirScan.LoadingScanCache"), file) , 0); //$NON-NLS-1$
			return (HashMap<String, Container>) ois.readObject();
		}
		catch(Throwable e)
		{
		}
		return new HashMap<>();
	}

}
