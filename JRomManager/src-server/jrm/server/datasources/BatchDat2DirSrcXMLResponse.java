package jrm.server.datasources;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import jrm.misc.SettingsEnum;
import jrm.server.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchDat2DirSrcXMLResponse extends XMLResponse
{

	public BatchDat2DirSrcXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		String[] srcdirs = StringUtils.split(request.session.getUser().settings.getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|');
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(srcdirs.length-1));
		writer.writeElement("totalRows", Integer.toString(srcdirs.length));
		writer.writeStartElement("data");
		for(String srcdir : srcdirs)
		{
			writer.writeElement("record", 
				new SimpleAttribute("name", srcdir)
			);
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws Exception
	{
		if(operation.hasData("name"))
		{
			String[] srcdirs = StringUtils.split(request.session.getUser().settings.getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|');
			List<String> lsrcdirs = Stream.of(srcdirs).collect(Collectors.toList());
			final List<String> names = operation.getDatas("name").stream().filter(n->!lsrcdirs.contains(n)).collect(Collectors.toList());
			if(names.size()>0)
			{
				lsrcdirs.addAll(names);
				request.session.getUser().settings.setProperty(SettingsEnum.dat2dir_srcdirs, lsrcdirs.stream().collect(Collectors.joining("|")));
				request.session.getUser().settings.saveSettings();
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				for(final String name : names)
					writer.writeElement("record", new SimpleAttribute("name", name));
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
	protected void remove(Operation operation) throws Exception
	{
		if(operation.hasData("name"))
		{
			final String[] srcdirs = StringUtils.split(request.session.getUser().settings.getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|');
			final List<String> lsrcdirs = Stream.of(srcdirs).collect(Collectors.toList());
			final List<String> names = operation.getDatas("name").stream().filter(lsrcdirs::contains).collect(Collectors.toList());
			if(names.size()>0)
			{
				lsrcdirs.removeAll(names);
				request.session.getUser().settings.setProperty(SettingsEnum.dat2dir_srcdirs, lsrcdirs.stream().collect(Collectors.joining("|")));
				request.session.getUser().settings.saveSettings();
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				for(final String name : names)
					writer.writeElement("record", new SimpleAttribute("name", name));
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
