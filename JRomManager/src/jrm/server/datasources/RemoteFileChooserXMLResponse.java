package jrm.server.datasources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import jrm.server.TempFileInputStream;
import jrm.xml.EnhancedXMLStreamWriter;

public class RemoteFileChooserXMLResponse extends XMLResponse
{

	public RemoteFileChooserXMLResponse(XMLRequest request)
	{
		super(request);
	}


	@Override
	protected Response fetch() throws Exception
	{
		final File tmpfile = File.createTempFile("JRM", null);
		final boolean isDir;
		switch(request.data.get("context"))
		{
			case "tfRomsDest":
			case "tfDisksDest":
			case "tfSWDest":
			case "tfSWDisksDest":
			case "tfSamplesDest":
			case "listSrcDir":
				isDir = true;
				break;
			default:
				isDir = false;
				break;
		}
		try (OutputStream out = new FileOutputStream(tmpfile))
		{
			XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
			XMLStreamWriter writer = new EnhancedXMLStreamWriter(outputFactory.createXMLStreamWriter(out));
			writer.writeStartDocument("utf-8", "1.0");
			writer.writeStartElement("response");
			writer.writeAttribute("status", "0");
			writer.writeAttribute("startRow", "0");
			Path dir = request.session.getUser().settings.getWorkPath();
			if(request.data.containsKey("parent"))
				dir = new File(request.data.get("parent")).toPath();
			writer.writeStartElement("parent");
			writer.writeCData(dir.toString());
			writer.writeEndElement();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, entry -> isDir ? Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS) : true))
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
				writer.writeStartElement("endRow");
				writer.writeCharacters(cnt+"");
				writer.writeEndElement();
				writer.writeStartElement("totalRows");
				writer.writeCharacters(cnt+"");
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.close();
		}
		return NanoHTTPD.newFixedLengthResponse(Status.OK, "text/xml", new TempFileInputStream(tmpfile), tmpfile.length());
	}

	@Override
	protected Response add() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Response update() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Response delete() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

}
