package jrm.fullserver.lpr;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.Json;

import jrm.server.shared.WebSession;
import jrm.server.shared.actions.ActionsMgr;

public class LongPollingReqMgr implements ActionsMgr
{
	private final static Map<String, LongPollingReqMgr> cmds = new HashMap<>();

	private WebSession session;

	public LongPollingReqMgr(WebSession session)
	{
		setSession(session);
	}
	
	public void process(String msg)
	{
		System.err.println(msg);
		processActions(this, Json.parse(msg).asObject());
	}

	@Override
	public void setSession(WebSession session)
	{
		if(session==null)
			throw new NullPointerException("Session not found");
		cmds.put(session.getSessionId(), this);
		this.session = session;
	}

	@Override
	public void unsetSession(WebSession session)
	{
		saveSettings();
		cmds.remove(session.getSessionId());
	}

	@Override
	public void send(String msg) throws IOException
	{
		session.lprMsg.add(msg);
	}

	@Override
	public boolean isOpen()
	{
		return true;
	}

	@Override
	public WebSession getSession()
	{
		return session;
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
		cmds.forEach((id,socket)->socket.saveSettings());
	}


}
