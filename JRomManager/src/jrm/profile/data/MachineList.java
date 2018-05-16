package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.xml.stream.XMLStreamException;

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
	public final HashMap<String, Machine> m_byname = new HashMap<>();
	public final HashMap<String, HashSet<String>> samplesets = new HashMap<>();

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
				return String.format("%d/%d", machine.countHave(), machine.roms.size() + machine.disks.size());
			case 4:
				return machine.cloneof != null ? (m_byname.containsKey(machine.cloneof) ? m_byname.get(machine.cloneof) : machine.cloneof) : null;
			case 5:
				return machine.romof != null && !machine.romof.equals(machine.cloneof) ? (m_byname.containsKey(machine.romof) ? m_byname.get(machine.romof) : machine.romof) : null;
			case 6:
				return machine.sampleof;
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
		final boolean filterIncludeClones = Profile.curr_profile.getProperty("filter.InclClones", true);
		final boolean filterIncludeDisks = Profile.curr_profile.getProperty("filter.InclDisks", true);
		final Driver.StatusType filterMinDriverStatus = Driver.StatusType.valueOf(Profile.curr_profile.getProperty("filter.DriverStatus", Driver.StatusType.preliminary.toString()));
		final DisplayOrientation filterDisplayOrientation = DisplayOrientation.valueOf(Profile.curr_profile.getProperty("filter.DisplayOrientation", DisplayOrientation.any.toString()));
		final CabinetType filterCabinetType = CabinetType.valueOf(Profile.curr_profile.getProperty("filter.CabinetType", CabinetType.any.toString()));
		final String filterYearMin = Profile.curr_profile.getProperty("filter.YearMin", "");
		final String filterYearMax = Profile.curr_profile.getProperty("filter.YearMax", "????");

		return getList().stream().filter(t -> {
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

	@Override
	public boolean containsName(final String name)
	{
		return m_byname.containsKey(name);
	}

	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress, final boolean is_mame, final boolean filtered) throws XMLStreamException, IOException
	{
		if(is_mame)
			writer.writeStartElement("mame");
		else
			writer.writeStartElement("datafile");
		final List<Machine> list = filtered ? getFilteredStream().collect(Collectors.toList()) : getList();
		int i = 0;
		progress.setProgress("Exporting", i, list.size());
		for(final Machine m : list)
		{
			progress.setProgress(String.format("Exporting %s", m.name), ++i);
			m.export(writer, is_mame);
		}
		writer.writeEndElement();
	}
}
