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
import java.util.List;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Export.SimpleAttribute;
import jrm.profile.data.Software.Part.DataArea;
import jrm.profile.data.Software.Part.DiskArea;

/**
 * This define a MESS software
 * @author optyfr
 */
@SuppressWarnings("serial")
public class Software extends Anyware implements Serializable
{
	/**
	 * The publisher name
	 */
	public final StringBuffer publisher = new StringBuffer();
	/**
	 * Is this software supported, default to yes
	 */
	public Supported supported = Supported.yes;
	/**
	 * The software compatibility string (a list of machine dependents tags separated with commas)
	 */
	public String compatibility = null;
	/**
	 * The {@link Part}s list associated with the software
	 */
	public final List<Part> parts = new ArrayList<>();

	/**
	 * The software list from which came this software
	 */
	public SoftwareList sl = null;

	/**
	 * The Supported values definition
	 */
	public enum Supported implements Serializable
	{
		no,
		partial,
		yes;	// default value

		public Supported getXML()
		{
			return this==yes?null:this;
		}
	};

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
				big,
				little;	// default value

				public Endianness getXML()
				{
					return this==little?null:this;
				}

			}

			/**
			 * name of this data area
			 */
			public String name;
			/**
			 * total rom size in this data area
			 */
			public int size;
			/**
			 * number of bits for ??? (not documented and not used by mame)
			 */
			public int databits = 8;
			/**
			 * byte ordering
			 */
			public Endianness endianness = Endianness.little;
			/**
			 * list of roms
			 */
			public List<Rom> roms = new ArrayList<>();
		}

		/**
		 * Disk area containing {@link Disk}s
		 */
		public static class DiskArea implements Serializable
		{
			/**
			 * name of this disk area
			 */
			public String name;
			/**
			 * list of disks
			 */
			public List<Disk> disks = new ArrayList<>();
		}

		/**
		 * name of the part
		 */
		public String name;
		/**
		 * the interface used to load this part 
		 */
		public String intrface;
		/**
		 * The {@link List} of {@link DataArea}s
		 */
		public List<DataArea> dataareas = new ArrayList<>();
		/**
		 * The {@link List} of {@link DiskArea}s
		 */
		public List<DiskArea> diskareas = new ArrayList<>();
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
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void export(final EnhancedXMLStreamWriter writer) throws XMLStreamException, IOException
	{
		writer.writeStartElement("software", //$NON-NLS-1$
				new SimpleAttribute("name", name), //$NON-NLS-1$
				new SimpleAttribute("cloneof", cloneof), //$NON-NLS-1$
				new SimpleAttribute("supported", supported.getXML()) //$NON-NLS-1$
				);
		writer.writeElement("description", description); //$NON-NLS-1$
		if(year!=null && year.length()>0)
			writer.writeElement("year", year); //$NON-NLS-1$
		if(publisher!=null && publisher.length()>0)
			writer.writeElement("publisher", publisher); //$NON-NLS-1$
		for(final Part part : parts)
		{
			writer.writeStartElement("part", //$NON-NLS-1$
					new SimpleAttribute("name", part.name), //$NON-NLS-1$
					new SimpleAttribute("interface", part.intrface) //$NON-NLS-1$
					);
			for(final DataArea dataarea : part.dataareas)
			{
				writer.writeStartElement("dataarea", //$NON-NLS-1$
						new SimpleAttribute("name", dataarea.name), //$NON-NLS-1$
						new SimpleAttribute("size", dataarea.size), //$NON-NLS-1$
						new SimpleAttribute("width", dataarea.databits), //$NON-NLS-1$
						new SimpleAttribute("endianness", dataarea.endianness.getXML()) //$NON-NLS-1$
						);
				for(final Rom r : dataarea.roms)
					r.export(writer,true);
				writer.writeEndElement();
			}
			for(final DiskArea diskarea : part.diskareas)
			{
				writer.writeStartElement("diskarea", //$NON-NLS-1$
						new SimpleAttribute("name", diskarea.name) //$NON-NLS-1$
						);
				for(final Disk d : diskarea.disks)
					d.export(writer,true);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();

	}

	@Override
	public CharSequence getDescription()
	{
		return description;
	}

	@Override
	Stream<Rom> streamWithDevices(boolean excludeBios, boolean partial, boolean recurse)
	{
		return roms.stream();
	}

}
