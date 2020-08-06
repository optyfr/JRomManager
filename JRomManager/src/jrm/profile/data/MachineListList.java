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
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import jrm.aui.progress.ProgressHandler;
import jrm.profile.Profile;
import jrm.profile.manager.Export;
import jrm.xml.EnhancedXMLStreamWriter;

/**
 * Singleton List of machines lists
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	/**
	 * The {@link List} of {@link MachineList}, in fact the is only one item
	 */
	private final List<MachineList> ml_list;

	/**
	 * The attached list of software lists ({@link SoftwareListList}), if any
	 */
	public final SoftwareListList softwarelist_list;

	/**
	 * A mapping between a software list name and list of machines declared to be at least compatible with that software list 
	 */
	public final Map<String, List<Machine>> softwarelist_defs = new HashMap<>();

	/**
	 * The constructor, will initialize transients fields
	 */
	public MachineListList(Profile profile)
	{
		super(profile);
		softwarelist_list = new SoftwareListList(profile);
		ml_list = Collections.singletonList(new MachineList(profile));
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
	public void resetCache()
	{
		this.filtered_list = null;
		softwarelist_list.resetCache();
	}

	@Override
	public void setFilterCache(final EnumSet<AnywareStatus> filter)
	{
		profile.filter_ll = filter;
	}

	@Override
	public List<MachineList> getList()
	{
		return ml_list;
	}

	@Override
	public Stream<MachineList> getFilteredStream()
	{
		return getList().stream();
	}

	@Override
	protected List<MachineList> getFilteredList()
	{
		if(filtered_list == null)
			filtered_list = getFilteredStream().filter(t -> profile.filter_ll.contains(t.getStatus())).sorted().collect(Collectors.toList());
		return filtered_list;
	}

	/**
	 * Will return a list of machine for a given software list ordered by compatibility and driver status support
	 * @param softwarelist the name of the software list
	 * @param compatibility the compatibility string (if any) declared for a software in the software list
	 * @return the ordered {@link List} of {@link Machine} with best match first, or null if no machine were found for this software list
	 */
	public List<Machine> getSortedMachines(final String softwarelist, final String compatibility)
	{
		if(softwarelist_defs.containsKey(softwarelist))
			return softwarelist_defs.get(softwarelist).stream().filter(m -> m.isCompatible(softwarelist, compatibility) > 0).sorted((o1, o2) -> {
				int c1 = o1.isCompatible(softwarelist, compatibility);
				int c2 = o2.isCompatible(softwarelist, compatibility);
				if(o1.driver.getStatus() == Driver.StatusType.good)
					c1 += 2;
				if(o1.driver.getStatus() == Driver.StatusType.imperfect)
					c1 += 1;
				if(o2.driver.getStatus() == Driver.StatusType.good)
					c2 += 2;
				if(o2.driver.getStatus() == Driver.StatusType.imperfect)
					c2 += 1;
				if(c1 < c2)
					return 1;
				if(c1 > c2)
					return -1;
				return 0;
			}).collect(Collectors.toList());
		return null;
		
	}
	
	/**
	 * Find the best matching machine for a given software list ordered by compatibility and driver status support
	 * @param softwarelist the name of the software list
	 * @param compatibility the compatibility string (if any) declared for a software in the software list
	 * @return the best matched {@link Machine}
	 */
	public Machine findMachine(final String softwarelist, final String compatibility)
	{
		if(softwarelist_defs.containsKey(softwarelist))
			return getSortedMachines(softwarelist, compatibility).stream().findFirst().orElse(null);
		return null;
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
		final List<MachineList> lists = getFilteredStream().collect(Collectors.toList());
		if(lists.size() > 0)
		{
			writer.writeStartDocument("UTF-8","1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			if(is_mame)
			{
				writer.writeDTD("<!DOCTYPE mame [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/mame.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else
			{
				writer.writeDTD("<!DOCTYPE datafile [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/datafile.dtd"), Charset.forName("UTF-8")) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			for(final MachineList list : lists)
				list.export(writer, progress, is_mame, filtered);
			writer.writeEndDocument();
		}
	}

	@Override
	public int count()
	{
		return getList().size() + softwarelist_list.count();
	}

	@Override
	public AnywareList<?> getObject(int i)
	{
		if(i < getList().size())
			return getList().get(i);
		return softwarelist_list.getFilteredList().get(i - getList().size());
	}

	@Override
	public String getDescription(int i)
	{
		if(i < getList().size())
			return profile.session.msgs.getString("MachineListList.AllMachines");
		return softwarelist_list.getDescription(i - getList().size());
	}

	@Override
	public String getHaveTot(int i)
	{
		if(i < getList().size())
			return String.format("%d/%d", getList().get(i).countHave(), getList().get(i).countAll()); //$NON-NLS-1$
		return softwarelist_list.getHaveTot(i - getList().size());
	}
}
