package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Export.SimpleAttribute;
import jrm.profile.filter.CatVer.SubCategory;
import jrm.profile.filter.NPlayers.NPlayer;

@SuppressWarnings("serial")
public class Machine extends Anyware implements Serializable
{
	public String romof = null;
	public String sampleof = null;
	public boolean isbios = false;
	public boolean ismechanical = false;
	public boolean isdevice = false;
	public final StringBuffer manufacturer = new StringBuffer();
	public final Driver driver = new Driver();
	public final Input input = new Input();
	public DisplayOrientation orientation = DisplayOrientation.any;
	public CabinetType cabinetType = CabinetType.upright;
	
	public final Map<String, SWList> swlists = new HashMap<>();
	
	public final List<String> device_ref = new ArrayList<>();
	public final HashMap<String, Machine> devices = new HashMap<>();
	
	public final Map<String, Slot> slots = new HashMap<>();

	public transient SubCategory subcat = null;
	public transient NPlayer nplayer = null;

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
	public String getFullName(final String filename)
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

	public boolean isSoftMachine()
	{
		return swlists.size()>0 || (isClone() && getParent().swlists.size()>0);
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
					return getParent().getSystem();
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
		return "[" + getType() + "] " + description.toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public int isCompatible(final String softwarelist, final String compatibility)
	{
		if(compatibility != null)
			if(new HashSet<>(Arrays.asList(compatibility.split(","))).contains(swlists.get(softwarelist).filter)) //$NON-NLS-1$
				return swlists.get(softwarelist).status == SWStatus.original ? 20 : 10;
		return swlists.get(softwarelist).status == SWStatus.original ? 2 : 1;
	}

	public void export(final EnhancedXMLStreamWriter writer, final boolean is_mame) throws XMLStreamException, IOException
	{
		if(is_mame)
		{
			writer.writeStartElement("machine", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("isbios", isbios?"yes":null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("isdevice", isdevice?"yes":null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("ismechanical", ismechanical?"yes":null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("cloneof", cloneof), //$NON-NLS-1$
					new SimpleAttribute("romof", romof), //$NON-NLS-1$
					new SimpleAttribute("sampleof", sampleof) //$NON-NLS-1$
					);
			writer.writeElement("description", description); //$NON-NLS-1$
			if(year!=null && year.length()>0)
				writer.writeElement("year", year); //$NON-NLS-1$
			if(manufacturer!=null && manufacturer.length()>0)
				writer.writeElement("manufacturer", manufacturer); //$NON-NLS-1$
			for(final Rom r : roms)
				r.export(writer, is_mame);
			for(final Disk d : disks)
				d.export(writer, is_mame);
			for(final SWList swlist : swlists.values())
			{
				writer.writeElement("softwarelist", //$NON-NLS-1$
						new SimpleAttribute("name", swlist.name), //$NON-NLS-1$
						new SimpleAttribute("status", swlist.status), //$NON-NLS-1$
						new SimpleAttribute("filter", swlist.filter) //$NON-NLS-1$
						);

			}
			if(driver!=null)
			{
				writer.writeElement("driver", //$NON-NLS-1$
						new SimpleAttribute("status", driver.getStatus()), //$NON-NLS-1$
						new SimpleAttribute("emulation", driver.getEmulation()), //$NON-NLS-1$
						new SimpleAttribute("cocktail", driver.getCocktail()), //$NON-NLS-1$
						new SimpleAttribute("savestate", driver.getSaveState()) //$NON-NLS-1$
						);

			}
			writer.writeEndElement();
		}
		else
		{
			writer.writeStartElement("game", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("isbios", isbios?"yes":null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("cloneof", cloneof), //$NON-NLS-1$
					new SimpleAttribute("romof", romof), //$NON-NLS-1$
					new SimpleAttribute("sampleof", sampleof) //$NON-NLS-1$
					);
			writer.writeElement("description", description); //$NON-NLS-1$
			if(year!=null && year.length()>0)
				writer.writeElement("year", year); //$NON-NLS-1$
			if(manufacturer!=null && manufacturer.length()>0)
				writer.writeElement("manufacturer", manufacturer); //$NON-NLS-1$
			for(final Rom r : roms)
				r.export(writer, is_mame);
			for(final Disk d : disks)
				d.export(writer, is_mame);
			writer.writeEndElement();
		}

	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Machine)
			return this.name.equals(((Machine)obj).name);
		return super.equals(obj);
	}

	@Override
	public CharSequence getDescription()
	{
		return description;
	}
	
	void getDevices(HashSet<Machine> machines, boolean excludeBios, boolean partial, boolean recurse)
	{
		if (!machines.contains(this))
		{
			machines.add(this);
			if (!isBios() || !excludeBios)
				getDevices(partial).forEach(m -> {
					if (!recurse)
						machines.add(m);
					else
						m.getDevices(machines, excludeBios, partial, recurse);
				});
		}
	}
	
	Stream<Machine> getDevices(boolean partial)
	{
		if(partial)
			return devices.values().stream().filter(device->device_ref.contains(device.name));
		return devices.values().stream();
	}
	
	Stream<Rom> streamWithDevices(boolean excludeBios, boolean partial, boolean recurse)
	{
		HashSet<Machine> machines = new HashSet<>();
		getDevices(machines, excludeBios, partial, recurse);
		return machines.stream().flatMap(m->m.roms.stream());
	}
	

}
