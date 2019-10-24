package jrm.server.ws;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;

public class CatVerWS
{
	private final WebSckt ws;

	public CatVerWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void load(JsonObject jso)
	{
		JsonValue jsv = jso.get("params").asObject().get("path");
		ws.session.curr_profile.setProperty(SettingsEnum.filter_catver_ini, jsv!=null&&!jsv.isNull()?jsv.asString():null); //$NON-NLS-1$
		ws.session.curr_profile.loadCatVer(null);
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
					add("cmd", "CatVer.loaded");
					add("params", new JsonObject() {{
						add("path", profile.catver != null ? profile.catver.file.getAbsolutePath() : null);
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}
}
