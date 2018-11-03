package jrm.server.datasources;

import java.util.List;

import jrm.server.datasources.XMLRequest.Operation;
import jrm.ui.basic.SrcDstResult;
import jrm.xml.SimpleAttribute;

public class BatchDat2DirSDRXMLResponse extends XMLResponse
{

	public BatchDat2DirSDRXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		List<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.session.getUser().settings.getProperty("dat2dir.sdr", "[]"));
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(sdrl.size()-1));
		writer.writeElement("totalRows", Integer.toString(sdrl.size()));
		writer.writeStartElement("data");
		for(SrcDstResult sdr : sdrl)
		{
			writer.writeElement("record", 
				new SimpleAttribute("src", sdr.src),
				new SimpleAttribute("dst", sdr.dst),
				new SimpleAttribute("result", sdr.result),
				new SimpleAttribute("selected", sdr.selected)
			);
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
