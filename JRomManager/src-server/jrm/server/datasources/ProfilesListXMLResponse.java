package jrm.server.datasources;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import jrm.profile.manager.Dir;
import jrm.profile.manager.ProfileNFO;
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
		if(operation.hasData("Parent"))
			dir = new File(operation.getData("Parent"));
		FileTableModel model = new FileTableModel(request.session, new Dir(dir));
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("parent", dir.toString());
		writer.writeElement("endRow", Integer.toString(model.getRowCount()-1));
		writer.writeElement("totalRows", Integer.toString(model.getRowCount()));
		writer.writeStartElement("data");
		for(int i = 0; i < model.getRowCount(); i++)
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("Name", model.getValueAt(i, 0).toString());
			writer.writeAttribute("Parent", model.getNfoAt(i).file.getParent());
			writer.writeAttribute("File", model.getNfoAt(i).file.getName());
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
	
	@Override
	protected void add(Operation operation) throws Exception
	{
		if(operation.hasData("Src"))
		{
			File dir = request.session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
			if(operation.hasData("Parent") && !StringUtils.isEmpty(operation.getData("Parent")))
				dir = new File(operation.getData("Parent"));
			File src = new File(operation.getData("Src"));
			if(src.exists() && src.isFile())
			{
				try
				{
					File dst = new File(dir, operation.getData("File"));
					if(!src.equals(dst))
						FileUtils.copyFile(src, dst, true);
					ProfileNFO nfo = ProfileNFO.load(request.session, dst);
					writer.writeStartElement("response");
					writer.writeElement("status", "0");
					writer.writeStartElement("data");
					writer.writeEmptyElement("record");
					writer.writeAttribute("Name", FileTableModel.getValueAt_(nfo, 0).toString());
					writer.writeAttribute("Parent", nfo.file.getParent());
					writer.writeAttribute("File", nfo.file.getName());
					writer.writeAttribute("version", FileTableModel.getValueAt_(nfo, 1).toString());
					writer.writeAttribute("haveSets", FileTableModel.getValueAt_(nfo, 2).toString());
					writer.writeAttribute("haveRoms", FileTableModel.getValueAt_(nfo, 3).toString());
					writer.writeAttribute("haveDisks", FileTableModel.getValueAt_(nfo, 4).toString());
					writer.writeAttribute("created", FileTableModel.getValueAt_(nfo, 5).toString());
					writer.writeAttribute("scanned", FileTableModel.getValueAt_(nfo, 6).toString());
					writer.writeAttribute("fixed", FileTableModel.getValueAt_(nfo, 7).toString());
					writer.writeEndElement();
					writer.writeEndElement();
				}
				catch(IOException ex)
				{
					failure(ex.getMessage());
				}
			}
			else
				failure("Source file does not exist");
		}
		else
			failure("Src is needed");
	}
	
	@Override
	protected void remove(Operation operation) throws Exception
	{
		File dir = request.session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
		if(operation.hasData("Parent") && !StringUtils.isEmpty(operation.getData("Parent")))
			dir = new File(operation.getData("Parent"));
		File dst = new File(dir, operation.getData("File"));
		ProfileNFO nfo = ProfileNFO.load(request.session, dst);
		if(request.session.curr_profile == null || !request.session.curr_profile.nfo.equals(nfo))
		{
			if(nfo.delete())
			{
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				writer.writeEmptyElement("record");
				writer.writeAttribute("Parent", nfo.file.getParent());
				writer.writeAttribute("File", nfo.file.getName());
				writer.writeEndElement();
				writer.writeEndElement();
			}
			else
				failure("Failed to delete profile");
		}
		else
			failure("Can't delete current loaded profile");
	}
	
	@Override
	protected void custom(Operation operation) throws Exception
	{
		switch(operation.operationId.toString())
		{
			case "DropCache":
				File dir = request.session.getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
				if(operation.hasData("Parent") && !StringUtils.isEmpty(operation.getData("Parent")))
					dir = new File(operation.getData("Parent"));
				File dst = new File(dir, operation.getData("File"));
				if(dst.isFile())
				{
					File cache = new File(dst.getAbsolutePath() + ".cache");
					if (cache.exists() && !cache.delete())
						failure("Can't delete " + cache.getPath());
					else
						success();
				}
				else
					failure("Can't find "+dst.getPath());
				break;
			default:
				super.custom(operation);
				break;
		}
	}
}
