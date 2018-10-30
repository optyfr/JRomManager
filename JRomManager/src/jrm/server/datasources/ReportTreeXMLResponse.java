package jrm.server.datasources;

import jrm.profile.report.Note;
import jrm.profile.report.Subject;
import jrm.server.datasources.XMLRequest.Operation;

public class ReportTreeXMLResponse extends XMLResponse
{

	public ReportTreeXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws Exception
	{
		writer.writeStartElement("response");
		writer.writeElement("status", "0");

		int parentID = Integer.valueOf(operation.data.get("ParentID"));
		if(parentID==0)
		{
			int start, end;
			int nodecount =request.session.report.size();
			writer.writeElement("startRow", Integer.toString(start=Math.min(nodecount-1,operation.startRow)));
			writer.writeElement("endRow", Integer.toString(end=Math.min(nodecount-1,operation.endRow)));
			writer.writeElement("totalRows", Integer.toString(nodecount));
	
			writer.writeStartElement("data");
			for(int i = start; i <= end; i++)
			{
				Subject s = request.session.report.get(i);
				writer.writeStartElement("record");
				writer.writeAttribute("ID", Integer.toString(s.getId()));
				writer.writeAttribute("ParentID", Integer.toString(s.getParent().getId()));
				writer.writeAttribute("title", s.toString());
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		else
		{
			Subject subject = request.session.report.findSubject(parentID);
			if(subject!=null)
			{
				int nodecount = subject.size();
				writer.writeElement("startRow", "0");
				writer.writeElement("endRow", Integer.toString(nodecount-1));
				writer.writeElement("totalRows", Integer.toString(nodecount));
				writer.writeStartElement("data");
				for(Note n : subject)
				{
					writer.writeStartElement("record");
					writer.writeAttribute("isFolder", "false");
					writer.writeAttribute("ID", Integer.toString(n.getId()));
					writer.writeAttribute("ParentID", Integer.toString(n.getParent().getId()));
					writer.writeAttribute("title", n.toString());
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}
		}
		writer.writeEndElement();
	}
}
