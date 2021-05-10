package jrm.server.shared.datasources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.StringUtils;

import jrm.profile.manager.ProfileNFO;
import jrm.server.shared.datasources.XMLRequest.Operation;
import lombok.val;

public class ProfilesListXMLResponse extends XMLResponse
{

	public ProfilesListXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws Exception
	{
		Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
		if (operation.hasData("Parent"))
			dir = pathAbstractor.getAbsolutePath(operation.getData("Parent"));
		val rows = ProfileNFO.list(request.getSession(), dir.toFile());
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("parent", pathAbstractor.getRelativePath(dir).toString());
		writer.writeElement("endRow", Integer.toString(rows.size() - 1));
		writer.writeElement("totalRows", Integer.toString(rows.size()));
		writer.writeStartElement("data");
		for (var i = 0; i < rows.size(); i++)
		{
			writer.writeEmptyElement("record");
			writer.writeAttribute("Name", rows.get(i).getName());
			writer.writeAttribute("Parent", pathAbstractor.getRelativePath(rows.get(i).file.getParentFile().toPath()).toString());
			writer.writeAttribute("File", rows.get(i).file.getName());
			writer.writeAttribute("version", rows.get(i).getVersion());
			writer.writeAttribute("haveSets", rows.get(i).getHaveSets());
			writer.writeAttribute("haveRoms", rows.get(i).getHaveRoms());
			writer.writeAttribute("haveDisks", rows.get(i).getHaveDisks());
			writer.writeAttribute("created", rows.get(i).getCreated());
			writer.writeAttribute("scanned", rows.get(i).getScanned());
			writer.writeAttribute("fixed", rows.get(i).getFixed());
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}

	@Override
	protected void add(Operation operation) throws Exception
	{
		if (operation.hasData("Src"))
		{
			Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
			if (operation.hasData("Parent") && !StringUtils.isEmpty(operation.getData("Parent")))
				dir = pathAbstractor.getAbsolutePath(operation.getData("Parent"));
			val src = pathAbstractor.getAbsolutePath(operation.getData("Src"));
			if (Files.exists(src) && Files.isRegularFile(src))
			{
				try
				{
					Path dst = dir.resolve(operation.getData("File"));
					if (!src.equals(dst))
						Files.copy(src, dst, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
					final var nfo = ProfileNFO.load(request.getSession(), dst.toFile());
					writer.writeStartElement("response");
					writer.writeElement("status", "0");
					writer.writeStartElement("data");
					writer.writeEmptyElement("record");
					writer.writeAttribute("Name", nfo.getName());
					writer.writeAttribute("Parent", pathAbstractor.getRelativePath(nfo.file.getParentFile().toPath()).toString());
					writer.writeAttribute("File", nfo.file.getName());
					writer.writeAttribute("version", nfo.getVersion());
					writer.writeAttribute("haveSets", nfo.getHaveSets());
					writer.writeAttribute("haveRoms", nfo.getHaveRoms());
					writer.writeAttribute("haveDisks", nfo.getHaveDisks());
					writer.writeAttribute("created", nfo.getCreated());
					writer.writeAttribute("scanned", nfo.getScanned());
					writer.writeAttribute("fixed", nfo.getFixed());
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
	protected void remove(Operation operation) throws Exception
	{
		Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
		if (operation.hasData("Parent") && !StringUtils.isEmpty(operation.getData("Parent")))
			dir = pathAbstractor.getAbsolutePath(operation.getData("Parent"));
		val dst = dir.resolve(operation.getData("File"));
		ProfileNFO nfo = ProfileNFO.load(request.getSession(), dst.toFile());
		if (request.session.curr_profile == null || !request.getSession().curr_profile.nfo.equals(nfo))
		{
			if (nfo.delete())
			{
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				writer.writeEmptyElement("record");
				writer.writeAttribute("Parent", pathAbstractor.getRelativePath(nfo.file.getParentFile().toPath()).toString());
				writer.writeAttribute("File", nfo.file.getName());
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
	protected void custom(Operation operation) throws Exception
	{
		switch (operation.getOperationId().toString())
		{
			case "DropCache":
			{
				Path dir = request.getSession().getUser().getSettings().getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize();
				if (operation.hasData("Parent") && !StringUtils.isEmpty(operation.getData("Parent")))
					dir = pathAbstractor.getAbsolutePath(operation.getData("Parent"));
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
				break;
			}
			default:
				super.custom(operation);
				break;
		}
	}
}
