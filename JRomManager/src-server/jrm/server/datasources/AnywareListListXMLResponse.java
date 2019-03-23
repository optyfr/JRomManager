package jrm.server.datasources;

import javax.xml.stream.XMLStreamException;

import jrm.misc.Log;
import jrm.profile.data.AnywareList;
import jrm.profile.data.MachineList;
import jrm.profile.data.MachineListList;
import jrm.server.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class AnywareListListXMLResponse extends XMLResponse
{

	public AnywareListListXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}
	
	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		final boolean reset = Boolean.valueOf(operation.getData("reset"));
		final MachineListList mll = request.session.curr_profile.machinelist_list;
		if(reset)
			mll.resetCache();
		fetch_array(operation,mll==null?0:mll.count(), (i,count)->{
			AnywareList<?> list = mll.getObject(i);
			try
			{
				writer.writeElement("record", 
					new SimpleAttribute("status", list.getStatus()),
					new SimpleAttribute("name", list instanceof MachineList?"*":list.getBaseName()),
					new SimpleAttribute("description", mll.getDescription(i)),
					new SimpleAttribute("have", mll.getHaveTot(i))
				);
			}
			catch (XMLStreamException e)
			{
				Log.err(e.getMessage(),e);
			}
		});
		writer.writeEndElement();
	}
}
