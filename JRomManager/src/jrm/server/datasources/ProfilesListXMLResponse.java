package jrm.server.datasources;

import java.io.File;

import jrm.profile.manager.Dir;
import jrm.server.datasources.XMLRequest.Operation;
import jrm.ui.profile.manager.FileTableModel;

public class ProfilesListXMLResponse extends XMLResponse
{

	public ProfilesListXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		File dir = request.session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
		if(operation.data.containsKey("Path"))
			dir = new File(operation.data.get("Path"));
		FileTableModel model = new FileTableModel(request.session, new Dir(dir));
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(model.getRowCount()-1));
		writer.writeElement("totalRows", Integer.toString(model.getRowCount()));
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
	}
}
