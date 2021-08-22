package jrm.server.shared.datasources;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import jrm.misc.ExceptionUtils;
import jrm.misc.Log;
import jrm.server.shared.TempFileInputStream;
import jrm.server.shared.WebSession;
import jrm.server.shared.datasources.XMLRequest.Operation.Sorter;
import lombok.Getter;

public class XMLRequest
{

	public static class Operation
	{
		public static class Sorter
		{
			String name;
			boolean desc = false;

			public Sorter(String value)
			{
				if (value.length() > 0 && value.charAt(0) == '-')
				{
					desc = true;
					name = value.substring(1);
				}
				else
					name = value;
			}

			/**
			 * @return the name
			 */
			public String getName()
			{
				return name;
			}

			/**
			 * @return the desc
			 */
			public boolean isDesc()
			{
				return desc;
			}
		}

		StringBuilder operationType = new StringBuilder();
		StringBuilder operationId = new StringBuilder();
		int startRow = 0;
		int endRow = Integer.MAX_VALUE;
		List<Sorter> sort = new ArrayList<>();
		private Map<String, List<String>> data = new HashMap<>();
		Map<String, String> oldValues = new HashMap<>();

		public boolean hasData(String key)
		{
			return data.containsKey(key);
		}

		public String getData(String key)
		{
			if (data.containsKey(key))
			{
				List<String> value = data.get(key);
				if (!value.isEmpty())
					return value.get(0);
			}
			return null;
		}

		boolean addData(String key, String value)
		{
			return data.computeIfAbsent(key, v -> new ArrayList<>()).add(value);
		}

		public List<String> getDatas(String key)
		{
			return data.get(key);
		}

		/**
		 * @return the sort
		 */
		public List<Sorter> getSort()
		{
			return sort;
		}

		/**
		 * @return the operationId
		 */
		public StringBuilder getOperationId()
		{
			return operationId;
		}

		/**
		 * @return the operationType
		 */
		public StringBuilder getOperationType()
		{
			return operationType;
		}

		/**
		 * @return the startRow
		 */
		public int getStartRow()
		{
			return startRow;
		}

		/**
		 * @return the endRow
		 */
		public int getEndRow()
		{
			return endRow;
		}

		/**
		 * @return the oldValues
		 */
		public Map<String, String> getOldValues()
		{
			return oldValues;
		}
	}

	public class Transaction
	{
		List<Operation> operations = new ArrayList<>();

		/**
		 * @return the operations
		 */
		public List<Operation> getOperations()
		{
			return operations;
		}
	}

	Operation operation = null;

	Transaction transaction = null;

	@Getter
	WebSession session;

	public XMLRequest(WebSession session, InputStream in, long len) throws IOException
	{
		this.session = session;
		try
		{
			final var factory = SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			final var parser = factory.newSAXParser();
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			parser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			try (final var tfis = TempFileInputStream.newInstance(in, len))
			{
				parser.parse(tfis, new XMLRequestHandler());
			}
		}
		catch (ParserConfigurationException | SAXException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	/**
	 * @return the transaction
	 */
	public Transaction getTransaction()
	{
		return transaction;
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation()
	{
		return operation;
	}

	private final class XMLRequestHandler extends org.xml.sax.helpers.DefaultHandler
	{
		boolean isRequest = false;
		boolean inOperationType = false;
		boolean inOperationId = false;
		boolean inStartRow = false;
		boolean inEndRow = false;
		boolean inSortBy = false;
		boolean inData = false;
		boolean inOldValues = false;
		StringBuilder datavalue = new StringBuilder();
		Operation currentRequest;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if (qName.equals("request"))
			{
				currentRequest = new Operation();
				if (getTransaction() != null)
					getTransaction().getOperations().add(currentRequest);
				else
					operation = currentRequest;
				isRequest = true;
			}
			else if (qName.equals("transaction"))
				transaction = new Transaction();
			else if (isRequest)
			{
				switch (qName)
				{
					case "operationType":
						inOperationType = true;
						break;
					case "operationId":
						inOperationId = true;
						break;
					case "startRow":
						inStartRow = true;
						datavalue.setLength(0);
						break;
					case "endRow":
						inEndRow = true;
						datavalue.setLength(0);
						break;
					case "sortBy":
						inSortBy = true;
						datavalue.setLength(0);
						break;
					case "data":
						inData = true;
						break;
					case "oldValues":
						inOldValues = true;
						break;
					default:
						if (inData || inOldValues)
							datavalue.setLength(0);
						break;
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			switch (qName)
			{
				case "operationType":
					inOperationType = false;
					break;
				case "operationId":
					inOperationId = false;
					break;
				case "startRow":
					inStartRow = false;
					ExceptionUtils.unthrow(start -> currentRequest.startRow = start, Integer::parseInt, datavalue.toString());
					datavalue.setLength(0);
					break;
				case "endRow":
					inEndRow = false;
					ExceptionUtils.unthrow(end -> currentRequest.endRow = end, Integer::parseInt, datavalue.toString());
					datavalue.setLength(0);
					break;
				case "sortBy":
					inSortBy = false;
					currentRequest.getSort().add(new Sorter(datavalue.toString()));
					datavalue.setLength(0);
					break;
				case "data":
					inData = false;
					break;
				case "oldValues":
					inOldValues = false;
					break;
				default:
					if (inData)
					{
						currentRequest.addData(qName, datavalue.toString());
						datavalue.setLength(0);
					}
					else if (inOldValues)
					{
						currentRequest.getOldValues().put(qName, datavalue.toString());
						datavalue.setLength(0);
					}
					break;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			if (inOperationType)
				currentRequest.getOperationType().append(ch, start, length);
			else if (inOperationId)
				currentRequest.getOperationId().append(ch, start, length);
			else if (inStartRow || inEndRow)
				datavalue.append(ch, start, length);
			else if (inSortBy)
				datavalue.append(ch, start, length);
			else if (inData)
				datavalue.append(ch, start, length);
			else if (inOldValues)
				datavalue.append(ch, start, length);
		}
	}
}
