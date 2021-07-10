package jrm.server.shared.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jrm.server.shared.WebSession;
import jrm.server.shared.actions.CatVerActions;
import jrm.server.shared.actions.NPlayersActions;
import jrm.server.shared.actions.ProfileActions;
import jrm.server.shared.lpr.LongPollingReqMgr;
import lombok.val;

@SuppressWarnings("serial")
public class ActionServlet extends HttpServlet
{

	private static final String APPLICATION_JSON = "application/json";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			if ("/actions/cmd".equals(req.getRequestURI()))
			{
				if (req.getContentLengthLong() < 0)
					resp.setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);
				else if (req.getContentLength() < 0)
					resp.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
				else if (req.getContentLength() > 0)
				{
					if (req.getContentType().equalsIgnoreCase(APPLICATION_JSON))
					{
						final var buf = new byte[req.getContentLength()];
						req.getInputStream().read(buf, 0, req.getContentLength());
						new LongPollingReqMgr((WebSession) req.getSession().getAttribute("session")).process(new String(buf, StandardCharsets.UTF_8));
						resp.setContentLength(0);
						resp.setContentType(APPLICATION_JSON);
						resp.setStatus(HttpServletResponse.SC_OK);
					}
					else
						resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				}
				else
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
			else
				resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		catch (Exception e)
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException
	{
		try
		{
			WebSession sess = (WebSession) req.getSession().getAttribute("session");
			switch (req.getRequestURI())
			{
				case "/actions/init":
				{
					doInit(sess);
					doLPR(resp, sess);
					break;
				}
				case "/actions/lpr":
				{
					doLPR(resp, sess);
					break;
				}
				default:
					resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
					break;
			}
		}
		catch (IOException e)
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * @param resp
	 * @param sess
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void doInit(WebSession sess)
	{
		final var cmd = new LongPollingReqMgr(sess);
		if (sess.curr_profile != null)
		{
			new ProfileActions(cmd).loaded(sess.curr_profile);
			new CatVerActions(cmd).loaded(sess.curr_profile);
			new NPlayersActions(cmd).loaded(sess.curr_profile);
		}
		if (sess.getWorker() != null && sess.getWorker().isAlive() && sess.getWorker().progress != null)
			sess.getWorker().progress.reload(cmd);
	}

	/**
	 * @param resp
	 * @param sess
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void doLPR(HttpServletResponse resp, WebSession sess) throws IOException
	{
		if (!WebSession.isTerminate())
		{
			try
			{
				val msgs = new ArrayList<String>();
				var msg = sess.getLprMsg().poll(20, TimeUnit.SECONDS);
				if (msg == null && WebSession.isTerminate())
					resp.setStatus(HttpServletResponse.SC_GONE);
				else
				{
					msgs.add(msg);
					while (msgs.size() <= 100)
					{
						if (null == (msg = sess.getLprMsg().poll()))
							break;
						msgs.add(msg);
					}
					msg = encapsulate(msgs);
					sendResp(resp, msg);
				}
			}
			catch (InterruptedException e)
			{
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				Thread.currentThread().interrupt();
			}
		}
		else
			resp.setStatus(HttpServletResponse.SC_GONE);
	}

	/**
	 * @param resp
	 * @param msg
	 * @throws IOException
	 */
	private void sendResp(HttpServletResponse resp, String msg) throws IOException
	{
		resp.setContentType(APPLICATION_JSON);
		resp.setStatus(HttpServletResponse.SC_OK);
		if (msg != null)
		{
			resp.setContentLength(msg.getBytes(StandardCharsets.UTF_8).length);
			resp.getWriter().write(msg);
		}
		else
			resp.setContentLength(0);
	}

	/**
	 * @param msgs
	 * @return
	 */
	private String encapsulate(final java.util.ArrayList<java.lang.String> msgs)
	{
		String msg;
		if (msgs.size() > 1)
			msg = "{\"cmd\":\"Global.multiCMD\",\"params\":[" + msgs.stream().collect(Collectors.joining(",")) + "]}";
		else
			msg = msgs.get(0);
		return msg;
	}
}
