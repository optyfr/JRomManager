package jrm.server.shared.datasources;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import jrm.batch.DirUpdaterResults;
import jrm.batch.DirUpdaterResults.DirUpdaterResult;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchDat2DirResultXMLResponse extends XMLResponse
{

	public BatchDat2DirResultXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		final String src = operation.getData("src");
		final DirUpdaterResults results = src!=null?DirUpdaterResults.load(request.getSession(), pathAbstractor.getAbsolutePath(src).toFile()):null;
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString((results!=null?results.getResults().size():0)-1));
		writer.writeElement("totalRows", Integer.toString(results!=null?results.getResults().size():0));
		writer.writeStartElement("data");
		if(results!=null)
		{
			for(DirUpdaterResult result : results.getResults())
			{
				writer.writeElement("record", 
					new SimpleAttribute("src", pathAbstractor.getRelativePath((result.getDat()))),
					new SimpleAttribute("have", result.getStats().getSetFoundOk()),
					new SimpleAttribute("create", result.getStats().getSetCreateComplete()),
					new SimpleAttribute("fix", result.getStats().getSetFoundFixComplete()),
					new SimpleAttribute("miss", result.getStats().getSetCreate() + result.getStats().getSetFound() + result.getStats().getSetMissing() - (result.getStats().getSetCreateComplete() + result.getStats().getSetFoundFixComplete() + result.getStats().getSetFoundOk())),
					new SimpleAttribute("total", result.getStats().getSetCreate() + result.getStats().getSetFound() + result.getStats().getSetMissing())
				);
			}
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
