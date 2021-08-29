package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.misc.Log;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.Disk;
import jrm.profile.data.EntityBase;
import jrm.profile.data.MachineList;
import jrm.profile.data.Rom;
import jrm.profile.data.Sample;
import jrm.server.shared.datasources.XMLRequest.Operation;

public class AnywareXMLResponse extends XMLResponse
{

	private static final String STATUS = "status";

	public AnywareXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	private AnywareList<?> getList(Operation operation)
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
	

	private Anyware getWare(AnywareList<?> al, Operation operation)
	{
		String ware = operation.getData("ware");
		final Anyware aw;
		if(ware==null || al==null)
			aw = null;
		else
			aw = al.getByName(ware);
		return aw;
	}
	
	private void writeRecord(final AnywareList<?> al, final Anyware aw, final EntityBase e)
	{
		try
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("list", al instanceof MachineList?"*":al.getBaseName());
			writer.writeAttribute("ware", aw.getBaseName());
			writer.writeAttribute("name", e.getBaseName());
			writer.writeAttribute(STATUS, e.getStatus().toString());
			if (e instanceof Rom)
				writeRomInfos((Rom)e);
			else if (e instanceof Disk)
				writeDiskInfos((Disk)e);
			else if (e instanceof Sample)
				writer.writeAttribute("type", "SAMPLE");
		}
		catch (XMLStreamException ex)
		{
			Log.err(ex.getMessage(),ex);
		}
	}


	/**
	 * @param d
	 * @throws XMLStreamException
	 */
	protected void writeDiskInfos(Disk d) throws XMLStreamException
	{
		writer.writeAttribute("type", "DISK");
		if(d.getSize()>0)
			writer.writeAttribute("size", Long.toString(d.getSize()));
		if(d.getCrc()!=null)
			writer.writeAttribute("crc", d.getCrc());
		if(d.getMd5()!=null)
			writer.writeAttribute("md5", d.getMd5());
		if(d.getSha1()!=null)
			writer.writeAttribute("sha1", d.getSha1());
		if(d.getMerge()!=null)
			writer.writeAttribute("merge", d.getMerge());
		if(d.getDumpStatus()!=null)
			writer.writeAttribute("dumpstatus", d.getDumpStatus().toString());
	}


	/**
	 * @param r
	 * @throws XMLStreamException
	 */
	protected void writeRomInfos(Rom r) throws XMLStreamException
	{
		writer.writeAttribute("type", "ROM");
		if(r.getSize()>0)
			writer.writeAttribute("size", Long.toString(r.getSize()));
		if(r.getCrc()!=null)
			writer.writeAttribute("crc", r.getCrc());
		if(r.getMd5()!=null)
			writer.writeAttribute("md5", r.getMd5());
		if(r.getSha1()!=null)
			writer.writeAttribute("sha1", r.getSha1());
		if(r.getMerge()!=null)
			writer.writeAttribute("merge", r.getMerge());
		if(r.getDumpStatus()!=null)
			writer.writeAttribute("dumpstatus", r.getDumpStatus().toString());
	}
	
	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement(STATUS, "0");
		final Set<String> lstatus = operation.hasData(STATUS)?Stream.of(operation.getData(STATUS).split(",")).collect(Collectors.toSet()):null;
		final var reset = Boolean.parseBoolean(operation.getData("reset"));
		final AnywareList<?> al = getList(operation);
		final Anyware aw = getWare(al,operation);
		if(aw!=null)
		{
			if(reset)
				aw.resetCache();
			final List<EntityBase> faw = new ArrayList<>();
			for(var i = 0; i < aw.count(); i++)
			{
				var a = aw.getObject(i);
				if (lstatus != null && !lstatus.contains(a.getStatus().toString()))
					continue;
				faw.add(a);
			}
			fetchList(operation, faw, (a, i) -> writeRecord(al, aw, a));
		}
		writer.writeEndElement();
	}

}
