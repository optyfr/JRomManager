package jrm.server.shared.datasources;

import java.io.File;

import jrm.batch.DirUpdaterResults;
import jrm.batch.DirUpdaterResults.DirUpdaterResult;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchDat2DirResultXMLResponse extends XMLResponse
{

	public BatchDat2DirResultXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		final String src = operation.getData("src");
		final DirUpdaterResults results = src!=null?DirUpdaterResults.load(request.getSession(), new File(src)):null;
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString((results!=null?results.results.size():0)-1));
		writer.writeElement("totalRows", Integer.toString(results!=null?results.results.size():0));
		writer.writeStartElement("data");
		if(results!=null)
		{
			for(DirUpdaterResult result : results.results)
			{
				writer.writeElement("record", 
					new SimpleAttribute("src", result.dat.toString()),
					new SimpleAttribute("have", result.stats.set_create_complete + result.stats.set_found_fixcomplete + result.stats.set_found_ok),
					new SimpleAttribute("miss", result.stats.set_create + result.stats.set_found + result.stats.set_missing - (result.stats.set_create_complete + result.stats.set_found_fixcomplete + result.stats.set_found_ok)),
					new SimpleAttribute("total", result.stats.set_create + result.stats.set_found + result.stats.set_missing)
				);
			}
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
