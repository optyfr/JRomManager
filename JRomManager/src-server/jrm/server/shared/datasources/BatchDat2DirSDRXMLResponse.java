package jrm.server.shared.datasources;

import java.io.IOException;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.misc.SettingsEnum;
import jrm.server.shared.datasources.XMLRequest.Operation;

public class BatchDat2DirSDRXMLResponse extends SDRXMLResponse
{
	public BatchDat2DirSDRXMLResponse(XMLRequest request) throws IOException, XMLStreamException
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws XMLStreamException
	{
		SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
		needSave(sdrl, SettingsEnum.dat2dir_sdr);
		writeResponse(operation, sdrl);
	}


	
	@Override
	protected void add(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("src"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
			needSave(sdrl, SettingsEnum.dat2dir_sdr);
			final SrcDstResult sdr = new SrcDstResult(operation.getData("src"));
			Optional<SrcDstResult> candidate = sdrl.stream().filter(s->s.getSrc().equals(operation.getData("src"))).findAny();
			if(!candidate.isPresent())
			{
				sdrl.add(sdr);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr,SrcDstResult.toJSON(sdrl));
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
		if(operation.hasData("id"))
		{
			failure(SRC_IS_MISSING_IN_REQUEST);
			return;
		}
		final SDRList sdrl = SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
		needSave(sdrl, SettingsEnum.dat2dir_sdr);
		Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.getId().equals(operation.getData("id"))).findFirst();
		if(!candidate.isPresent())
		{
			failure("not in list");
			return;
		}
		if(operation.hasData("src") || operation.hasData("dst") || operation.hasData(SELECTED))
		{
			final var sdr = candidate.get();
			if(operation.hasData("src"))
				sdr.setSrc(operation.getData("src"));
			if(operation.hasData("dst"))
				sdr.setDst(operation.getData("dst"));
			if(operation.hasData(SELECTED))
				sdr.setSelected(Boolean.parseBoolean(operation.getData(SELECTED)));
			request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr,SrcDstResult.toJSON(sdrl));
			request.getSession().getUser().getSettings().saveSettings();
			writeResponseSingle(sdr);
		}
		else
			failure("field to update is missing in request");
	}
	
	@Override
	protected void remove(Operation operation) throws XMLStreamException
	{
		if(operation.hasData("id"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
			needSave(sdrl, SettingsEnum.dat2dir_sdr);
			Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.getId().equals(operation.getData("id"))).findFirst();
			if(candidate.isPresent())
			{
				final var sdr = candidate.get();
				sdrl.remove(sdr);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr,SrcDstResult.toJSON(sdrl));
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
