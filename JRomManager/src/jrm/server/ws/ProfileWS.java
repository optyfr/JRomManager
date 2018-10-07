package jrm.server.ws;

import java.io.IOException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import jrm.profile.data.Systms;

public class ProfileWS
{
	private final WebSckt ws;

	public ProfileWS(WebSckt ws)
	{
		this.ws = ws;
	}

	@SuppressWarnings("serial")
	public void loaded(boolean success, String name, Systms systems)
	{
		try
		{
			ws.send(new JsonObject() {{
				add("cmd", "Profile.loaded");
				add("params", new JsonObject() {{
					add("success", success);
					add("name", name);
					if(systems!=null)
						add("systems", new JsonArray() {{
							systems.forEach(s-> add(new JsonObject() {{
								add("type", s.getType().toString());
								add("name", s.getName());
							}}));
						}});
				}});
			}}.toString());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
