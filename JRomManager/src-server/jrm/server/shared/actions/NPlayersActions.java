package jrm.server.shared.actions;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.security.PathAbstractor;

public class NPlayersActions
{
	private final ActionsMgr ws;

	public NPlayersActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	public void load(JsonObject jso)
	{
		JsonValue jsv = jso.get("params").asObject().get("path");
		ws.getSession().getCurr_profile().setProperty(SettingsEnum.filter_nplayers_ini, jsv!=null&&!jsv.isNull()?jsv.asString():null); //$NON-NLS-1$
		ws.getSession().getCurr_profile().loadNPlayers(null);
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
					add("cmd", "NPlayers.loaded");
					add("params", new JsonObject() {{
						add("path", profile.nplayers != null ? PathAbstractor.getRelativePath(ws.getSession(), profile.nplayers.file.toPath()).toString() : null);
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
