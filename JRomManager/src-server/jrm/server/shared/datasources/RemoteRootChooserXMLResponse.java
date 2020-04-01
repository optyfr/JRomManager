package jrm.server.shared.datasources;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

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
		writer.writeStartElement("data");
		long cnt = 0;
		if(request.session.server && request.session.multiuser)
		{
			for(Path root : Arrays.asList(Paths.get("%work"),Paths.get("%shared")))
			{
				writer.writeElement("record",
					new SimpleAttribute("Name", root.getFileName() != null ? root.getFileName() : root),
					new SimpleAttribute("Path", root)
				);
				cnt++;
			}
		}
		else
		{
			for(Path root : FileSystems.getDefault().getRootDirectories())
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
		}
		writer.writeEndElement();
		writer.writeElement("endRow", Long.toString(cnt-1));
		writer.writeElement("totalRows", Long.toString(cnt));
		writer.writeEndElement();
	}
}
