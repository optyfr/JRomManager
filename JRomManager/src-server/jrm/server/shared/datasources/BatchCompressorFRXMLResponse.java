package jrm.server.shared.datasources;

import java.io.File;
import java.util.Map.Entry;
import java.util.TreeMap;
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
		if (request.getSession().tmp_compressor_lst == null)
			request.getSession().tmp_compressor_lst = new TreeMap<>();
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(request.getSession().tmp_compressor_lst.size() - 1));
		writer.writeElement("totalRows", Integer.toString(request.getSession().tmp_compressor_lst.size()));
		writer.writeStartElement("data");
		for(Entry<String, FileResult> sr : request.getSession().tmp_compressor_lst.entrySet())
		{
			writer.writeElement("record", 
				new SimpleAttribute("id", sr.getKey()),
				new SimpleAttribute("file", sr.getValue().file),
				new SimpleAttribute("result", sr.getValue().result)
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
			FileResult fr = new FileResult(new File(operation.getData("file")));
			request.getSession().tmp_compressor_lst.put(id, fr);
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			writer.writeStartElement("data");
			writer.writeElement("record", 
				new SimpleAttribute("id", id),
				new SimpleAttribute("file", fr.file),
				new SimpleAttribute("result", fr.result)
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
			final FileResult fr = request.getSession().tmp_compressor_lst.get(id);
			if(fr!=null)
			{
				if(operation.hasData("file") || operation.hasData("result"))
				{
					if(operation.hasData("file"))
						fr.file = new File(operation.getData("file"));
					if(operation.hasData("result"))
						fr.result = operation.getData("result");
					writer.writeStartElement("response");
					writer.writeElement("status", "0");
					writer.writeStartElement("data");
					writer.writeElement("record", 
						new SimpleAttribute("id", id),
						new SimpleAttribute("file", fr.file),
						new SimpleAttribute("result", fr.result)
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
			if(request.getSession().tmp_compressor_lst.remove(id)!=null)
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
				request.getSession().tmp_compressor_lst.clear();
				success();
				break;
		}
		
	}
}
