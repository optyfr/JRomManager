package jrm.server.shared.datasources;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import jrm.misc.Log;
import jrm.profile.data.AnywareList;
import jrm.profile.data.MachineList;
import jrm.profile.data.MachineListList;
import jrm.server.shared.datasources.XMLRequest.Operation;
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
		final Set<String> lstatus = operation.hasData("status")?Stream.of(operation.getData("status").split(",")).collect(Collectors.toSet()):null;
		final boolean reset = Boolean.valueOf(operation.getData("reset"));
		final MachineListList mll = request.session.curr_profile.machinelist_list;
		if(mll!=null)
		{
			if(reset)
				mll.resetCache();
			final List<AnywareList<?>> fll = new ArrayList<>();
			for(int i = 0; i < mll.count(); i++)
			{
				AnywareList<?> l = mll.getObject(i);
				if(lstatus!=null)
					if(!lstatus.contains(l.getStatus().toString()))
						continue;
				fll.add(l);
			}
			fetch_list(operation, fll, (list, i) -> {
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
		}
		writer.writeEndElement();
	}
}
