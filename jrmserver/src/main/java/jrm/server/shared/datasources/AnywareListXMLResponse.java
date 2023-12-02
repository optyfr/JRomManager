package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.misc.Log;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.Machine;
import jrm.profile.data.MachineList;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.server.shared.datasources.XMLRequest.Operation.Sorter;

public class AnywareListXMLResponse extends XMLResponse
{

	private static final String RESPONSE = "response";
	private static final String NAME = "name";
	private static final String SELECTED = "selected";
	private static final String SAMPLEOF = "sampleof";
	private static final String ROMOF = "romof";
	private static final String CLONEOF = "cloneof";
	private static final String DESCRIPTION = "description";
	private static final String STATUS = "status";

	public AnywareListXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	private AnywareList<? extends Anyware> getList(Operation operation)
	{
		String list = operation.getData("list");
		final AnywareList<?> al;
		if(list==null)
			al = null;
		else if(list.equals("*"))
			al = request.session.getCurrProfile().getMachineListList().get(0);
		else
			al = request.session.getCurrProfile().getMachineListList().getSoftwareListList().getByName(list);
		return al;
	}
	
	private Predicate<Anyware> getFilter(Operation operation)
	{
		final Set<String> lstatus = operation.hasData(STATUS) ? Stream.of(operation.getData(STATUS).split(",")).collect(Collectors.toSet()) : null;
		final String lname = operation.hasData(NAME) ? operation.getData(NAME).toLowerCase() : null;
		final String ldesc = operation.hasData(DESCRIPTION) ? operation.getData(DESCRIPTION).toLowerCase() : null;
		final String lcloneof = operation.hasData(CLONEOF) ? operation.getData(CLONEOF).toLowerCase() : null;
		final String lromof = operation.hasData(ROMOF) ? operation.getData(ROMOF).toLowerCase() : null;
		final String lsampleof = operation.hasData(SAMPLEOF) ? operation.getData(SAMPLEOF).toLowerCase() : null;
		final Boolean lselected = operation.hasData(SELECTED) ? Boolean.valueOf(operation.getData(SELECTED)) : null;
		return ware -> !filterStatus(lstatus, ware) && !filterSelected(lselected, ware) && !filterName(lname, ware) && !filteDesc(ldesc, ware) && !filterCloneOf(lcloneof, ware) && !filterRomOf(lromof, ware) && !filterSampleOf(lsampleof, ware);
	}


	/**
	 * @param lsampleof
	 * @param ware
	 * @return
	 */
	private boolean filterSampleOf(final String lsampleof, Anyware ware)
	{
		return lsampleof != null && ware instanceof Machine m && (m.getSampleof() == null || !m.getSampleof().toLowerCase().contains(lsampleof));
	}


	/**
	 * @param lromof
	 * @param ware
	 * @return
	 */
	private boolean filterRomOf(final String lromof, Anyware ware)
	{
		return lromof != null && ware instanceof Machine m && (m.getRomof() == null || !m.getRomof().toLowerCase().contains(lromof));
	}


	/**
	 * @param lcloneof
	 * @param ware
	 * @return
	 */
	private boolean filterCloneOf(final String lcloneof, Anyware ware)
	{
		return lcloneof != null && (ware.getCloneof() == null || !ware.getCloneof().toLowerCase().contains(lcloneof));
	}


	/**
	 * @param ldesc
	 * @param ware
	 * @return
	 */
	private boolean filteDesc(final String ldesc, Anyware ware)
	{
		return ldesc != null && !ware.description.toString().toLowerCase().contains(ldesc);
	}


	/**
	 * @param lname
	 * @param ware
	 * @return
	 */
	private boolean filterName(final String lname, Anyware ware)
	{
		return lname != null && !ware.getBaseName().toLowerCase().contains(lname);
	}


	/**
	 * @param lselected
	 * @param ware
	 * @return
	 */
	private boolean filterSelected(final Boolean lselected, Anyware ware)
	{
		return lselected != null && ware.isSelected() != lselected;
	}


	/**
	 * @param lstatus
	 * @param ware
	 * @return
	 */
	private boolean filterStatus(final Set<String> lstatus, Anyware ware)
	{
		return lstatus != null && !lstatus.contains(ware.getStatus().toString());
	}
	
	private Comparator<Anyware> getSorter(Operation operation)
	{
		return (o1, o2) -> {
			if (operation.getSort().isEmpty())
				return o1.getBaseName().compareToIgnoreCase(o2.getBaseName());
			for (Sorter s : operation.getSort())
			{
				switch (s.getName())
				{
					case NAME:
					{
						final int ret = sortByName(o1, o2, s);
						if (ret != 0)
							return ret;
						break;
					}
					case DESCRIPTION:
					{
						final int ret = sortByDesc(o1, o2, s);
						if (ret != 0)
							return ret;
						break;
					}
					default:
						break;
				}
			}
			return 0;
		};
	}


	/**
	 * @param o1
	 * @param o2
	 * @param s
	 * @return
	 */
	private int sortByDesc(Anyware o1, Anyware o2, Sorter s)
	{
		return (s.isDesc() ? o2 : o1).description.toString().compareToIgnoreCase((s.isDesc() ? o1 : o2).description.toString());
	}


	/**
	 * @param o1
	 * @param o2
	 * @param s
	 * @return
	 */
	private int sortByName(Anyware o1, Anyware o2, Sorter s)
	{
		return (s.isDesc() ? o2 : o1).getBaseName().compareToIgnoreCase((s.isDesc() ? o1 : o2).getBaseName());
	}
	
	private Stream<? extends Anyware> buildStream(AnywareList<? extends Anyware> al, Operation operation)
	{
		return al.getFilteredList().stream().filter(getFilter(operation)).sorted(getSorter(operation));
	}
	
	private List<Anyware> buildList(AnywareList<?> al, Operation operation)
	{
		return al.getFilteredList().stream().filter(getFilter(operation)).sorted(getSorter(operation)).collect(Collectors.toList());
	}

	private void writeRecord(final AnywareList<?> al, final Anyware aw)
	{
		try
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("list", al instanceof MachineList?"*":al.getBaseName());
			writer.writeAttribute(STATUS, aw.getStatus().toString());
			writer.writeAttribute(NAME, aw.getBaseName());
			writer.writeAttribute(DESCRIPTION, aw.getDescription().toString());
			writer.writeAttribute("have",  String.format("%d/%d", aw.countHave(), aw.countAll()));
			if(aw.getCloneof()!=null)
			{
				writer.writeAttribute(CLONEOF,  aw.getCloneof());
				writer.writeAttribute("cloneof_status",  al.getByName(aw.getCloneof()).getStatus().toString());
			}
			if(aw instanceof Machine m)
			{
				MachineList ml = (MachineList)al;
				if(m.isIsbios())
					writer.writeAttribute("type", "BIOS");
				else if(m.isIsdevice())
					writer.writeAttribute("type", "DEVICE");
				else if(m.isIsmechanical())
					writer.writeAttribute("type", "MECHANICAL");
				else
					writer.writeAttribute("type", "STANDARD");
				if(m.getRomof()!=null)
				{
					writer.writeAttribute(ROMOF,  m.getRomof());
					writer.writeAttribute("romof_status",  al.getByName(m.getRomof()).getStatus().toString());
				}
				if(m.getSampleof()!=null)
				{
					writer.writeAttribute(SAMPLEOF,  m.getSampleof());
					writer.writeAttribute("sampleof_status", ml.samplesets.getByName(m.getSampleof()).getStatus().toString());
				}
			}
			writer.writeAttribute(SELECTED, Boolean.toString(aw.isSelected()));
		}
		catch (XMLStreamException e)
		{
			Log.err(e.getMessage(),e);
		}
	}
	
	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		final var reset = Boolean.parseBoolean(operation.getData("reset"));
		final AnywareList<?> al = getList(operation);
		if(al != null)
		{
			if(reset)
				al.resetCache();
			fetchStream(operation, buildStream(al, operation), rec -> writeRecord(al, rec));
		}
		writer.writeEndElement();
	}
	
	@Override
	protected void update(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		final AnywareList<?> al = getList(operation);
		if(al != null)
		{
			String name = operation.getData(NAME);
			if(name != null)
			{
				Anyware aw = al.getByName(name);
				if(aw != null)
				{
					final var selected = Boolean.valueOf(operation.getData(SELECTED));
					if(selected != null)
						aw.setSelected(selected);
					writeRecord(al, aw);
				}
			}
		}
		writer.writeEndElement();
	}
	
	@Override
	protected void custom(Operation operation) throws XMLStreamException
	{
		if(operation.getOperationId().toString().equals("find"))
		{
			find(operation);
		}
		else if(operation.getOperationId().toString().equals("selectNone"))
		{
			selectNone(operation);
		}
		else if(operation.getOperationId().toString().equals("selectAll"))
		{
			selectAll(operation);
		}
		else if(operation.getOperationId().toString().equals("selectInvert"))
		{
			selectInvert(operation);
		}
		else
			failure("custom operation with id "+operation.getOperationId()+" not implemented");
	}


	/**
	 * @param operation
	 * @throws XMLStreamException
	 */
	private void selectInvert(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		final AnywareList<?> al = getList(operation);
		if(al != null)
		{
			List<Anyware> list = buildList(al, operation);
			for(Anyware aw : list)
				aw.setSelected(!aw.isSelected());
		}
		writer.writeEndElement();
	}


	/**
	 * @param operation
	 * @throws XMLStreamException
	 */
	private void selectAll(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		final AnywareList<?> al = getList(operation);
		if(al != null)
		{
			List<Anyware> list = buildList(al, operation);
			for(Anyware aw : list)
				aw.setSelected(true);
		}
		writer.writeEndElement();
	}


	/**
	 * @param operation
	 * @throws XMLStreamException
	 */
	private void selectNone(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		final AnywareList<?> al = getList(operation);
		if(al != null)
		{
			List<Anyware> list = buildList(al, operation);
			for(Anyware aw : list)
				aw.setSelected(false);
		}
		writer.writeEndElement();
	}


	/**
	 * @param operation
	 * @throws XMLStreamException
	 */
	private void find(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		final AnywareList<?> al = getList(operation);
		if (al != null && operation.hasData("find"))
		{
			List<Anyware> list = buildList(al, operation);
			final String find = operation.getData("find");
			for (var i = 0; i < list.size(); i++)
			{
				if (list.get(i).getBaseName().equals(find))
				{
					writer.writeElement("found", Integer.toString(i));
					break;
				}
			}
		}
		writer.writeEndElement();
	}
}
