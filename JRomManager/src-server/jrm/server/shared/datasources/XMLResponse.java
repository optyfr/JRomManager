package jrm.server.shared.datasources;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.Range;

import jrm.misc.Log;
import jrm.security.PathAbstractor;
import jrm.server.shared.TempFileInputStream;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.EnhancedXMLStreamWriter;

public abstract class XMLResponse implements Closeable
{
	protected XMLRequest request;
	private final Path tmpfile;
	private final OutputStream out;
	protected final EnhancedXMLStreamWriter writer;
	protected PathAbstractor pathAbstractor;

	protected XMLResponse(XMLRequest request) throws Exception
	{
		this.request = request;
		pathAbstractor = new PathAbstractor(request.getSession());
		final var attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---"));
		tmpfile = Files.createTempFile("JRM", null, attr);
		out = new BufferedOutputStream(Files.newOutputStream(tmpfile));
		writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(out));
		writer.writeStartDocument("utf-8", "1.0");
	}

	private void processOperation(Operation operation) throws Exception
	{
		switch (operation.getOperationType().toString())
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
			case "custom":
				custom(operation);
				break;
			default:
				failure(operation.getOperationType() + " not implemented");
				break;
		}
	}

	public TempFileInputStream processRequest() throws Exception
	{
		if (request.getTransaction() != null)
		{
			writer.writeStartElement("responses");
			for (Operation operation : request.getTransaction().getOperations())
				processOperation(operation);
			writer.writeEndElement();
		}
		else
			processOperation(request.getOperation());
		return new TempFileInputStream(tmpfile.toFile());
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

	protected void custom(Operation operation) throws Exception
	{
		failure("custom operation not implemented");
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
			Log.err(e.getMessage(), e);
		}
		finally
		{
			out.close();
		}
	}

	protected void error(int status) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement("status", Integer.toString(status));
		writer.writeEndElement();
	}

	protected void error(int status, String data) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement("status", Integer.toString(status));
		writer.writeElement("data", data);
		writer.writeEndElement();
	}

	protected void error(int status, Map<String, List<String>> data) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement("status", Integer.toString(status));
		if (data != null)
		{
			writer.writeStartElement("errors");
			for (Map.Entry<String, List<String>> entry : data.entrySet())
			{
				writer.writeStartElement(entry.getKey());
				for (String msg : entry.getValue())
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

	@FunctionalInterface
	protected interface fetchArrayCallback
	{
		public void apply(int idx, int count);
	}

	protected void fetch_array(Operation operation, int count, fetchArrayCallback cb) throws Exception
	{
		int start, end;
		writer.writeElement("startRow", Integer.toString(start = Math.min(count - 1, operation.getStartRow())));
		writer.writeElement("endRow", Integer.toString(end = Math.min(count - 1, operation.getEndRow())));
		writer.writeElement("totalRows", Integer.toString(count));
		writer.writeStartElement("data");
		if (count > 0)
			for (int i = start; i <= end; i++)
				cb.apply(i, count);
		writer.writeEndElement();
	}

	@FunctionalInterface
	protected interface fetchListCallback<T>
	{
		public void apply(T obj, int idx);
	}

	protected <T> void fetch_list(Operation operation, List<T> list, fetchListCallback<T> cb) throws Exception
	{
		final int start;
		final int end;
		final int count = list.size();
		writer.writeElement("startRow", Integer.toString(start = Math.min(count - 1, operation.getStartRow())));
		writer.writeElement("endRow", Integer.toString(end = Math.min(count - 1, operation.getEndRow())));
		writer.writeElement("totalRows", Integer.toString(count));
		writer.writeStartElement("data");
		if (count > 0)
			for (int idx = start; idx <= end; idx++)
				cb.apply(list.get(idx), idx);
		writer.writeEndElement();
	}

	@FunctionalInterface
	protected interface fetchStreamCallback<T>
	{
		public void apply(T t);
	}

	protected <T> void fetch_stream(Operation operation, Stream<T> stream, fetchStreamCallback<T> cb) throws Exception
	{
		final int start = operation.getStartRow();
		final int end = operation.getEndRow();
		final var range = Range.between(start, end);
		final int[] count = { 0 };
		writer.writeStartElement("data");
		stream.filter(o -> range.contains(++count[0])).forEachOrdered(cb::apply);
		writer.writeEndElement();
		writer.writeElement("startRow", Integer.toString(Math.min(count[0] - 1, start)));
		writer.writeElement("endRow", Integer.toString(Math.min(count[0] - 1, end)));
		writer.writeElement("totalRows", Integer.toString(count[0]));
	}

}
