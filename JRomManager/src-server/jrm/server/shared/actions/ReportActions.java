package jrm.server.shared.actions;

import java.io.IOException;
import java.util.EnumSet;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Report;

public class ReportActions
{
	private final ActionsMgr ws;

	public ReportActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	@SuppressWarnings("serial")
	public void setFilter(JsonObject jso, boolean lite)
	{
		final JsonObject pjso = jso.get("params").asObject();
		final Report report = lite ? ws.getSession().tmp_report : ws.getSession().report;
		EnumSet<FilterOptions> options = report.getHandler().getFilterOptions().clone();
		for (Member m : pjso)
		{
			try
			{
				FilterOptions option = FilterOptions.valueOf(m.getName());
				JsonValue value = m.getValue();
				if (value.asBoolean())
					options.add(option);
				else
					options.remove(option);

			}
			catch (IllegalArgumentException ex)
			{

			}
		}
		report.getHandler().filter(options.toArray(new FilterOptions[0]));
		try
		{
			if (ws.isOpen())
			{
				ws.send(Json.object().add("cmd", lite ? "ReportLite.applyFilters" : "Report.applyFilters").add("params", new JsonObject()
				{
					{
						EnumSet.allOf(FilterOptions.class).forEach(f -> add(f.toString(), options.contains(f)));
					}
				}).toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}
}
