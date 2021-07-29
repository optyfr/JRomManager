package jrm.server;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import jrm.misc.Log;
import jrm.server.shared.WebSession;

public class SessionListener implements HttpSessionListener
{

	@Override
	public void sessionDestroyed(HttpSessionEvent se)
	{
		Log.debug(() -> "Destroying session " + se.getSession().getId());
		WebSession ws = (WebSession) se.getSession().getAttribute("session");
		if (ws != null)
			ws.close();
	}

	@Override
	public void sessionCreated(HttpSessionEvent se)
	{
		Log.debug(() -> "Creating session " + se.getSession().getId());
		se.getSession().setAttribute("session", new WebSession(se.getSession().getId()));
	}

}
