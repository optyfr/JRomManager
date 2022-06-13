package jrm.fullserver.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eclipsesource.json.JsonObject;

import jrm.misc.Log;
import jrm.server.handlers.AbstractSessionServlet;
import jrm.server.shared.WebSession;

@SuppressWarnings("serial")
public class SessionServlet extends AbstractSessionServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			final var jso = new JsonObject();
			jso.add("authenticated", true);
			final var ws = (WebSession) req.getSession().getAttribute("session");
			jso.add("admin", ws.getUser().isAdmin());
			fillAndSendJSO(req, resp, jso);
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
