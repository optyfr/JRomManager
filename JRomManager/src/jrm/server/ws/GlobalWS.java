package jrm.server.ws;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

public class GlobalWS
{
	private final WebSckt ws;

	public GlobalWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void setProperty(JsonObject jso)
	{
		JsonObject pjso = jso.get("params").asObject();
		for(Member m : pjso)
		{
			JsonValue value = m.getValue();
			if(value.isBoolean())
				ws.session.getUser().settings.setProperty(m.getName(), value.asBoolean());
			else if(value.isString())
				ws.session.getUser().settings.setProperty(m.getName(), value.asString());
			else
				ws.session.getUser().settings.setProperty(m.getName(), value.toString());
		}
		ws.session.getUser().settings.saveSettings();
	}
	
}
