package jrm.server.datasources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import jrm.profile.manager.Dir;
import jrm.server.TempFileInputStream;
import jrm.ui.profile.manager.FileTableModel;
import jrm.xml.EnhancedXMLStreamWriter;

public class ProfilesListXMLResponse extends XMLResponse
{

	public ProfilesListXMLResponse(XMLRequest request)
	{
		super(request);
	}


	@Override
	protected Response fetch() throws Exception
	{
		File tmpfile = File.createTempFile("JRM", null);
		try (OutputStream out = new FileOutputStream(tmpfile))
		{
			File dir = request.session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
			if(request.data.containsKey("Path"))
				dir = new File(request.data.get("Path"));
			FileTableModel model = new FileTableModel(new Dir(dir));
			XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
			XMLStreamWriter writer = new EnhancedXMLStreamWriter(outputFactory.createXMLStreamWriter(out));
			writer.writeStartDocument("utf-8", "1.0");
			writer.writeStartElement("response");
			writer.writeAttribute("status", "0");
			writer.writeAttribute("startRow", "0");
			writer.writeAttribute("endRow", model.getRowCount()+"");
			writer.writeAttribute("totalRows", model.getRowCount()+"");
			writer.writeStartElement("data");
			for(int i = 0; i < model.getRowCount(); i++)
			{
				writer.writeEmptyElement("record");
				writer.writeAttribute("Name", model.getValueAt(i, 0).toString());
				writer.writeAttribute("Path", model.getNfoAt(i).file.toString());
				writer.writeAttribute("version", model.getValueAt(i, 1).toString());
				writer.writeAttribute("haveSets", model.getValueAt(i, 2).toString());
				writer.writeAttribute("haveRoms", model.getValueAt(i, 3).toString());
				writer.writeAttribute("haveDisks", model.getValueAt(i, 4).toString());
				writer.writeAttribute("created", model.getValueAt(i, 5).toString());
				writer.writeAttribute("scanned", model.getValueAt(i, 6).toString());
				writer.writeAttribute("fixed", model.getValueAt(i, 7).toString());
			}
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
