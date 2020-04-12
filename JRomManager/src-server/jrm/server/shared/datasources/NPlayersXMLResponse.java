package jrm.server.shared.datasources;

import jrm.profile.filter.NPlayer;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;
import lombok.val;

public class NPlayersXMLResponse extends XMLResponse
{

	public NPlayersXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		val session = request.session;
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString((session.curr_profile.nplayers==null?0:request.getSession().curr_profile.nplayers.getList_nplayers().size())-1));
		writer.writeElement("totalRows", Integer.toString(session.curr_profile.nplayers==null?0:request.getSession().curr_profile.nplayers.getList_nplayers().size()));
		writer.writeStartElement("data");
		if(session.curr_profile.nplayers!=null)
		{
			for(NPlayer nplayer : session.curr_profile.nplayers)
			{
				writer.writeElement("record", 
					new SimpleAttribute("ID", nplayer.getPropertyName()),
					new SimpleAttribute("Name", nplayer.name),
					new SimpleAttribute("Cnt", nplayer.size()),
					new SimpleAttribute("isSelected", nplayer.isSelected(session.curr_profile))
				);
			}
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
