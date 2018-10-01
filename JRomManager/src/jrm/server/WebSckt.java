package jrm.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

class WebSckt extends WebSocket implements SessionStub
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
		try
		{
			send("Received "+messageFrame.getTextPayload());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote)
	{
		System.out.println("websocket closed......");
	}

	@Override
	protected void onException(IOException e)
	{

	}

	@Override
	protected void onOpen()
	{
		System.out.println("websocket opened......");
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