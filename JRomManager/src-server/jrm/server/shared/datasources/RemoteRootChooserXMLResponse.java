package jrm.server.shared.datasources;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;
import lombok.val;

public class RemoteRootChooserXMLResponse extends XMLResponse
{

	public RemoteRootChooserXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		Map<String,Path> paths = new LinkedHashMap<>();
		if(request.session.server && request.session.multiuser)
		{
			switch(operation.getData("context"))
			{
				case "listSrcDir":
				case "importDat":
				case "addDatSrc":
				case "addDat":
					paths.put("Work", Paths.get("%work"));
					paths.put("Shared", Paths.get("%shared"));
					break;
				case "importSettings":
				case "exportSettings":
					paths.put("Presets", Paths.get("%presets"));
					break;
				default:
					paths.put("Work", Paths.get("%work"));
					break;
			}
		}
		else
		{
			paths.put("Work", Paths.get("%work"));
			for(Path root : FileSystems.getDefault().getRootDirectories())
			{
				try
				{
					if(Files.isDirectory(root) && Files.exists(root))
						paths.put((root.getFileName() != null ? root.getFileName() : root).toString(), root);
				}
				catch(Throwable e)
				{
				}
			}
		}
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Long.toString(paths.size()-1));
		writer.writeElement("totalRows", Long.toString(paths.size()));
		writer.writeStartElement("data");
		for(val root : paths.entrySet())
		{
			writer.writeElement("record",
				new SimpleAttribute("Name", root.getKey()),
				new SimpleAttribute("Path", root.getValue())
			);
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
