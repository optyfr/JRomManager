package jrm.server.shared.datasources;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import jrm.profile.report.Note;
import jrm.profile.report.Report;
import jrm.profile.report.Subject;
import jrm.profile.report.SubjectSet;
import jrm.server.shared.datasources.XMLRequest.Operation;

public class ReportTreeXMLResponse extends XMLResponse
{

	private static final String RECORD = "record";
	private static final String PARENT_ID = "ParentID";
	private static final String STATUS = "status";

	public ReportTreeXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	@Override
	protected void fetch(Operation operation) throws XMLStreamException, IOException
	{
		writer.writeStartElement("response");
		writer.writeElement(STATUS, "0");

		var report = request.session.getReport();
		if (operation.hasData("src"))
		{
			final var srcfile = pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();
			final var reportfile = Report.getReportFile(request.session, srcfile);
			if (request.session.getTmpReport() == null || !(request.session.getTmpReport().getReportFile(request.session).equals(reportfile) && request.session.getTmpReport().getFileModified() == reportfile.lastModified()))
				request.session.setTmpReport(Report.load(request.session, srcfile));
			report = request.session.getTmpReport();
		}

		final var parentID = Integer.parseInt(operation.getData(PARENT_ID));
		if (parentID == 0)
		{
			fetchRoot(operation, report);
		}
		else
		{
			fetchNode(report, parentID);
		}
		writer.writeEndElement();
	}

	/**
	 * @param report
	 * @param parentID
	 * @throws XMLStreamException
	 */
	private void fetchNode(Report report, final int parentID) throws XMLStreamException
	{
		final var subject = report.getHandler().getFilteredReport().findSubject(parentID);
		if (subject != null)
		{
			int nodecount = subject.size();
			writer.writeElement("startRow", "0");
			writer.writeElement("endRow", Integer.toString(nodecount - 1));
			writer.writeElement("totalRows", Integer.toString(nodecount));
			writer.writeStartElement("data");
			for (Note n : subject)
			{
				writer.writeStartElement(RECORD);
				writer.writeAttribute("ID", Integer.toString(n.getId()));
				writer.writeAttribute(PARENT_ID, Integer.toString(parentID));
				writer.writeAttribute("title", n.getHTML());
				writer.writeAttribute("class", n.getClass().getSimpleName());
				writer.writeAttribute("isFolder", Boolean.toString(false));
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
	}

	/**
	 * @param operation
	 * @param report
	 * @param parentID
	 * @throws XMLStreamException
	 */
	private void fetchRoot(Operation operation, Report report) throws XMLStreamException
	{
		int start;
		int end;
		int nodecount = report.getHandler().getFilteredReport().size();
		start = Math.min(nodecount - 1, operation.getStartRow());
		writer.writeElement("startRow", Integer.toString(start));
		end = Math.min(nodecount - 1, operation.getEndRow());
		writer.writeElement("endRow", Integer.toString(end));
		writer.writeElement("totalRows", Integer.toString(nodecount));
		writer.writeElement("infos", report.stats.getStatus());

		if (nodecount > 0)
		{
			writer.writeStartElement("data");
			for (int i = start; i <= end; i++)
			{
				Subject s = report.getHandler().getFilteredReport().get(i);
				writer.writeStartElement(RECORD);
				writer.writeAttribute("ID", Integer.toString(s.getId()));
				writer.writeAttribute(PARENT_ID, Integer.toString(0));
				writer.writeAttribute("title", s.getHTML());
				writer.writeAttribute("class", s.getClass().getSimpleName());
				if (s instanceof SubjectSet)
				{
					writer.writeAttribute(STATUS, ((SubjectSet) s).getStatus().toString());
					writer.writeAttribute("hasNotes", Boolean.toString(((SubjectSet) s).hasNotes()));
					writer.writeAttribute("isFixable", Boolean.toString(((SubjectSet) s).isFixable()));
				}
				writer.writeAttribute("isFolder", Boolean.toString(!s.getNotes().isEmpty()));
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
	}

	@Override
	protected void custom(Operation operation) throws XMLStreamException, IOException
	{
		if ("detail".equals(operation.getOperationId().toString()))
		{
			var report = request.session.getReport();
			if (operation.hasData("src"))
			{
				final var srcfile = pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();
				final var reportfile = Report.getReportFile(request.session, srcfile);
				if (request.session.getTmpReport() == null || !(request.session.getTmpReport().getReportFile(request.session).equals(reportfile) && request.session.getTmpReport().getFileModified() == reportfile.lastModified()))
					request.session.setTmpReport(Report.load(request.session, srcfile));
				report = request.session.getTmpReport();
			}
			final var parentID = Integer.parseInt(operation.getData(PARENT_ID));
			final var subject = report.getHandler().getFilteredReport().findSubject(parentID);
			writer.writeStartElement("response");
			writer.writeElement(STATUS, "0");
			writer.writeStartElement("data");
			for (Note n : subject)
			{
				if (n.getId() == Integer.valueOf(operation.getData("ID")))
				{
					writer.writeStartElement(RECORD);
					writer.writeAttribute("ID", Integer.toString(n.getId()));
					writer.writeAttribute(PARENT_ID, Integer.toString(parentID));
					writer.writeAttribute("Name", n.getName());
					writer.writeAttribute("CRC", n.getCrc());
					writer.writeAttribute("SHA1", n.getSha1());
					writer.writeStartElement("Detail");
					writer.writeCData(n.getDetail());
					writer.writeEndElement();
					writer.writeEndElement();
				}
			}
			writer.writeEndElement();
			writer.writeEndElement();
		}
		else
			super.custom(operation);
	}
}
