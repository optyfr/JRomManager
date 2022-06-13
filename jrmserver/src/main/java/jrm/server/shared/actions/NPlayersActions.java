package jrm.server.shared.actions;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.security.PathAbstractor;

public class NPlayersActions
{
	private final ActionsMgr ws;

	public NPlayersActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	@SuppressWarnings("exports")
	public void load(JsonObject jso)
	{
		JsonValue jsv = jso.get("params").asObject().get("path");
		ws.getSession().getCurrProfile().setProperty(ProfileSettingsEnum.filter_nplayers_ini, jsv!=null&&!jsv.isNull()?jsv.asString():null); //$NON-NLS-1$
		ws.getSession().getCurrProfile().loadNPlayers(null);
		ws.getSession().getCurrProfile().saveSettings();
		loaded(ws.getSession().getCurrProfile());
	}
	
	public void loaded(final Profile profile)
	{
		try
		{
			if(ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "NPlayers.loaded");
				final var params = new JsonObject();
				params.add("path", profile.getNplayers() != null ? PathAbstractor.getRelativePath(ws.getSession(), profile.getNplayers().file.toPath()).toString() : null);
				msg.add("params", params);
				ws.send(msg.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}
}
