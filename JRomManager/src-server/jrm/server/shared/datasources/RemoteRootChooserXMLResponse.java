package jrm.server.shared.datasources;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class RemoteRootChooserXMLResponse extends XMLResponse
{

	public RemoteRootChooserXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		Iterable<Path> rd = FileSystems.getDefault().getRootDirectories();
		long cnt = 0;
		writer.writeStartElement("data");
		for(Path root : rd)
		{
			try
			{
				if(Files.isDirectory(root) && Files.exists(root))
				{
					writer.writeElement("record",
						new SimpleAttribute("Name", root.getFileName() != null ? root.getFileName() : root),
						new SimpleAttribute("Path", root)
					);
					cnt++;
				}
			}
			catch(Throwable e)
			{
				
			}
		}
		writer.writeEndElement();
		writer.writeElement("endRow", Long.toString(cnt-1));
		writer.writeElement("totalRows", Long.toString(cnt));
		writer.writeEndElement();
	}
}
