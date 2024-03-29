package jrm.server.shared.datasources;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import jrm.batch.Compressor.FileResult;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;

public class BatchCompressorFRXMLResponse extends XMLResponse
{

	private static final String RESULT = "result";
	private static final String RECORD = "record";
	private static final String STATUS = "status";
	private static final String RESPONSE = "response";

	public BatchCompressorFRXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(request.getSession().getCachedCompressorList().size() - 1));
		writer.writeElement("totalRows", Integer.toString(request.getSession().getCachedCompressorList().size()));
		writer.writeStartElement("data");
		for(final var sr : request.getSession().getCachedCompressorList().entrySet())
			writeRecord(writer, sr.getKey(), sr.getValue().getFile(), sr.getValue().getResult());
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("file"))
		{
			String id = UUID.randomUUID().toString();
			FileResult fr = new FileResult(Paths.get(operation.getData("file")));
			request.getSession().getCachedCompressorList().put(id, fr);
			writer.writeStartElement(RESPONSE);
			writer.writeElement(STATUS, "0");
			writer.writeStartElement("data");
			writeRecord(writer, id, fr.getFile(), fr.getResult());
			writer.writeEndElement();
			writer.writeEndElement();
		}
		else
			failure("file is missing in request");
	}
	
	@Override
	protected void update(Operation operation) throws XMLStreamException
	{
		if(!operation.hasData("id"))
		{
			failure("id is missing in request");
			return;
		}
		final String id = operation.getData("id");
		final FileResult fr = request.getSession().getCachedCompressorList().get(id);
		if (fr == null)
		{
			failure(id + " not in list");
			return;
		}
		if (!operation.hasData("file") && !operation.hasData(RESULT))
		{
			failure("field to update is missing in request");
			return;
		}
		if(operation.hasData("file"))
			fr.setFile(Paths.get(operation.getData("file")));
		if(operation.hasData(RESULT))
			fr.setResult(operation.getData(RESULT));
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeStartElement("data");
		writeRecord(writer, id, fr.getFile(), fr.getResult());
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	private void writeRecord(EnhancedXMLStreamWriter writer, String id, Path file, String result) throws XMLStreamException
	{
		writer.writeElement(RECORD, 
			new SimpleAttribute("id", id),
			new SimpleAttribute("file", file),
			new SimpleAttribute(RESULT, result)
		);
	}
	
	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("id"))
		{
			final String id = operation.getData("id");
			if(request.getSession().getCachedCompressorList().remove(id)!=null)
			{
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writer.writeStartElement("data");
				writer.writeElement(RECORD, new SimpleAttribute("id", id));
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
	protected void custom(Operation operation) throws XMLStreamException
	{
		if("clear".equals(operation.getOperationId().toString()))
		{
			request.getSession().getCachedCompressorList().clear();
			success();
		}
		
	}
}
