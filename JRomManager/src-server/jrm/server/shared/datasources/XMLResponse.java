package jrm.server.shared.datasources;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.Range;

import jrm.misc.IOUtils;
import jrm.misc.Log;
import jrm.security.PathAbstractor;
import jrm.server.shared.TempFileInputStream;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.EnhancedXMLStreamWriter;

public abstract class XMLResponse implements Closeable
{
	private static final String TOTAL_ROWS = "totalRows";
	private static final String END_ROW = "endRow";
	private static final String START_ROW = "startRow";
	private static final String STATUS = "status";
	private static final String RESPONSE = "response";
	protected XMLRequest request;
	private final Path tmpfile;
	private final OutputStream out;
	protected final EnhancedXMLStreamWriter writer;
	protected PathAbstractor pathAbstractor;

	protected XMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		this.request = request;
		pathAbstractor = new PathAbstractor(request.getSession());
		tmpfile = IOUtils.createTempFile("JRM", null);
		out = new BufferedOutputStream(Files.newOutputStream(tmpfile));
		writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newFactory().createXMLStreamWriter(out));
		writer.writeStartDocument("utf-8", "1.0");
	}

	private void processOperation(Operation operation) throws XMLStreamException, IOException
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

	public TempFileInputStream processRequest() throws XMLStreamException, IOException
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
		writer.flush();
		out.flush();
		return new TempFileInputStream(tmpfile.toFile());
	}

	protected void fetch(Operation operation) throws XMLStreamException, IOException	//NOSONAR
	{
		failure("fetch operation not implemented");
	}

	protected void add(Operation operation) throws XMLStreamException, IOException	//NOSONAR
	{
		failure("add operation not implemented");
	}

	protected void update(Operation operation) throws XMLStreamException, IOException	//NOSONAR
	{
		failure("update operation not implemented");
	}

	protected void remove(Operation operation) throws XMLStreamException, IOException	//NOSONAR
	{
		failure("delete operation not implemented");
	}

	protected void custom(Operation operation) throws XMLStreamException, IOException	//NOSONAR
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
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, Integer.toString(status));
		writer.writeEndElement();
	}

	protected void error(int status, String data) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, Integer.toString(status));
		writer.writeElement("data", data);
		writer.writeEndElement();
	}

	protected void error(int status, Map<String, List<String>> data) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, Integer.toString(status));
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

	protected void noError() throws XMLStreamException
	{
		error(0);
	}

	protected void success() throws XMLStreamException
	{
		error(0);
	}

	protected void otherError(String msg) throws XMLStreamException
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

	protected void loginIncorrect() throws XMLStreamException
	{
		error(-5);
	}

	protected void loginRequired() throws XMLStreamException
	{
		error(-7);
	}

	@FunctionalInterface
	protected interface FetchArrayCallback
	{
		public void apply(int idx, int count);
	}

	protected void fetchArray(Operation operation, int count, FetchArrayCallback cb) throws XMLStreamException
	{
		final int start = Math.min(count - 1, operation.getStartRow());
		final int end = Math.min(count - 1, operation.getEndRow());
		writer.writeElement(START_ROW, Integer.toString(start));
		writer.writeElement(END_ROW, Integer.toString(end));
		writer.writeElement(TOTAL_ROWS, Integer.toString(count));
		writer.writeStartElement("data");
		if (count > 0)
			for (int i = start; i <= end; i++)
				cb.apply(i, count);
		writer.writeEndElement();
	}

	@FunctionalInterface
	protected interface FetchListCallback<T>
	{
		public void apply(T obj, int idx) throws XMLStreamException;
	}

	protected <T> void fetchList(Operation operation, List<T> list, FetchListCallback<T> cb) throws XMLStreamException
	{
		final int count = list.size();
		final int start = Math.min(count - 1, operation.getStartRow());
		final int end = Math.min(count - 1, operation.getEndRow());
		writer.writeElement(START_ROW, Integer.toString(start));
		writer.writeElement(END_ROW, Integer.toString(end));
		writer.writeElement(TOTAL_ROWS, Integer.toString(count));
		writer.writeStartElement("data");
		if (count > 0)
			for (int idx = start; idx <= end; idx++)
				cb.apply(list.get(idx), idx);
		writer.writeEndElement();
	}

	@FunctionalInterface
	protected interface FetchStreamCallback<T>
	{
		public void apply(T t);
	}

	protected <T> void fetchStream(Operation operation, Stream<T> stream, FetchStreamCallback<T> cb) throws XMLStreamException
	{
		final int start = operation.getStartRow();
		final int end = operation.getEndRow();
		final var range = Range.between(start, end);
		final int[] count = { 0 };
		writer.writeStartElement("data");
		stream.filter(o -> range.contains(++count[0])).forEachOrdered(cb::apply);
		writer.writeEndElement();
		writer.writeElement(START_ROW, Integer.toString(Math.min(count[0] - 1, start)));
		writer.writeElement(END_ROW, Integer.toString(Math.min(count[0] - 1, end)));
		writer.writeElement(TOTAL_ROWS, Integer.toString(count[0]));
	}

}
