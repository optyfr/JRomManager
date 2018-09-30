package jrm.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class XMLRequest
{
	StringBuffer operationType = new StringBuffer();
	Map<String,String> data = new HashMap<>();

	public XMLRequest(InputStream in, int len) throws IOException
	{
		File file = File.createTempFile("JRMSRV", null);
		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));)
		{
			for (int i = 0; i < len; i++)
				out.write(in.read());
			out.close();
		}

		try
		{
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			final SAXParser parser = factory.newSAXParser();
			parser.parse(new TempFileInputStream(file), new org.xml.sax.helpers.DefaultHandler()
			{
				boolean isRequest = false;
				boolean inOperationType = false;
				boolean inData = false;
				StringBuffer datavalue = new StringBuffer();

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
				{
					if (qName.equals("request"))
						isRequest = true;
					else if (isRequest)
					{
						switch (qName)
						{
							case "operationType":
								inOperationType = true;
								break;
							case "data":
								inData = true;
								break;
							default:
								if(inData)
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
						case "data":
							inData = false;
							break;
						default:
							if(inData)
							{
								data.put(qName, datavalue.toString());
								datavalue.setLength(0);
							}
							break;
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException
				{
					if (inOperationType)
					{
						operationType.append(ch, start, length);
					}
					else if(inData)
					{
						datavalue.append(ch, start, length);
					}
				}
			});
		}
		catch (ParserConfigurationException | SAXException e)
		{
			e.printStackTrace();
		}
	}

}
