package jrm.server.datasources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import jrm.misc.GlobalSettings;
import jrm.server.TempFileInputStream;
import jrm.ui.profile.manager.DirNode;
import jrm.xml.EnhancedXMLStreamWriter;

public class ProfilesTreeXMLResponse extends XMLResponse
{

	public ProfilesTreeXMLResponse(XMLRequest request)
	{
		super(request);
	}

	private int countNode(DirNode node)
	{
		int count = 0;
		for (final Enumeration<?> children = node.children(); children.hasMoreElements();)
		{
			final DirNode child = (DirNode) children.nextElement();
			if (child.getChildCount() > 0)
				count += countNode(child);
			else
				count++;
		}
		return ++count;
	}

	private void outputNode(XMLStreamWriter writer, DirNode node, String parentID, AtomicInteger id) throws XMLStreamException
	{
		String strID = id.toString();
		if (id.get() > 0)
		{
			writer.writeStartElement("record");
			writer.writeAttribute("ID", id.toString());
			writer.writeAttribute("Path", node.getDir().getFile().getPath());
			writer.writeAttribute("title", node.getDir().getFile().getName());
			writer.writeAttribute("isFolder", "true");
			if(parentID!=null)
				writer.writeAttribute("ParentID", parentID);
			writer.writeEndElement();
		}
		id.incrementAndGet();
		for (final Enumeration<?> children = node.children(); children.hasMoreElements();)
		{
			final DirNode child = (DirNode) children.nextElement();
			outputNode(writer, child, strID, id);
		}
	}

	@Override
	protected Response fetch() throws Exception
	{
		File tmpfile = File.createTempFile("JRM", null);
		try (OutputStream out = new FileOutputStream(tmpfile))
		{
			DirNode root = new DirNode(GlobalSettings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile());
			XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
			XMLStreamWriter writer = new EnhancedXMLStreamWriter(outputFactory.createXMLStreamWriter(out));
			writer.writeStartDocument("utf-8", "1.0");
			writer.writeStartElement("response");
			writer.writeAttribute("status", "0");
			writer.writeAttribute("startRow", "0");
			int nodecount = countNode(root);
			writer.writeAttribute("endRow", nodecount == 0 ? "-1" : nodecount + "");
			writer.writeStartElement("data");
			outputNode(writer, root, null, new AtomicInteger());
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
