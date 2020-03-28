package jrm.server.shared.datasources;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import jrm.profile.manager.ProfileNFO;
import jrm.server.shared.datasources.XMLRequest.Operation;
import lombok.val;

public class ProfilesListXMLResponse extends XMLResponse
{

	public ProfilesListXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		File dir = request.getSession().getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
		if(operation.hasData("Parent"))
			dir = new File(operation.getData("Parent"));
		val rows = ProfileNFO.list(request.getSession(), dir);
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("parent", dir.toString());
		writer.writeElement("endRow", Integer.toString(rows.size()-1));
		writer.writeElement("totalRows", Integer.toString(rows.size()));
		writer.writeStartElement("data");
		for(int i = 0; i < rows.size(); i++)
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("Name", rows.get(i).getName());
			writer.writeAttribute("Parent", rows.get(i).file.getParent());
			writer.writeAttribute("File", rows.get(i).file.getName());
			writer.writeAttribute("version", rows.get(i).getVersion());
			writer.writeAttribute("haveSets", rows.get(i).getHaveSets());
			writer.writeAttribute("haveRoms", rows.get(i).getHaveRoms());
			writer.writeAttribute("haveDisks", rows.get(i).getHaveDisks());
			writer.writeAttribute("created", rows.get(i).getCreated());
			writer.writeAttribute("scanned", rows.get(i).getScanned());
			writer.writeAttribute("fixed", rows.get(i).getFixed());
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws Exception
	{
		if(operation.hasData("Src"))
		{
			File dir = request.getSession().getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
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
					ProfileNFO nfo = ProfileNFO.load(request.getSession(), dst);
					writer.writeStartElement("response");
					writer.writeElement("status", "0");
					writer.writeStartElement("data");
					writer.writeEmptyElement("record");
					writer.writeAttribute("Name", nfo.getName());
					writer.writeAttribute("Parent", nfo.file.getParent());
					writer.writeAttribute("File", nfo.file.getName());
					writer.writeAttribute("version", nfo.getVersion());
					writer.writeAttribute("haveSets", nfo.getHaveSets());
					writer.writeAttribute("haveRoms", nfo.getHaveRoms());
					writer.writeAttribute("haveDisks", nfo.getHaveDisks());
					writer.writeAttribute("created", nfo.getCreated());
					writer.writeAttribute("scanned", nfo.getScanned());
					writer.writeAttribute("fixed", nfo.getFixed());
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
		File dir = request.getSession().getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
		if(operation.hasData("Parent") && !StringUtils.isEmpty(operation.getData("Parent")))
			dir = new File(operation.getData("Parent"));
		File dst = new File(dir, operation.getData("File"));
		ProfileNFO nfo = ProfileNFO.load(request.getSession(), dst);
		if(request.getSession().curr_profile == null || !request.getSession().curr_profile.nfo.equals(nfo))
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
		switch(operation.getOperationId().toString())
		{
			case "DropCache":
				File dir = request.getSession().getUser().settings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile();
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
