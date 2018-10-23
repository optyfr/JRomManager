package jrm.server.ws;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;

import jrm.profile.Profile;

public class NPlayersWS
{
	private final WebSckt ws;

	public NPlayersWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void load(JsonObject jso)
	{
		ws.session.curr_profile.setProperty("filter.nplayers.ini", jso.get("params").asObject().getString("path", null)); //$NON-NLS-1$
		ws.session.curr_profile.loadNPlayers(null);
		ws.session.curr_profile.saveSettings();
		loaded(ws.session.curr_profile);
	}
	
	@SuppressWarnings("serial")
	void loaded(final Profile profile)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "NPlayers.loaded");
					add("params", new JsonObject() {{
						add("path", profile.nplayers != null ? profile.nplayers.file.getAbsolutePath() : null);
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
