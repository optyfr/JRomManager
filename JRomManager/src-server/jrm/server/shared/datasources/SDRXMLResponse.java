package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.misc.SettingsEnum;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

abstract class SDRXMLResponse extends XMLResponse
{
	protected static final String SRC_IS_MISSING_IN_REQUEST = "Src is missing in request";
	protected static final String RESULT = "result";
	protected static final String RECORD = "record";
	protected static final String SELECTED = "selected";
	protected static final String RESPONSE = "response";
	protected static final String STATUS = "status";

	protected SDRXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}

	/**
	 * @param sdr
	 * @throws XMLStreamException
	 */
	protected void writeRecord(SrcDstResult sdr) throws XMLStreamException
	{
		writer.writeElement(RECORD, 
			new SimpleAttribute("id", sdr.getId()),
			new SimpleAttribute("src", sdr.getSrc()),
			new SimpleAttribute("dst", Optional.ofNullable(sdr.getDst()).orElse("")),
			new SimpleAttribute(RESULT, sdr.getResult()),
			new SimpleAttribute(SELECTED, sdr.isSelected())
		);
	}

	/**
	 * @param operation
	 * @param sdrl
	 * @throws XMLStreamException
	 */
	protected void writeResponse(Operation operation, SDRList sdrl) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		fetchList(operation, sdrl, (sdr, idx) -> writeRecord(sdr));
		writer.writeEndElement();
	}

	/**
	 * @param sdr
	 * @throws XMLStreamException
	 */
	protected void writeResponseSingle(final SrcDstResult sdr) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeStartElement("data");
		writeRecord(sdr);
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * @param sdr
	 * @throws XMLStreamException
	 */
	protected void writeResponseKey(final SrcDstResult sdr) throws XMLStreamException
	{
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeStartElement("data");
		writer.writeElement(RECORD, new SimpleAttribute("id", sdr.getId()));
		writer.writeEndElement();
		writer.writeEndElement();
	}

	/**
	 * @param sdrl
	 */
	protected void needSave(SDRList sdrl, SettingsEnum ppt)
	{
		if (sdrl.isNeedSave())
		{
			request.getSession().getUser().getSettings().setProperty(ppt,SrcDstResult.toJSON(sdrl));
			request.getSession().getUser().getSettings().saveSettings();
		}
	}
}
