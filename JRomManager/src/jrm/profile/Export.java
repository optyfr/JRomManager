package jrm.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jrm.profile.data.SoftwareList;
import jrm.ui.ProgressHandler;

public class Export
{
	public final static class SimpleAttribute
	{
		private String name;
		private Object value;

		public SimpleAttribute(String name, Object value)
		{
			this.name = name;
			this.value = value;
		}
	}

	public final static class EnhancedXMLStreamWriter implements XMLStreamWriter
	{
		private static enum Seen
		{
			NOTHING,
			ELEMENT,
			DATA;
		}

		private final XMLStreamWriter writer;
		private final String indentStep;
		private final Deque<Seen> stateStack = new ArrayDeque<Seen>();
		private Seen state = Seen.NOTHING;
		private int depth = 0;

		public EnhancedXMLStreamWriter(XMLStreamWriter writer)
		{
			this(writer, "\t");
		}

		public EnhancedXMLStreamWriter(XMLStreamWriter writer, String indentStep)
		{
			this.writer = writer;
			this.indentStep = indentStep;
		}

		@Override
		public void close() throws XMLStreamException
		{
			writer.close();
		}

		@Override
		public void flush() throws XMLStreamException
		{
			writer.flush();
		}

		@Override
		public NamespaceContext getNamespaceContext()
		{
			return writer.getNamespaceContext();
		}

		@Override
		public String getPrefix(String uri) throws XMLStreamException
		{
			return writer.getPrefix(uri);
		}

		@Override
		public Object getProperty(String name) throws IllegalArgumentException
		{
			return writer.getProperty(name);
		}

		@Override
		public void setDefaultNamespace(String uri) throws XMLStreamException
		{
			writer.setDefaultNamespace(uri);
		}

		@Override
		public void setNamespaceContext(NamespaceContext context) throws XMLStreamException
		{
			writer.setNamespaceContext(context);
		}

		@Override
		public void setPrefix(String prefix, String uri) throws XMLStreamException
		{
			writer.setPrefix(prefix, uri);
		}

		@Override
		public void writeAttribute(String localName, String value) throws XMLStreamException
		{
			writer.writeAttribute(localName, value);
		}

		@Override
		public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException
		{
			writer.writeAttribute(prefix, namespaceURI, localName, value);
		}

		@Override
		public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException
		{
			writer.writeAttribute(namespaceURI, localName, value);
		}

		@Override
		public void writeCData(String data) throws XMLStreamException
		{
			state = Seen.DATA;
			writer.writeCData(data);
		}

		@Override
		public void writeCharacters(String text) throws XMLStreamException
		{
			state = Seen.DATA;
			writer.writeCharacters(text);
		}

		@Override
		public void writeCharacters(char[] text, int start, int len) throws XMLStreamException
		{
			state = Seen.DATA;
			writer.writeCharacters(text, start, len);
		}

		@Override
		public void writeComment(String data) throws XMLStreamException
		{
			writer.writeComment(data);
		}

		@Override
		public void writeDTD(String dtd) throws XMLStreamException
		{
			writer.writeDTD(dtd);
		}

		@Override
		public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException
		{
			writer.writeDefaultNamespace(namespaceURI);
		}

		@Override
		public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException
		{
			onEmptyElement();
			writer.writeEmptyElement(namespaceURI, localName);
		}

		@Override
		public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
		{
			onEmptyElement();
			writer.writeEmptyElement(prefix, localName, namespaceURI);
		}

		@Override
		public void writeEmptyElement(String localName) throws XMLStreamException
		{
			onEmptyElement();
			writer.writeEmptyElement(localName);
		}

		@Override
		public void writeEndDocument() throws XMLStreamException
		{
			writer.writeEndDocument();
		}

		@Override
		public void writeEndElement() throws XMLStreamException
		{
			onEndElement();
			writer.writeEndElement();
		}

		@Override
		public void writeEntityRef(String name) throws XMLStreamException
		{
			writer.writeEntityRef(name);
		}

		@Override
		public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException
		{
			writer.writeNamespace(prefix, namespaceURI);
		}

		@Override
		public void writeProcessingInstruction(String target) throws XMLStreamException
		{
			writer.writeProcessingInstruction(target);
		}

		@Override
		public void writeProcessingInstruction(String target, String data) throws XMLStreamException
		{
			writer.writeProcessingInstruction(target, data);
		}

		@Override
		public void writeStartDocument() throws XMLStreamException
		{
			writer.writeStartDocument();

			if(indentStep != null)
			{
				doNewline();
			}
		}

		@Override
		public void writeStartDocument(String version) throws XMLStreamException
		{
			writer.writeStartDocument(version);

			if(indentStep != null)
			{
				doNewline();
			}
		}

		@Override
		public void writeStartDocument(String encoding, String version) throws XMLStreamException
		{
			writer.writeStartDocument(encoding, version);

			if(indentStep != null)
			{
				doNewline();
			}
		}

		@Override
		public void writeStartElement(String localName) throws XMLStreamException
		{
			onStartElement();
			writer.writeStartElement(localName);
		}

		@Override
		public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException
		{
			onStartElement();
			writer.writeStartElement(namespaceURI, localName);
		}

		@Override
		public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException
		{
			onStartElement();
			writer.writeStartElement(prefix, localName, namespaceURI);
		}

		private void doIndent() throws XMLStreamException
		{
			if(indentStep != null)
			{
				for(int i = 0; i < depth; i++)
				{
					writer.writeCharacters(indentStep);
				}
			}
		}

		private void doNewline() throws XMLStreamException
		{
			writer.writeCharacters("\n");
		}

		private void onEmptyElement() throws XMLStreamException
		{
			state = Seen.ELEMENT;

			if((indentStep != null) && (depth > 0))
			{
				doNewline();
				doIndent();
			}
		}

		private void onEndElement() throws XMLStreamException
		{
			depth--;

			if((indentStep != null) && (state == Seen.ELEMENT))
			{
				doNewline();
				doIndent();
			}

			state = stateStack.removeFirst();
		}

		private void onStartElement() throws XMLStreamException
		{
			stateStack.addFirst(Seen.ELEMENT);
			state = Seen.NOTHING;

			if((indentStep != null) && (depth > 0))
			{
				doNewline();
				doIndent();
			}

			depth++;
		}

		public void writeElement(String localName, SimpleAttribute... attributes) throws XMLStreamException
		{
			writeEmptyElement(localName);
			if(attributes != null)
				for(SimpleAttribute attr : attributes)
					if(attr.value != null)
						writeAttribute(attr.name, attr.value.toString());
		}

		public void writeStartElement(String localName, SimpleAttribute... attributes) throws XMLStreamException
		{
			writeStartElement(localName);
			if(attributes != null)
				for(SimpleAttribute attr : attributes)
					if(attr.value != null)
						writeAttribute(attr.name, attr.value.toString());
		}

		public void writeElement(String localName, CharSequence text, SimpleAttribute... attributes) throws XMLStreamException
		{
			writeStartElement(localName, attributes);
			if(text != null)
				writeCharacters(text.toString());
			writeEndElement();
		}
	}

	public enum ExportType
	{
		MAME,
		DATAFILE,
		SOFTWARELIST
	}

	public Export(Profile profile, File file, ExportType type, boolean filtered, SoftwareList selection, ProgressHandler progress)
	{
		EnhancedXMLStreamWriter writer = null;
		try(FileOutputStream fos = new FileOutputStream(file))
		{
			writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8"));
			switch(type)
			{
				case MAME:
					profile.machinelist_list.export(writer, progress, true, filtered);
					break;
				case DATAFILE:
					profile.machinelist_list.export(writer, progress, false, filtered);
					break;
				case SOFTWARELIST:
					profile.machinelist_list.softwarelist_list.export(writer, progress, filtered, selection);
					break;
			}
			writer.close();
		}
		catch(FactoryConfigurationError | XMLStreamException | IOException e)
		{
			e.printStackTrace();
		}
	}

}
