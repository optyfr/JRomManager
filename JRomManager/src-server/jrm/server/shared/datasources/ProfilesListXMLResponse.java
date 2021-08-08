package jrm.server.shared.datasources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;

import jrm.profile.manager.ProfileNFO;
import jrm.server.shared.datasources.XMLRequest.Operation;
import lombok.val;

public class ProfilesListXMLResponse extends XMLResponse
{

	private static final String STATUS = "status";
	private static final String RESPONSE = "response";
	private static final String PARENT = "Parent";
	private static final String XMLFILES = "xmlfiles";

	public ProfilesListXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve(XMLFILES).toAbsolutePath().normalize();
		if (operation.hasData(PARENT))
			dir = pathAbstractor.getAbsolutePath(operation.getData(PARENT));
		val rows = ProfileNFO.list(request.getSession(), dir.toFile());
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("parent", pathAbstractor.getRelativePath(dir).toString());
		writer.writeElement("endRow", Integer.toString(rows.size() - 1));
		writer.writeElement("totalRows", Integer.toString(rows.size()));
		writer.writeStartElement("data");
		for (var i = 0; i < rows.size(); i++)
			writeRecord(rows.get(i));
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * @param nfo
	 * @throws XMLStreamException
	 */
	private void writeRecord(final ProfileNFO nfo) throws XMLStreamException
	{
		writer.writeEmptyElement("record");
		writer.writeAttribute("Name", nfo.getName());
		writer.writeAttribute(PARENT, pathAbstractor.getRelativePath(nfo.getFile().getParentFile().toPath()).toString());
		writer.writeAttribute("File", nfo.getFile().getName());
		writer.writeAttribute("version", nfo.getVersion());
		writer.writeAttribute("haveSets", nfo.getHaveSets());
		writer.writeAttribute("haveRoms", nfo.getHaveRoms());
		writer.writeAttribute("haveDisks", nfo.getHaveDisks());
		writer.writeAttribute("created", nfo.getCreated());
		writer.writeAttribute("scanned", nfo.getScanned());
		writer.writeAttribute("fixed", nfo.getFixed());
	}

	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if (operation.hasData("Src"))
		{
			Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve(XMLFILES).toAbsolutePath().normalize();
			if (operation.hasData(PARENT) && !StringUtils.isEmpty(operation.getData(PARENT)))
				dir = pathAbstractor.getAbsolutePath(operation.getData(PARENT));
			val src = pathAbstractor.getAbsolutePath(operation.getData("Src"));
			if (Files.exists(src) && Files.isRegularFile(src))
			{
				try
				{
					Path dst = dir.resolve(operation.getData("File"));
					if (!src.equals(dst))
						Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
					final var nfo = ProfileNFO.load(request.getSession(), dst.toFile());
					writer.writeStartElement(RESPONSE);
					writer.writeElement(STATUS, "0");
					writer.writeStartElement("data");
					writeRecord(nfo);
					writer.writeEndElement();
					writer.writeEndElement();
				}
				catch (IOException ex)
				{
					failure(ex.getMessage());
				}
			}
			else
				failure("Source file does not exist");
		}
		else
			failure("Src is needed");
	}

	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve(XMLFILES).toAbsolutePath().normalize();
		if (operation.hasData(PARENT) && !StringUtils.isEmpty(operation.getData(PARENT)))
			dir = pathAbstractor.getAbsolutePath(operation.getData(PARENT));
		val dst = dir.resolve(operation.getData("File"));
		ProfileNFO nfo = ProfileNFO.load(request.getSession(), dst.toFile());
		if (request.session.getCurrProfile() == null || !request.getSession().getCurrProfile().getNfo().equals(nfo))
		{
			if (nfo.delete())
			{
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writer.writeStartElement("data");
				writer.writeEmptyElement("record");
				writer.writeAttribute(PARENT, pathAbstractor.getRelativePath(nfo.getFile().getParentFile().toPath()).toString());
				writer.writeAttribute("File", nfo.getFile().getName());
				writer.writeEndElement();
				writer.writeEndElement();
			}
			else
				failure("Failed to delete profile");
		}
		else
			failure("Can't delete current loaded profile");
	}

	@Override
	protected void custom(Operation operation) throws XMLStreamException, IOException
	{
		if("DropCache".equals(operation.getOperationId().toString()))
		{
			Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve(XMLFILES).toAbsolutePath().normalize();
			if (operation.hasData(PARENT) && !StringUtils.isEmpty(operation.getData(PARENT)))
				dir = pathAbstractor.getAbsolutePath(operation.getData(PARENT));
			val dst = dir.resolve(operation.getData("File"));
			if (Files.isRegularFile(dst))
			{
				val cache = dir.resolve(operation.getData("File") + ".cache");
				if (Files.exists(cache) && !cache.toFile().delete())
					failure("Can't delete " + cache);
				else
					success();
			}
			else
				failure("Can't find " + dst);
		}
		else
			super.custom(operation);
	}
}
