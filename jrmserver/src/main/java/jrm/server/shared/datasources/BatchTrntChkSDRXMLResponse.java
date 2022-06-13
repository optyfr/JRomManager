package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import jrm.aui.basic.AbstractSrcDstResult;
import jrm.aui.basic.SDRList;
import jrm.aui.basic.SrcDstResult;
import jrm.misc.SettingsEnum;
import jrm.server.shared.datasources.XMLRequest.Operation;

public class BatchTrntChkSDRXMLResponse extends SDRXMLResponse
{

	public BatchTrntChkSDRXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		SDRList<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr));
		needSave(sdrl, SettingsEnum.trntchk_sdr);
		writeResponse(operation, sdrl);
	}


	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("src"))
		{
			final SDRList<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr));
			needSave(sdrl, SettingsEnum.trntchk_sdr);
			final var sdr = new SrcDstResult(operation.getData("src"));
			Optional<SrcDstResult> candidate = sdrl.stream().filter(s->s.getSrc().equals(operation.getData("src"))).findAny();
			if(!candidate.isPresent())
			{
				sdrl.add(sdr);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,AbstractSrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writeResponseSingle(sdr);
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
		if(!operation.hasData("id"))
		{
			failure(SRC_IS_MISSING_IN_REQUEST);
			return;
		}
		final SDRList<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr));
		needSave(sdrl, SettingsEnum.trntchk_sdr);
		final Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.getId().equals(operation.getData("id"))).findFirst();
		if(!candidate.isPresent())
		{
			failure("not in list");
			return;
		}
		if(!operation.hasData("src") && !operation.hasData("dst") && !operation.hasData(SELECTED))
		{
			failure("field to update is missing in request");
			return;
		}
		final AbstractSrcDstResult sdr = candidate.get();
		if(operation.hasData("src"))
			sdr.setSrc(operation.getData("src"));
		if(operation.hasData("dst"))
			sdr.setDst(operation.getData("dst"));
		if(operation.hasData(SELECTED))
			sdr.setSelected(Boolean.parseBoolean(operation.getData(SELECTED)));
		request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,AbstractSrcDstResult.toJSON(sdrl));
		request.getSession().getUser().getSettings().saveSettings();
		writeResponseSingle(sdr);
	}
	
	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("id"))
		{
			final SDRList<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr));
			needSave(sdrl, SettingsEnum.trntchk_sdr);
			final Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.getId().equals(operation.getData("id"))).findFirst();
			if(candidate.isPresent())
			{
				final AbstractSrcDstResult sdr = candidate.get();
				sdrl.remove(sdr);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,AbstractSrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writeResponseKey(sdr);
				
			}
			else
				failure("not in the list");
		}
		else
			failure(SRC_IS_MISSING_IN_REQUEST);
	}
}
