package jrm.server.ws;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;

import jrm.profile.Profile;

public class CatVerWS
{
	private final WebSckt ws;

	public CatVerWS(WebSckt ws)
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
					add("cmd", "CatVer.loaded");
					add("params", new JsonObject() {{
						add("path", profile.catver != null ? profile.catver.file.getAbsolutePath() : null);
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
