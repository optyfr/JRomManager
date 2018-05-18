package jrm.profile.data;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Export.SimpleAttribute;
import jrm.profile.data.Software.Part.DataArea;
import jrm.profile.data.Software.Part.DiskArea;

@SuppressWarnings("serial")
public class Software extends Anyware implements Serializable
{
	public final StringBuffer publisher = new StringBuffer();
	public Supported supported = Supported.yes;
	public String compatibility = null;
	public final List<Part> parts = new ArrayList<>();

	public SoftwareList sl = null;

	public enum Supported implements Serializable
	{
		no,
		partial,
		yes;

		public Supported getXML()
		{
			return this==yes?null:this;
		}
	};

	public static class Part implements Serializable
	{
		public static class DataArea implements Serializable
		{
			public enum Endianness implements Serializable
			{
				big,
				little;

				public Endianness getXML()
				{
					return this==little?null:this;
				}

			}

			public String name;
			public int size;
			public int width = 8;
			public Endianness endianness = Endianness.little;
			public List<Rom> roms = new ArrayList<>();
		}

		public static class DiskArea implements Serializable
		{
			public String name;
			public List<Disk> disks = new ArrayList<>();
		}

		public String name;
		public String intrface;
		public List<DataArea> dataareas = new ArrayList<>();
		public List<DiskArea> diskareas = new ArrayList<>();
	}

	public Software()
	{
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
						new SimpleAttribute("width", dataarea.width), //$NON-NLS-1$
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

}
