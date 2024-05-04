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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;

import jrm.profile.Profile;
import jrm.profile.filter.CatVer.Category.SubCategory;
import jrm.profile.filter.NPlayer;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Represent a complete Machine (or a game set in Logiqx terminology)
 * @author optyfr
 */
public class Machine extends Anyware implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * if defined, romof will tell that this machine will share rom with another machine named in that romof field
	 */
	protected @Getter @Setter String romof = null;
	/**
	 * if defined, sampleof will tell that this machine will have samples contained within a sampleset ({@link Samples}) named by that sampleof field
	 */
	protected @Getter @Setter String sampleof = null;
	/**
	 * is that machine a system bios?
	 */
	protected @Getter @Setter boolean isbios = false;
	/**
	 * is that machine electro-mechanical (fruit machines, pinballs, etc ...)
	 */
	protected @Getter @Setter boolean ismechanical = false;
	/**
	 * is that machine a device (serial interface, disk controller, etc ...)
	 */
	protected @Getter @Setter boolean isdevice = false;

	protected @Getter @Setter String sourcefile = null;
	
	/**
	 * the manufacturer, if known
	 */
	public final StringBuilder manufacturer = new StringBuilder();
	/**
	 * the {@link Driver} informations
	 */
	public final Driver driver = new Driver();
	/**
	 * The {@link Input} informations
	 */
	public final Input input = new Input();
	/**
	 * The {@link DisplayOrientation} informations
	 */
	protected @Getter @Setter DisplayOrientation orientation = DisplayOrientation.any;
	/**
	 * The {@link CabinetType} informations
	 */
	protected @Getter @Setter CabinetType cabinetType = CabinetType.upright;
	
	/**
	 * the software lists that this machine is linked to (if this machine is a computer or a home console) 
	 */
	private final @Getter Map<String, SWList> swlists = new HashMap<>();
	
	/**
	 * A "machine device" references list
	 */
	private final @Getter List<String> deviceRef = new ArrayList<>();
	/**
	 * The mapping between each device_ref string and a {@link Machine} (with flag {@link #isdevice})
	 */
	protected transient @Getter Map<String, Machine> deviceMachines = new HashMap<>();
	/**
	 * an I/O device list
	 */
	private final @Getter List<Device> devices = new ArrayList<>();
	
	/**
	 * a slot group of optional devices slots
	 */
	private final @Getter Map<String, Slot> slots = new HashMap<>();

	/**
	 * category/subcategory as defined by catver.ini
	 */
	protected transient @Getter @Setter SubCategory subcat = null;

	/**
	 * nplayer as defined by nplayers.ini
	 */
	protected transient @Getter @Setter NPlayer nplayer = null;
	
	/**
	 * source as defined by sourcefile property
	 */
	protected transient @Getter @Setter Source source  = null;
	
	/**
	 * SWList link reference with support status and filter option 
	 */
	@SuppressWarnings("serial")
	public @Data class SWList implements Serializable
	{
		private String name;
		private SWStatus status;
		private String filter;
	}

	/**
	 * is this swlist is a compatible list or an original list of softwares for this computer/console 
	 */
	public enum SWStatus
	{
		original,	//NOSONAR
		compatible	//NOSONAR
	}

	/**
	 * The display orientation possibilities 
	 */
	public enum DisplayOrientation
	{
		any,	//NOSONAR
		horizontal,	//NOSONAR
		vertical	//NOSONAR
	}

	/**
	 * The supported cabinet type 
	 */
	public enum CabinetType
	{
		any,	//NOSONAR
		upright,	//NOSONAR
		cocktail	//NOSONAR
	}

	public Machine(Profile profile)
	{
		super(profile);
	}

	@Override
	public Machine getParent()
	{
		return getParent(Machine.class);
	}

	/**
	 * the Serializable method for special serialization handling (in that case : initialize transient default values) 
	 * @param in the serialization inputstream 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(final java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		in.defaultReadObject();
		initTransient();
	}

	/**
	 * The method called to initialize transient and static fields
	 */
	@Override
	protected void initTransient()
	{
		super.initTransient();
		deviceMachines = new HashMap<>();
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

	/**
	 * Is this machine a machine with a software list?<br>
	 * This is not 100% accurate since lot of unsupported but defined machines does not have yet software lists defined
	 * @return true if it's a software machine
	 */
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

	/**
	 * get the machine compatibility level with a software list according its filter tag versus an optional software compatibility value
	 * @param softwarelist a software list name
	 * @param compatibility the compatibility string (if any) declared for a software in the software list
	 * @return higher is the returned int value, higher will be the level of compatibility
	 */
	public int isCompatible(final String softwarelist, final String compatibility)
	{
		if(compatibility != null && new HashSet<>(Arrays.asList(StringUtils.split(compatibility,','))).contains(swlists.get(softwarelist).filter)) //$NON-NLS-1$
				return swlists.get(softwarelist).status == SWStatus.original ? 20 : 10;
		return swlists.get(softwarelist).status == SWStatus.original ? 2 : 1;
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param is_mame is it mame (true) or logqix (false) format ?
	 * @throws XMLStreamException
	 */
	public void export(final EnhancedXMLStreamWriter writer, final boolean is_mame, final Set<ExportMode> modes) throws XMLStreamException
	{
		if(is_mame)
		{
			exportMame(writer, modes);
			return;
		}
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
		if(manufacturer.length()>0)
			writer.writeElement("manufacturer", manufacturer); //$NON-NLS-1$
		final var missing = modes.contains(ExportMode.MISSING);
		final var have = modes.contains(ExportMode.HAVE);
		final var all = modes.contains(ExportMode.ALL) || modes.contains(ExportMode.FILTERED);
		for(final var r : getRoms())
			if(all || (missing && r.getStatus()==EntityStatus.KO) || (have && r.getStatus()==EntityStatus.OK))
				r.export(writer, is_mame);
		for(final Disk d : getDisks())
			if(all || (missing && d.getStatus()==EntityStatus.KO) || (have && d.getStatus()==EntityStatus.OK))
				d.export(writer, is_mame);
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @throws XMLStreamException
	 */
	private void exportMame(final EnhancedXMLStreamWriter writer, final Set<ExportMode> modes) throws XMLStreamException
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
		if (manufacturer.length() > 0)
			writer.writeElement("manufacturer", manufacturer); //$NON-NLS-1$
		final var missing = modes.contains(ExportMode.MISSING);
		final var have = modes.contains(ExportMode.HAVE);
		final var all = modes.contains(ExportMode.ALL) || modes.contains(ExportMode.FILTERED);
		for(final Rom r : getRoms())
			if(all || (missing && r.getStatus()==EntityStatus.KO) || (have && r.getStatus()==EntityStatus.OK))
				r.export(writer, true);
		for(final Disk d : getDisks())
			if(all || (missing && d.getStatus()==EntityStatus.KO) || (have && d.getStatus()==EntityStatus.OK))
				d.export(writer, true);
		for(final SWList swlist : swlists.values())
		{
			writer.writeElement("softwarelist", //$NON-NLS-1$
				new SimpleAttribute("name", swlist.name), //$NON-NLS-1$
				new SimpleAttribute("status", swlist.status), //$NON-NLS-1$
				new SimpleAttribute("filter", swlist.filter) //$NON-NLS-1$
			);
		}
		writer.writeElement("driver", //$NON-NLS-1$
			new SimpleAttribute("status", driver.getStatus()), //$NON-NLS-1$
			new SimpleAttribute("emulation", driver.getEmulation()), //$NON-NLS-1$
			new SimpleAttribute("cocktail", driver.getCocktail()), //$NON-NLS-1$
			new SimpleAttribute("savestate", driver.getSaveState()) //$NON-NLS-1$
		);
		writer.writeEndElement();
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Machine m)
			return this.name.equals(m.name);
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

	@Override
	public CharSequence getDescription()
	{
		return description;
	}
	
	/**
	 * build the list of associated machine devices
	 * @param machines the set of machine to fill up with
	 * @param excludeBios exclude any bios devices
	 * @param partial exclude devices from slots that are not defined in device_ref
	 * @param recurse also get devices of devices and so on
	 */
	protected void getDevices(HashSet<Machine> machines, boolean excludeBios, boolean partial, boolean recurse)
	{
		if (!machines.contains(this))
		{
			machines.add(this);
			if (!isBios() || !excludeBios)
			{
				getDevices(partial).forEach(m -> {
					if (!recurse)
						machines.add(m);
					else
						m.getDevices(machines, excludeBios, partial, recurse);
				});
			}
		}
	}
	
	/**
	 * Stream associated machine devices
	 * @param partial exclude machines devices from slots that are not defined in device_ref
	 * @return a {@link Stream}&lt;{@link Machine}&gt;
	 */
	private Stream<Machine> getDevices(boolean partial)
	{
		if(partial)
			return deviceMachines.values().stream().filter(device->deviceRef.contains(device.name));
		return deviceMachines.values().stream();
	}
	
	@Override
	protected Stream<Rom> streamWithDevices(boolean excludeBios, boolean partial, boolean recurse)
	{
		HashSet<Machine> machines = new HashSet<>();
		getDevices(machines, excludeBios, partial, recurse);
		return machines.stream().flatMap(m->m.getRoms().stream());
	}
	
	/**
	 * get the selection state in profile properties according  {@link #getPropertyName()}
	 * @return true if selected
	 */
	public boolean isSelected()
	{
		return profile.getProperty("filter.machine." + getName(), true);
	}

	/**
	 * set the selection state in profile properties according {@link #getPropertyName()}
	 * @param selected the selection state to set
	 */
	public void setSelected(final boolean selected)
	{
		profile.setProperty("filter.machine." + getName(), selected);
	}
}
