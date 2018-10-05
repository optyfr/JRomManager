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
import jrm.security.Session;
import jrm.server.Server;
import jrm.server.SessionStub;

public class WebSckt extends WebSocket implements SessionStub
{
	public final static Map<String, WebSckt> sockets = new HashMap<>();
	private Server server;
	
	public WebSckt(Server server, IHTTPSession handshakeRequest)
	{
		super(handshakeRequest);
		this.server = server;
		setSession(Server.getSession(handshakeRequest.getCookies().read("session")));
		System.out.println("websocket created for session "+getSession()+"......");
	}

	public static WebSckt get(Session session)
	{
		return sockets.get(session.getSessionId());
	}
	
	@Override
	protected void onPong(WebSocketFrame pongFrame)
	{
	}

	@Override
	protected void onMessage(WebSocketFrame messageFrame)
	{
		try
		{
			JsonObject jso = Json.parse(messageFrame.getTextPayload()).asObject();
			if (jso != null)
			{
				switch (jso.getString("cmd", "unknown"))
				{
					case "Profile.load":
					{
						ProgressWS progress = new ProgressWS(this);
						Profile.load(session, new File(jso.get("params").asObject().getString("path", null)), progress);
						progress.close();
						break;
					}
					default:
						System.err.println("Unknown command : " + jso.getString("cmd", "unknown"));
						break;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println(messageFrame.getTextPayload());
			e.printStackTrace();
		}
	}

	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote)
	{
		System.out.println("websocket closed, removing session "+getSession()+"......");
		unsetSession(session);
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
	}

	private Session session;
	
	@Override
	public Session getSession()
	{
		return session;
	}

	@Override
	public void setSession(Session session)
	{
		if(session==null)
			throw new NullPointerException("Session not found");
		sockets.put(session.getSessionId(), this);
		this.session = session;
		
	}

	@Override
	public void unsetSession(Session session)
	{
		sockets.remove(getSession().getSessionId());
		server.unsetSession(session);
		this.session = null;
	}

}