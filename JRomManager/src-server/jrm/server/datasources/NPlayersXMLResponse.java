package jrm.server.datasources;

import jrm.profile.filter.NPlayer;
import jrm.server.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class NPlayersXMLResponse extends XMLResponse
{

	public NPlayersXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString((request.session.curr_profile.nplayers==null?0:request.session.curr_profile.nplayers.getList_nplayers().size())-1));
		writer.writeElement("totalRows", Integer.toString(request.session.curr_profile.nplayers==null?0:request.session.curr_profile.nplayers.getList_nplayers().size()));
		writer.writeStartElement("data");
		if(request.session.curr_profile.nplayers!=null)
		{
			for(NPlayer nplayer : request.session.curr_profile.nplayers)
			{
				writer.writeElement("record", 
					new SimpleAttribute("ID", nplayer.getPropertyName()),
					new SimpleAttribute("Name", nplayer.name),
					new SimpleAttribute("Cnt", nplayer.size()),
					new SimpleAttribute("isSelected", nplayer.isSelected(request.session.curr_profile))
				);
			}
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
