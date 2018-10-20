package jrm.server.datasources;

import jrm.profile.data.AnywareList;
import jrm.profile.data.MachineList;
import jrm.server.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class MachineListListXMLResponse extends XMLResponse
{

	public MachineListListXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString((request.session.curr_profile.machinelist_list==null?0:request.session.curr_profile.machinelist_list.getRowCount())-1));
		writer.writeElement("totalRows", Integer.toString(request.session.curr_profile.machinelist_list==null?0:request.session.curr_profile.machinelist_list.getRowCount()));
		writer.writeStartElement("data");
		if(request.session.curr_profile.machinelist_list!=null)
		{
			for(int i = 0; i < request.session.curr_profile.machinelist_list.getRowCount(); i++)
			{
				AnywareList<?> list = (AnywareList<?>)request.session.curr_profile.machinelist_list.getValueAt(i, 0);
				writer.writeElement("record", 
					new SimpleAttribute("status", list.getStatus()),
					new SimpleAttribute("name", list instanceof MachineList?request.session.msgs.getString("MachineListListRenderer.*"):list.getBaseName()),
					new SimpleAttribute("description", request.session.curr_profile.machinelist_list.getValueAt(i, 1)),
					new SimpleAttribute("have", request.session.curr_profile.machinelist_list.getValueAt(i, 2))
				);
			}
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
