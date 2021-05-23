package jrm.server.shared.datasources;

import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.UUID;

import jrm.batch.Compressor.FileResult;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchCompressorFRXMLResponse extends XMLResponse
{

	public BatchCompressorFRXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(request.getSession().getCachedCompressorList().size() - 1));
		writer.writeElement("totalRows", Integer.toString(request.getSession().getCachedCompressorList().size()));
		writer.writeStartElement("data");
		for(Entry<String, FileResult> sr : request.getSession().getCachedCompressorList().entrySet())
		{
			writer.writeElement("record", 
				new SimpleAttribute("id", sr.getKey()),
				new SimpleAttribute("file", sr.getValue().getFile()),
				new SimpleAttribute("result", sr.getValue().getResult())
			);
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws Exception
	{
		if(operation.hasData("file"))
		{
			String id = UUID.randomUUID().toString();
			FileResult fr = new FileResult(Paths.get(operation.getData("file")));
			request.getSession().getCachedCompressorList().put(id, fr);
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			writer.writeStartElement("data");
			writer.writeElement("record", 
				new SimpleAttribute("id", id),
				new SimpleAttribute("file", fr.getFile()),
				new SimpleAttribute("result", fr.getResult())
			);
			writer.writeEndElement();
			writer.writeEndElement();
		}
		else
			failure("file is missing in request");
	}
	
	@Override
	protected void update(Operation operation) throws Exception
	{
		if(operation.hasData("id"))
		{
			final String id = operation.getData("id");
			final FileResult fr = request.getSession().getCachedCompressorList().get(id);
			if(fr!=null)
			{
				if(operation.hasData("file") || operation.hasData("result"))
				{
					if(operation.hasData("file"))
						fr.setFile(Paths.get(operation.getData("file")));
					if(operation.hasData("result"))
						fr.setResult(operation.getData("result"));
					writer.writeStartElement("response");
					writer.writeElement("status", "0");
					writer.writeStartElement("data");
					writer.writeElement("record", 
						new SimpleAttribute("id", id),
						new SimpleAttribute("file", fr.getFile()),
						new SimpleAttribute("result", fr.getResult())
					);
					writer.writeEndElement();
					writer.writeEndElement();
				}
				else
					failure("field to update is missing in request");
			}
			else
				failure(id + " not in list");
		}
		else
			failure("id is missing in request");
	}
	
	@Override
	protected void remove(Operation operation) throws Exception
	{
		if(operation.hasData("id"))
		{
			final String id = operation.getData("id");
			if(request.getSession().getCachedCompressorList().remove(id)!=null)
			{
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				writer.writeElement("record", new SimpleAttribute("id", id));
				writer.writeEndElement();
				writer.writeEndElement();
				
			}
			else
				failure(id + " is not in the list");
		}
		else
			failure("Src is missing in request");
	}
	
	@Override
	protected void custom(Operation operation) throws Exception
	{
		switch(operation.getOperationId().toString())
		{
			case "clear":
				request.getSession().getCachedCompressorList().clear();
				success();
				break;
		}
		
	}
}
