package jrm.fullserver.handlers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jrm.server.lpr.LongPollingReqMgr;
import jrm.server.shared.WebSession;
import jrm.server.shared.actions.CatVerActions;
import jrm.server.shared.actions.NPlayersActions;
import jrm.server.shared.actions.ProfileActions;
import lombok.val;

@SuppressWarnings("serial")
public class ActionServlet extends HttpServlet
{

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			switch (req.getRequestURI())
			{
				case "/actions/cmd":
					if (req.getContentLengthLong() < 0)
						resp.setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);
					else if (req.getContentLength() < 0)
						resp.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
					else if (req.getContentLength() > 0)
					{
						if (req.getContentType().equalsIgnoreCase("application/json"))
						{
							byte[] buf = new byte[req.getContentLength()];
							req.getInputStream().read(buf, 0, req.getContentLength());
							new LongPollingReqMgr((WebSession) req.getSession().getAttribute("session")).process(new String(buf, StandardCharsets.UTF_8));
							resp.setContentLength(0);
							resp.setContentType("application/json");
							resp.setStatus(HttpServletResponse.SC_OK);
						}
						else
							resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					}
					else
						resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					break;
				default:
					resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
					break;
			}
		}
		catch (Exception e)
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			WebSession sess = (WebSession) req.getSession().getAttribute("session");
			switch (req.getRequestURI())
			{
				case "/actions/init":
				{
					LongPollingReqMgr cmd = new LongPollingReqMgr(sess);
					if (sess.curr_profile != null)
					{
						new ProfileActions(cmd).loaded(sess.curr_profile);
						new CatVerActions(cmd).loaded(sess.curr_profile);
						new NPlayersActions(cmd).loaded(sess.curr_profile);
					}
					if (sess.getWorker() != null && sess.getWorker().isAlive())
						if (sess.getWorker().progress != null)
							sess.getWorker().progress.reload(cmd);
					if(!WebSession.isTerminate())
					{
						val msg = sess.lprMsg.poll(20, TimeUnit.SECONDS);
						if(msg == null && WebSession.isTerminate())
							resp.setStatus(HttpServletResponse.SC_GONE);
						else
						{
							resp.setContentType("application/json");
							resp.setStatus(HttpServletResponse.SC_OK);
							if (msg != null)
							{
								resp.setContentLength(msg.getBytes(StandardCharsets.UTF_8).length);
								resp.getWriter().write(msg);
							}
							else
								resp.setContentLength(0);
						}
					}
					else
						resp.setStatus(HttpServletResponse.SC_GONE);
					break;
				}
				case "/actions/lpr":
				{
					if(!WebSession.isTerminate())
					{
						val msg = sess.lprMsg.poll(20, TimeUnit.SECONDS);
						if(msg == null && WebSession.isTerminate())
							resp.setStatus(HttpServletResponse.SC_GONE);
						else
						{
							resp.setContentType("application/json");
							resp.setStatus(HttpServletResponse.SC_OK);
							if (msg != null)
							{
								resp.setContentLength(msg.getBytes(StandardCharsets.UTF_8).length);
								resp.getWriter().write(msg);
							}
							else
								resp.setContentLength(0);
						}
					}
					else
						resp.setStatus(HttpServletResponse.SC_GONE);
					break;
				}
				default:
					resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
					break;
			}
		}
		catch (InterruptedException e)
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
