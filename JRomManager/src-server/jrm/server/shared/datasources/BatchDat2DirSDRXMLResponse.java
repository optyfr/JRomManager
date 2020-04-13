package jrm.server.shared.datasources;

import java.util.List;
import java.util.Optional;

import jrm.misc.SettingsEnum;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.ui.basic.SrcDstResult;
import jrm.xml.SimpleAttribute;

public class BatchDat2DirSDRXMLResponse extends XMLResponse
{

	public BatchDat2DirSDRXMLResponse(XMLRequest request) throws Exception
	{
		super(request);
	}


	@Override
	protected void fetch(Operation operation) throws Exception
	{
		List<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
		writer.writeStartElement("response");
		writer.writeElement("status", "0");
		writer.writeElement("startRow", "0");
		writer.writeElement("endRow", Integer.toString(sdrl.size()-1));
		writer.writeElement("totalRows", Integer.toString(sdrl.size()));
		writer.writeStartElement("data");
		for(SrcDstResult sdr : sdrl)
		{
			writer.writeElement("record", 
				new SimpleAttribute("src", pathAbstractor.getRelativePath(sdr.src)),
				new SimpleAttribute("dst", sdr.dst!=null?pathAbstractor.getRelativePath(sdr.dst):""),
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
			final List<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
			final SrcDstResult sdr = new SrcDstResult() {{src=pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();}};
			if(!sdrl.contains(sdr))
			{
				sdrl.add(sdr);
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				writer.writeElement("record", 
					new SimpleAttribute("src", pathAbstractor.getRelativePath(sdr.src)),
					new SimpleAttribute("dst", sdr.dst!=null?pathAbstractor.getRelativePath(sdr.dst):""),
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
			final List<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
			final SrcDstResult search = new SrcDstResult() {{src=pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();}};
			Optional<SrcDstResult> candidate = sdrl.stream().filter(p->p.equals(search)).findFirst();
			if(candidate.isPresent())
			{
				if(operation.hasData("dst") || operation.hasData("selected"))
				{
					if(operation.hasData("dst"))
						candidate.get().dst = pathAbstractor.getAbsolutePath(operation.getData("dst")).toFile();
					else
						candidate.get().selected = Boolean.parseBoolean(operation.getData("selected"));
					request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr,SrcDstResult.toJSON(sdrl));
					request.getSession().getUser().getSettings().saveSettings();
					writer.writeStartElement("response");
					writer.writeElement("status", "0");
					writer.writeStartElement("data");
					writer.writeElement("record", 
						new SimpleAttribute("src", pathAbstractor.getRelativePath(candidate.get().src)),
						new SimpleAttribute("dst", candidate.get().dst!=null?pathAbstractor.getRelativePath(candidate.get().dst):""),
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
				failure(search.src + " not in list");
		}
		else
			failure("Src is missing in request");
	}
	
	@Override
	protected void remove(Operation operation) throws Exception
	{
		if(operation.hasData("src"))
		{
			final List<SrcDstResult> sdrl =  SrcDstResult.fromJSON(request.getSession().getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
			final SrcDstResult search = new SrcDstResult() {{src=pathAbstractor.getAbsolutePath(operation.getData("src")).toFile();}};
			if(sdrl.remove(search))
			{
				request.getSession().getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr,SrcDstResult.toJSON(sdrl));
				request.getSession().getUser().getSettings().saveSettings();
				writer.writeStartElement("response");
				writer.writeElement("status", "0");
				writer.writeStartElement("data");
				writer.writeElement("record", new SimpleAttribute("src", pathAbstractor.getRelativePath(search.src)));
				writer.writeEndElement();
				writer.writeEndElement();
				
			}
			else
				failure(search.src + " is not in the list");
		}
		else
			failure("Src is missing in request");
	}
}
