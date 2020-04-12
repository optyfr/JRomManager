package jrm.server.ws;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.eclipsesource.json.Json;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import jrm.misc.Log;
import jrm.server.Server;
import jrm.server.shared.WebSession;
import jrm.server.shared.actions.CatVerActions;
import jrm.server.shared.actions.ActionsMgr;
import jrm.server.shared.actions.NPlayersActions;
import jrm.server.shared.actions.ProfileActions;

public class WebScktMgr extends WebSocket implements ActionsMgr
{
	private final static Map<String, WebScktMgr> sockets = new HashMap<>();
//	private Server server;
	WebSession session;
	private String sessionid;
	private PingService pingService;
	
	public WebScktMgr(Server server, IHTTPSession handshakeRequest)
	{
		super(handshakeRequest);
//		this.server = server;
		setSession(Server.getSession(handshakeRequest.getCookies().read("session")));
		Log.info("websocket created for session "+session);
	}
/*
	private static WebSckt get(Session session)
	{
		return sockets.get(session.getSessionId());
	}
*/	
	@Override
	protected void onPong(WebSocketFrame pongFrame)
	{
		pingService.pong();
	}

	
	@Override
	protected void onMessage(WebSocketFrame messageFrame)
	{
		processActions(this,Json.parse(messageFrame.getTextPayload()).asObject());
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
			new ProfileActions(this).loaded(session.curr_profile);
			new CatVerActions(this).loaded(session.curr_profile);
			new NPlayersActions(this).loaded(session.curr_profile);
		}
		if(session.getWorker() != null && session.getWorker().isAlive())
		{
			if(session.getWorker().progress!=null)
			{
				session.getWorker().progress.reload(this);
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
				WebScktMgr.this.ping(PAYLOAD);
				Log.trace("sent ping");
				ping++;
				if (ping - pong > 3)
					WebScktMgr.this.close(CloseCode.GoingAway, "Missed too many ping requests.", false);
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
			session.getUser().getSettings().saveSettings();
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

	@Override
	public WebSession getSession()
	{
		return session;
	}


}