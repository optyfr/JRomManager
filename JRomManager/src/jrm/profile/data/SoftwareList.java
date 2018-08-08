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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.TableCellRenderer;
import javax.xml.stream.XMLStreamException;

import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.data.Software.Supported;
import jrm.ui.profile.data.SoftwareListRenderer;
import jrm.ui.progress.ProgressHandler;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;

/**
 * a {@link Software} list 
 * @author optyfr
 */
@SuppressWarnings("serial")
public final class SoftwareList extends AnywareList<Software> implements Systm, Serializable
{
//	public String name; // required
	
	/**
	 * description of the software list
	 */
	public final StringBuffer description = new StringBuffer();

	/**
	 * The {@link ArrayList} of {@link Software}
	 */
	private final List<Software> s_list = new ArrayList<>();
	/**
	 * The by name {@link HashMap} of {@link Software}
	 */
	private final Map<String, Software> s_byname = new HashMap<>();

	/**
	 * The constructor, will initialize transients fields
	 */
	public SoftwareList()
	{
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
	public boolean add(final Software software)
	{
		software.sl = this;
		s_byname.put(software.name, software);
		return s_list.add(software);
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
		return SoftwareListRenderer.columnsRenderers[columnIndex];
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
				return String.format("%d/%d", software.countHave(), software.roms.size() + software.disks.size()); //$NON-NLS-1$
			case 4:
				return software.cloneof != null ? s_byname.get(software.cloneof) : null;
			case 5:
				return software.selected;
		}
		return null;
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex)
	{
		return columnIndex==5;
	}

	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
	{
		if(columnIndex==5 && aValue instanceof Boolean)
		{
			final Software software = getFilteredList().get(rowIndex);
			software.selected = (Boolean)aValue;
		}
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
		return "[" + getType() + "] " + description.toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public Stream<Software> getFilteredStream()
	{
		/*
		 * get all needed profile options
		 */
		final boolean filterIncludeClones = Profile.curr_profile.getProperty("filter.InclClones", true); //$NON-NLS-1$
		final boolean filterIncludeDisks = Profile.curr_profile.getProperty("filter.InclDisks", true); //$NON-NLS-1$
		final Supported filterMinSoftwareSupportedLevel = Supported.valueOf(Profile.curr_profile.getProperty("filter.MinSoftwareSupportedLevel", Supported.no.toString())); //$NON-NLS-1$
		final String filterYearMin = Profile.curr_profile.getProperty("filter.YearMin", ""); //$NON-NLS-1$ //$NON-NLS-2$
		final String filterYearMax = Profile.curr_profile.getProperty("filter.YearMax", "????"); //$NON-NLS-1$ //$NON-NLS-2$
		
		return getList().stream().filter(t -> {
			if(t.year.length()>0)
			{	// exclude machines outside defined year range
				if(filterYearMin.compareTo(t.year.toString())>0)
					return false;
				if(filterYearMax.compareTo(t.year.toString())<0)
					return false;
			}
			if(filterMinSoftwareSupportedLevel==Supported.partial && t.supported==Supported.no)	// exclude support=no software if min software support is partial
				return false;
			if(filterMinSoftwareSupportedLevel==Supported.yes && t.supported!=Supported.yes) // exclude support!=yes if min software support is yes
				return false;
			if(!filterIncludeClones && t.isClone())	// exclude clones machines
				return false;
			if(!filterIncludeDisks && t.disks.size()>0)	// exclude softwares with disks
				return false;
			if(!t.getSystem().isSelected())	// exclude software for which their software list were not selected
				return false;
			return true;	// otherwise include
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

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param filtered do we use the current machine filters of none
	 * @param progress the {@link ProgressHandler} to show the current progress
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void export(final EnhancedXMLStreamWriter writer, boolean filtered, final ProgressHandler progress) throws XMLStreamException, IOException
	{
		writer.writeStartElement("softwarelist", //$NON-NLS-1$
				new SimpleAttribute("name",name), //$NON-NLS-1$
				new SimpleAttribute("description",description) //$NON-NLS-1$
				);
		final List<Software> list = filtered?getFilteredStream().collect(Collectors.toList()):getList();
		for(final Software s : list)
		{
			progress.setProgress(String.format(Messages.getString("SoftwareList.Exporting_%s"), s.getFullName()), progress.getValue()+1); //$NON-NLS-1$
			if(!filtered || s.selected)
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

	@Override
	public Software putByName(Software t)
	{
		return s_byname.put(t.name, t);
	}
	
	/**
	 * named map filtered cache
	 */
	private transient Map<String, Software> s_filtered_byname = null;

	@Override
	public void resetFilteredName()
	{
		s_filtered_byname = getFilteredStream().collect(Collectors.toMap(Software::getBaseName, Function.identity()));
	}

	@Override
	public boolean containsFilteredName(String name)
	{
		if(s_filtered_byname==null)
			resetFilteredName();
		return s_filtered_byname.containsKey(name);
	}

	@Override
	public Software getFilteredByName(String name)
	{
		if(s_filtered_byname==null)
			resetFilteredName();
		return s_filtered_byname.get(name);
	}
}
