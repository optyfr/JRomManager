package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import jrm.Messages;
import jrm.profile.Export;
import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.ui.AnywareListListRenderer;
import jrm.ui.ProgressHandler;

@SuppressWarnings("serial")
public final class MachineListList extends AnywareListList<MachineList> implements Serializable
{
	private final List<MachineList> ml_list = Collections.singletonList(new MachineList());

	public final SoftwareListList softwarelist_list = new SoftwareListList();

	public final Map<String, List<Machine>> softwarelist_defs = new HashMap<>();

	public MachineListList()
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
		return AnywareListListRenderer.columns.length;
	}

	@Override
	public String getColumnName(final int columnIndex)
	{
		return AnywareListListRenderer.columns[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex)
	{
		return AnywareListListRenderer.columnsTypes[columnIndex];
	}

	@Override
	public TableCellRenderer getColumnRenderer(final int columnIndex)
	{
		return AnywareListListRenderer.columnsRenderers[columnIndex] != null ? AnywareListListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
	}

	@Override
	public int getColumnWidth(final int columnIndex)
	{
		return AnywareListListRenderer.columnsWidths[columnIndex];
	}

	@Override
	public int getRowCount()
	{
		return ml_list.size() + softwarelist_list.getRowCount();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex)
	{
		if(rowIndex < ml_list.size())
		{
			switch(columnIndex)
			{
				case 0:
					return ml_list.get(rowIndex);
				case 1:
					return Messages.getString("MachineListList.AllMachines"); //$NON-NLS-1$
				case 2:
					return String.format("%d/%d", ml_list.get(rowIndex).countHave(), ml_list.get(rowIndex).countAll()); //$NON-NLS-1$
			}
		}
		else
			return softwarelist_list.getValueAt(rowIndex - ml_list.size(), columnIndex);
		return null;
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
			filtered_list = getFilteredStream().filter(t -> AnywareListList.filter.contains(t.getStatus())).sorted().collect(Collectors.toList());
		return filtered_list;
	}

	public Machine findMachine(final String softwarelist, final String compatibility)
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
			}).findFirst().orElse(null);
		return null;
	}

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
}
