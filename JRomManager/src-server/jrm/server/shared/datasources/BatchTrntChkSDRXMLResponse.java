package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.misc.SettingsEnum;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchTrntChkSDRXMLResponse extends XMLResponse
{

	private static final String SRC_IS_MISSING_IN_REQUEST = "Src is missing in request";
	private static final String SELECTED = "selected";
	private static final String RESULT = "result";
	private static final String STATUS = "status";
	private static final String RECORD = "record";
	private static final String RESPONSE = "response";

	public BatchTrntChkSDRXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
		if (sdrl.isNeedSave())
		{
			request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
			request.getSession().getUser().getSettings().saveSettings();
		}
		writer.writeStartElement(RESPONSE);
		writer.writeElement(STATUS, "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(sdrl.size()-1));
		writer.writeElement("totalRows", Integer.toString(sdrl.size()));
		writer.writeStartElement("data");
		for(SrcDstResult sdr : sdrl)
			writeRecord(sdr);
		writer.writeEndElement();
		writer.writeEndElement();
	}


	/**
	 * @param sdr
	 * @throws XMLStreamException
	 */
	private void writeRecord(SrcDstResult sdr) throws XMLStreamException
	{
		writer.writeElement(RECORD, 
			new SimpleAttribute("id", sdr.getId()),
			new SimpleAttribute("src", sdr.getSrc()),
			new SimpleAttribute("dst", Optional.ofNullable(sdr.getDst()).orElse("")),
			new SimpleAttribute(RESULT, sdr.getResult()),
			new SimpleAttribute(SELECTED, sdr.isSelected())
		);
	}
	
	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("src"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
			if (sdrl.isNeedSave())
			{
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
			}
			final var sdr = new SrcDstResult(operation.getData("src"));
			Optional<SrcDstResult> candidate = sdrl.stream().filter(s->s.getSrc().equals(operation.getData("src"))).findAny();
			if(!candidate.isPresent())
			{
				sdrl.add(sdr);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writer.writeStartElement("data");
				writeRecord(sdr);
				writer.writeEndElement();
				writer.writeEndElement();
			}
			else
				failure("Entry already exists");
		}
		else
			failure(SRC_IS_MISSING_IN_REQUEST);
	}
	
	@Override
	protected void update(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("id"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
			if (sdrl.isNeedSave())
			{
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
			}
			final Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.getId().equals(operation.getData("id"))).findFirst();
			if(candidate.isPresent())
			{
				if(operation.hasData("src") || operation.hasData("dst") || operation.hasData(SELECTED))
				{
					final var sdr = candidate.get();
					if(operation.hasData("src"))
						sdr.setSrc(operation.getData("src"));
					if(operation.hasData("dst"))
						sdr.setDst(operation.getData("dst"));
					if(operation.hasData(SELECTED))
						sdr.setSelected(Boolean.parseBoolean(operation.getData(SELECTED)));
					request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
					request.getSession().getUser().getSettings().saveSettings();
					writer.writeStartElement(RESPONSE);
					writer.writeElement(STATUS, "0");
					writer.writeStartElement("data");
					writeRecord(sdr);
					writer.writeEndElement();
					writer.writeEndElement();
				}
				else
					failure("field to update is missing in request");
			}
			else
				failure("not in list");
		}
		else
			failure(SRC_IS_MISSING_IN_REQUEST);
	}
	
	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("id"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
			if (sdrl.isNeedSave())
			{
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
			}
			final Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.getId().equals(operation.getData("id"))).findFirst();
			if(candidate.isPresent())
			{
				sdrl.remove(candidate.get());
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement(RESPONSE);
				writer.writeElement(STATUS, "0");
				writer.writeStartElement("data");
				writer.writeElement(RECORD, new SimpleAttribute("id", candidate.get().getId()));
				writer.writeEndElement();
				writer.writeEndElement();
				
			}
			else
				failure("not in the list");
		}
		else
			failure(SRC_IS_MISSING_IN_REQUEST);
	}
}
