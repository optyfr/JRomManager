package jrm.server.handlers;

import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.misc.Log;
import jrm.server.Server;
import jrm.server.WebSession;

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
			final Map<String, String> headers = session.getHeaders();
			final String bodylenstr = headers.get("content-length");
			if (bodylenstr != null)
			{
				long bodylen = Long.parseLong(bodylenstr);
				WebSession sess = Server.getSession(session.getCookies().read("session"));
				if (headers.get("content-type").equals("text/xml"))
				{
					switch (urlParams.get("action"))
					{
						case "lpr":
							break;
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
	
	@Override
	public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			final Map<String, String> headers = session.getHeaders();
			final String bodylenstr = headers.get("content-length");
			if (bodylenstr != null)
			{
				long bodylen = Long.parseLong(bodylenstr);
				WebSession sess = Server.getSession(session.getCookies().read("session"));
				if (headers.get("content-type").equals("text/xml"))
				{
					switch (urlParams.get("action"))
					{
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
