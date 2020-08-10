package jrm.server.handlers;

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
			val ws = (WebSession)req.getSession().getAttribute("session");
			val sessionid = req.getSession().getId();
			String msg = new JsonObject()
			{{
				add("session", sessionid);
				add("msgs", new JsonObject()
				{{
					List<LanguageRange> lr = LanguageRange.parse(req.getHeader("accept-language"));
					ResourceBundle rb = ws.msgs = Messages.loadBundle(lr.size() > 0 ? Locale.lookup(lr, Arrays.asList(Locale.getAvailableLocales())) : Locale.getDefault());
					rb.keySet().forEach(k -> {
						if (k != null && !k.isEmpty())
							add(k, rb.getString(k));
					});
				}});
				add("settings", ws.getUser().getSettings().asJSO());
			}}.toString();
			resp.setContentLength(msg.getBytes().length);
			resp.getWriter().write(msg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
