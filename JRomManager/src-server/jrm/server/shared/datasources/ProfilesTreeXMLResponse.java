package jrm.server.shared.datasources;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;

import jrm.misc.Log;
import jrm.profile.manager.Dir;
import jrm.profile.manager.DirTree;
import jrm.server.shared.datasources.XMLRequest.Operation;
import lombok.val;

public class ProfilesTreeXMLResponse extends XMLResponse
{

	public ProfilesTreeXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}

	private int countNode(DirTree.Node<Dir> node)
	{
		int count = 0;
		for(val child : node)
		{
			if (child.getChildCount() > 0)
				count += countNode(child);
			else
				count++;
		}
		return ++count;
	}

	private void outputNode(XMLStreamWriter writer, DirTree.Node<Dir> node, String parentID, AtomicInteger id) throws XMLStreamException
	{
		String strID = id.toString();
		if (id.get() > 0)
		{
			request.getSession().tmp_profile_lst.put(id.get(), node.getData().getFile().toPath());
			writer.writeStartElement("record");
			writer.writeAttribute("ID", id.toString());
			writer.writeAttribute("Path", node.getData().getFile().getPath());
			writer.writeAttribute("title", node.getData().getFile().getName());
			writer.writeAttribute("isFolder", "true");
			if(parentID!=null)
				writer.writeAttribute("ParentID", parentID);
			writer.writeEndElement();
		}
		else
			request.getSession().tmp_profile_lst = new TreeMap<>();
		id.incrementAndGet();
		for(val child : node)
			outputNode(writer, child, strID, id);
	}

	@Override
	protected void fetch(Operation operation) throws Exception
	{
		DirTree root = new DirTree(request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile());
		int nodecount = countNode(root.getRoot());
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(nodecount-1));
		writer.writeElement("totalRows", Integer.toString(nodecount));
		writer.writeStartElement("data");
		outputNode(writer, root.getRoot(), null, new AtomicInteger());
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws Exception
	{
	//	DirNode root = new DirNode(request.session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile());
		int key = request.getSession().tmp_profile_lst.lastKey()+1;
		String basepath = operation.getData("Path");
		if(basepath==null || basepath.isEmpty())
			basepath = request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toString();
		Path path = Files.createDirectory(Paths.get(basepath, operation.getData("title")));
		request.getSession().tmp_profile_lst.put(key, path);
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeStartElement("data");
		writer.writeStartElement("record");
		writer.writeAttribute("ID", Integer.toString(key));
		writer.writeAttribute("Path", path.toString());
		writer.writeAttribute("title", operation.getData("title"));
		writer.writeAttribute("isFolder", "true");
		writer.writeAttribute("ParentID", operation.getData("ParentID"));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void update(Operation operation) throws Exception
	{
		Integer ID = Integer.valueOf(operation.getData("ID"));
		Path path = request.getSession().tmp_profile_lst.get(ID);
		Log.debug(path);
		String title = operation.getData("title");
		path = Files.move(path, path.getParent().resolve(title));
		request.getSession().tmp_profile_lst.put(ID, path);
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeStartElement("data");
		writer.writeStartElement("record");
		writer.writeAttribute("ID", ID.toString());
		writer.writeAttribute("Path", path.toString());
		writer.writeAttribute("title", title);
		writer.writeAttribute("isFolder", "true");
		writer.writeAttribute("ParentID", operation.getOldValues().get("ParentID"));
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void remove(Operation operation) throws Exception
	{
		Integer ID = Integer.valueOf(operation.getData("ID"));
		Path path = request.getSession().tmp_profile_lst.get(ID);
		FileUtils.deleteDirectory(path.toFile());
		request.getSession().tmp_profile_lst.remove(ID);
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeStartElement("data");
		writer.writeStartElement("record");
		writer.writeAttribute("ID", ID.toString());
		writer.writeEndElement();
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
