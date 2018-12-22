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
			mll.reset();
		fetch_array(operation,mll==null?0:mll.getRowCount(), (i,count)->{
			AnywareList<?> list = (AnywareList<?>)mll.getValueAt(i, 0);
			try
			{
				writer.writeElement("record", 
					new SimpleAttribute("status", list.getStatus()),
					new SimpleAttribute("name", list instanceof MachineList?"*":list.getBaseName()),
					new SimpleAttribute("description", mll.getValueAt(i, 1)),
					new SimpleAttribute("have", mll.getValueAt(i, 2))
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
