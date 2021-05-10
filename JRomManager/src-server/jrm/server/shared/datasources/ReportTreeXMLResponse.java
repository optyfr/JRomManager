package jrm.server.shared.datasources;

import javax.xml.stream.XMLStreamException;

import jrm.profile.report.Note;
import jrm.profile.report.Report;
import jrm.profile.report.Subject;
import jrm.profile.report.SubjectSet;
import jrm.server.shared.datasources.XMLRequest.Operation;

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

		var report = request.session.report;
		if (operation.hasData("src"))
		{
			final var srcfile = pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();
			final var reportfile = Report.getReportFile(request.session, srcfile);
			if (request.session.tmp_report == null || !(request.session.tmp_report.getReportFile(request.session).equals(reportfile) && request.session.tmp_report.getFileModified() == reportfile.lastModified()))
				request.session.tmp_report = Report.load(request.session, srcfile);
			report = request.session.tmp_report;
		}

		final var parentID = Integer.parseInt(operation.getData("ParentID"));
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
				writer.writeStartElement("record");
				writer.writeAttribute("ID", Integer.toString(n.getId()));
				writer.writeAttribute("ParentID", Integer.toString(parentID));
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
				writer.writeStartElement("record");
				writer.writeAttribute("ID", Integer.toString(s.getId()));
				writer.writeAttribute("ParentID", Integer.toString(0));
				writer.writeAttribute("title", s.getHTML());
				writer.writeAttribute("class", s.getClass().getSimpleName());
				if (s instanceof SubjectSet)
				{
					writer.writeAttribute("status", ((SubjectSet) s).getStatus().toString());
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
	protected void custom(Operation operation) throws Exception
	{
		if ("detail".equals(operation.getOperationId().toString()))
		{
			var report = request.session.report;
			if (operation.hasData("src"))
			{
				final var srcfile = pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();
				final var reportfile = Report.getReportFile(request.session, srcfile);
				if (request.session.tmp_report == null || !(request.session.tmp_report.getReportFile(request.session).equals(reportfile) && request.session.tmp_report.getFileModified() == reportfile.lastModified()))
					request.session.tmp_report = Report.load(request.session, srcfile);
				report = request.session.tmp_report;
			}
			final var parentID = Integer.parseInt(operation.getData("ParentID"));
			final var subject = report.getHandler().getFilteredReport().findSubject(parentID);
			writer.writeStartElement("response");
			writer.writeElement("status", "0");
			writer.writeStartElement("data");
			for (Note n : subject)
			{
				if (n.getId() == Integer.valueOf(operation.getData("ID")))
				{
					writer.writeStartElement("record");
					writer.writeAttribute("ID", Integer.toString(n.getId()));
					writer.writeAttribute("ParentID", Integer.toString(parentID));
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
