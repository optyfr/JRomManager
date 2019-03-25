package jrm.server.datasources;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jrm.batch.TrntChkReport;
import jrm.batch.TrntChkReport.Child;
import jrm.batch.TrntChkReport.Status;
import jrm.server.datasources.XMLRequest.Operation;

public class BatchTrntChkReportTreeXMLResponse extends XMLResponse
{
	public BatchTrntChkReportTreeXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws Exception
	{
		TrntChkReport report = null;
		if (operation.hasData("src"))
		{
			final File srcfile = new File(operation.getData("src"));
			final File reportfile = TrntChkReport.getReportFile(request.session, srcfile);
			if (request.session.tmp_tc_report == null || !(request.session.tmp_tc_report.getReportFile(request.session).equals(reportfile) && request.session.tmp_tc_report.getFileModified() == reportfile.lastModified()))
				request.session.tmp_tc_report = TrntChkReport.load(request.session, srcfile);
			report = request.session.tmp_tc_report;
		}
		if (report != null)
		{
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			Boolean showok = Optional.ofNullable(operation.getData("showOK")).map(Boolean::valueOf).orElse(true);
			writer.writeElement("showOK", showok.toString());
			Long parentID = Long.valueOf(operation.getData("ParentID"));
			if (parentID == 0)
			{
				List<Child> nodes = report.nodes.stream().filter(n -> showok || n.data.status != Status.OK).collect(Collectors.toList());
				int start, end, nodecount = nodes.size();
				writer.writeElement("startRow", Integer.toString(start = Math.min(nodecount - 1, operation.startRow)));
				writer.writeElement("endRow", Integer.toString(end = Math.min(nodecount - 1, operation.endRow)));
				writer.writeElement("totalRows", Integer.toString(nodecount));

				if (nodecount > 0)
				{
					writer.writeStartElement("data");
					for (int i = start; i <= end; i++)
					{
						Child n = nodes.get(i);
						writer.writeStartElement("record");
						writer.writeAttribute("ID", Long.toString(n.uid));
						writer.writeAttribute("ParentID", parentID.toString());
						writer.writeAttribute("title", n.data.title);
						if (n.data.length != null)
							writer.writeAttribute("length", n.data.length.toString());
						writer.writeAttribute("status", n.data.status.toString());
						writer.writeAttribute("isFolder", Boolean.toString(n.children != null && n.children.size() > 0));
						writer.writeEndElement();
					}
					writer.writeEndElement();
				}
			}
			else
			{
				Child parent = report.all.get(parentID);
				if (parent != null)
				{
					int nodecount = parent.children != null ? parent.children.size() : 0;
					writer.writeElement("startRow", "0");
					writer.writeElement("endRow", Integer.toString(nodecount - 1));
					writer.writeElement("totalRows", Integer.toString(nodecount));
					writer.writeStartElement("data");
					if (parent.children != null)
						for (Child n : parent.children)
						{
							writer.writeStartElement("record");
							writer.writeAttribute("ID", Long.toString(n.uid));
							writer.writeAttribute("ParentID", parentID.toString());
							writer.writeAttribute("title", n.data.title);
							if (n.data.length != null)
								writer.writeAttribute("length", n.data.length.toString());
							writer.writeAttribute("status", n.data.status.toString());
							writer.writeAttribute("isFolder", Boolean.toString(n.children != null && n.children.size() > 0));
							writer.writeEndElement();
						}
					writer.writeEndElement();
				}
			}
			writer.writeEndElement();
		}
		else
			success();
	}
}
