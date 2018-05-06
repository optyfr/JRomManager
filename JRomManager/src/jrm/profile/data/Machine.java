package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Export.SimpleAttribute;

@SuppressWarnings("serial")
public class Machine extends Anyware implements Serializable
{
	public String romof = null;
	public String sampleof = null;
	public boolean isbios = false;
	public boolean ismechanical = false;
	public boolean isdevice = false;
	public StringBuffer manufacturer = new StringBuffer();
	public Driver driver = new Driver();
	public Input input = new Input();
	public DisplayOrientation orientation = DisplayOrientation.any;
	public CabinetType cabinetType = CabinetType.upright;
	public Map<String, SWList> swlists = new HashMap<>();

	public class SWList implements Serializable
	{
		public String name;
		public SWStatus status;
		public String filter;
	}

	public enum SWStatus
	{
		original,
		compatible
	}

	public enum DisplayOrientation
	{
		any,
		horizontal,
		vertical;
	}

	public enum CabinetType
	{
		any,
		upright,
		cocktail;
	}

	public Machine()
	{
	}

	@Override
	public Machine getParent()
	{
		return getParent(Machine.class);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getFullName()
	{
		return name;
	}

	@Override
	public String getFullName(String filename)
	{
		return filename;
	}

	@Override
	public boolean isBios()
	{
		return isbios;
	}

	@Override
	public boolean isRomOf()
	{
		return romof != null;
	}

	@Override
	public Type getType()
	{
		if(parent != null)
			return ((Machine) parent).getType();
		if(isbios)
			return Type.BIOS;
		if(ismechanical)
			return Type.MECHANICAL;
		if(isdevice)
			return Type.DEVICE;
		return Type.STANDARD;
	}

	@Override
	public Systm getSystem()
	{
		switch(getType())
		{
			case BIOS:
				if(parent != null)
					return parent.getSystem();
				return this;
			case DEVICE:
				return SystmDevice.DEVICE;
			case MECHANICAL:
				return SystmMechanical.MECHANICAL;
			case STANDARD:
			default:
				return SystmStandard.STANDARD;
		}
	}

	@Override
	public String toString()
	{
		return "[" + getType() + "] " + description.toString();
	}

	public int isCompatible(String softwarelist, String compatibility)
	{
		if(compatibility != null)
			if(new HashSet<String>(Arrays.asList(compatibility.split(","))).contains(swlists.get(softwarelist).filter))
				return swlists.get(softwarelist).status == SWStatus.original ? 20 : 10;
		return swlists.get(softwarelist).status == SWStatus.original ? 2 : 1;
	}

	public void export(EnhancedXMLStreamWriter writer, boolean is_mame) throws XMLStreamException, IOException
	{
		if(is_mame)
		{
			writer.writeStartElement("machine", 
					new SimpleAttribute("name", name),
					new SimpleAttribute("isbios", isbios?"yes":null),
					new SimpleAttribute("isdevice", isdevice?"yes":null),
					new SimpleAttribute("ismechanical", ismechanical?"yes":null),
					new SimpleAttribute("cloneof", cloneof),
					new SimpleAttribute("romof", romof),
					new SimpleAttribute("sampleof", sampleof)
			);
			writer.writeElement("description", description);
			if(year!=null && year.length()>0)
				writer.writeElement("year", year);
			if(manufacturer!=null && manufacturer.length()>0)
				writer.writeElement("manufacturer", manufacturer);
			for(Rom r : roms)
				r.export(writer, is_mame);
			for(Disk d : disks)
				d.export(writer, is_mame);
			for(SWList swlist : swlists.values())
			{
				writer.writeElement("softwarelist",
					new SimpleAttribute("name", swlist.name),
					new SimpleAttribute("status", swlist.status),
					new SimpleAttribute("filter", swlist.filter)
				);
				
			}
			if(driver!=null)
			{
				writer.writeElement("driver",
					new SimpleAttribute("status", driver.getStatus()),
					new SimpleAttribute("emulation", driver.getEmulation()),
					new SimpleAttribute("cocktail", driver.getCocktail()),
					new SimpleAttribute("savestate", driver.getSaveState())
				);
				
			}
			writer.writeEndElement();
		}
		else
		{
			writer.writeStartElement("game", 
					new SimpleAttribute("name", name), 
					new SimpleAttribute("isbios", isbios?"yes":null),
					new SimpleAttribute("cloneof", cloneof),
					new SimpleAttribute("romof", romof),
					new SimpleAttribute("sampleof", sampleof)
			);
			writer.writeElement("description", description);
			if(year!=null && year.length()>0)
				writer.writeElement("year", year);
			if(manufacturer!=null && manufacturer.length()>0)
				writer.writeElement("manufacturer", manufacturer);
			for(Rom r : roms)
				r.export(writer, is_mame);
			for(Disk d : disks)
				d.export(writer, is_mame);
			writer.writeEndElement();
		}

	}
}
