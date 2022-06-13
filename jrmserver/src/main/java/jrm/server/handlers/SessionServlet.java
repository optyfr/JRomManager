package jrm.server.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eclipsesource.json.JsonObject;

import jrm.misc.Log;

@SuppressWarnings("serial")
public class SessionServlet extends AbstractSessionServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			fillAndSendJSO(req, resp, new JsonObject());
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

}
