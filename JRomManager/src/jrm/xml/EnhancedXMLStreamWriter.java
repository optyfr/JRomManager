/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.xml;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * An XMLStreamWriter with indentation and line return.
 *
 * @author optyfr
 */
public final class EnhancedXMLStreamWriter implements XMLStreamWriter
{
	
	/**
	 * The Enum Seen.
	 *
	 * @author optyfr
	 */
	private enum Seen
	{
		
		/** The nothing. */
		NOTHING,
		
		/** The element. */
		ELEMENT,
		
		/** The data. */
		DATA;
	}

	/** The writer. */
	private final XMLStreamWriter writer;
	
	/** The indent step. */
	private final String indentStep;
	
	/** The state stack. */
	private final Deque<EnhancedXMLStreamWriter.Seen> stateStack = new ArrayDeque<>();
	
	/** The state. */
	private EnhancedXMLStreamWriter.Seen state = Seen.NOTHING;
	
	/** The depth. */
	private int depth = 0;

	/**
	 * Instantiates a new enhanced XML stream writer.
	 *
	 * @param writer the writer
	 */
	public EnhancedXMLStreamWriter(final XMLStreamWriter writer)
	{
		this(writer, "\t"); //$NON-NLS-1$
	}

	/**
	 * Instantiates a new enhanced XML stream writer.
	 *
	 * @param writer the writer
	 * @param indentStep the indent step
	 */
	public EnhancedXMLStreamWriter(final XMLStreamWriter writer, final String indentStep)
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
	public String getPrefix(final String uri) throws XMLStreamException
	{
		return writer.getPrefix(uri);
	}

	@Override
	public Object getProperty(final String name) throws IllegalArgumentException
	{
		return writer.getProperty(name);
	}

	@Override
	public void setDefaultNamespace(final String uri) throws XMLStreamException
	{
		writer.setDefaultNamespace(uri);
	}

	@Override
	public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException
	{
		writer.setNamespaceContext(context);
	}

	@Override
	public void setPrefix(final String prefix, final String uri) throws XMLStreamException
	{
		writer.setPrefix(prefix, uri);
	}

	@Override
	public void writeAttribute(final String localName, final String value) throws XMLStreamException
	{
		if(value!=null)
			writer.writeAttribute(localName, value);
	}

	@Override
	public void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value) throws XMLStreamException
	{
		if(value!=null)
			writer.writeAttribute(prefix, namespaceURI, localName, value);
	}

	@Override
	public void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException
	{
		if(value!=null)
			writer.writeAttribute(namespaceURI, localName, value);
	}

	@Override
	public void writeCData(final String data) throws XMLStreamException
	{
		state = Seen.DATA;
		writer.writeCData(data);
	}

	@Override
	public void writeCharacters(final String text) throws XMLStreamException
	{
		state = Seen.DATA;
		writer.writeCharacters(text);
	}

	@Override
	public void writeCharacters(final char[] text, final int start, final int len) throws XMLStreamException
	{
		state = Seen.DATA;
		writer.writeCharacters(text, start, len);
	}

	@Override
	public void writeComment(final String data) throws XMLStreamException
	{
		writer.writeComment(data);
	}

	@Override
	public void writeDTD(final String dtd) throws XMLStreamException
	{
		writer.writeDTD(dtd);
	}

	@Override
	public void writeDefaultNamespace(final String namespaceURI) throws XMLStreamException
	{
		writer.writeDefaultNamespace(namespaceURI);
	}

	@Override
	public void writeEmptyElement(final String namespaceURI, final String localName) throws XMLStreamException
	{
		onEmptyElement();
		writer.writeEmptyElement(namespaceURI, localName);
	}

	@Override
	public void writeEmptyElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException
	{
		onEmptyElement();
		writer.writeEmptyElement(prefix, localName, namespaceURI);
	}

	@Override
	public void writeEmptyElement(final String localName) throws XMLStreamException
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
	public void writeEntityRef(final String name) throws XMLStreamException
	{
		writer.writeEntityRef(name);
	}

	@Override
	public void writeNamespace(final String prefix, final String namespaceURI) throws XMLStreamException
	{
		writer.writeNamespace(prefix, namespaceURI);
	}

	@Override
	public void writeProcessingInstruction(final String target) throws XMLStreamException
	{
		writer.writeProcessingInstruction(target);
	}

	@Override
	public void writeProcessingInstruction(final String target, final String data) throws XMLStreamException
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
	public void writeStartDocument(final String version) throws XMLStreamException
	{
		writer.writeStartDocument(version);

		if(indentStep != null)
		{
			doNewline();
		}
	}

	@Override
	public void writeStartDocument(final String encoding, final String version) throws XMLStreamException
	{
		writer.writeStartDocument(encoding, version);

		if(indentStep != null)
		{
			doNewline();
		}
	}

	@Override
	public void writeStartElement(final String localName) throws XMLStreamException
	{
		onStartElement();
		writer.writeStartElement(localName);
	}

	@Override
	public void writeStartElement(final String namespaceURI, final String localName) throws XMLStreamException
	{
		onStartElement();
		writer.writeStartElement(namespaceURI, localName);
	}

	@Override
	public void writeStartElement(final String prefix, final String localName, final String namespaceURI) throws XMLStreamException
	{
		onStartElement();
		writer.writeStartElement(prefix, localName, namespaceURI);
	}

	/**
	 * Do indent.
	 *
	 * @throws XMLStreamException the XML stream exception
	 */
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

	/**
	 * Do newline.
	 *
	 * @throws XMLStreamException the XML stream exception
	 */
	private void doNewline() throws XMLStreamException
	{
		writer.writeCharacters("\n"); //$NON-NLS-1$
	}

	/**
	 * On empty element.
	 *
	 * @throws XMLStreamException the XML stream exception
	 */
	private void onEmptyElement() throws XMLStreamException
	{
		state = Seen.ELEMENT;

		if((indentStep != null) && (depth > 0))
		{
			doNewline();
			doIndent();
		}
	}

	/**
	 * On end element.
	 *
	 * @throws XMLStreamException the XML stream exception
	 */
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

	/**
	 * On start element.
	 *
	 * @throws XMLStreamException the XML stream exception
	 */
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

	/**
	 * Write element.
	 *
	 * @param localName the local name
	 * @param attributes the attributes
	 * @throws XMLStreamException the XML stream exception
	 */
	public void writeElement(final String localName, final SimpleAttribute... attributes) throws XMLStreamException
	{
		writeEmptyElement(localName);
		if(attributes != null)
			for(final SimpleAttribute attr : attributes)
				if(attr.name != null && !attr.name.isEmpty() && attr.value != null)
					writeAttribute(attr.name, attr.value.toString());
	}

	/**
	 * Write start element.
	 *
	 * @param localName the local name
	 * @param attributes the attributes
	 * @throws XMLStreamException the XML stream exception
	 */
	public void writeStartElement(final String localName, final SimpleAttribute... attributes) throws XMLStreamException
	{
		writeStartElement(localName);
		if(attributes != null)
			for(final SimpleAttribute attr : attributes)
				if(attr.name != null && !attr.name.isEmpty() && attr.value != null)
					writeAttribute(attr.name, attr.value.toString());
	}

	/**
	 * Write element.
	 *
	 * @param localName the local name
	 * @param text the text
	 * @param attributes the attributes
	 * @throws XMLStreamException the XML stream exception
	 */
	public void writeElement(final String localName, final CharSequence text, final SimpleAttribute... attributes) throws XMLStreamException
	{
		writeStartElement(localName, attributes);
		if(text != null)
			writeCharacters(text.toString());
		writeEndElement();
	}
	
	/**
	 * Ecrit un attribut en verifiant la validitÃ© du contenu des donnÃ©es
	 * @param code
	 * @param value
	 * @throws XMLStreamException
	 */
	public void write(String code, Object value) throws XMLStreamException
	{
		if(value == null) return;
		if(value instanceof String)
			writer.writeAttribute(code, (String)value);
		else
			writer.writeAttribute(code, value.toString());
	}
	
}