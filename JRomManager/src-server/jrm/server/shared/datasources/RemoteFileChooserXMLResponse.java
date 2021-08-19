package jrm.server.shared.datasources;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import jrm.misc.Log;
import jrm.server.shared.datasources.XMLRequest.Operation;
import lombok.val;

public class RemoteFileChooserXMLResponse extends XMLResponse
{
	private static final String PARENT = "parent";
	private static final String PATHS = "paths";
	private static final String REL_PATH = "RelPath";
	private static final String INITIAL_PATH = "initialPath";
	private static final String IS_DIR = "isDir";
	private static final String MODIFIED = "Modified";
	private static final String RECORD = "record";
	private static final String STATUS = "status";
	private static final String RESPONSE = "response";
	private static final String CONTEXT = "context";
	Path root;
	Options options = null;

	static class Options
	{
		final boolean isDir;
		final String pathmatcher;

		public Options(String context)
		{
			switch (context)
			{
				case "tfRomsDest":
				case "tfDisksDest":
				case "tfSWDest":
				case "tfSWDisksDest":
				case "tfSamplesDest":
				case "listSrcDir":
				case "addDatSrc":
				case "updDat":
				case "updTrnt":
					isDir = true;
					pathmatcher = null;
					break;
				case "addTrnt":
					pathmatcher = "glob:*.torrent";
					isDir = false;
					break;
				case "importDat":
				case "addDat":
					pathmatcher = "glob:*.{xml,dat}";
					isDir = false;
					break;
				case "addArc":
					pathmatcher = "glob:*.{zip,7z,rar,arj,tar,lzh,lha,tgz,tbz,tbz2,rpm,iso,deb,cab}";
					isDir = false;
					break;
				case "importSettings":
				case "exportSettings":
					pathmatcher = "glob:*.properties";
					isDir = false;
					break;
				default:
					pathmatcher = null;
					isDir = false;
					break;
			}
		}
	}

	public static class CaseInsensitiveFileFinder
	{
		private CaseInsensitiveFileFinder()
		{
			throw new IllegalStateException("Utility class");
		}

		private static Path findDir(Path dir) throws IOException
		{
			try (final var stream = Files.list(dir.getParent()))
			{
				return stream.filter(Files::isDirectory).filter(p -> p.getFileName().toString().equalsIgnoreCase(dir.getFileName().toString())).findFirst().orElse(null);
			}
		}

		private static Path findLast(Path path) throws IOException
		{
			if (Files.exists(path))
				return path;
			try (final var stream = Files.list(path.getParent()))
			{
				return stream.filter(p -> p.getFileName().toString().equalsIgnoreCase(path.getFileName().toString())).findFirst().orElse(null);
			}
		}

		public static Optional<Path> findFileIgnoreCase(final Path parent, final String fileName)
		{
			Path testpath = null;
			try
			{
				if (!Files.exists(parent))
				{
					// test all
					testpath = parent.getRoot();
					for (var i = 0; i < parent.getNameCount(); i++)
						if (null == (testpath = findDir(testpath.resolve(parent.getName(i)))))
							break;
				}
				else
					testpath = parent;
				if (testpath != null)
					testpath = findLast(testpath.resolve(fileName));
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(), e);
			}
			return Optional.ofNullable(testpath);
		}

		public static Optional<Path> findFileIgnoreCase(final Path path)
		{
			return findFileIgnoreCase(path.getParent(), path.getFileName().toString());
		}

		public static Optional<File> findFileIgnoreCase(final String file)
		{
			var path = Paths.get(file);
			return findFileIgnoreCase(path.getParent(), path.getFileName().toString()).map(Path::toFile);
		}

		public static Optional<File> findFileIgnoreCase(final String parentDir, final String fileName)
		{
			return findFileIgnoreCase(Paths.get(parentDir), fileName).map(Path::toFile);
		}
	}

	public RemoteFileChooserXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws XMLStreamException, IOException
	{
		if (operation.hasData(CONTEXT))
			options = new Options(operation.getData(CONTEXT));
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeElement("startRow", "0");
		Path parent = writeParent(getParent(operation));
		PathMatcher matcher = options.pathmatcher != null ? parent.getFileSystem().getPathMatcher(options.pathmatcher) : null;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent, entry -> {
			if (options.isDir)
				return Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS);
			else if (matcher != null && Files.isRegularFile(entry))
				return matcher.matches(entry.getFileName());
			return true;
		}))
		{
			long cnt = 0;
			writer.writeStartElement("data");
			if (!parent.equals(root) && parent.getParent() != null)
			{
				writer.writeStartElement(RECORD);
				writer.write("Name", "..");
				writer.write("Path", pathAbstractor.getRelativePath(parent).getParent());
				writer.write("Size", -1);
				writer.write(MODIFIED, Files.getLastModifiedTime(parent.getParent()));
				writer.write(IS_DIR, true);
				writer.writeEndElement();
				cnt++;
			}
			val initialPath = operation.hasData(INITIAL_PATH) ? CaseInsensitiveFileFinder.findFileIgnoreCase(pathAbstractor.getAbsolutePath(operation.getData(INITIAL_PATH))) : Optional.empty();
			for (Path entry : stream)
			{
				BasicFileAttributeView view = Files.getFileAttributeView(entry, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
				BasicFileAttributes attr = view.readAttributes();
				writer.writeStartElement(RECORD);
				writer.write("Name", entry.getFileName());
				writer.write("Path", pathAbstractor.getRelativePath(entry));
				writer.write(REL_PATH, pathAbstractor.getRelativePath(entry));
				writer.write("Size", !attr.isRegularFile() ? -1 : attr.size());
				writer.write(MODIFIED, attr.lastModifiedTime());
				writer.write(IS_DIR, attr.isDirectory());
				if (initialPath.isPresent() && entry.equals(initialPath.get()))
					writer.write("isSelected", true);
				writer.writeEndElement();
				cnt++;
			}
			writer.writeEndElement();
			writer.writeElement("endRow", Long.toString(cnt - 1));
			writer.writeElement("totalRows", Long.toString(cnt));
		}
		writer.writeEndElement();
	}

	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if (operation.hasData(CONTEXT))
			options = new Options(operation.getData(CONTEXT));
		Path parent = getParent(operation);
		String name = operation.getData("Name");
		Path entry = parent.resolve(name);
		if (name != null && Files.isDirectory(parent) && !Files.exists(entry))
		{
			try
			{
				Files.createDirectory(entry);
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writeParent(parent);
				writer.writeStartElement("data");
				writer.writeStartElement(RECORD);
				writer.writeAttribute("Name", entry.getFileName().toString());
				writer.writeAttribute("Path", pathAbstractor.getRelativePath(entry).toString());
				writer.writeAttribute(REL_PATH, pathAbstractor.getRelativePath(entry).toString());
				writer.writeAttribute("Size", "-1");
				writer.writeAttribute(MODIFIED, Files.getLastModifiedTime(entry).toString());
				writer.writeAttribute(IS_DIR, Boolean.TRUE.toString());
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();
			}
			catch (Exception ex)
			{
				failure(ex.getMessage());
			}
		}
		else
			failure("Can't create " + name);
	}

	@Override
	protected void update(Operation operation) throws XMLStreamException
	{
		if (operation.hasData(CONTEXT))
			options = new Options(operation.getData(CONTEXT));
		Path parent = getParent(operation);
		String name = operation.getData("Name");
		String oldname = operation.oldValues.get("Name");
		Path entry = parent.resolve(name);
		Path oldentry = parent.resolve(oldname);
		if (name != null && oldname != null && Files.isDirectory(parent) && Files.exists(oldentry) && !Files.exists(entry))
		{
			try
			{
				Files.move(oldentry, entry);
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writeParent(parent);
				writer.writeStartElement("data");
				writer.writeStartElement(RECORD);
				writer.writeAttribute("Name", entry.getFileName().toString());
				writer.writeAttribute("Path", pathAbstractor.getRelativePath(entry).toString());
				writer.writeAttribute(REL_PATH, pathAbstractor.getRelativePath(entry).toString());
				writer.writeAttribute("Size", "-1");
				writer.writeAttribute(MODIFIED, Files.getLastModifiedTime(entry).toString());
				writer.writeAttribute(IS_DIR, Boolean.TRUE.toString());
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();
			}
			catch (Exception ex)
			{
				failure(ex.getMessage());
			}
		}
		else
			failure("Can't update " + oldname + " to " + name);
	}

	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		if (operation.hasData(CONTEXT))
			options = new Options(operation.getData(CONTEXT));
		Path parent = getParent(operation);
		String name = operation.getData("Name");
		Path entry = parent.resolve(name);
		if (name != null && Files.exists(entry))
		{
			try
			{
				try (final var stream = Files.walk(entry))
				{
					stream.map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete); // recursive dir delete
				}
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writeParent(parent);
				writer.writeStartElement("data");
				writer.writeStartElement(RECORD);
				writer.writeAttribute("Name", entry.getFileName().toString());
				writer.writeEndElement();
				writer.writeEndElement();
				writer.writeEndElement();

			}
			catch (Exception ex)
			{
				failure(ex.getMessage());
			}
		}
		else
			failure("Can't remove " + name);
	}

	@Override
	protected void custom(Operation operation) throws XMLStreamException, IOException
	{

		if (operation.getOperationId().toString().equals("expand"))
		{
			customExpand(operation);
		}
		else if (operation.operationId.toString().equals("extract_subfolder"))
		{
			if (operation.hasData("Path"))
			{
				var zipfile = pathAbstractor.getAbsolutePath(operation.getData("Path"));
				Path dest = zipfile.getParent().resolve(StringUtils.substring(zipfile.getFileName().toString(), 0, -4));
				unzip(zipfile, dest);
				success();
			}
			else
				failure("path missing");
		}
		else if (operation.operationId.toString().equals("extract_here"))
		{
			if (operation.hasData("Path"))
			{
				var zipfile = pathAbstractor.getAbsolutePath(operation.getData("Path"));
				Path dest = zipfile.getParent();
				unzip(zipfile, dest);
				success();
			}
			else
				failure("path missing");
		}
		else
			super.custom(operation);
	}

	/**
	 * @param operation
	 * @throws XMLStreamException
	 * @throws SecurityException
	 * @throws IOException
	 */
	private void customExpand(Operation operation) throws XMLStreamException, SecurityException, IOException
	{
		if (operation.hasData(PATHS))
		{
			var dir = request.getSession().getUser().getSettings().getWorkPath();
			if (operation.hasData(PARENT))
				dir = new File(operation.getData(PARENT)).toPath();
			writer.writeStartElement(RESPONSE);
			writer.writeElement(STATUS, "0");
			writer.writeElement(PARENT, dir.toString());
			writer.writeStartElement("data");
			var cnt = new AtomicInteger();
			if ("addArc".equals(operation.getData(CONTEXT)))
			{
				customExpandAddArc(operation, cnt);
			}
			else
			{
				for (String path : operation.getDatas(PATHS))
				{
					var entry = Paths.get(path);
					writer.writeEmptyElement(RECORD);
					writer.writeAttribute("Name", entry.getFileName().toString());
					writer.writeAttribute("Path", pathAbstractor.getRelativePath(entry).toString());
					cnt.incrementAndGet();
				}
			}
			writer.writeEndElement();
			writer.writeElement("endRow", Long.toString(cnt.get() - 1L));
			writer.writeElement("totalRows", Long.toString(cnt.get()));
			writer.writeEndElement();
		}
		else
			failure("paths missing");
	}

	/**
	 * @param operation
	 * @param cnt
	 * @throws SecurityException
	 * @throws IOException
	 */
	private void customExpandAddArc(Operation operation, AtomicInteger cnt) throws SecurityException, IOException
	{
		for (String path : operation.getDatas(PATHS))
		{
			var entry = pathAbstractor.getAbsolutePath(path);
			Files.walkFileTree(entry, new SimpleFileVisitor<Path>()
			{
				String[] exts = new String[] { "zip", "7z", "rar", "arj", "tar", "lzh", "lha", "tgz", "tbz", "tbz2", "rpm", "iso", "deb", "cab" };

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					if (FilenameUtils.isExtension(file.getFileName().toString(), exts))
					{
						try
						{
							writer.writeEmptyElement(RECORD);
							writer.writeAttribute("Name", file.getFileName().toString());
							writer.writeAttribute("Path", pathAbstractor.getRelativePath(file).toString());
							cnt.incrementAndGet();
						}
						catch (XMLStreamException e)
						{
							// ignore silently
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	private String getRootName()
	{
		return pathAbstractor.getRelativePath(root).toString();
	}

	private Path getParent(Operation operation)
	{
		var parent = request.getSession().getUser().getSettings().getWorkPath();
		root = parent;
		if (operation.hasData("root"))
			root = parent = pathAbstractor.getAbsolutePath(operation.getData("root"));
		if (operation.hasData(PARENT))
			parent = pathAbstractor.getAbsolutePath(operation.getData(PARENT));
		else if (operation.hasData(INITIAL_PATH))
		{
			parent = pathAbstractor.getAbsolutePath(operation.getData(INITIAL_PATH));
			if (!Files.isDirectory(parent))
				parent = parent.getParent();
		}
		return parent;
	}

	private Path writeParent(Path parent) throws XMLStreamException
	{
		writer.writeElement(PARENT, pathAbstractor.getRelativePath(parent).toString());
		writer.writeElement("relparent", pathAbstractor.getRelativePath(parent).toString());
		writer.writeElement("root", pathAbstractor.getRelativePath(root).toString());
		writer.writeElement("parentRelative", Paths.get(getRootName()).resolve(root.relativize(parent)).toString());
		return parent;
	}

	private static class EnumerationToIterator<T> implements Iterator<T>
	{
		Enumeration<T> enumeration;

		public EnumerationToIterator(Enumeration<T> enmueration)
		{
			this.enumeration = enmueration;
		}

		@Override
		public boolean hasNext()
		{
			return enumeration.hasMoreElements();
		}

		@Override
		public T next()
		{
			if (!enumeration.hasMoreElements())
				throw new NoSuchElementException();
			return enumeration.nextElement();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}

	private void unzip(Path zipfile, Path outputPath)
	{
		try (var zf = new ZipFile(zipfile.toFile()))
		{

			Enumeration<? extends ZipEntry> zipEntries = zf.entries();
			new EnumerationToIterator<>(zipEntries).forEachRemaining(entry -> {
				try
				{
					if (entry.isDirectory())
					{
						var dirToCreate = outputPath.resolve(entry.getName());
						if (!Files.exists(dirToCreate))
							Files.createDirectories(dirToCreate);
					}
					else
					{
						var fileToCreate = outputPath.resolve(entry.getName());
						if (!Files.exists(fileToCreate.getParent()))
							Files.createDirectories(fileToCreate.getParent());
						Files.copy(zf.getInputStream(entry), fileToCreate);
					}
				}
				catch (IOException ei)
				{
					Log.err(ei.getMessage(), ei);
				}
			});
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}

	}

}
