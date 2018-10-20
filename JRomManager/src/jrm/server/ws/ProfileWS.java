package jrm.server.ws;

import java.io.IOException;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import jrm.profile.Profile;

public class ProfileWS
{
	private final WebSckt ws;

	public ProfileWS(WebSckt ws)
	{
		this.ws = ws;
	}

	@SuppressWarnings("serial")
	public void loaded(final Profile profile)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Profile.loaded");
					add("params", new JsonObject() {{
						add("success", profile!=null);
						if(profile!=null)
						{
							add("name", profile.getName());
							if(profile.systems!=null)
							{
								add("systems", new JsonArray() {{
									profile.systems.forEach(s-> add(new JsonObject() {{
										add("name", s.toString());
										add("selected", s.isSelected(profile));
										add("property", s.getPropertyName());
										add("type", s.getType().toString());
									}}));
								}});
							}
							if(profile.settings!=null)
								add("settings",profile.settings.asJSO());
						}
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
