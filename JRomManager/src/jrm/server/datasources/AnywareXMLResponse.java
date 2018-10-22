package jrm.server.datasources;

import javax.xml.stream.XMLStreamException;

import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.Entity;
import jrm.profile.data.MachineList;
import jrm.server.datasources.XMLRequest.Operation;

public class AnywareXMLResponse extends XMLResponse
{

	public AnywareXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	private AnywareList<?> get_list(Operation operation) throws Exception
	{
		String list = operation.data.get("list");
		final AnywareList<?> al;
		if(list==null)
			al = null;
		else if(list.equals("*"))
			al = request.session.curr_profile.machinelist_list.get(0);
		else
			al = request.session.curr_profile.machinelist_list.softwarelist_list.getByName(list);
		return al;
	}
	

	private Anyware get_ware(AnywareList<?> al, Operation operation) throws Exception
	{
		String ware = operation.data.get("ware");
		final Anyware aw;
		if(ware==null || al==null)
			aw = null;
		else
			aw = al.getByName(ware);
		return aw;
	}
	
	private void write_record(final AnywareList<?> al, final Anyware aw, final Entity e)
	{
		try
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("list", al instanceof MachineList?"*":al.getBaseName());
			writer.writeAttribute("ware", aw.getBaseName());
			writer.writeAttribute("name", e.getBaseName());
		}
		catch (XMLStreamException ex)
		{
			ex.printStackTrace();
		}
	}
	
	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		final boolean reset = Boolean.valueOf(operation.data.get("reset"));
		final AnywareList<?> al = get_list(operation);
		final Anyware aw = get_ware(al,operation);
		if(aw!=null)
		{
			if(reset)
				aw.reset();
			fetch_array(operation, aw.getRowCount(), (i, count) -> {
				write_record(al, aw, (Entity)aw.getValueAt(i, 0));
			});
		}
		writer.writeEndElement();
	}

}
