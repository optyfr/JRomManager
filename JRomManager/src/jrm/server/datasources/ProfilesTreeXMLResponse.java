package jrm.server.datasources;

import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jrm.server.datasources.XMLRequest.Operation;
import jrm.ui.profile.manager.DirNode;

public class ProfilesTreeXMLResponse extends XMLResponse
{

	public ProfilesTreeXMLResponse(XMLRequest request) throws Exception
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
	protected void fetch(Operation operation) throws Exception
	{
		DirNode root = new DirNode(request.session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile());
		int nodecount = countNode(root);
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(nodecount == 0 ? -1 : nodecount));
		writer.writeElement("totalRows", Integer.toString(nodecount == 0 ? -1 : nodecount));
		writer.writeStartElement("data");
		outputNode(writer, root, null, new AtomicInteger());
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
