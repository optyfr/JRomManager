package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;

import jrm.misc.SettingsEnum;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchDat2DirSrcXMLResponse extends XMLResponse
{

	private static final String RECORD = "record";
	private static final String STATUS = "status";
	private static final String RESPONSE = "response";

	public BatchDat2DirSrcXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		final String[] srcdirs = StringUtils.split(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|');
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(srcdirs.length-1));
		writer.writeElement("totalRows", Integer.toString(srcdirs.length));
		writer.writeStartElement("data");
		for(final var srcdir : srcdirs)
			writer.writeElement(RECORD, new SimpleAttribute("name", srcdir));
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("name"))
		{
			final String[] srcdirs = StringUtils.split(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|');
			final List<String> lsrcdirs = Stream.of(srcdirs).collect(Collectors.toList());
			final List<String> names = operation.getDatas("name").stream().filter(n->!lsrcdirs.contains(n)).collect(Collectors.toList());
			if(!names.isEmpty())
			{
				lsrcdirs.addAll(names);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_srcdirs, lsrcdirs.stream().collect(Collectors.joining("|")));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writer.writeStartElement("data");
				for(final var name : names)
					writer.writeElement(RECORD, new SimpleAttribute("name", name));
				writer.writeEndElement();
				writer.writeEndElement();
			}
			else
				failure("Entry already exists");
		}
		else
			failure("name is missing in request");
	}
	
	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("name"))
		{
			final String[] srcdirs = StringUtils.split(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|');
			final List<String> lsrcdirs = Stream.of(srcdirs).collect(Collectors.toList());
			final List<String> names = operation.getDatas("name").stream().filter(lsrcdirs::contains).collect(Collectors.toList());
			if(!names.isEmpty())
			{
				lsrcdirs.removeAll(names);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_srcdirs, lsrcdirs.stream().collect(Collectors.joining("|")));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writer.writeStartElement("data");
				for(final var name : names)
					writer.writeElement(RECORD, new SimpleAttribute("name", name));
				writer.writeEndElement();
				writer.writeEndElement();
			}
			else
				failure("Entry does not exist");
		}
		else
			failure("name is missing in request");
	}
}
