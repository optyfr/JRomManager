package jrm.server.shared.datasources;

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

	public AnywareListXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	private AnywareList<? extends Anyware> get_list(Operation operation) throws Exception
	{
		String list = operation.getData("list");
		final AnywareList<?> al;
		if(list==null)
			al = null;
		else if(list.equals("*"))
			al = request.session.curr_profile.getMachineListList().get(0);
		else
			al = request.session.curr_profile.getMachineListList().softwarelist_list.getByName(list);
		return al;
	}
	
	private Predicate<Anyware> getFilter(Operation operation)
	{
		final Set<String> lstatus = operation.hasData("status")?Stream.of(operation.getData("status").split(",")).collect(Collectors.toSet()):null;
		final String lname = operation.hasData("name")?operation.getData("name").toLowerCase():null;
		final String ldesc = operation.hasData("description")?operation.getData("description").toLowerCase():null;
		final String lcloneof = operation.hasData("cloneof")?operation.getData("cloneof").toLowerCase():null;
		final String lromof = operation.hasData("romof")?operation.getData("romof").toLowerCase():null;
		final String lsampleof = operation.hasData("sampleof")?operation.getData("sampleof").toLowerCase():null;
		final Boolean lselected = operation.hasData("selected")?Boolean.valueOf(operation.getData("selected")):null;
		return (ware) -> {
			if(lstatus!=null)
				if(!lstatus.contains(ware.getStatus().toString()))
					return false;
			if(lselected!=null)
				if(ware.isSelected()!=lselected)
					return false;
			if(lname!=null)
				if(!ware.getBaseName().toLowerCase().contains(lname))
					return false;
			if(ldesc!=null)
				if(!ware.description.toString().toLowerCase().contains(ldesc))
					return false;
			if(lcloneof!=null)
				if(ware.getCloneof()==null || !ware.getCloneof().toString().toLowerCase().contains(lcloneof))
					return false;
			if(lromof!=null && ware instanceof Machine)
				if(((Machine)ware).getRomof()==null || !((Machine)ware).getRomof().toString().toLowerCase().contains(lromof))
					return false;
			if(lsampleof!=null && ware instanceof Machine)
				if(((Machine)ware).getSampleof()==null || !((Machine)ware).getSampleof().toString().toLowerCase().contains(lsampleof))
					return false;
			return true;
		};
	}
	
	private Comparator<Anyware> getSorter(Operation operation)
	{
		return (o1,o2)->{
			if(operation.getSort().size()>0)
			{
				for(Sorter s : operation.getSort())
				{
					switch(s.getName())
					{
						case "name":
						{
							int ret = (s.isDesc() ? o2 : o1).getBaseName().compareToIgnoreCase((s.isDesc() ? o1 : o2).getBaseName());
							if (ret != 0)
								return ret;
							break;
						}
						case "description":
						{
							int ret = (s.isDesc() ? o2 : o1).description.toString().compareToIgnoreCase((s.isDesc() ? o1 : o2).description.toString());
							if (ret != 0)
								return ret;
							break;
						}
					}
				}
				return 0;
			}
			else
				return o1.getBaseName().compareToIgnoreCase(o2.getBaseName());
		};
	}
	
	private Stream<? extends Anyware> build_stream(AnywareList<? extends Anyware> al, Operation operation)
	{
		return al.getFilteredList().stream().filter(getFilter(operation)).sorted(getSorter(operation));
	}
	
	private List<Anyware> build_list(AnywareList<?> al, Operation operation)
	{
		return al.getFilteredList().stream().filter(getFilter(operation)).sorted(getSorter(operation)).collect(Collectors.toList());
	}

	private void write_record(final AnywareList<?> al, final Anyware aw)
	{
		try
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("list", al instanceof MachineList?"*":al.getBaseName());
			writer.writeAttribute("status", aw.getStatus().toString());
			writer.writeAttribute("name", aw.getBaseName());
			writer.writeAttribute("description", aw.getDescription().toString());
			writer.writeAttribute("have",  String.format("%d/%d", aw.countHave(), aw.countAll()));
			if(aw.getCloneof()!=null)
			{
				writer.writeAttribute("cloneof",  aw.getCloneof());
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
					writer.writeAttribute("romof",  m.getRomof());
					writer.writeAttribute("romof_status",  al.getByName(m.getRomof()).getStatus().toString());
				}
				if(m.getSampleof()!=null)
				{
					writer.writeAttribute("sampleof",  m.getSampleof());
					writer.writeAttribute("sampleof_status", ml.samplesets.getByName(m.getSampleof()).getStatus().toString());
				}
			}
			writer.writeAttribute("selected", Boolean.toString(aw.isSelected()));
		}
		catch (XMLStreamException e)
		{
			Log.err(e.getMessage(),e);
		}
	}
	
	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		final boolean reset = Boolean.valueOf(operation.getData("reset"));
		final AnywareList<?> al = get_list(operation);
		if(al != null)
		{
			if(reset)
				al.resetCache();
			fetch_stream(operation, build_stream(al, operation), record -> write_record(al, record));
/*			List<Anyware> list = build_list(al, operation);
			fetch_array(operation, list.size(), (i, count) -> {
				write_record(al, list.get(i));
			});*/
		}
		writer.writeEndElement();
	}
	
	@Override
	protected void update(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		final AnywareList<?> al = get_list(operation);
		if(al != null)
		{
			String name = operation.getData("name");
			if(name != null)
			{
				Anyware aw = al.getByName(name);
				if(aw != null)
				{
					Boolean selected = Boolean.valueOf(operation.getData("selected"));
					if(selected != null)
						aw.setSelected(selected);
					write_record(al, aw);
				}
			}
		}
		writer.writeEndElement();
	}
	
	@Override
	protected void custom(Operation operation) throws Exception
	{
		if(operation.getOperationId().toString().equals("find"))
		{
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			final AnywareList<?> al = get_list(operation);
			if(al != null)
			{
				if(operation.hasData("find"))
				{
					List<Anyware> list = build_list(al, operation);
					final String find = operation.getData("find");
					for(int i = 0; i < list.size(); i++)
					{
						if(list.get(i).getBaseName().equals(find))
						{
							writer.writeElement("found", Integer.toString(i));
							break;
						}
					}
				}
			}
			writer.writeEndElement();
		}
		else if(operation.getOperationId().toString().equals("selectNone"))
		{
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			final AnywareList<?> al = get_list(operation);
			if(al != null)
			{
				List<Anyware> list = build_list(al, operation);
				for(Anyware aw : list)
					aw.setSelected(false);
			}
			writer.writeEndElement();
		}
		else if(operation.getOperationId().toString().equals("selectAll"))
		{
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			final AnywareList<?> al = get_list(operation);
			if(al != null)
			{
				List<Anyware> list = build_list(al, operation);
				for(Anyware aw : list)
					aw.setSelected(true);
			}
			writer.writeEndElement();
		}
		else if(operation.getOperationId().toString().equals("selectInvert"))
		{
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			final AnywareList<?> al = get_list(operation);
			if(al != null)
			{
				List<Anyware> list = build_list(al, operation);
				for(Anyware aw : list)
					aw.setSelected(!aw.isSelected());
			}
			writer.writeEndElement();
		}
		else
			failure("custom operation with id "+operation.getOperationId()+" not implemented");
	}
}
