package jrm.server.datasources;

import javax.xml.stream.XMLStreamException;

import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.Disk;
import jrm.profile.data.EntityBase;
import jrm.profile.data.MachineList;
import jrm.profile.data.Rom;
import jrm.profile.data.Sample;
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
	
	private void write_record(final AnywareList<?> al, final Anyware aw, final EntityBase e)
	{
		try
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("list", al instanceof MachineList?"*":al.getBaseName());
			writer.writeAttribute("ware", aw.getBaseName());
			writer.writeAttribute("name", e.getBaseName());
			writer.writeAttribute("status", e.getStatus().toString());
			if (e instanceof Rom)
			{
				Rom r = (Rom)e;
				writer.writeAttribute("type", "ROM");
				if(r.getSize()>0)
					writer.writeAttribute("size", Long.toString(r.getSize()));
				if(r.getCRC()!=null)
					writer.writeAttribute("crc", r.getCRC());
				if(r.getMD5()!=null)
					writer.writeAttribute("md5", r.getMD5());
				if(r.getSHA1()!=null)
					writer.writeAttribute("sha1", r.getSHA1());
				if(r.merge!=null)
					writer.writeAttribute("merge", r.merge);
				if(r.status!=null)
					writer.writeAttribute("dumpstatus", r.status.toString());
			}
			else if (e instanceof Disk)
			{
				Disk d = (Disk)e;
				writer.writeAttribute("type", "DISK");
				if(d.getSize()>0)
					writer.writeAttribute("size", Long.toString(d.getSize()));
				if(d.getCRC()!=null)
					writer.writeAttribute("crc", d.getCRC());
				if(d.getMD5()!=null)
					writer.writeAttribute("md5", d.getMD5());
				if(d.getSHA1()!=null)
					writer.writeAttribute("sha1", d.getSHA1());
				if(d.merge!=null)
					writer.writeAttribute("merge", d.merge);
				if(d.status!=null)
					writer.writeAttribute("dumpstatus", d.status.toString());
			}
			else if (e instanceof Sample)
			{
				writer.writeAttribute("type", "SAMPLE");
			}
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
				write_record(al, aw, (EntityBase)aw.getValueAt(i, 0));
			});
		}
		writer.writeEndElement();
	}

}
