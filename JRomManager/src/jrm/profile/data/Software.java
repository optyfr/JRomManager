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
	public StringBuffer publisher = new StringBuffer();
	public Supported supported = Supported.yes;
	public String compatibility = null;
	public List<Part> parts = new ArrayList<>();

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
	public String getFullName(String filename)
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
	
	public void export(EnhancedXMLStreamWriter writer) throws XMLStreamException, IOException
	{
		writer.writeStartElement("software", 
				new SimpleAttribute("name", name),
				new SimpleAttribute("cloneof", cloneof),
				new SimpleAttribute("supported", supported.getXML())
		);
		writer.writeElement("description", description);
		if(year!=null && year.length()>0)
			writer.writeElement("year", year);
		if(publisher!=null && publisher.length()>0)
			writer.writeElement("publisher", publisher);
		for(Part part : parts)
		{
			writer.writeStartElement("part", 
					new SimpleAttribute("name", part.name),
					new SimpleAttribute("interface", part.intrface)
			);
			for(DataArea dataarea : part.dataareas)
			{
				writer.writeStartElement("dataarea", 
						new SimpleAttribute("name", dataarea.name),
						new SimpleAttribute("size", dataarea.size),
						new SimpleAttribute("width", dataarea.width),
						new SimpleAttribute("endianness", dataarea.endianness.getXML())
				);
				for(Rom r : dataarea.roms)
					r.export(writer,true);
				writer.writeEndElement();
			}
			for(DiskArea diskarea : part.diskareas)
			{
				writer.writeStartElement("diskarea", 
						new SimpleAttribute("name", diskarea.name)
				);
				for(Disk d : diskarea.disks)
					d.export(writer,true);
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		writer.writeEndElement();
		
	}

}
