package jrm.server.handlers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Locale.LanguageRange;

import com.eclipsesource.json.JsonObject;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jrm.locale.Messages;
import jrm.server.shared.WebSession;
import lombok.val;

@SuppressWarnings("serial")
public abstract class AbstractSessionServlet extends HttpServlet
{
	/**
	 * @param req
	 * @param resp
	 * @param jso
	 * @throws IOException
	 */
	protected void fillAndSendJSO(HttpServletRequest req, HttpServletResponse resp, JsonObject jso) throws IOException
	{
		val ws = (WebSession)req.getSession().getAttribute("session");
		val sessionid = req.getSession().getId();
		jso.add("session", sessionid);
		final var msgs = new JsonObject();
		List<LanguageRange> lr = LanguageRange.parse(req.getHeader("accept-language"));
		ws.setMsgs(Messages.loadBundle(!lr.isEmpty() ? Locale.lookup(lr, Arrays.asList(Locale.getAvailableLocales())) : Locale.getDefault()));
		ResourceBundle rb = ws.getMsgs();
		rb.keySet().forEach(k -> {
			if (k != null && !k.isEmpty())
				msgs.add(k, rb.getString(k));
		});
		jso.add("msgs", msgs);
		jso.add("settings", ws.getUser().getSettings().asJSO());
		final var jsonStr = jso.toString();
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/json");
		resp.setContentLength(jsonStr.getBytes("UTF-8").length);
		resp.getWriter().write(jsonStr);
		resp.getWriter().flush();
	}
}
