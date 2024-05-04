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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Software.Supported;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;
import lombok.Getter;

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
	private final @Getter StringBuilder description = new StringBuilder();

	/**
	 * The {@link ArrayList} of {@link Software}
	 */
	private final List<Software> swList = new ArrayList<>();
	/**
	 * The by name {@link HashMap} of {@link Software}
	 */
	private final Map<String, Software> swByName = new HashMap<>();

	/**
	 * The constructor, will initialize transients fields
	 */
	public SoftwareList(Profile profile)
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
	public boolean add(final Software software)
	{
		software.setSl(this);
		swByName.put(software.name, software);
		return swList.add(software);
	}

	@Override
	public List<Software> getList()
	{
		return swList;
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

	private class FilterOptions
	{
		/*
		 * get all needed profile options
		 */
		final boolean filterIncludeClones = profile.getProperty(ProfileSettingsEnum.filter_InclClones, Boolean.class); //$NON-NLS-1$
		final boolean filterIncludeDisks = profile.getProperty(ProfileSettingsEnum.filter_InclDisks, Boolean.class); //$NON-NLS-1$
		final Supported filterMinSoftwareSupportedLevel = Supported.valueOf(profile.getProperty(ProfileSettingsEnum.filter_MinSoftwareSupportedLevel, String.class)); //$NON-NLS-1$
		final String filterYearMin = profile.getProperty(ProfileSettingsEnum.filter_YearMin, String.class); //$NON-NLS-1$ //$NON-NLS-2$
		final String filterYearMax = profile.getProperty(ProfileSettingsEnum.filter_YearMax, String.class); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	@Override
	public Stream<Software> getFilteredStream()
	{
		final var options = new FilterOptions();
		
		return getList().stream().filter(t -> {
			if(!getYearFilter(options, t))
				return false;
			if(options.filterMinSoftwareSupportedLevel==Supported.partial && t.getSupported()==Supported.no)	// exclude support=no software if min software support is partial
				return false;
			if(options.filterMinSoftwareSupportedLevel==Supported.yes && t.getSupported()!=Supported.yes) // exclude support!=yes if min software support is yes
				return false;
			if(!options.filterIncludeClones && t.isClone())	// exclude clones machines
				return false;
			if(!options.filterIncludeDisks && t.getDisks().size()>0)	// exclude softwares with disks
				return false;
			return t.getSystem().isSelected(profile);	// exclude software for which their software list were not selected
		});
	}

	/**
	 * @param options
	 * @param t
	 */
	private boolean getYearFilter(final FilterOptions options, Software t)
	{
		if(t.year.length()>0)
		{	// exclude machines outside defined year range
			if(options.filterYearMin.compareTo(t.year.toString())>0)
				return false;
			if(options.filterYearMax.compareTo(t.year.toString())<0)
				return false;
		}
		return true;
	}

	@Override
	public List<Software> getFilteredList()
	{
		if(filteredList == null)
			filteredList = getFilteredStream().filter(t -> profile.getFilterList().contains(t.getStatus())).sorted().collect(Collectors.toList());
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
		return getFilteredStream().filter(t -> t.getStatus()==AnywareStatus.COMPLETE).count();
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param modes the export modes
	 * @param progress the {@link ProgressHandler} to show the current progress
	 * @throws XMLStreamException
	 */
	public void export(final EnhancedXMLStreamWriter writer, Set<ExportMode> modes, final ProgressHandler progress) throws XMLStreamException
	{
		writer.writeStartElement("softwarelist", //$NON-NLS-1$
				new SimpleAttribute("name", name), //$NON-NLS-1$
				new SimpleAttribute("description", description) //$NON-NLS-1$
		);
		final List<Software> list = modes.contains(ExportMode.FILTERED) ? getFilteredStream().toList() : getList();
		for (final Software s : list) {
			if (progress.isCancel())
				break;
			progress.setProgress(String.format(profile.getSession().getMsgs().getString("SoftwareList.Exporting_%s"), //$NON-NLS-1$
					s.getFullName()), progress.getCurrent() + 1);
			if (modes.contains(ExportMode.ALL)
				|| (modes.contains(ExportMode.FILTERED) && s.isSelected())
				|| (modes.contains(ExportMode.MISSING) && s.getStatus() != AnywareStatus.COMPLETE && s.getStatus() != AnywareStatus.UNKNOWN)
				|| (modes.contains(ExportMode.HAVE) && s.getStatus() != AnywareStatus.MISSING && s.getStatus() != AnywareStatus.UNKNOWN)
			)
				s.export(writer, null, modes);
		}
		writer.writeEndElement();
	}

	@Override
	public boolean containsName(final String name)
	{
		return swByName.containsKey(name);
	}

	@Override
	public Software getByName(String name)
	{
		return swByName.get(name);
	}

	@Override
	public Software putByName(Software t)
	{
		return swByName.put(t.name, t);
	}
	
	/**
	 * named map filtered cache
	 */
	private transient Map<String, Software> swFilteredByName = null;

	@Override
	public void resetFilteredName()
	{
		swFilteredByName = getFilteredStream().collect(Collectors.toMap(Software::getBaseName, Function.identity()));
	}

	@Override
	public boolean containsFilteredName(String name)
	{
		if(swFilteredByName==null)
			resetFilteredName();
		return swFilteredByName.containsKey(name);
	}

	@Override
	public Software getFilteredByName(String name)
	{
		if(swFilteredByName==null)
			resetFilteredName();
		return swFilteredByName.get(name);
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
