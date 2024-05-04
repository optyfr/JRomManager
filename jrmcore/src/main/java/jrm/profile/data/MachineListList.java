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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import jrm.aui.progress.ProgressHandler;
import jrm.profile.Profile;
import jrm.profile.manager.Export;
import jrm.xml.EnhancedXMLStreamWriter;
import lombok.Getter;

/**
 * Singleton List of machines lists
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	private static final String UTF_8 = "UTF-8";

	/**
	 * The {@link List} of {@link MachineList}, in fact the is only one item
	 */
	private final List<MachineList> mlList;

	/**
	 * The attached list of software lists ({@link SoftwareListList}), if any
	 */
	private final @Getter SoftwareListList softwareListList;

	/**
	 * A mapping between a software list name and list of machines declared to be at least compatible with that software list 
	 */
	private final @Getter Map<String, List<Machine>> softwareListDefs = new HashMap<>();

	/**
	 * The constructor, will initialize transients fields
	 */
	public MachineListList(Profile profile)
	{
		super(profile);
		softwareListList = new SoftwareListList(profile);
		mlList = Collections.singletonList(new MachineList(profile));
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
	public void resetCache()
	{
		this.filteredList = null;
		softwareListList.resetCache();
	}

	@Override
	public void setFilterCache(final Set<AnywareStatus> filter)
	{
		profile.setFilterListLists(filter);
	}

	@Override
	public List<MachineList> getList()
	{
		return mlList;
	}

	@Override
	public Stream<MachineList> getFilteredStream()
	{
		return getList().stream();
	}

	@Override
	public List<MachineList> getFilteredList()
	{
		if(filteredList == null)
			filteredList = getFilteredStream().filter(t -> profile.getFilterListLists().contains(t.getStatus())).sorted().collect(Collectors.toList());
		return filteredList;
	}

	/**
	 * Will return a list of machine for a given software list ordered by compatibility and driver status support
	 * @param softwarelist the name of the software list
	 * @param compatibility the compatibility string (if any) declared for a software in the software list
	 * @return the ordered {@link List} of {@link Machine} with best match first, or null if no machine were found for this software list
	 */
	public List<Machine> getSortedMachines(final String softwarelist, final String compatibility)
	{
		if (softwareListDefs.containsKey(softwarelist))
		{
			return softwareListDefs.get(softwarelist).stream()
					.filter(m -> m.isCompatible(softwarelist, compatibility) > 0)
					.sorted((o1, o2) -> getComparator(softwarelist, compatibility, o1, o2))
					.toList();
		}
		return List.of();
	}

	/**
	 * @param softwarelist
	 * @param compatibility
	 * @param o1
	 * @param o2
	 * @return
	 */
	protected int getComparator(final String softwarelist, final String compatibility, Machine o1, Machine o2)
	{
		int c1 = o1.isCompatible(softwarelist, compatibility);
		int c2 = o2.isCompatible(softwarelist, compatibility);
		if (o1.driver.getStatus() == Driver.StatusType.good)
			c1 += 2;
		if (o1.driver.getStatus() == Driver.StatusType.imperfect)
			c1 += 1;
		if (o2.driver.getStatus() == Driver.StatusType.good)
			c2 += 2;
		if (o2.driver.getStatus() == Driver.StatusType.imperfect)
			c2 += 1;
		if (c1 < c2)
			return 1;
		if (c1 > c2)
			return -1;
		return 0;
	}
	
	/**
	 * Find the best matching machine for a given software list ordered by compatibility and driver status support
	 * @param softwarelist the name of the software list
	 * @param compatibility the compatibility string (if any) declared for a software in the software list
	 * @return the best matched {@link Machine}
	 */
	public Machine findMachine(final String softwarelist, final String compatibility)
	{
		if(softwareListDefs.containsKey(softwarelist))
		{
			final var list = getSortedMachines(softwarelist, compatibility);
			if(list!=null)
				return list.stream().findFirst().orElse(null);
		}
		return null;
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param progress the {@link ProgressHandler} to show the current progress
	 * @param is_mame is it mame (true) or logqix (false) format ?
	 * @param modes the export modes
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress, final boolean is_mame, final Set<ExportMode> modes) throws XMLStreamException, IOException
	{
		final List<MachineList> lists = getFilteredStream().toList();
		if(lists.size() > 0)
		{
			writer.writeStartDocument(UTF_8,"1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			if(is_mame)
			{
				writer.writeDTD("<!DOCTYPE mame [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/mame.dtd"), StandardCharsets.UTF_8) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			else
			{
				writer.writeDTD("<!DOCTYPE datafile [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/datafile.dtd"), StandardCharsets.UTF_8) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			for(final MachineList list : lists)
				list.export(writer, progress, is_mame, modes);
			writer.writeEndDocument();
		}
	}

	@Override
	public int count()
	{
		return getList().size() + softwareListList.count();
	}

	@Override
	public AnywareList<? extends Anyware> getObject(int i)
	{
		if(i < getList().size())
			return getList().get(i);
		return softwareListList.getFilteredList().get(i - getList().size());
	}

	@Override
	public String getDescription(int i)
	{
		if(i < getList().size())
			return profile.getSession().getMsgs().getString("MachineListList.AllMachines");
		return softwareListList.getDescription(i - getList().size());
	}

	@Override
	public String getHaveTot(int i)
	{
		if(i < getList().size())
			return String.format("%d/%d", getList().get(i).countHave(), getList().get(i).countAll()); //$NON-NLS-1$
		return softwareListList.getHaveTot(i - getList().size());
	}
}
