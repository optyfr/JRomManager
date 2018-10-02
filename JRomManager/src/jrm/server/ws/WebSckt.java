package jrm.server.ws;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import jrm.profile.Profile;
import jrm.server.SessionStub;

public class WebSckt extends WebSocket implements SessionStub
{
	public static Map<String, WebSckt> sockets = new HashMap<>();
	
	public WebSckt(IHTTPSession handshakeRequest)
	{
		super(handshakeRequest);
		setSession(handshakeRequest.getCookies().read("session"));
		sockets.put(getSession(), this);
		System.out.println("websocket created for session "+getSession()+"......");
	}

	public static WebSckt get(String session)
	{
		return sockets.get(session);
	}
	
	@Override
	protected void onPong(WebSocketFrame pongFrame)
	{

	}

	@Override
	protected void onMessage(WebSocketFrame messageFrame)
	{
		JsonObject jso = Json.parse(messageFrame.getTextPayload()).asObject();
		if(jso!=null)
		{
			System.out.println("cmd:"+jso.getString("cmd", "unknown"));
			JsonObject params = jso.get("params").asObject();
			switch(jso.getString("cmd", "unknown"))
			{
				case "loadProfile":
					Profile.load(new File(params.getString("path", null)), new ProgressWS(this));
					break;
			}
		}
	}

	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote)
	{
		System.out.println("websocket closed, removing session "+getSession()+"......");
		sockets.remove(getSession());
	}

	@Override
	protected void onException(IOException e)
	{

	}

	@Override
	protected void onOpen()
	{
		System.out.println("websocket opened......");
		//TODO need to send prefs
/*		try
		{
			send(Json.object().add("cmd", "openProgress").toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}*/
	}

	private String session;
	
	@Override
	public String getSession()
	{
		return session;
	}

	@Override
	public void setSession(String session)
	{
		this.session = session;
		
	}

}