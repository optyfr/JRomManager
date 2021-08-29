package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import jrm.batch.TrntChkReport;
import jrm.batch.TrntChkReport.Child;
import jrm.batch.TrntChkReport.Status;
import jrm.profile.report.ReportFile;
import jrm.server.shared.datasources.XMLRequest.Operation;

public class BatchTrntChkReportTreeXMLResponse extends XMLResponse
{
	private static final String PARENT_ID = "ParentID";
	private static final String STATUS = "status";

	public BatchTrntChkReportTreeXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		TrntChkReport report = null;
		if (operation.hasData("src"))
		{
			final var srcfile =  pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();
			final var reportfile = ReportFile.getReportFile(request.getSession(), srcfile);
			if (request.session.getTmpTCReport() == null || !(request.session.getTmpTCReport().getReportFile(request.getSession()).equals(reportfile) && request.getSession().getTmpTCReport().getFileModified() == reportfile.lastModified()))
				request.session.setTmpTCReport(TrntChkReport.load(request.getSession(), srcfile));
			report = request.session.getTmpTCReport();
		}
		if (report != null)
		{
			writer.writeStartElement("response");
			writer.writeElement(STATUS, "0");
			Boolean showok = Optional.ofNullable(operation.getData("showOK")).map(Boolean::valueOf).orElse(true);
			writer.writeElement("showOK", showok.toString());
			var parentID = Long.valueOf(operation.getData(PARENT_ID));
			if (parentID == 0)
				fetchRoot(operation, report, showok);
			else
				fetchNode(report, parentID);
			writer.writeEndElement();
		}
		else
			success();
	}

	/**
	 * @param report
	 * @param parentID
	 * @throws XMLStreamException
	 */
	private void fetchNode(TrntChkReport report, Long parentID) throws XMLStreamException
	{
		Child parent = report.getAll().get(parentID);
		if (parent != null)
		{
			int nodecount = parent.getChildren() != null ? parent.getChildren().size() : 0;
			writer.writeElement("startRow", "0");
			writer.writeElement("endRow", Integer.toString(nodecount - 1));
			writer.writeElement("totalRows", Integer.toString(nodecount));
			writer.writeStartElement("data");
			if (parent.getChildren() != null)
				for (Child n : parent.getChildren())
				{
					writer.writeStartElement("record");
					writer.writeAttribute("ID", Long.toString(n.getUid()));
					writer.writeAttribute(PARENT_ID, parentID.toString());
					writer.writeAttribute("title", n.getData().getTitle());
					if (n.getData().getLength() != null)
						writer.writeAttribute("length", n.getData().getLength().toString());
					writer.writeAttribute(STATUS, n.getData().getStatus().toString());
					writer.writeAttribute("isFolder", Boolean.toString(n.getChildren() != null && !n.getChildren().isEmpty()));
					writer.writeEndElement();
				}
			writer.writeEndElement();
		}
	}

	/**
	 * @param operation
	 * @param report
	 * @param showok
	 * @throws XMLStreamException
	 */
	private void fetchRoot(Operation operation, TrntChkReport report, Boolean showok) throws XMLStreamException
	{
		List<Child> nodes = report.getNodes().stream().filter(n -> showok || n.getData().getStatus() != Status.OK).collect(Collectors.toList());
		int start;
		int end;
		var nodecount = nodes.size();
		start = Math.min(nodecount - 1, operation.getStartRow());
		writer.writeElement("startRow", Integer.toString(start));
		end = Math.min(nodecount - 1, operation.getEndRow());
		writer.writeElement("endRow", Integer.toString(end));
		writer.writeElement("totalRows", Integer.toString(nodecount));

		if (nodecount > 0)
		{
			writer.writeStartElement("data");
			for (int i = start; i <= end; i++)
			{
				Child n = nodes.get(i);
				writer.writeStartElement("record");
				writer.writeAttribute("ID", Long.toString(n.getUid()));
				writer.writeAttribute(PARENT_ID, "0");
				writer.writeAttribute("title", n.getData().getTitle());
				if (n.getData().getLength() != null)
					writer.writeAttribute("length", n.getData().getLength().toString());
				writer.writeAttribute(STATUS, n.getData().getStatus().toString());
				writer.writeAttribute("isFolder", Boolean.toString(n.getChildren() != null && !n.getChildren().isEmpty()));
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
	}
}
