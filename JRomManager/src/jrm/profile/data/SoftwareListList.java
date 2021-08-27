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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import jrm.aui.progress.ProgressHandler;
import jrm.profile.Profile;
import jrm.profile.manager.Export;
import jrm.xml.EnhancedXMLStreamWriter;

/**
 * List of {@link SoftwareList}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class SoftwareListList extends AnywareListList<SoftwareList> implements Serializable, ByName<SoftwareList>
{
	private static final String N_OF_T = "%d/%d";

	/**
	 * The {@link List} of {@link SoftwareList}
	 */
	private final ArrayList<SoftwareList> swListList = new ArrayList<>();
	
	/**
	 * The by name {@link HashMap} of {@link SoftwareList}
	 */
	private final HashMap<String, SoftwareList> swListByName = new HashMap<>();

	/**
	 * The constructor, will initialize transients fields
	 */
	public SoftwareListList(Profile profile)
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
	public void resetCache()
	{
		this.filteredList = null;
	}

	@Override
	public void setFilterCache(final Set<AnywareStatus> filter)
	{
		// not used
	}

	@Override
	public List<SoftwareList> getList()
	{
		return swListList;
	}

	@Override
	public Stream<SoftwareList> getFilteredStream()
	{
		return getList().stream().filter(sl -> sl.getSystem().isSelected(sl.profile));
	}

	@Override
	public List<SoftwareList> getFilteredList()
	{
		if(filteredList == null)
			filteredList = getFilteredStream().filter(t -> profile.getFilterListLists().contains(t.getStatus())).sorted().collect(Collectors.toList());
		return filteredList;
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param progress the {@link ProgressHandler} to show the current progress
	 * @param filtered do we use the current machine filters of none
	 * @param selection the selected software list (null if none selected)
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void export(final EnhancedXMLStreamWriter writer, final ProgressHandler progress, final boolean filtered, final SoftwareList selection) throws XMLStreamException, IOException
	{
		final List<SoftwareList> lists;
		if(selection!=null)
			lists = Collections.singletonList(selection);
		else
		{
			if(filtered)
				lists=getFilteredStream().collect(Collectors.toList());
			else
				lists=getList();
		}
		if(lists.size() > 0)
		{
			writer.writeStartDocument("UTF-8","1.0"); //$NON-NLS-1$ //$NON-NLS-2$
			if(lists.size() > 1)
			{
				writer.writeDTD("<!DOCTYPE softwarelists [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelists.dtd"), StandardCharsets.UTF_8) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				writer.writeStartElement("softwarelists"); //$NON-NLS-1$
			}
			else
				writer.writeDTD("<!DOCTYPE softwarelist [\n" + IOUtils.toString(Export.class.getResourceAsStream("/jrm/resources/dtd/softwarelist.dtd"), StandardCharsets.UTF_8) + "\n]>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			progress.setProgress("Exporting", 0, lists.stream().flatMapToInt(sl -> IntStream.of(sl.size())).sum()); //$NON-NLS-1$
			progress.setProgress2(String.format(N_OF_T, 0, lists.size()), 0, lists.size()); //$NON-NLS-1$
			for(final SoftwareList list : lists)
			{
				list.export(writer, filtered, progress);
				progress.setProgress2(String.format(N_OF_T, progress.getValue2()+1, lists.size()), progress.getValue2()+1); //$NON-NLS-1$
			}
			writer.writeEndDocument();
		}
	}

	@Override
	public boolean containsName(String name)
	{
		return swListByName.containsKey(name);
	}

	@Override
	public SoftwareList getByName(String name)
	{
		return swListByName.get(name);
	}

	@Override
	public SoftwareList putByName(SoftwareList t)
	{
		return swListByName.put(t.name, t);
	}

	/**
	 * named map filtered cache
	 */
	private transient Map<String, SoftwareList> swListFilteredByName = null;

	@Override
	public void resetFilteredName()
	{
		swListFilteredByName = getFilteredStream().collect(Collectors.toMap(SoftwareList::getBaseName, Function.identity()));
	}

	@Override
	public boolean containsFilteredName(String name)
	{
		if(swListFilteredByName==null)
			resetFilteredName();
		return swListFilteredByName.containsKey(name);
	}

	@Override
	public SoftwareList getFilteredByName(String name)
	{
		if(swListFilteredByName==null)
			resetFilteredName();
		return swListFilteredByName.get(name);
	}

	@Override
	public int count()
	{
		return getFilteredList().size();
	}

	@Override
	public SoftwareList getObject(int i)
	{
		return getFilteredList().get(i);
	}

	@Override
	public String getDescription(int i)
	{
		return getObject(i).getDescription().toString();
	}

	@Override
	public String getHaveTot(int i)
	{
		return String.format(N_OF_T, getFilteredList().get(i).countHave(), getFilteredList().get(i).countAll()); //$NON-NLS-1$
	}
}
