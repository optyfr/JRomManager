package jrm.server.shared.actions;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;

public class CatVerActions
{
	private final ActionsMgr ws;

	public CatVerActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	public void load(JsonObject jso)
	{
		JsonValue jsv = jso.get("params").asObject().get("path");
		ws.getSession().getCurr_profile().setProperty(SettingsEnum.filter_catver_ini, jsv!=null&&!jsv.isNull()?jsv.asString():null); //$NON-NLS-1$
		ws.getSession().getCurr_profile().loadCatVer(null);
		ws.getSession().getCurr_profile().saveSettings();
		loaded(ws.getSession().getCurr_profile());
	}
	
	@SuppressWarnings("serial")
	public void loaded(final Profile profile)
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
