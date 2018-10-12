package jrm.server.ws;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

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
	private final static Map<String, WebSckt> sockets = new HashMap<>();
	private Server server;
	private Session session;
	private PingService pingService;
	
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
		pingService.pong();
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
						new Thread(()->{
							if (session.curr_profile != null)
								session.curr_profile.saveSettings();
							ProgressWS progress = new ProgressWS(WebSckt.this);
							session.curr_profile = Profile.load(session, new File(jso.get("params").asObject().getString("path", null)), progress);
							session.curr_profile.nfo.save(session);
							session.report.setProfile(session.curr_profile);
							progress.close();
							new ProfileWS(this).loaded(session.curr_profile);
						}).start();
						break;
					}
					case "Profile.setProperty":
					{
						JsonObject pjso = jso.get("params").asObject();
						for(Member m : pjso)
						{
							JsonValue value = m.getValue();
							if(value.isBoolean())
								session.curr_profile.setProperty(m.getName(), value.asBoolean());
							else if(value.isString())
								session.curr_profile.setProperty(m.getName(), value.asString());
							else
								session.curr_profile.setProperty(m.getName(), value.toString());
						}
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
		try
		{
			pingService.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		pingService = new PingService();
		//TODO need to send prefs
	}
	
	private class PingService implements Closeable
	{
		private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		private long ping = 0, pong = 0;
		private byte[] PAYLOAD = "JRomManager".getBytes();

		private PingService()
		{
			service.scheduleAtFixedRate(new Runnable()
			{
				@Override
				public void run()
				{
					ping();
				}
			}, 4, 4, TimeUnit.SECONDS);
		}

		private void ping()
		{
			try
			{
				WebSckt.this.ping(PAYLOAD);
			//	System.out.println("sent ping");
				ping++;
				if (ping - pong > 3)
					WebSckt.this.close(CloseCode.GoingAway, "Missed too many ping requests.", false);
			}
			catch (IOException e)
			{
			}
		}

		private void pong()
		{
		//	System.out.println("rec pong");
			pong++;
		}

		@Override
		public void close() throws IOException
		{
			service.shutdownNow();
		}
	}

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
		if (session.curr_profile != null)
			session.curr_profile.saveSettings();
		session.getUser().settings.saveSettings();
		sockets.remove(getSession().getSessionId());
		server.unsetSession(session);
		this.session = null;
	}


}