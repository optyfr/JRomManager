package jrm.server.shared.actions;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.security.PathAbstractor;

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
	
	public void loaded(final Profile profile)
	{
		try
		{
			if(ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "CatVer.loaded");
				final var params = new JsonObject();
				params.add("path", profile.catver != null ? PathAbstractor.getRelativePath(ws.getSession(), profile.catver.file.toPath()).toString() : null);
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
