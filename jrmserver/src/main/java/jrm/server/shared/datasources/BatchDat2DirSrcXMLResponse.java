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
		final String[] srcdirs = getSrcDirs();
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		fetchArray(operation, srcdirs.length, (i, count) -> writeRecord(srcdirs[i]));
		writer.writeEndElement();
	}


	/**
	 * @return
	 */
	private String[] getSrcDirs()
	{
		return StringUtils.split(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs),'|');
	}
	
	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("name"))
		{
			final List<String> lsrcdirs = Stream.of(getSrcDirs()).collect(Collectors.toList());
			final List<String> names = operation.getDatas("name").stream().filter(n->!lsrcdirs.contains(n)).collect(Collectors.toList());
			if(!names.isEmpty())
			{
				lsrcdirs.addAll(names);
				save(lsrcdirs);
				writeResponse(operation, names);
			}
			else
				failure("Entry already exists");
		}
		else
			failure("name is missing in request");
	}


	/**
	 * @param operation
	 * @param names
	 * @throws XMLStreamException
	 */
	private void writeResponse(Operation operation, final List<String> names) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		fetchList(operation, names, (name, idx) -> writeRecord(name));
		writer.writeEndElement();
	}


	/**
	 * @param lsrcdirs
	 */
	private void save(final List<String> lsrcdirs)
	{
		request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_srcdirs, lsrcdirs.stream().collect(Collectors.joining("|")));
		request.getSession().getUser().getSettings().saveSettings();
	}


	/**
	 * @param name
	 * @throws XMLStreamException
	 */
	private void writeRecord(final String name) throws XMLStreamException
	{
		writer.writeElement(RECORD, new SimpleAttribute("name", name));
	}
	
	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("name"))
		{
			final List<String> lsrcdirs = Stream.of(getSrcDirs()).collect(Collectors.toList());
			final List<String> names = operation.getDatas("name").stream().filter(lsrcdirs::contains).collect(Collectors.toList());
			if(!names.isEmpty())
			{
				lsrcdirs.removeAll(names);
				save(lsrcdirs);
				writeResponse(operation, names);
			}
			else
				failure("Entry does not exist");
		}
		else
			failure("name is missing in request");
	}
}
