package jrm.server.datasources;

import jrm.server.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchDat2DirSrcXMLResponse extends XMLResponse
{

	public BatchDat2DirSrcXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		String[] srcdirs = request.session.getUser().settings.getProperty("dat2dir.srcdirs", "").split("\\|");
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(srcdirs.length-1));
		writer.writeElement("totalRows", Integer.toString(srcdirs.length));
		writer.writeStartElement("data");
		for(String srcdir : srcdirs)
		{
			writer.writeElement("record", 
				new SimpleAttribute("name", srcdir)
			);
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
