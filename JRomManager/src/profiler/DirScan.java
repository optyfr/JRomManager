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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.codec.binary.Hex;

import data.Archive;
import data.Container;
import data.Directory;
import data.Entry;
import data.Rom;
import jdk.nashorn.internal.ir.annotations.Immutable;
import misc.Log;
import ui.ProgressHandler;

public class DirScan
{

	List<Container> containers = Collections.synchronizedList(new ArrayList<>());
	Map<String, Container> containers_byname = Collections.synchronizedMap(new HashMap<>());
	Map<String, Entry> entries_bycrc = Collections.synchronizedMap(new HashMap<>());
	Map<String, Entry> entries_bysha1 = Collections.synchronizedMap(new HashMap<>());

	public DirScan(File dir, ProgressHandler handler)
	{
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
			StreamSupport.stream(stream.spliterator(), true).forEach(p -> {
				Container c;
				File file = p.toFile();
				if (null == (c = containers_byname.get(file.getName())) || c.modified != file.lastModified() || (c instanceof Archive && c.size != file.length()))
				{
					if (file.isFile())
						containers.add(c = new Archive(file));
					else if (file.isDirectory())
						containers.add(c = new Directory(file));
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
		boolean need_sha1 = true;
		handler.setProgress("Scanning files in '" + dir + "'...", i.get(), containers.size());
		containers.parallelStream().forEach(c -> {
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
							try (FileSystem fs = FileSystems.newFileSystem(f.toPath(), null);)
							{
								final Path root = fs.getPath("/");
								Files.walkFileTree(root, new SimpleFileVisitor<Path>()
								{
									@Override
									public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
									{
										Map<String, Object> attr = Files.readAttributes(file, "zip:*");
										Entry entry = new Entry(file.toString());
										c.add(entry);
										entry.size = (Long) attr.get("size");
										entry.crc = String.format("%08x", attr.get("crc"));
										try (InputStream is = new BufferedInputStream(Files.newInputStream(file)))
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
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}
										entries_bycrc.put(entry.crc + "." + entry.size, entry);
										if (entry.sha1 != null)
											entries_bysha1.put(entry.sha1, entry);
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
						for (Entry entry : c.entries)
						{
							entries_bycrc.put(entry.crc + "." + entry.size, entry);
							if (entry.sha1 != null)
								entries_bysha1.put(entry.sha1, entry);
						}
					}

				}
				else if (c instanceof Directory)
				{

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
