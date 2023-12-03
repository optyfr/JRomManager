package jrm.server.handlers;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
