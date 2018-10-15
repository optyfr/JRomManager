package jrm.server.datasources;

import java.io.*;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import jrm.server.TempFileInputStream;
import jrm.server.datasources.XMLRequest.Operation;
import jrm.xml.EnhancedXMLStreamWriter;

abstract class XMLResponse implements Closeable
{
	protected XMLRequest request;
	private final File tmpfile;
	private final OutputStream out;
	protected final EnhancedXMLStreamWriter writer;

	public XMLResponse(XMLRequest request) throws Exception
	{
		this.request = request;
		tmpfile = File.createTempFile("JRM", null);
		out = new FileOutputStream(tmpfile);
		writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(out));
		writer.writeStartDocument("utf-8", "1.0");
	}
	
	private void processOperation(Operation operation) throws Exception
	{
		switch(operation.operationType.toString())
		{
			case "fetch":
				fetch(operation);
				break;
			case "add":
				add(operation);
				break;
			case "update":
				update(operation);
				break;
			case "remove":
				remove(operation);
				break;
			default:
				failure(operation.operationType + " not implemented");
				break;
		}
	}
	
	public Response processRequest() throws Exception
	{
		if(request.transaction!=null)
		{
			writer.writeStartElement("responses");
			for(Operation operation : request.transaction.operations)
				processOperation(operation);
			writer.writeEndElement();
		}
		else
			processOperation(request.operation);
		return NanoHTTPD.newFixedLengthResponse(Status.OK, "text/xml", new TempFileInputStream(tmpfile), tmpfile.length());
	}
	
	protected void fetch(Operation operation) throws Exception
	{
		failure("fetch operation not implemented");
	}
	
	protected void add(Operation operation) throws Exception
	{
		failure("add operation not implemented");
	}
	
	protected void update(Operation operation) throws Exception
	{
		failure("update operation not implemented");
	}
	
	protected void remove(Operation operation) throws Exception
	{
		failure("delete operation not implemented");
	}
	

	@Override
	public void close() throws IOException
	{
		try
		{
			writer.writeEndDocument();
			writer.close();
		}
		catch (XMLStreamException e)
		{
			e.printStackTrace();
		}
		finally
		{
			out.close();
		}
	}

	protected void error(int status) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement("status",Integer.toString(status));
		writer.writeEndElement();
	}
	
	protected void error(int status, String data) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement("status",Integer.toString(status));
		writer.writeElement("data", data);
		writer.writeEndElement();
	}
	
	protected void error(int status, Map<String,List<String>> data) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement("status",Integer.toString(status));
		if(data!=null)
		{
			writer.writeStartElement("errors");
			for(Map.Entry<String, List<String>> entry : data.entrySet())
			{
				writer.writeStartElement(entry.getKey());
				for(String msg : entry.getValue())
					writer.writeElement("errorMessage", msg);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
	}

	protected void no_error() throws XMLStreamException
	{
		error(0);
	}
	
	protected void success() throws XMLStreamException
	{
		error(0);
	}
	
	protected void other_error(String msg) throws XMLStreamException
	{
		error(-1, msg);
	}
	
	protected void failure(String msg) throws XMLStreamException
	{
		error(-1, msg);
	}
	
	protected void failure() throws XMLStreamException
	{
		error(-1);
	}
	
	protected void login_incorrect() throws XMLStreamException
	{
		error(-5);
	}
	
	protected void login_required() throws XMLStreamException
	{
		error(-7);
	}
	
}
