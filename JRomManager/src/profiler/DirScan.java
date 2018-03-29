package profiler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;
import java.util.zip.CRC32;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;

import data.Archive;
import data.Container;
import data.Directory;
import data.Disk;
import data.Entry;
import data.Rom;
import io.CHDInfoReader;
import misc.Log;
import ui.ProgressHandler;

public class DirScan
{

	List<Container> containers = Collections.synchronizedList(new ArrayList<>());
	Map<String, Container> containers_byname = Collections.synchronizedMap(new HashMap<>());
	Map<String, Entry> entries_bycrc = Collections.synchronizedMap(new HashMap<>());
	Map<String, Entry> entries_bysha1 = Collections.synchronizedMap(new HashMap<>());

	boolean need_sha1 = true;
	boolean use_parallelism = true;

	public DirScan(Profile profile, File dir, ProgressHandler handler)
	{
		need_sha1 = profile.getProperty("need_sha1", false);
		use_parallelism = profile.getProperty("use_parallelism", false);
		
		Path path = Paths.get(dir.getAbsolutePath());

		/*
		 * Loading scan cache
		 */
		containers_byname = load(dir, handler);

		/*
		 * List files;
		 */

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path))
		{
			handler.setProgress("Listing files in '" + dir + "' ...", 0, (int) Files.list(path).count());
			StreamSupport.stream(stream.spliterator(), use_parallelism).forEach(p -> {
				Container c;
				File file = p.toFile();
				try
				{
					BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
					if (null == (c = containers_byname.get(file.getName())) || c.modified != attr.lastModifiedTime().toMillis() || (c instanceof Archive && c.size != attr.size()))
					{
						if (attr.isRegularFile())
							containers.add(c = new Archive(file, attr));
						else if (attr.isDirectory())
							containers.add(c = new Directory(file, attr));
						else
							c = null;
						if (c != null)
						{
							c.up2date = true;
							containers_byname.put(file.getName(), c);
						}
					}
					else
					{
						c.up2date = true;
						containers.add(c);
					}
					handler.setProgress("Listing files in '" + dir + "'... (" + containers.size() + ")", containers.size());
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			});
			if (containers_byname.entrySet().removeIf(entry -> !entry.getValue().up2date))
				Log.info("Removed some scache elements");
		}
		catch (IOException e)
		{
			Log.err("IOException when listing", e);
		}
		catch (Throwable e)
		{
			Log.err("Other Exception when listing", e);
		}
		AtomicInteger i = new AtomicInteger(0);
		handler.setProgress("Scanning files in '" + dir + "'...", i.get(), containers.size());
		(use_parallelism?containers.stream():containers.parallelStream()).forEach(c -> {
			try
			{
				File f = c.file;
				if (c instanceof Archive)
				{
					if (c.loaded < 1 || (need_sha1 && c.loaded < 2))
					{
						String ext = FilenameUtils.getExtension(f.toString());
						if (ext.equalsIgnoreCase("zip"))
						{
							Map<String,Object> env = new HashMap<>();
							env.put("useTempFile", Boolean.TRUE);
							try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:"+f.toURI()), env);)
							{
								final Path root = fs.getPath("/");
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
								c.loaded = need_sha1 ? 2 : 1;
							}
						}
					}
					else
					{
						for (Entry entry : c.getEntries())
							update_entry(profile, entry);
					}

				}
				else if (c instanceof Directory)
				{
					Files.walkFileTree(f.toPath(), new SimpleFileVisitor<Path>()
					{
						@Override
						public FileVisitResult visitFile(Path entry_path, BasicFileAttributes attrs) throws IOException
						{
							update_entry(profile, c.add(new Entry(entry_path.toString(),attrs)), entry_path);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
						{
							return FileVisitResult.CONTINUE;
						}
					});
					c.loaded = need_sha1 ? 2 : 1;
				}
				handler.setProgress("Scanned " + f.getName(), i.incrementAndGet(), null, i.get() + "/" + containers.size() + " (" + (int) (i.get() * 100.0 / containers.size()) + "%)");
			}
			catch (IOException e)
			{
				Log.err("IOException when scanning", e);
			}
			catch (Throwable e)
			{
				Log.err("Other Exception when listing", e);
			}
		});

		save(dir, containers_byname);

	}

	private void update_entry(Profile profile, Entry entry) throws IOException
	{
		update_entry(profile, entry, null);
	}
	private void update_entry(Profile profile, Entry entry, Path entry_path) throws IOException
	{
		if (entry.size == 0 && entry.crc == null)
		{
			if (entry_path == null)
				entry_path = getPath(entry);
			Map<String, Object> entry_zip_attrs = Files.readAttributes(entry_path, "zip:*");
			entry.size = (Long) entry_zip_attrs.get("size");
			entry.crc = String.format("%08x", entry_zip_attrs.get("crc"));
			entries_bycrc.put(entry.crc + "." + entry.size, entry);
		}
		if (entry.sha1 == null && (need_sha1 || entry.crc==null || profile.suspicious_crc.contains(entry.crc)))
		{
			if (entry_path == null)
				entry_path = getPath(entry);
			if(entry.type==Entry.Type.CHD)
			{
				CHDInfoReader chd_info = new CHDInfoReader(entry_path.toFile());
				entry.sha1 = chd_info.getSHA1();
				entries_bysha1.put(entry.sha1, entry);
			}
			else
			{
				try (InputStream is = new BufferedInputStream(Files.newInputStream(entry_path), 8192))
				{
					MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
					byte[] buffer = new byte[8192];
					int len = is.read(buffer);
					while (len != -1)
					{
						sha1.update(buffer, 0, len);
						len = is.read(buffer);
					}
					entry.sha1 = Hex.encodeHexString(sha1.digest());
					entries_bysha1.put(entry.sha1, entry);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private Path getPath(Entry entry) throws IOException
	{
		try (FileSystem srcfs = FileSystems.newFileSystem(entry.parent.file.toPath(), null);)
		{
			return srcfs.getPath(entry.file);
		}
	}
	
	public Entry find_byhash(Rom r)
	{
		if (r.sha1 != null)
		{
			Entry entry = null;
			if (null != (entry = entries_bysha1.get(r.sha1)))
				return entry;
		}
		return entries_bycrc.get(r.crc + "." + r.size);
	}

	public Entry find_byhash(Disk d)
	{
		return entries_bysha1.get(d.sha1);
	}

	private static File getCacheFile(File file)
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File cachedir = new File(workdir, "cache");
		cachedir.mkdirs();
		CRC32 crc = new CRC32();
		crc.update(file.getAbsolutePath().getBytes());
		return new File(cachedir, String.format("%08x", crc.getValue()) + ".scache");
	}

	private void save(File file, Object obj)
	{
		try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getCacheFile(file)))))
		{
			oos.writeObject(obj);
		}
		catch (Throwable e)
		{
		}
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Container> load(File file, ProgressHandler handler)
	{
		File cachefile = getCacheFile(file);
		try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(cachefile))))
		{
			handler.setProgress("Loading scan cache for '" + file + "'...", -1);
			return (HashMap<String, Container>) ois.readObject();
		}
		catch (Throwable e)
		{
		}
		return new HashMap<>();
	}

}
