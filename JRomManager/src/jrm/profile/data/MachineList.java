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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Profile;
import jrm.profile.data.Driver.StatusType;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.ui.progress.ProgressHandler;
import jrm.xml.EnhancedXMLStreamWriter;


/**
 * A list of {@link Machine}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class MachineList extends AnywareList<Machine> implements Serializable
{
	/**
	 * The {@link ArrayList} of {@link Machine}
	 */
	private final ArrayList<Machine> m_list = new ArrayList<>();
	/**
	 * The by name {@link HashMap} of {@link Machine}
	 */
	private final HashMap<String, Machine> m_byname = new HashMap<>();
	/**
	 * The associated Samples set as a {@link SamplesList}
	 */
	public final SamplesList samplesets = new SamplesList();


	/**
	 * The constructor, will initialize transients fields
	 */
	public MachineList(Profile profile)
	{
		super(profile);
		initTransient();
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

	@Override
	protected void initTransient()
	{
		super.initTransient();
	}


	@Override
	public List<Machine> getList()
	{
		return m_list;
	}

	@Override
	public Stream<Machine> getFilteredStream()
	{
		/*
		 * get all needed profile options
		 */
		final boolean filterIncludeClones = profile.getProperty("filter.InclClones", true); //$NON-NLS-1$
		final boolean filterIncludeDisks = profile.getProperty("filter.InclDisks", true); //$NON-NLS-1$
		final boolean filterIncludeSamples = profile.getProperty("filter.InclSamples", true); //$NON-NLS-1$
		final Driver.StatusType filterMinDriverStatus = Driver.StatusType.valueOf(profile.getProperty("filter.DriverStatus", Driver.StatusType.preliminary.toString())); //$NON-NLS-1$
		final DisplayOrientation filterDisplayOrientation = DisplayOrientation.valueOf(profile.getProperty("filter.DisplayOrientation", DisplayOrientation.any.toString())); //$NON-NLS-1$
		final CabinetType filterCabinetType = CabinetType.valueOf(profile.getProperty("filter.CabinetType", CabinetType.any.toString())); //$NON-NLS-1$
		final String filterYearMin = profile.getProperty("filter.YearMin", ""); //$NON-NLS-1$ //$NON-NLS-2$
		final String filterYearMax = profile.getProperty("filter.YearMax", "????"); //$NON-NLS-1$ //$NON-NLS-2$
		final boolean excludeGames = profile.getProperty("exclude_games", false); //$NON-NLS-1$
		final boolean excludeMachines = profile.getProperty("exclude_machines", false); //$NON-NLS-1$

		if(excludeGames && !excludeMachines)
		{	// special case where we want to keep computers & consoles machines but not arcade games machines (let's call it mess mode)
			HashSet<Machine> machines = new HashSet<>();
			getList().stream().filter(t -> t.isSoftMachine()).forEach(m -> m.getDevices(machines, false, false, true));
			final HashSet<Machine> all_devices = new HashSet<>();
			getList().stream().filter(t -> !t.isdevice).forEach(m -> m.getDevices(all_devices,false, false, true));
			all_devices.removeAll(all_devices.stream().filter(t->!t.isdevice).collect(Collectors.toSet()));
			return Stream.concat(machines.stream().filter(t -> !t.isSoftMachine()), getList().stream().filter(t -> t.isdevice && !all_devices.contains(t)));
		}
		
/*		if(excludeGames && excludeMachines)
		{
			HashSet<Machine> machines = new HashSet<>();
			getList().stream().filter(t -> t.isSoftMachine()).forEach(m -> m.getMachineDevices(machines));
			final HashSet<Machine> all_devices = new HashSet<>();
			getList().stream().filter(t -> !t.isdevice).forEach(m -> m.getMachineDevices(all_devices));
			all_devices.removeAll(all_devices.stream().filter(t->!t.isdevice).collect(Collectors.toSet()));
			return Stream.concat(machines.stream().filter(t -> !t.isSoftMachine()), getList().stream().filter(t -> t.isdevice && !all_devices.contains(t)));
		}*/
		
		return getList().stream().filter(t -> {
			if(excludeGames && !t.isdevice && !t.isbios && !t.isSoftMachine())	// exclude pure games (pure means not bios nor devices)
				return false;
			if(excludeMachines && !t.isdevice && !t.isbios && t.isSoftMachine())	// exclude computer/console
				return false;
			/*
			 * Apply simple filters
			 */
			if(!t.isdevice)	// exception on devices
			{
				if(filterMinDriverStatus == StatusType.imperfect && t.driver.getStatus() == StatusType.preliminary)	// exclude preliminary when min driver status is imperfect
					return false;
				if(filterMinDriverStatus == StatusType.good && t.driver.getStatus() != StatusType.good)	// exclude non good status when min driver status is good
					return false;
				if(!t.ismechanical)	// exception on mechanical
				{
					if(filterDisplayOrientation == DisplayOrientation.horizontal && t.orientation == DisplayOrientation.vertical)	// exclude "vertical only" when display filter is "horizontal only"
						return false;
					if(filterDisplayOrientation == DisplayOrientation.vertical && t.orientation == DisplayOrientation.horizontal)	// exclude "horizontal only" when display filter is "vertical only"
						return false;
					if(!t.isbios)	// exception on bios
					{
						if(filterCabinetType == CabinetType.upright && t.cabinetType == CabinetType.cocktail)	// exclude "cocktail only" if cabinet filter is "upright only"
							return false;
						if(filterCabinetType == CabinetType.cocktail && t.cabinetType == CabinetType.upright)	// exclude "upright only" if cabinet filter is "cocktail only"
							return false;
					}
				}
			}
			if(t.year.length() > 0)
			{	// exclude machines outside defined year range
				if(filterYearMin.compareTo(t.year.toString()) > 0)
					return false;
				if(filterYearMax.compareTo(t.year.toString()) < 0)
					return false;
			}
			if(!filterIncludeClones && t.isClone())	// exclude clones machines
				return false;
			if(!filterIncludeDisks && t.disks.size() > 0)	// exclude machines with disks
				return false;
			if(!filterIncludeSamples && t.samples.size() > 0)	// exclude machines with samples
				return false;
			if(!t.getSystem().isSelected(profile))	// exclude machines for which their BIOS system were not selected
				return false;
			/*
			 * apply advanced filters
			 */
			if(t.subcat != null && !t.subcat.isSelected())	// exclude if subcat is not selected
				return false;
			if(t.nplayer != null && !t.nplayer.isSelected(profile)) // exclude if nplayer is not selected
				return false;
			return true;	// otherwise include
		});
	}

	@Override
	public List<Machine> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(machine -> {
				return profile.filter_l.contains(machine.getStatus());
			}).sorted().collect(Collectors.toList());
		return filtered_list;
	}

	@Override
	public long countAll()
	{
		return getFilteredStream().count();
	}

	@Override
	public long countHave()
	{
		return getFilteredStream().filter(t -> {
			return t.getStatus() == AnywareStatus.COMPLETE;
		}).count();
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param progress the {@link ProgressHandler} to show the current progress
	 * @param is_mame is it mame (true) or logqix (false) format ?
	 * @param filtered do we use the current machine filters of none
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress, final boolean is_mame, final boolean filtered) throws XMLStreamException, IOException
	{
		if(is_mame)
			writer.writeStartElement("mame"); //$NON-NLS-1$
		else
			writer.writeStartElement("datafile"); //$NON-NLS-1$
		final List<Machine> list = filtered ? getFilteredStream().collect(Collectors.toList()) : getList();
		int i = 0;
		progress.setProgress(profile.session.msgs.getString("MachineList.Exporting"), i, list.size()); //$NON-NLS-1$
		for(final Machine m : list)
		{
			progress.setProgress(String.format(profile.session.msgs.getString("MachineList.Exporting_%s"), m.name), ++i); //$NON-NLS-1$
			if(!filtered || m.selected)
				m.export(writer, is_mame);
		}
		writer.writeEndElement();
	}

	@Override
	public boolean containsName(final String name)
	{
		return m_byname.containsKey(name);
	}

	@Override
	public Machine getByName(String name)
	{
		return m_byname.get(name);
	}

	@Override
	public Machine putByName(Machine t)
	{
		return m_byname.put(t.getName(), t);
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * named map filtered cache
	 */
	private transient Map<String, Machine> m_filtered_byname = null;

	@Override
	public void resetFilteredName()
	{
		m_filtered_byname = getFilteredStream().collect(Collectors.toMap(Machine::getBaseName, Function.identity()));
	}

	@Override
	public boolean containsFilteredName(String name)
	{
		if(m_filtered_byname==null)
			resetFilteredName();
		return m_filtered_byname.containsKey(name);
	}

	@Override
	public Machine getFilteredByName(String name)
	{
		if(m_filtered_byname==null)
			resetFilteredName();
		return m_filtered_byname.get(name);
	}

}
