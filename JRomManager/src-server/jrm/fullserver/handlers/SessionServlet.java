package jrm.fullserver.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eclipsesource.json.JsonObject;

import jrm.locale.Messages;
import jrm.misc.Log;
import jrm.server.shared.WebSession;

import lombok.val;

@SuppressWarnings("serial")
public class SessionServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("text/json");
			val ws = (WebSession) req.getSession().getAttribute("session");
			val sessionid = req.getSession().getId();
			final var jso = new JsonObject();
			jso.add("session", sessionid);
			jso.add("authenticated", true);
			jso.add("admin", ws.getUser().isAdmin());
			final var msgs = new JsonObject();
			List<LanguageRange> lr = LanguageRange.parse(req.getHeader("accept-language"));
			ws.setMsgs(Messages.loadBundle(!lr.isEmpty() ? Locale.lookup(lr, Arrays.asList(Locale.getAvailableLocales())) : Locale.getDefault()));
			ResourceBundle rb = ws.getMsgs();
			rb.keySet().forEach(k -> {
				if (k != null && !k.isEmpty())
					msgs.add(k, rb.getString(k));
			});
			jso.add("msgs", msgs);
			final var jsonStr = jso.toString();
			resp.setContentLength(jsonStr.getBytes().length);
			resp.getWriter().write(jsonStr);
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
