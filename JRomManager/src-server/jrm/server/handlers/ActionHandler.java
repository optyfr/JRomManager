package jrm.server.handlers;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.misc.Log;
import jrm.server.Server;
import jrm.server.lpr.LongPollingReqMgr;
import jrm.server.shared.WebSession;
import jrm.server.shared.actions.CatVerActions;
import jrm.server.shared.actions.NPlayersActions;
import jrm.server.shared.actions.ProfileActions;

public class ActionHandler extends DefaultHandler
{
	@Override
	public String getText()
	{
		return "";
	}

	@Override
	public Status getStatus()
	{
		return Status.OK;
	}

	@Override
	public String getMimeType()
	{
		return "text/xml";
	}

	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			WebSession sess = Server.getSession(session.getCookies().read("session"));
			if(sess!=null)
			{
				switch (urlParams.get("action"))
				{
					case "init":
					{
						LongPollingReqMgr cmd = new LongPollingReqMgr(sess);
						if(sess.curr_profile!=null)
						{
							new ProfileActions(cmd).loaded(sess.curr_profile);
							new CatVerActions(cmd).loaded(sess.curr_profile);
							new NPlayersActions(cmd).loaded(sess.curr_profile);
						}
						if(sess.getWorker() != null && sess.getWorker().isAlive())
							if(sess.getWorker().progress!=null)
								sess.getWorker().progress.reload(cmd);
						return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", sess.lprMsg.poll(20, TimeUnit.SECONDS));
					}
					case "lpr":
						return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", sess.lprMsg.poll(20, TimeUnit.SECONDS));
					default:
						Log.err(urlParams.get("action"));
						break;
				}
			}
			else
				return NanoHTTPD.newFixedLengthResponse(Status.UNAUTHORIZED, "text/plain", "No session");
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(),e);
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
		return new Error404UriHandler().get(uriResource, urlParams, session);
	}
	
	@Override
	public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			final Map<String, String> headers = session.getHeaders();
			final String bodylenstr = headers.get("content-length");
			if (bodylenstr != null)
			{
				int bodylen = Integer.parseInt(bodylenstr);
				WebSession sess = Server.getSession(session.getCookies().read("session"));
				if (headers.get("content-type").equals("application/json"))
				{
					switch (urlParams.get("action"))
					{
						case "cmd":
							byte[] buf = new byte[bodylen];
							session.getInputStream().read( buf, 0, bodylen );
							new LongPollingReqMgr(sess).process(new String(buf,StandardCharsets.UTF_8));
							return NanoHTTPD.newFixedLengthResponse(Status.OK, "application/json", null);
						default:
							Log.err(urlParams.get("action"));
							session.getInputStream().skip(bodylen);
							break;
					}
				}
				else
					session.getInputStream().skip(bodylen);
			}
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(),e);
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
		return new Error404UriHandler().get(uriResource, urlParams, session);
	}
}
