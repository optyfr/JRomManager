package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.xml.stream.XMLStreamException;

import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Export.SimpleAttribute;
import jrm.profile.Profile;
import jrm.profile.data.Software.Supported;
import jrm.ui.ProgressHandler;
import jrm.ui.SoftwareListRenderer;

@SuppressWarnings("serial")
public class SoftwareList extends AnywareList<Software> implements Systm, Serializable, Comparable<SoftwareList>
{
	public String name; // required
	public final StringBuffer description = new StringBuffer();

	private final List<Software> s_list = new ArrayList<>();
	public final Map<String, Software> s_byname = new HashMap<>();

	public SoftwareList()
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
	public boolean add(final Software software)
	{
		software.sl = this;
		s_byname.put(software.name, software);
		return s_list.add(software);
	}

	@Override
	public int compareTo(final SoftwareList o)
	{
		return name.compareTo(o.name);
	}

	@Override
	public int getColumnCount()
	{
		return SoftwareListRenderer.columns.length;
	}

	@Override
	public String getColumnName(final int columnIndex)
	{
		return SoftwareListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex)
	{
		return SoftwareListRenderer.columnsTypes[columnIndex];
	}

	@Override
	public TableCellRenderer getColumnRenderer(final int columnIndex)
	{
		return SoftwareListRenderer.columnsRenderers[columnIndex] != null ? SoftwareListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	@Override
	public int getColumnWidth(final int columnIndex)
	{
		return SoftwareListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return getFilteredList().size();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex)
	{
		final Software software = getFilteredList().get(rowIndex);
		switch(columnIndex)
		{
			case 0:
				return software;
			case 1:
				return software;
			case 2:
				return software.description.toString();
			case 3:
				return String.format("%d/%d", software.countHave(), software.roms.size() + software.disks.size());
			case 4:
				return software.cloneof != null ? s_byname.get(software.cloneof) : null;
		}
		return null;
	}

	@Override
	public List<Software> getList()
	{
		return s_list;
	}

	@Override
	public Type getType()
	{
		return Type.SOFTWARELIST;
	}

	@Override
	public Systm getSystem()
	{
		return this;
	}

	@Override
	public String toString()
	{
		return "[" + getType() + "] " + description.toString();
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Stream<Software> getFilteredStream()
	{
		final boolean filterIncludeClones = Profile.curr_profile.getProperty("filter.InclClones", true);
		final boolean filterIncludeDisks = Profile.curr_profile.getProperty("filter.InclDisks", true);
		final Supported filterMinSoftwareSupportedLevel = Supported.valueOf(Profile.curr_profile.getProperty("filter.MinSoftwareSupportedLevel", Supported.no.toString()));
		final String filterYearMin = Profile.curr_profile.getProperty("filter.YearMin", "");
		final String filterYearMax = Profile.curr_profile.getProperty("filter.YearMax", "????");
		return getList().stream().filter(t -> {
			if(t.year.length()>0)
			{
				if(filterYearMin.compareTo(t.year.toString())>0)
					return false;
				if(filterYearMax.compareTo(t.year.toString())<0)
					return false;
			}
			if(filterMinSoftwareSupportedLevel==Supported.partial && t.supported==Supported.no)
				return false;
			if(filterMinSoftwareSupportedLevel==Supported.yes && t.supported!=Supported.yes)
				return false;
			if(!filterIncludeClones && t.isClone())
				return false;
			if(!filterIncludeDisks && t.disks.size()>0)
				return false;
			if(!t.getSystem().isSelected())
				return false;
			return true;
		});
	}

	@Override
	protected List<Software> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(t -> AnywareList.filter.contains(t.getStatus())).sorted().collect(Collectors.toList());
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
		return getFilteredStream().filter(t -> t.getStatus()==AnywareStatus.COMPLETE).count();
	}

	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress) throws XMLStreamException, IOException
	{
		writer.writeStartElement("softwarelist",
				new SimpleAttribute("name",name),
				new SimpleAttribute("description",description)
				);
		final List<Software> list = getFilteredStream().collect(Collectors.toList());
		for(final Software s : list)
		{
			progress.setProgress(String.format("Exporting %s", s.getFullName()), progress.getValue()+1);
			s.export(writer);
		}
		writer.writeEndElement();
	}

	@Override
	public boolean containsName(final String name)
	{
		return s_byname.containsKey(name);
	}

	@Override
	public Software getByName(String name)
	{
		return s_byname.get(name);
	}
}
