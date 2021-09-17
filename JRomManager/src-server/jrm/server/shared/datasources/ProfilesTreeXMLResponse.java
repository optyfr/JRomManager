package jrm.server.shared.datasources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;

import jrm.misc.Log;
import jrm.misc.Tree.Node;
import jrm.profile.manager.Dir;
import jrm.profile.manager.DirTree;
import jrm.server.shared.datasources.XMLRequest.Operation;
import lombok.val;

public class ProfilesTreeXMLResponse extends XMLResponse
{

	private static final String STATUS = "status";
	private static final String RESPONSE = "response";
	private static final String PARENT_ID = "ParentID";
	private static final String IS_FOLDER = "isFolder";
	private static final String TITLE = "title";
	private static final String RECORD = "record";

	public ProfilesTreeXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	private int countNode(Node<Dir> node)
	{
		var count = 0;
		for(val child : node)
		{
			if (child.getChildCount() > 0)
				count += countNode(child);
			else
				count++;
		}
		return count;
	}

	private void outputNode(XMLStreamWriter writer, Node<Dir> node, String parentID, AtomicInteger id) throws XMLStreamException
	{
		var strID = id.toString();
		if (id.get() > 0)
		{
			request.getSession().putProfileList(id.get(), node.getData().getFile().toPath());
			writer.writeStartElement(RECORD);
			writer.writeAttribute("ID", id.toString());
			writer.writeAttribute("Path", pathAbstractor.getRelativePath(node.getData().getFile().toPath()).toString());
			writer.writeAttribute(TITLE, node.getData().getFile().getName());
			writer.writeAttribute(IS_FOLDER, "true");
			if(parentID!=null)
				writer.writeAttribute(PARENT_ID, parentID);
			writer.writeEndElement();
		}
		else
			request.getSession().newProfileList();
		id.incrementAndGet();
		for(val child : node)
			outputNode(writer, child, strID, id);
	}

	@Override
	protected void fetch(Operation operation) throws XMLStreamException, IOException
	{
		val rootpath = request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
		Files.createDirectories(rootpath);
		val root = new DirTree(rootpath.toFile());
		int nodecount = countNode(root.getRoot());
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(nodecount-1));
		writer.writeElement("totalRows", Integer.toString(nodecount));
		writer.writeStartElement("data");
		outputNode(writer, root.getRoot(), null, new AtomicInteger());
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws XMLStreamException, IOException
	{
		var key = request.getSession().getLastProfileListKey()+1;
		var basepath = operation.getData("Path");
		if(basepath==null || basepath.isEmpty())
			basepath = request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toString();
		var path = Files.createDirectory(Paths.get(basepath, operation.getData(TITLE)));
		request.getSession().putProfileList(key, path);
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeStartElement("data");
		writer.writeStartElement(RECORD);
		writer.writeAttribute("ID", Integer.toString(key));
		writer.writeAttribute("Path", pathAbstractor.getRelativePath(path).toString());
		writer.writeAttribute(TITLE, operation.getData(TITLE));
		writer.writeAttribute(IS_FOLDER, "true");
		writer.writeAttribute(PARENT_ID, operation.getData(PARENT_ID));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void update(Operation operation) throws XMLStreamException, IOException
	{
		var id = Integer.valueOf(operation.getData("ID"));
		var path = request.getSession().getProfileList(id);
		Log.debug(path);
		var title = operation.getData(TITLE);
		path = Files.move(path, path.getParent().resolve(title));
		request.getSession().putProfileList(id, path);
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeStartElement("data");
		writer.writeStartElement(RECORD);
		writer.writeAttribute("ID", id.toString());
		writer.writeAttribute("Path", pathAbstractor.getRelativePath(path).toString());
		writer.writeAttribute(TITLE, title);
		writer.writeAttribute(IS_FOLDER, "true");
		writer.writeAttribute(PARENT_ID, operation.getOldValues().get(PARENT_ID));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void remove(Operation operation) throws XMLStreamException, IOException
	{
		var id = Integer.valueOf(operation.getData("ID"));
		var path = request.getSession().getProfileList(id);
		FileUtils.deleteDirectory(path.toFile());
		request.getSession().removeProfileList(id);
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeStartElement("data");
		writer.writeStartElement(RECORD);
		writer.writeAttribute("ID", id.toString());
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
