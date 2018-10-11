package jrm.server.datasources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import jrm.server.TempFileInputStream;
import jrm.xml.EnhancedXMLStreamWriter;

public class RemoteRootChooserXMLResponse extends XMLResponse
{

	public RemoteRootChooserXMLResponse(XMLRequest request)
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
			Iterable<Path> rd = FileSystems.getDefault().getRootDirectories();
			long cnt = 0;
			writer.writeStartElement("data");
			for(Path root : rd)
			{
				try
				{
					if(Files.isDirectory(root) && Files.exists(root))
					{
						writer.writeEmptyElement("record");
						writer.writeAttribute("Name", root.getFileName()!=null?root.getFileName().toString():root.toString());
						writer.writeAttribute("Path", root.toString());
						cnt++;
					}
				}
				catch(Throwable e)
				{
					
				}
			}
			writer.writeEndElement();
			writer.writeStartElement("endRow");
			writer.writeCharacters(cnt+"");
			writer.writeEndElement();
			writer.writeStartElement("totalRows");
			writer.writeCharacters(cnt+"");
			writer.writeEndElement();
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
