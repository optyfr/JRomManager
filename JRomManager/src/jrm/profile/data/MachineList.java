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
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.ProfileSettingsEnum;
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

	private class FilterOptions
	{
		final boolean excludeGames = profile.getProperty(ProfileSettingsEnum.exclude_games, Boolean.class); //$NON-NLS-1$
		final boolean excludeMachines = profile.getProperty(ProfileSettingsEnum.exclude_machines, Boolean.class); //$NON-NLS-1$
		final boolean filterIncludeClones = profile.getProperty(ProfileSettingsEnum.filter_InclClones, Boolean.class); //$NON-NLS-1$
		final boolean filterIncludeDisks = profile.getProperty(ProfileSettingsEnum.filter_InclDisks, Boolean.class); //$NON-NLS-1$
		final boolean filterIncludeSamples = profile.getProperty(ProfileSettingsEnum.filter_InclSamples, Boolean.class); //$NON-NLS-1$
		final Driver.StatusType filterMinDriverStatus = Driver.StatusType.valueOf(profile.getProperty(ProfileSettingsEnum.filter_DriverStatus, String.class)); //$NON-NLS-1$
		final DisplayOrientation filterDisplayOrientation = DisplayOrientation.valueOf(profile.getProperty(ProfileSettingsEnum.filter_DisplayOrientation, String.class)); //$NON-NLS-1$
		final CabinetType filterCabinetType = CabinetType.valueOf(profile.getProperty(ProfileSettingsEnum.filter_CabinetType, String.class)); //$NON-NLS-1$
		final String filterYearMin = profile.getProperty(ProfileSettingsEnum.filter_YearMin, String.class); //$NON-NLS-1$ //$NON-NLS-2$
		final String filterYearMax = profile.getProperty(ProfileSettingsEnum.filter_YearMax, String.class); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public Stream<Machine> getFilteredStream()
	{
		final var options = new FilterOptions();

		if(options.excludeGames && !options.excludeMachines)
			return getMessFilteredStream();

		/*
		 * get all needed profile options
		 */
		
		return getList().stream().filter(t -> {
			if(options.excludeGames && !t.isdevice && !t.isbios && !t.isSoftMachine())	// exclude pure games (pure means not bios nor devices)
				return false;
			if(options.excludeMachines && !t.isdevice && !t.isbios && t.isSoftMachine())	// exclude computer/console
				return false;
			/*
			 * Apply simple filters
			 */
			if(!getSimpleFilters(options, t))
				return false;
			/*
			 * Apply advanced filters
			 */
			if(!getAdvancedFilters(t))	//NOSONAR
				return false;
			
			return true;	// otherwise include
		});
	}

	/**
	 * @param t
	 */
	private boolean getAdvancedFilters(Machine t)
	{
		if(t.subcat != null && !t.subcat.isSelected())	// exclude if subcat is not selected
			return false;
		if(t.nplayer != null && !t.nplayer.isSelected(profile))	//NOSONAR // exclude if nplayer is not selected
			return false;
		return true;
	}

	/**
	 * @param options
	 * @param t
	 */
	private boolean getSimpleFilters(final FilterOptions options, Machine t)
	{
		if(!getNonDeviceFilter(options, t))
			return false;
		if(!getYearFilter(options, t))
			return false;
		if(!options.filterIncludeClones && t.isClone())	// exclude clones machines
			return false;
		if(!options.filterIncludeDisks && t.getDisks().size() > 0)	// exclude machines with disks
			return false;
		if(!options.filterIncludeSamples && t.getSamples().size() > 0)	// exclude machines with samples
			return false;
		if(!t.getSystem().isSelected(profile))	// exclude machines for which their BIOS system were not selected
			return false;
		if(Optional.ofNullable(t.getSource()).map(s->!s.isSelected(profile)).orElse(false)) //NOSONAR exclude machines for which their source file were not selected
			return false;
		return true;
	}

	/**
	 * @param filterYearMin
	 * @param filterYearMax
	 * @param t
	 */
	private boolean getYearFilter(final FilterOptions options, Machine t)
	{
		if(t.year.length() > 0)
		{	// exclude machines outside defined year range
			if(options.filterYearMin.compareTo(t.year.toString()) > 0)
				return false;
			if(options.filterYearMax.compareTo(t.year.toString()) < 0)
				return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	private Stream<Machine> getMessFilteredStream()
	{
		// special case where we want to keep computers & consoles machines but not arcade games machines (let's call it mess mode)
		HashSet<Machine> machines = new HashSet<>();
		getList().stream().filter(Machine::isSoftMachine).forEach(m -> m.getDevices(machines, false, false, true));
		final HashSet<Machine> allDevices = new HashSet<>();
		getList().stream().filter(t -> !t.isdevice).forEach(m -> m.getDevices(allDevices,false, false, true));
		allDevices.removeAll(allDevices.stream().filter(t->!t.isdevice).collect(Collectors.toSet()));
		return Stream.concat(machines.stream().filter(t -> !t.isSoftMachine()), getList().stream().filter(t -> t.isdevice && !allDevices.contains(t)));
	}

	/**
	 * @param filterMinDriverStatus
	 * @param filterDisplayOrientation
	 * @param filterCabinetType
	 * @param t
	 */
	private boolean getNonDeviceFilter(final FilterOptions options, Machine t)
	{
		if(!t.isdevice)	// exception on devices
		{
			if(options.filterMinDriverStatus == StatusType.imperfect && t.driver.getStatus() == StatusType.preliminary)	// exclude preliminary when min driver status is imperfect
				return false;
			if(options.filterMinDriverStatus == StatusType.good && t.driver.getStatus() != StatusType.good)	// exclude non good status when min driver status is good
				return false;
			if(!getNonMechanicalFilter(options, t))
				return false;
		}
		return true;
	}

	/**
	 * @param filterDisplayOrientation
	 * @param filterCabinetType
	 * @param t
	 */
	private boolean getNonMechanicalFilter(final FilterOptions options, Machine t)
	{
		if(!t.ismechanical)	// exception on mechanical
		{
			if(options.filterDisplayOrientation == DisplayOrientation.horizontal && t.orientation == DisplayOrientation.vertical)	// exclude "vertical only" when display filter is "horizontal only"
				return false;
			if(options.filterDisplayOrientation == DisplayOrientation.vertical && t.orientation == DisplayOrientation.horizontal)	// exclude "horizontal only" when display filter is "vertical only"
				return false;
			if(!getNonBiosFilter(options, t))
				return false;
		}
		return true;
	}

	/**
	 * @param filterCabinetType
	 * @param t
	 */
	private boolean getNonBiosFilter(final FilterOptions options, Machine t)
	{
		if(!t.isbios)	// exception on bios
		{
			if(options.filterCabinetType == CabinetType.upright && t.cabinetType == CabinetType.cocktail)	// exclude "cocktail only" if cabinet filter is "upright only"
				return false;
			if(options.filterCabinetType == CabinetType.cocktail && t.cabinetType == CabinetType.upright)	// exclude "upright only" if cabinet filter is "cocktail only"
				return false;
		}
		return true;
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
	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress, final boolean is_mame, final boolean filtered) throws XMLStreamException
	{
		if(is_mame)
			writer.writeStartElement("mame"); //$NON-NLS-1$
		else
			writer.writeStartElement("datafile"); //$NON-NLS-1$
		final List<Machine> list = filtered ? getFilteredStream().collect(Collectors.toList()) : getList();
		var i = 0;
		progress.setProgress(profile.getSession().getMsgs().getString("MachineList.Exporting"), i, list.size()); //$NON-NLS-1$
		for(final Machine m : list)
		{
			if(progress.isCancel())
				break;
			progress.setProgress(String.format(profile.getSession().getMsgs().getString("MachineList.Exporting_%s"), m.name), ++i); //$NON-NLS-1$
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
