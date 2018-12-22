package jrm.server.ws;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import jrm.misc.Log;
import jrm.security.Session;
import jrm.server.Server;
import jrm.server.SessionStub;
import jrm.server.WebSession;

public class WebSckt extends WebSocket implements SessionStub
{
	private final static Map<String, WebSckt> sockets = new HashMap<>();
//	private Server server;
	WebSession session;
	private String sessionid;
	private PingService pingService;
	
	public WebSckt(Server server, IHTTPSession handshakeRequest)
	{
		super(handshakeRequest);
//		this.server = server;
		setSession(Server.getSession(handshakeRequest.getCookies().read("session")));
		Log.info("websocket created for session "+session);
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
				this.session.lastAction = new Date();
				switch (jso.getString("cmd", "unknown"))
				{
					case "Global.setProperty":
					{
						new GlobalWS(this).setProperty(jso);
						break;
					}
					case "Profile.load":
					{
						new ProfileWS(this).load(jso);
						break;
					}
					case "Profile.scan":
					{
						new ProfileWS(this).scan(jso);
						break;
					}
					case "Profile.fix":
					{
						new ProfileWS(this).fix(jso);
						break;
					}
					case "Profile.setProperty":
					{
						new ProfileWS(this).setProperty(jso);
						break;
					}
					case "ReportLite.setFilter":
					{
						new ReportWS(this).setFilter(jso,true);
						break;
					}
					case "Report.setFilter":
					{
						new ReportWS(this).setFilter(jso,false);
						break;
					}
					case "CatVer.load":
					{
						new CatVerWS(this).load(jso);
						break;
					}
					case "NPlayers.load":
					{
						new NPlayersWS(this).load(jso);
						break;
					}
					case "Progress.cancel":
					{
						if (session.worker != null && session.worker.isAlive() && session.worker.progress != null)
							session.worker.progress.cancel();
						break;
					}
					case "Dat2Dir.start":
					{
						new Dat2DirWS(this).start(jso);
						break;
					}
					case "Dir2Dat.start":
					{
						new Dir2DatWS(this).start(jso);
						break;
					}
					case "TrntChk.start":
					{
						new TrntChkWS(this).start(jso);
						break;
					}
					case "Compressor.start":
					{
						new CompressorWS(this).start(jso);
						break;
					}
					case "Dat2Dir.settings":
					{
						new Dat2DirWS(this).settings(jso);
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
			Log.err(e.getMessage(),e);
		}
	}

	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote)
	{
		Log.info("websocket close for session "+sessionid);
		try
		{
			pingService.close();
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
		if(session!=null)
			unsetSession(session);
	}

	@Override
	protected void onException(IOException e)
	{

	}

	@Override
	protected void onOpen()
	{
		Log.info("websocket opened for session "+sessionid);
		pingService = new PingService();
		if(session.curr_profile!=null)
		{
			new ProfileWS(this).loaded(session.curr_profile);
			new CatVerWS(this).loaded(session.curr_profile);
			new NPlayersWS(this).loaded(session.curr_profile);
		}
		if(session.worker != null && session.worker.isAlive())
		{
			if(session.worker.progress!=null)
			{
				session.worker.progress.reload(this);
			}
		}
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
				Log.trace("sent ping");
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
			Log.trace("rec pong");
			pong++;
		}

		@Override
		public void close() throws IOException
		{
			service.shutdownNow();
		}
	}
	
	private void saveSettings()
	{
		if(session!=null)
		{
			if (session.curr_profile != null)
				session.curr_profile.saveSettings();
			session.getUser().settings.saveSettings();
			session = null;
		}
	}
	
	public static void saveAllSettings()
	{
		sockets.forEach((id,socket)->socket.saveSettings());
	}

	@Override
	public void setSession(WebSession session)
	{
		if(session==null)
			throw new NullPointerException("Session not found");
		sockets.put(this.sessionid=session.getSessionId(), this);
		this.session = session;
		
	}

	@Override
	public void unsetSession(WebSession session)
	{
		saveSettings();
		sockets.remove(session.getSessionId());
//		server.unsetSession(session);
	}


}