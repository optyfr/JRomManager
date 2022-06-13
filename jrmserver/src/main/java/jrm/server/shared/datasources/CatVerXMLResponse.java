package jrm.server.shared.datasources;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jrm.profile.filter.CatVer;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.Category.SubCategory;
import jrm.server.shared.datasources.XMLRequest.Operation;

public class CatVerXMLResponse extends XMLResponse
{
	private static final String IS_SELECTED = "isSelected";
	private static final String IS_FOLDER = "isFolder";
	private static final String PARENT_ID = "ParentID";
	private static final String RECORD = "record";

	public CatVerXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	private int countNode(CatVer catver)
	{
		var count = 0;
		if(catver!=null)
		{
			count++;
			for(var i = 0; i < catver.getListCategories().size(); i++)
			{
				count++;
				final var cat = catver.getListCategories().get(i);
				for(var j = 0; j < cat.getListSubCategories().size(); j++)
					count++;
			}
		}
		return count;
	}

	private void outputNode(XMLStreamWriter writer, CatVer catver) throws XMLStreamException
	{
		if(catver!=null)
		{
			writeRootNode(writer, catver);
			for(Category cat : catver)
			{
				writeCatNode(writer, catver, cat);
				for(SubCategory subcat : cat)
				{
					writeSubCatNode(writer, cat, subcat);
				}
			}
		}
	}

	/**
	 * @param writer
	 * @param cat
	 * @param subcat
	 * @throws XMLStreamException
	 */
	private void writeSubCatNode(XMLStreamWriter writer, Category cat, SubCategory subcat) throws XMLStreamException
	{
		writer.writeStartElement(RECORD);
		writer.writeAttribute("ID", subcat.getPropertyName());
		writer.writeAttribute("Name", subcat.name);
		writer.writeAttribute(PARENT_ID, cat.getPropertyName());
		writer.writeAttribute("Cnt", Integer.toString(subcat.size()));
		writer.writeAttribute(IS_FOLDER, Boolean.toString(false));
		writer.writeAttribute(IS_SELECTED, Boolean.toString(subcat.isSelected()));
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param catver
	 * @param cat
	 * @throws XMLStreamException
	 */
	private void writeCatNode(XMLStreamWriter writer, CatVer catver, Category cat) throws XMLStreamException
	{
		writer.writeStartElement(RECORD);
		writer.writeAttribute("ID", cat.getPropertyName());
		writer.writeAttribute("Name", cat.name);
		writer.writeAttribute(PARENT_ID, catver.getPropertyName());
		writer.writeAttribute(IS_FOLDER, Boolean.toString(!cat.getListSubCategories().isEmpty()));
		writer.writeAttribute(IS_SELECTED, Boolean.toString(cat.isSelected()));
		byte isOpen = 0;
		for(SubCategory subcat : cat)
		{
			isOpen |= (subcat.isSelected()?0x1:0x0);
			isOpen |= (!subcat.isSelected()?0x2:0x0);
		}
		writer.writeAttribute("isOpen", Boolean.toString(isOpen==3));
		writer.writeEndElement();
	}

	/**
	 * @param writer
	 * @param catver
	 * @throws XMLStreamException
	 */
	private void writeRootNode(XMLStreamWriter writer, CatVer catver) throws XMLStreamException
	{
		writer.writeStartElement(RECORD);
		writer.writeAttribute("ID", catver.getPropertyName());
		writer.writeAttribute("Name", request.session.getMsgs().getString("CatVer.AllCategories"));
		writer.writeAttribute(PARENT_ID, "1");
		writer.writeAttribute(IS_FOLDER, Boolean.toString(!catver.getListCategories().isEmpty()));
		writer.writeAttribute(IS_SELECTED, Boolean.toString(catver.isSelected()));
		writer.writeAttribute("isOpen", Boolean.TRUE.toString());
		writer.writeEndElement();
	}

	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		int nodecount = countNode(request.session.getCurrProfile().getCatver());
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(nodecount-1));
		writer.writeElement("totalRows", Integer.toString(nodecount));
		writer.writeStartElement("data");
		outputNode(writer, request.session.getCurrProfile().getCatver());
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
