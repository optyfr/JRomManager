package jrm.server.datasources;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jrm.profile.filter.CatVer;
import jrm.profile.filter.CatVer.Category;
import jrm.profile.filter.CatVer.SubCategory;
import jrm.server.datasources.XMLRequest.Operation;

public class CatVerXMLResponse extends XMLResponse
{

	public CatVerXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}

	private int countNode(CatVer catver)
	{
		int count = 0;
		if(catver!=null)
		{
			count++;
			for(int i = 0; i < catver.getChildCount(); i++)
			{
				count++;
				Category cat = (Category)catver.getChildAt(i);
				for(int j = 0; j < cat.getChildCount(); j++)
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
			writer.writeAttribute("isFolder", Boolean.toString(!catver.isLeaf()));
			writer.writeAttribute("isSelected", Boolean.toString(catver.isSelected()));
			writer.writeAttribute("isOpen", Boolean.TRUE.toString());
			writer.writeEndElement();
			for(int i = 0; i < catver.getChildCount(); i++)
			{
				Category cat = (Category)catver.getChildAt(i);
				writer.writeStartElement("record");
				writer.writeAttribute("ID", cat.getPropertyName());
				writer.writeAttribute("Name", cat.name);
				writer.writeAttribute("ParentID", catver.getPropertyName());
				writer.writeAttribute("isFolder", Boolean.toString(!cat.isLeaf()));
				writer.writeAttribute("isSelected", Boolean.toString(cat.isSelected()));
				byte isOpen = 0;
				for(int j = 0; j < cat.getChildCount(); j++)
				{
					SubCategory subcat = (SubCategory)cat.getChildAt(j);
					isOpen |= (subcat.isSelected()?0x1:0x0);
					isOpen |= (!subcat.isSelected()?0x2:0x0);
				}
				writer.writeAttribute("isOpen", Boolean.toString(isOpen==3));
				writer.writeEndElement();
				for(int j = 0; j < cat.getChildCount(); j++)
				{
					SubCategory subcat = (SubCategory)cat.getChildAt(j);
					writer.writeStartElement("record");
					writer.writeAttribute("ID", subcat.getPropertyName());
					writer.writeAttribute("Name", subcat.name);
					writer.writeAttribute("ParentID", cat.getPropertyName());
					writer.writeAttribute("Cnt", Integer.toString(subcat.size()));
					writer.writeAttribute("isFolder", Boolean.toString(!subcat.isLeaf()));
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
