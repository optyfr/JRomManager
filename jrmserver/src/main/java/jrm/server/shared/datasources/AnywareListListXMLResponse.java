package jrm.server.shared.datasources;

import java.io.IOException;
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

	private static final String STATUS = "status";

	public AnywareListListXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}
	
	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		writer.writeStartElement("response");
		writer.writeElement(STATUS, "0");
		final Set<String> lstatus = operation.hasData(STATUS)?Stream.of(operation.getData(STATUS).split(",")).collect(Collectors.toSet()):null;
		final var reset = Boolean.parseBoolean(operation.getData("reset"));
		final var mll = request.session.getCurrProfile().getMachineListList();
		if(mll!=null)
		{
			if(reset)
				mll.resetCache();
			final List<AnywareList<?>> fll = new ArrayList<>();
			for(var i = 0; i < mll.count(); i++)
			{
				AnywareList<?> l = mll.getObject(i);
				if (lstatus == null || lstatus.contains(l.getStatus().toString()))
					fll.add(l);
			}
			fetchList(operation, fll, (list, i) -> writeRecord(mll, list, i));
		}
		writer.writeEndElement();
	}

	/**
	 * @param mll
	 * @param list
	 * @param i
	 */
	private void writeRecord(final MachineListList mll, AnywareList<?> list, int i)
	{
		try
		{
			writer.writeElement("record", 
				new SimpleAttribute(STATUS, list.getStatus()),
				new SimpleAttribute("name", list instanceof MachineList?"*":list.getBaseName()),
				new SimpleAttribute("description", mll.getDescription(i)),
				new SimpleAttribute("have", mll.getHaveTot(i))
			);
		}
		catch (XMLStreamException e)
		{
			Log.err(e.getMessage(),e);
		}
	}
}
