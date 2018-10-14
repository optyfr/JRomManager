package jrm.server;

import java.util.Date;

import jrm.security.Session;
import jrm.server.ws.Worker;

public class WebSession extends Session
{
	public Worker worker = null;
	public Date lastAction = new Date();
	
	public WebSession(String sessionId)
	{
		super(sessionId);
	}

}
