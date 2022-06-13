package jrm.server.shared.datasources;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import jrm.profile.filter.NPlayer;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;
import lombok.val;

public class NPlayersXMLResponse extends XMLResponse
{

	public NPlayersXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		val session = request.session;
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString((session.getCurrProfile().getNplayers()==null?0:request.getSession().getCurrProfile().getNplayers().getListNPlayers().size())-1));
		writer.writeElement("totalRows", Integer.toString(session.getCurrProfile().getNplayers()==null?0:request.getSession().getCurrProfile().getNplayers().getListNPlayers().size()));
		writer.writeStartElement("data");
		if(session.getCurrProfile().getNplayers()!=null)
		{
			for(NPlayer nplayer : session.getCurrProfile().getNplayers())
			{
				writer.writeElement("record", 
					new SimpleAttribute("ID", nplayer.getPropertyName()),
					new SimpleAttribute("Name", nplayer.name),
					new SimpleAttribute("Cnt", nplayer.size()),
					new SimpleAttribute("isSelected", nplayer.isSelected(session.getCurrProfile()))
				);
			}
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
