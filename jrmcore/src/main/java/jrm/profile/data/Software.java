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
package jrm.profile.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Profile;
import jrm.profile.data.Software.Part.DataArea;
import jrm.profile.data.Software.Part.DiskArea;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;
import lombok.Getter;
import lombok.Setter;

/**
 * This define a MESS software
 * @author optyfr
 */
@SuppressWarnings("serial")
public class Software extends Anyware implements Serializable
{
	public Software(Profile profile)
	{
		super(profile);
	}

	/**
	 * The publisher name
	 */
	private final @Getter StringBuilder publisher = new StringBuilder();
	/**
	 * Is this software supported, default to yes
	 */
	private @Getter @Setter Supported supported = Supported.yes;
	/**
	 * The software compatibility string (a list of machine dependents tags separated with commas)
	 */
	private @Getter @Setter String compatibility = null;
	/**
	 * The {@link Part}s list associated with the software
	 */
	private final @Getter List<Part> parts = new ArrayList<>();

	/**
	 * The software list from which came this software
	 */
	private @Getter @Setter SoftwareList sl = null;

	/**
	 * The Supported values definition
	 */
	public enum Supported implements Serializable
	{
		no,	//NOSONAR
		partial,	//NOSONAR
		yes;	//NOSONAR	// default value

		public Supported getXML()
		{
			return this==yes?null:this;
		}
	}

	/**
	 * Part of Data/Disk areas
	 */
	public static class Part implements Serializable
	{
		/**
		 * Data area containing {@link Rom}s and various defs of the area
		 */
		public static class DataArea implements Serializable
		{
			/**
			 * words indianness
			 */
			public enum Endianness implements Serializable
			{
				big,	//NOSONAR
				little;	//NOSONAR	// default value

				public Endianness getXML()
				{
					return this==little?null:this;
				}

			}

			/**
			 * name of this data area
			 */
			private @Setter String name;
			/**
			 * total rom size in this data area
			 */
			private @Setter  int size;
			/**
			 * number of bits for ??? (not documented and not used by mame)
			 */
			private @Setter  int databits = 8;
			/**
			 * byte ordering
			 */
			private @Setter  Endianness endianness = Endianness.little;
			/**
			 * list of roms
			 */
			private @Getter List<Rom> roms = new ArrayList<>();
		}

		/**
		 * Disk area containing {@link Disk}s
		 */
		public static class DiskArea implements Serializable
		{
			/**
			 * name of this disk area
			 */
			private @Setter String name;
			/**
			 * list of disks
			 */
			private @Getter List<Disk> disks = new ArrayList<>();
		}

		/**
		 * name of the part
		 */
		private @Setter String name;
		/**
		 * the interface used to load this part 
		 */
		private @Getter @Setter String intrface;
		/**
		 * The {@link List} of {@link DataArea}s
		 */
		private @Getter List<DataArea> dataareas = new ArrayList<>();
		/**
		 * The {@link List} of {@link DiskArea}s
		 */
		private @Getter List<DiskArea> diskareas = new ArrayList<>();
	}

	@Override
	public Software getParent()
	{
		return getParent(Software.class);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getFullName()
	{
		return sl.name + File.separator + name;
	}

	@Override
	public String getFullName(final String filename)
	{
		return sl.name + File.separator + filename;
	}

	@Override
	public boolean isBios()
	{
		return false;
	}

	@Override
	public boolean isRomOf()
	{
		return false;
	}

	@Override
	public Type getType()
	{
		return Type.SOFTWARELIST;
	}

	@Override
	public Systm getSystem()
	{
		return sl;
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param entries can be null, if specified, will filter according this entries list
	 * @throws XMLStreamException
	 */
	public void export(final EnhancedXMLStreamWriter writer, Collection<Entry> entries) throws XMLStreamException
	{
		writer.writeStartElement("software", //$NON-NLS-1$
				new SimpleAttribute("name", name), //$NON-NLS-1$
				new SimpleAttribute("cloneof", cloneof), //$NON-NLS-1$
				new SimpleAttribute("supported", supported.getXML()) //$NON-NLS-1$
				);
		writer.writeElement("description", description); //$NON-NLS-1$
		if (year.length() > 0)
			writer.writeElement("year", year); //$NON-NLS-1$
		if (publisher.length() > 0)
			writer.writeElement("publisher", publisher); //$NON-NLS-1$
		for(final Part part : parts)
		{
			writer.writeStartElement("part", //$NON-NLS-1$
					new SimpleAttribute("name", part.name), //$NON-NLS-1$
					new SimpleAttribute("interface", part.intrface) //$NON-NLS-1$
					);
			exportRoms(writer, entries, part);
			exportDisks(writer, entries, part);
			writer.writeEndElement();
		}
		writer.writeEndElement();

	}

	/**
	 * @param writer
	 * @param entries
	 * @param part
	 * @throws XMLStreamException
	 */
	@SuppressWarnings("unlikely-arg-type")
	private void exportRoms(final EnhancedXMLStreamWriter writer, Collection<Entry> entries, final Part part) throws XMLStreamException
	{
		for(final DataArea dataarea : part.dataareas)
		{
			writer.writeStartElement("dataarea", //$NON-NLS-1$
					new SimpleAttribute("name", dataarea.name), //$NON-NLS-1$
					new SimpleAttribute("size", dataarea.size), //$NON-NLS-1$
					new SimpleAttribute("width", dataarea.databits), //$NON-NLS-1$
					new SimpleAttribute("endianness", dataarea.endianness.getXML()) //$NON-NLS-1$
					);
			for(final Rom r : dataarea.roms)
				if(entries==null || entries.contains(r))	//NOSONAR
					r.export(writer,true);
			writer.writeEndElement();
		}
	}

	/**
	 * @param writer
	 * @param entries
	 * @param part
	 * @throws XMLStreamException
	 */
	@SuppressWarnings("unlikely-arg-type")
	private void exportDisks(final EnhancedXMLStreamWriter writer, Collection<Entry> entries, final Part part) throws XMLStreamException
	{
		for(final DiskArea diskarea : part.diskareas)
		{
			writer.writeStartElement("diskarea", //$NON-NLS-1$
					new SimpleAttribute("name", diskarea.name) //$NON-NLS-1$
					);
			for(final Disk d : diskarea.disks)
				if(entries==null || entries.contains(d))	//NOSONAR
					d.export(writer,true);
			writer.writeEndElement();
		}
	}

	@Override
	public CharSequence getDescription()
	{
		return description;
	}

	@Override
	Stream<Rom> streamWithDevices(boolean excludeBios, boolean partial, boolean recurse)
	{
		return getRoms().stream();
	}

	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
}
