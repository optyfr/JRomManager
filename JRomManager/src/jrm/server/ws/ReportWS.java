package jrm.server.ws;

import java.io.IOException;
import java.util.EnumSet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import jrm.profile.report.FilterOptions;

import com.eclipsesource.json.JsonValue;

public class ReportWS
{
	private final WebSckt ws;

	public ReportWS(WebSckt ws)
	{
		this.ws = ws;
	}
	
	@SuppressWarnings("serial")
	void setFilter(JsonObject jso)
	{
		JsonObject pjso = jso.get("params").asObject();
		EnumSet<FilterOptions> options = ws.session.report.getModel().getFilterOptions().clone();
		for(Member m : pjso)
		{
			try
			{
				FilterOptions option = FilterOptions.valueOf(m.getName());
				JsonValue value = m.getValue();
				if(value.asBoolean())
					options.add(option);
				else
					options.remove(option);
				
			}
			catch(IllegalArgumentException ex)
			{
				
			}
		}
		ws.session.report.getModel().filter(options.toArray(new FilterOptions[0]));
		try
		{
			if(ws.isOpen())
			{
				ws.send(Json.object()
					.add("cmd", "Report.applyFilters")
					.add("params", new JsonObject() {{
						EnumSet.allOf(FilterOptions.class).forEach(f->add(f.toString(),options.contains(f)));
					}}).toString()
				);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
