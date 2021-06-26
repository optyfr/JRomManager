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

import jrm.aui.progress.ProgressHandler;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Driver.StatusType;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
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
	private final ArrayList<Machine> mList = new ArrayList<>();
	/**
	 * The by name {@link HashMap} of {@link Machine}
	 */
	private final HashMap<String, Machine> mByName = new HashMap<>();
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
		super.initTransient();
	}



	@Override
	public List<Machine> getList()
	{
		return mList;
	}

	@Override
	public Stream<Machine> getFilteredStream()
	{
		/*
		 * get all needed profile options
		 */
		final boolean filterIncludeClones = profile.getProperty(SettingsEnum.filter_InclClones, true); //$NON-NLS-1$
		final boolean filterIncludeDisks = profile.getProperty(SettingsEnum.filter_InclDisks, true); //$NON-NLS-1$
		final boolean filterIncludeSamples = profile.getProperty(SettingsEnum.filter_InclSamples, true); //$NON-NLS-1$
		final Driver.StatusType filterMinDriverStatus = Driver.StatusType.valueOf(profile.getProperty(SettingsEnum.filter_DriverStatus, Driver.StatusType.preliminary.toString())); //$NON-NLS-1$
		final var filterDisplayOrientation = DisplayOrientation.valueOf(profile.getProperty(SettingsEnum.filter_DisplayOrientation, DisplayOrientation.any.toString())); //$NON-NLS-1$
		final var filterCabinetType = CabinetType.valueOf(profile.getProperty(SettingsEnum.filter_CabinetType, CabinetType.any.toString())); //$NON-NLS-1$
		final String filterYearMin = profile.getProperty(SettingsEnum.filter_YearMin, ""); //$NON-NLS-1$ //$NON-NLS-2$
		final String filterYearMax = profile.getProperty(SettingsEnum.filter_YearMax, "????"); //$NON-NLS-1$ //$NON-NLS-2$
		final boolean excludeGames = profile.getProperty(SettingsEnum.exclude_games, false); //$NON-NLS-1$
		final boolean excludeMachines = profile.getProperty(SettingsEnum.exclude_machines, false); //$NON-NLS-1$

		if(excludeGames && !excludeMachines)
		{	// special case where we want to keep computers & consoles machines but not arcade games machines (let's call it mess mode)
			HashSet<Machine> machines = new HashSet<>();
			getList().stream().filter(Machine::isSoftMachine).forEach(m -> m.getDevices(machines, false, false, true));
			final HashSet<Machine> allDevices = new HashSet<>();
			getList().stream().filter(t -> !t.isdevice).forEach(m -> m.getDevices(allDevices,false, false, true));
			allDevices.removeAll(allDevices.stream().filter(t->!t.isdevice).collect(Collectors.toSet()));
			return Stream.concat(machines.stream().filter(t -> !t.isSoftMachine()), getList().stream().filter(t -> t.isdevice && !allDevices.contains(t)));
		}
		
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
			if(!filterIncludeDisks && t.getDisks().size() > 0)	// exclude machines with disks
				return false;
			if(!filterIncludeSamples && t.getSamples().size() > 0)	// exclude machines with samples
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
		if(filteredList == null)
			filteredList = getFilteredStream().filter(machine -> profile.getFilterList().contains(machine.getStatus())).sorted().collect(Collectors.toList());
		return filteredList;
	}

	@Override
	public long countAll()
	{
		return getFilteredStream().count();
	}

	@Override
	public long countHave()
	{
		return getFilteredStream().filter(t -> t.getStatus() == AnywareStatus.COMPLETE).count();
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
		var i = 0;
		progress.setProgress(profile.getSession().msgs.getString("MachineList.Exporting"), i, list.size()); //$NON-NLS-1$
		for(final Machine m : list)
		{
			progress.setProgress(String.format(profile.getSession().msgs.getString("MachineList.Exporting_%s"), m.name), ++i); //$NON-NLS-1$
			if(!filtered || m.isSelected())
				m.export(writer, is_mame);
		}
		writer.writeEndElement();
	}

	@Override
	public boolean containsName(final String name)
	{
		return mByName.containsKey(name);
	}

	@Override
	public Machine getByName(String name)
	{
		return mByName.get(name);
	}

	@Override
	public Machine putByName(Machine t)
	{
		return mByName.put(t.getName(), t);
	}

	@Override
	public String getName()
	{
		return name;
	}

	/**
	 * named map filtered cache
	 */
	private transient Map<String, Machine> mFilteredByName = null;

	@Override
	public void resetFilteredName()
	{
		mFilteredByName = getFilteredStream().collect(Collectors.toMap(Machine::getBaseName, Function.identity()));
	}

	@Override
	public boolean containsFilteredName(String name)
	{
		if(mFilteredByName==null)
			resetFilteredName();
		return mFilteredByName.containsKey(name);
	}

	@Override
	public Machine getFilteredByName(String name)
	{
		if(mFilteredByName==null)
			resetFilteredName();
		return mFilteredByName.get(name);
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
