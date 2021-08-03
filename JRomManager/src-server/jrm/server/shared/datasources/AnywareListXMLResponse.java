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
		final Set<String> lstatus = operation.hasData(STATUS)?Stream.of(operation.getData(STATUS).split(",")).collect(Collectors.toSet()):null;
		final String lname = operation.hasData(NAME)?operation.getData(NAME).toLowerCase():null;
		final String ldesc = operation.hasData(DESCRIPTION)?operation.getData(DESCRIPTION).toLowerCase():null;
		final String lcloneof = operation.hasData(CLONEOF)?operation.getData(CLONEOF).toLowerCase():null;
		final String lromof = operation.hasData(ROMOF)?operation.getData(ROMOF).toLowerCase():null;
		final String lsampleof = operation.hasData(SAMPLEOF)?operation.getData(SAMPLEOF).toLowerCase():null;
		final Boolean lselected = operation.hasData(SELECTED)?Boolean.valueOf(operation.getData(SELECTED)):null;
		return ware -> {
			if (lstatus != null && !lstatus.contains(ware.getStatus().toString()))
				return false;
			if (lselected != null && ware.isSelected() != lselected)
				return false;
			if (lname != null && !ware.getBaseName().toLowerCase().contains(lname))
				return false;
			if (ldesc != null && !ware.description.toString().toLowerCase().contains(ldesc))
				return false;
			if (lcloneof != null && (ware.getCloneof() == null || !ware.getCloneof().toLowerCase().contains(lcloneof)))
				return false;
			if (lromof != null && ware instanceof Machine && (((Machine) ware).getRomof() == null || !((Machine) ware).getRomof().toLowerCase().contains(lromof)))
				return false;
			if (lsampleof != null && ware instanceof Machine && (((Machine) ware).getSampleof() == null || !((Machine) ware).getSampleof().toLowerCase().contains(lsampleof)))
				return false;
			return true;
		};
	}
	
	private Comparator<Anyware> getSorter(Operation operation)
	{
		return (o1,o2)->{
			if(!operation.getSort().isEmpty())
			{
				for(Sorter s : operation.getSort())
				{
					switch(s.getName())
					{
						case NAME:
						{
							int ret = (s.isDesc() ? o2 : o1).getBaseName().compareToIgnoreCase((s.isDesc() ? o1 : o2).getBaseName());
							if (ret != 0)
								return ret;
							break;
						}
						case DESCRIPTION:
						{
							int ret = (s.isDesc() ? o2 : o1).description.toString().compareToIgnoreCase((s.isDesc() ? o1 : o2).description.toString());
							if (ret != 0)
								return ret;
							break;
						}
						default:
							break;
					}
				}
				return 0;
			}
			else
				return o1.getBaseName().compareToIgnoreCase(o2.getBaseName());
		};
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
			if(aw instanceof Machine)
			{
				Machine m = (Machine)aw;
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
		else if(operation.getOperationId().toString().equals("selectNone"))
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
		else if(operation.getOperationId().toString().equals("selectAll"))
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
		else if(operation.getOperationId().toString().equals("selectInvert"))
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
		else
			failure("custom operation with id "+operation.getOperationId()+" not implemented");
	}
}
