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
package jrm.profile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JOptionPane;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jrm.profile.data.SoftwareList;
import jrm.ui.ProgressHandler;

/**
 * Export a profile into one of the {@link ExportType}
 * @author optyfr
 *
 */
public class Export
{
	/**
	 * A simple attribute only with {@link #name} and {@link #value}
	 */
	public final static class SimpleAttribute
	{
		private final String name;
		private final Object value;

		public SimpleAttribute(final String name, final Object value)
		{
			this.name = name;
			this.value = value;
		}
	}

	/**
	 * An XMLStreamWriter with indentation and line return
	 */
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
		private final Deque<Seen> stateStack = new ArrayDeque<>();
		private Seen state = Seen.NOTHING;
		private int depth = 0;

		public EnhancedXMLStreamWriter(final XMLStreamWriter writer)
		{
			this(writer, "\t"); //$NON-NLS-1$
		}

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
			writer.writeAttribute(localName, value);
		}

		@Override
		public void writeAttribute(final String prefix, final String namespaceURI, final String localName, final String value) throws XMLStreamException
		{
			writer.writeAttribute(prefix, namespaceURI, localName, value);
		}

		@Override
		public void writeAttribute(final String namespaceURI, final String localName, final String value) throws XMLStreamException
		{
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
			writer.writeCharacters("\n"); //$NON-NLS-1$
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

		public void writeElement(final String localName, final SimpleAttribute... attributes) throws XMLStreamException
		{
			writeEmptyElement(localName);
			if(attributes != null)
				for(final SimpleAttribute attr : attributes)
					if(attr.name != null && !attr.name.isEmpty() && attr.value != null)
						writeAttribute(attr.name, attr.value.toString());
		}

		public void writeStartElement(final String localName, final SimpleAttribute... attributes) throws XMLStreamException
		{
			writeStartElement(localName);
			if(attributes != null)
				for(final SimpleAttribute attr : attributes)
					if(attr.name != null && !attr.name.isEmpty() && attr.value != null)
						writeAttribute(attr.name, attr.value.toString());
		}

		public void writeElement(final String localName, final CharSequence text, final SimpleAttribute... attributes) throws XMLStreamException
		{
			writeStartElement(localName, attributes);
			if(text != null)
				writeCharacters(text.toString());
			writeEndElement();
		}
	}

	/**
	 * The supported export types enum
	 */
	public enum ExportType
	{
		/**
		 * Export into latest Mame format
		 */
		MAME,
		/**
		 * Export into Logiqx datfile format
		 */
		DATAFILE,
		/**
		 * Export Software list(s) using Mame software list format
		 */
		SOFTWARELIST
	}

	/**
	 * Will export a {@code profile} to a {@code file} in the {@code type} format, {@code filtered} (or not), only a {@code selection} SoftwareList (or none), and show a {@code progress} bar
	 * @param profile the {@link Profile} to export
	 * @param file the destination {@link File}
	 * @param type the {@link ExportType} format
	 * @param filtered whether we should use selected filter or not
	 * @param selection if {@link ExportType#SOFTWARELIST} type, will export only the selected {@link SoftwareList}, null to export all software lists in a single file
	 * @param progress optional {@link ProgressHandler} to show export progression
	 */
	public Export(final Profile profile, final File file, final ExportType type, final boolean filtered, final SoftwareList selection, final ProgressHandler progress)
	{
		EnhancedXMLStreamWriter writer = null;
		try(FileOutputStream fos = new FileOutputStream(file))
		{
			writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8")); //$NON-NLS-1$
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
			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			e.printStackTrace();
		}
	}

}
