package jrm.server.shared.datasources;

import java.util.Optional;

import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.misc.SettingsEnum;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.xml.SimpleAttribute;

public class BatchTrntChkSDRXMLResponse extends XMLResponse
{

	public BatchTrntChkSDRXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
		if (sdrl.isNeedSave())
		{
			request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
			request.getSession().getUser().getSettings().saveSettings();
		}
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(sdrl.size()-1));
		writer.writeElement("totalRows", Integer.toString(sdrl.size()));
		writer.writeStartElement("data");
		for(SrcDstResult sdr : sdrl)
		{
			writer.writeElement("record", 
				new SimpleAttribute("id", sdr.id),
				new SimpleAttribute("src", sdr.src),
				new SimpleAttribute("dst", sdr.dst!=null?sdr.dst:""),
				new SimpleAttribute("result", sdr.result),
				new SimpleAttribute("selected", sdr.selected)
			);
		}
		writer.writeEndElement();
		writer.writeEndElement();
	}
	
	@Override
	protected void add(Operation operation) throws Exception
	{
		if(operation.hasData("src"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
			if (sdrl.isNeedSave())
			{
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
			}
			final SrcDstResult sdr = new SrcDstResult(operation.getData("src"));
			Optional<SrcDstResult> candidate = sdrl.stream().filter(s->s.src.equals(operation.getData("src"))).findAny();
			if(!candidate.isPresent())
			{
				sdrl.add(sdr);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				writer.writeElement("record", 
					new SimpleAttribute("id", sdr.id),
					new SimpleAttribute("src", sdr.src),
					new SimpleAttribute("dst", sdr.dst!=null?sdr.dst:""),
					new SimpleAttribute("result", sdr.result),
					new SimpleAttribute("selected", sdr.selected)
				);
				writer.writeEndElement();
				writer.writeEndElement();
			}
			else
				failure("Entry already exists");
		}
		else
			failure("Src is missing in request");
	}
	
	@Override
	protected void update(Operation operation) throws Exception
	{
		if(operation.hasData("src"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
			if (sdrl.isNeedSave())
			{
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
			}
			Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.id.equals(operation.getData("id"))).findFirst();
			if(candidate.isPresent())
			{
				if(operation.hasData("src") || operation.hasData("dst") || operation.hasData("selected"))
				{
					if(operation.hasData("src"))
						candidate.get().src = operation.getData("src");
					if(operation.hasData("dst"))
						candidate.get().dst = operation.getData("dst");
					if(operation.hasData("selected"))
						candidate.get().selected = Boolean.parseBoolean(operation.getData("selected"));
					request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
					request.getSession().getUser().getSettings().saveSettings();
					writer.writeStartElement("response");
					writer.writeElement("status", "0");
					writer.writeStartElement("data");
					writer.writeElement("record", 
							new SimpleAttribute("id", candidate.get().id),
						new SimpleAttribute("src", candidate.get().src),
						new SimpleAttribute("dst", candidate.get().dst!=null?candidate.get().dst:""),
						new SimpleAttribute("result", candidate.get().result),
						new SimpleAttribute("selected", candidate.get().selected)
					);
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
			failure("Src is missing in request");
	}
	
	@Override
	protected void remove(Operation operation) throws Exception
	{
		if(operation.hasData("id"))
		{
			final SDRList sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr, "[]"));
			if (sdrl.isNeedSave())
			{
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
			}
			Optional<SrcDstResult> candidate = sdrl.stream().filter(sdr->sdr.id.equals(operation.getData("id"))).findFirst();
			if(candidate.isPresent())
			{
				sdrl.remove(candidate.get());
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				writer.writeElement("record", new SimpleAttribute("id", candidate.get().id));
				writer.writeEndElement();
				writer.writeEndElement();
				
			}
			else
				failure("not in the list");
		}
		else
			failure("Src is missing in request");
	}
}
