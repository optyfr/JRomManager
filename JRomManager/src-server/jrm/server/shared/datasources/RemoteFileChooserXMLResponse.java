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
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FilenameUtils;

import jrm.server.shared.datasources.XMLRequest.Operation;

public class RemoteFileChooserXMLResponse extends XMLResponse
{

	public RemoteFileChooserXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		final boolean isDir;
		final String pathmatcher;
		switch(operation.getData("context"))
		{
			case "tfRomsDest":
			case "tfDisksDest":
			case "tfSWDest":
			case "tfSWDisksDest":
			case "tfSamplesDest":
			case "listSrcDir":
			case "addDatSrc":
				isDir = true;
				pathmatcher = null;
				break;
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
				pathmatcher = "glob:*.{xml,dat}";
				isDir = false;
				break;
			case "addDat":
				pathmatcher = "glob:*.{xml,dat}";
				isDir = false;
				break;
			case "addArc":
				pathmatcher = "glob:*.{zip,7z,rar,arj,tar,lzh,lha,tgz,tbz,tbz2,rpm,iso,deb,cab}";
				isDir = false;
				break;
			default:
				pathmatcher = null;
				isDir = false;
				break;
		}
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		Path dir = request.getSession().getUser().getSettings().getWorkPath();
		if (operation.hasData("context"))
		{
			String saved_dir = request.getSession().getUser().getSettings().getProperty("dir." + operation.getData("context"), null);
			if (saved_dir != null && !saved_dir.isEmpty())
			{
				Path saved_path = Paths.get(saved_dir);
				if (Files.isDirectory(saved_path))
					dir = saved_path;
			}
		}
		if(operation.hasData("parent"))
		{
			switch(operation.getData("parent"))
			{
				case "%work":
					dir = request.getSession().getUser().getSettings().getWorkPath();
					break;
				case "%shared":
					dir = request.getSession().getUser().getSettings().getBasePath().resolve("users").resolve("shared");
					Files.createDirectories(dir);
					break;
				default:
					dir = new File(operation.getData("parent")).toPath();
					break;
			}
		}
		writer.writeElement("parent", dir.toString());
		PathMatcher matcher = pathmatcher!=null ? dir.getFileSystem().getPathMatcher(pathmatcher) : null;
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, entry -> {
			if(isDir)
				return Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS);
			else if(matcher!=null && Files.isRegularFile(entry))
				return matcher.matches(entry.getFileName());
			return true;
		}))
		{
			long cnt = 0;
			writer.writeStartElement("data");
			if (dir.getParent() != null)
			{
				writer.writeEmptyElement("record");
				writer.writeAttribute("Name", "..");
				writer.writeAttribute("Path", dir.getParent().toString());
				writer.writeAttribute("Size", "-1");
				writer.writeAttribute("Modified", Files.getLastModifiedTime(dir.getParent()).toString());
				writer.writeAttribute("isDir", "true");
				cnt++;
			}
			for (Path entry : stream)
			{
				BasicFileAttributeView view = Files.getFileAttributeView(entry, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
				BasicFileAttributes attr = view.readAttributes();
				writer.writeEmptyElement("record");
				writer.writeAttribute("Name", entry.getFileName().toString());
				writer.writeAttribute("Path", entry.toString());
				writer.writeAttribute("Size", !attr.isRegularFile()?"-1":Long.toString(attr.size()));
				writer.writeAttribute("Modified", attr.lastModifiedTime().toString());
				writer.writeAttribute("isDir", Boolean.toString(attr.isDirectory()));
				cnt++;
			}
			writer.writeEndElement();
			writer.writeElement("endRow", Long.toString(cnt-1));
			writer.writeElement("totalRows", Long.toString(cnt));
		}
		writer.writeEndElement();
	}

	@Override
	protected void add(Operation operation) throws Exception
	{
		Path dir = request.getSession().getUser().getSettings().getWorkPath();
		if(operation.hasData("parent"))
			dir = new File(operation.getData("parent")).toPath();
		String name = operation.getData("Name");
		Path entry = dir.resolve(name);
		if(name!=null && Files.isDirectory(dir) && !Files.exists(entry))
		{
			try
			{
				Files.createDirectory(entry);
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeElement("parent", dir.toString());
				writer.writeStartElement("data");
				writer.writeEmptyElement("record");
				writer.writeAttribute("Name", entry.getFileName().toString());
				writer.writeAttribute("Path", entry.toString());
				writer.writeAttribute("Size", "-1");
				writer.writeAttribute("Modified", Files.getLastModifiedTime(entry).toString());
				writer.writeAttribute("isDir", Boolean.TRUE.toString());
				writer.writeEndElement();
				writer.writeEndElement();
			}
			catch(Exception ex)
			{
				failure(ex.getMessage());
			}
		}
		else
			failure("Can't create "+name);
	}
		
	@Override
	protected void update(Operation operation) throws Exception
	{
		Path dir = request.getSession().getUser().getSettings().getWorkPath();
		if(operation.hasData("parent"))
			dir = new File(operation.getData("parent")).toPath();
		String name = operation.getData("Name");
		String oldname = operation.getOldValues().get("Name");
		Path entry = dir.resolve(name);
		Path oldentry = dir.resolve(oldname);
		if(name!=null && oldname!=null && Files.isDirectory(dir) && Files.exists(oldentry) && !Files.exists(entry))
		{
			try
			{
				Files.move(oldentry, entry);
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeElement("parent", dir.toString());
				writer.writeStartElement("data");
				writer.writeEmptyElement("record");
				writer.writeAttribute("Name", entry.getFileName().toString());
				writer.writeAttribute("Path", entry.toString());
				writer.writeAttribute("Size", "-1");
				writer.writeAttribute("Modified", Files.getLastModifiedTime(entry).toString());
				writer.writeAttribute("isDir", Boolean.TRUE.toString());
				writer.writeEndElement();
				writer.writeEndElement();
			}
			catch(Exception ex)
			{
				failure(ex.getMessage());
			}
		}
		else
			failure("Can't update " + oldname + " to " + name);
	}
		
	@Override
	protected void remove(Operation operation) throws Exception
	{
		Path dir = request.getSession().getUser().getSettings().getWorkPath();
		if(operation.hasData("parent"))
			dir = new File(operation.getData("parent")).toPath();
		String name = operation.getData("Name");
		Path entry = dir.resolve(name);
		if(name!=null && Files.exists(entry))
		{
			try
			{
				//Files.delete(entry);
				Files.walk(entry).map(Path::toFile).sorted(Comparator.reverseOrder()).forEach(File::delete);	// recursive dir delete
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeElement("parent", dir.toString());
				writer.writeStartElement("data");
				writer.writeEmptyElement("record");
				writer.writeAttribute("Name", entry.getFileName().toString());
				writer.writeEndElement();
				writer.writeEndElement();
			}
			catch(Exception ex)
			{
				failure(ex.getMessage());
			}
		}
		else
			failure("Can't remove " + name);
	}
	
	@Override
	protected void custom(Operation operation) throws Exception
	{
		
		if(operation.getOperationId().toString().equals("expand"))
		{
			if(operation.hasData("paths"))
			{
				Path dir = request.getSession().getUser().getSettings().getWorkPath();
				if(operation.hasData("parent"))
					dir = new File(operation.getData("parent")).toPath();
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeElement("parent", dir.toString());
				writer.writeStartElement("data");
				AtomicInteger cnt = new AtomicInteger();
				switch(operation.getData("context"))
				{
					case "addArc":
						for(String path : operation.getDatas("paths"))
						{
							Path entry = Paths.get(path);
							Files.walkFileTree(entry, new SimpleFileVisitor<Path>()
							{
								String[] exts = new String[] {"zip","7z","rar","arj","tar","lzh","lha","tgz","tbz","tbz2","rpm","iso","deb","cab"};
								
								@Override
								public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
								{
									if(FilenameUtils.isExtension(file.getFileName().toString(), exts))
									{
										try
										{
											writer.writeEmptyElement("record");
											writer.writeAttribute("Name", file.getFileName().toString());
											writer.writeAttribute("Path", file.toString());
											cnt.incrementAndGet();
										}
										catch (XMLStreamException e)
										{
										}
									}
									return FileVisitResult.CONTINUE;
								}
							});
						}
						break;
					default:
						for(String path : operation.getDatas("paths"))
						{
							Path entry = Paths.get(path);
							writer.writeEmptyElement("record");
							writer.writeAttribute("Name", entry.getFileName().toString());
							writer.writeAttribute("Path", entry.toString());
							cnt.incrementAndGet();
						}
						break;
				}
				writer.writeEndElement();
				writer.writeElement("endRow", Long.toString(cnt.get()-1));
				writer.writeElement("totalRows", Long.toString(cnt.get()));
				writer.writeEndElement();
			}
			else
				failure("paths missing");
		}
		else
			super.custom(operation);
	}
}
