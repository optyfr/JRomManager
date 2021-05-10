package jrm.server.shared.datasources;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jrm.profile.filter.CatVer;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.Category.SubCategory;
import jrm.server.shared.datasources.XMLRequest.Operation;

public class CatVerXMLResponse extends XMLResponse
{

	public CatVerXMLResponse(XMLRequest request) throws Exception
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
			writer.writeStartElement("record");
			writer.writeAttribute("ID", catver.getPropertyName());
			writer.writeAttribute("Name", request.session.msgs.getString("CatVer.AllCategories"));
			writer.writeAttribute("ParentID", "1");
			writer.writeAttribute("isFolder", Boolean.toString(!catver.getListCategories().isEmpty()));
			writer.writeAttribute("isSelected", Boolean.toString(catver.isSelected()));
			writer.writeAttribute("isOpen", Boolean.TRUE.toString());
			writer.writeEndElement();
			for(Category cat : catver)
			{
				writer.writeStartElement("record");
				writer.writeAttribute("ID", cat.getPropertyName());
				writer.writeAttribute("Name", cat.name);
				writer.writeAttribute("ParentID", catver.getPropertyName());
				writer.writeAttribute("isFolder", Boolean.toString(!cat.getListSubCategories().isEmpty()));
				writer.writeAttribute("isSelected", Boolean.toString(cat.isSelected()));
				byte isOpen = 0;
				for(SubCategory subcat : cat)
				{
					isOpen |= (subcat.isSelected()?0x1:0x0);
					isOpen |= (!subcat.isSelected()?0x2:0x0);
				}
				writer.writeAttribute("isOpen", Boolean.toString(isOpen==3));
				writer.writeEndElement();
				for(SubCategory subcat : cat)
				{
					writer.writeStartElement("record");
					writer.writeAttribute("ID", subcat.getPropertyName());
					writer.writeAttribute("Name", subcat.name);
					writer.writeAttribute("ParentID", cat.getPropertyName());
					writer.writeAttribute("Cnt", Integer.toString(subcat.size()));
					writer.writeAttribute("isFolder", Boolean.toString(false));
					writer.writeAttribute("isSelected", Boolean.toString(subcat.isSelected()));
					writer.writeEndElement();
				}
			}
		}
	}

	@Override
	protected void fetch(Operation operation) throws Exception
	{
		int nodecount = countNode(request.session.curr_profile.catver);
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(nodecount-1));
		writer.writeElement("totalRows", Integer.toString(nodecount));
		writer.writeStartElement("data");
		outputNode(writer, request.session.curr_profile.catver);
		writer.writeEndElement();
		writer.writeEndElement();
	}
}
