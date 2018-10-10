package jrm.server.datasources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
		File tmpfile = File.createTempFile("JRM", null);
		try (OutputStream out = new FileOutputStream(tmpfile))
		{
			XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
			XMLStreamWriter writer = new EnhancedXMLStreamWriter(outputFactory.createXMLStreamWriter(out));
			writer.writeStartDocument("utf-8", "1.0");
			writer.writeStartElement("response");
			writer.writeAttribute("status", "0");
			writer.writeAttribute("startRow", "0");
			Path dir = request.session.getUser().settings.getWorkPath();
			if(request.data.containsKey("Parent"))
				dir = new File(request.data.get("Parent")).toPath();
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, new DirectoryStream.Filter<Path>() {
				@Override
				public boolean accept(Path entry) throws IOException
				{
					return true;
				}
				
			}))
			{
				long cnt = 0;
				writer.writeStartElement("data");
				for (Path entry : stream)
				{
					writer.writeEmptyElement("record");
					writer.writeAttribute("Name", entry.getFileName().toString());
					writer.writeAttribute("Parent", dir.toString());
					writer.writeAttribute("isDir", Boolean.toString(Files.isDirectory(entry)));
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
