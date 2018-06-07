package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.xml.stream.XMLStreamException;

import jrm.Messages;
import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Profile;
import jrm.profile.data.Driver.StatusType;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.ui.MachineListRenderer;
import jrm.ui.ProgressHandler;

@SuppressWarnings("serial")
public final class MachineList extends AnywareList<Machine> implements Serializable
{
	private final ArrayList<Machine> m_list = new ArrayList<>();
	private final HashMap<String, Machine> m_byname = new HashMap<>();
	public final SamplesList samplesets = new SamplesList();


	public MachineList()
	{
		initTransient();
	}

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
	public int getColumnCount()
	{
		return MachineListRenderer.columns.length;
	}

	@Override
	public String getColumnName(final int columnIndex)
	{
		return MachineListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex)
	{
		return MachineListRenderer.columnsTypes[columnIndex];
	}

	@Override
	public TableCellRenderer getColumnRenderer(final int columnIndex)
	{
		return MachineListRenderer.columnsRenderers[columnIndex] != null ? MachineListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	@Override
	public int getColumnWidth(final int columnIndex)
	{
		return MachineListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return getFilteredList().size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex)
	{
		final Machine machine = getFilteredList().get(rowIndex);
		switch(columnIndex)
		{
			case 0:
				return machine;
			case 1:
				return machine;
			case 2:
				return machine.description.toString();
			case 3:
				return String.format("%d/%d", machine.countHave(), machine.countAll()); //$NON-NLS-1$
			case 4:
				return machine.cloneof != null ? (m_byname.containsKey(machine.cloneof) ? m_byname.get(machine.cloneof) : machine.cloneof) : null;
			case 5:
				return machine.romof != null && !machine.romof.equals(machine.cloneof) ? (m_byname.containsKey(machine.romof) ? m_byname.get(machine.romof) : machine.romof) : null;
			case 6:
				return machine.sampleof != null ? (samplesets.containsName(machine.sampleof) ? samplesets.getByName(machine.sampleof) : machine.sampleof) : null;
		}
		return null;
	}

	@Override
	public List<Machine> getList()
	{
		return m_list;
	}

	@Override
	public Stream<Machine> getFilteredStream()
	{
		final boolean filterIncludeClones = Profile.curr_profile.getProperty("filter.InclClones", true); //$NON-NLS-1$
		final boolean filterIncludeDisks = Profile.curr_profile.getProperty("filter.InclDisks", true); //$NON-NLS-1$
		final boolean filterIncludeSamples = Profile.curr_profile.getProperty("filter.InclSamples", true); //$NON-NLS-1$
		final Driver.StatusType filterMinDriverStatus = Driver.StatusType.valueOf(Profile.curr_profile.getProperty("filter.DriverStatus", Driver.StatusType.preliminary.toString())); //$NON-NLS-1$
		final DisplayOrientation filterDisplayOrientation = DisplayOrientation.valueOf(Profile.curr_profile.getProperty("filter.DisplayOrientation", DisplayOrientation.any.toString())); //$NON-NLS-1$
		final CabinetType filterCabinetType = CabinetType.valueOf(Profile.curr_profile.getProperty("filter.CabinetType", CabinetType.any.toString())); //$NON-NLS-1$
		final String filterYearMin = Profile.curr_profile.getProperty("filter.YearMin", ""); //$NON-NLS-1$ //$NON-NLS-2$
		final String filterYearMax = Profile.curr_profile.getProperty("filter.YearMax", "????"); //$NON-NLS-1$ //$NON-NLS-2$
		final boolean excludeGames = Profile.curr_profile.getProperty("exclude_games", false); //$NON-NLS-1$
		final boolean excludeMachines = Profile.curr_profile.getProperty("exclude_machines", false); //$NON-NLS-1$

		if(excludeGames && !excludeMachines)
		{
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
			if(excludeGames && !t.isdevice && !t.isbios && !t.isSoftMachine())
				return false;
			if(excludeMachines && !t.isdevice && !t.isbios && t.isSoftMachine())
				return false;
			if(!t.isdevice)
			{
				if(filterMinDriverStatus == StatusType.imperfect && t.driver.getStatus() == StatusType.preliminary)
					return false;
				if(filterMinDriverStatus == StatusType.good && t.driver.getStatus() != StatusType.good)
					return false;
				if(!t.ismechanical)
				{
					if(filterDisplayOrientation == DisplayOrientation.horizontal && t.orientation == DisplayOrientation.vertical)
						return false;
					if(filterDisplayOrientation == DisplayOrientation.vertical && t.orientation == DisplayOrientation.horizontal)
						return false;
					if(!t.isbios)
					{
						if(filterCabinetType == CabinetType.upright && t.cabinetType == CabinetType.cocktail)
							return false;
						if(filterCabinetType == CabinetType.cocktail && t.cabinetType == CabinetType.upright)
							return false;
					}
				}
			}
			if(t.year.length() > 0)
			{
				if(filterYearMin.compareTo(t.year.toString()) > 0)
					return false;
				if(filterYearMax.compareTo(t.year.toString()) < 0)
					return false;
			}
			if(!filterIncludeClones && t.isClone())
				return false;
			if(!filterIncludeDisks && t.disks.size() > 0)
				return false;
			if(!filterIncludeSamples && t.samples.size() > 0)
				return false;
			if(!t.getSystem().isSelected())
				return false;
			if(t.subcat != null && !t.subcat.isSelected())
				return false;
			if(t.nplayer != null && !t.nplayer.isSelected())
				return false;
			return true;
		});
	}

	@Override
	protected List<Machine> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(t -> {
				return AnywareList.filter.contains(t.getStatus());
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

	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress, final boolean is_mame, final boolean filtered) throws XMLStreamException, IOException
	{
		if(is_mame)
			writer.writeStartElement("mame"); //$NON-NLS-1$
		else
			writer.writeStartElement("datafile"); //$NON-NLS-1$
		final List<Machine> list = filtered ? getFilteredStream().collect(Collectors.toList()) : getList();
		int i = 0;
		progress.setProgress(Messages.getString("MachineList.Exporting"), i, list.size()); //$NON-NLS-1$
		for(final Machine m : list)
		{
			progress.setProgress(String.format(Messages.getString("MachineList.Exporting_%s"), m.name), ++i); //$NON-NLS-1$
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
