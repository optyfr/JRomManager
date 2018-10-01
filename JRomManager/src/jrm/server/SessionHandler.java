package jrm.server;

import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

public class SessionHandler extends DefaultHandler
{
	String sessionid = UUID.randomUUID().toString();
	
	@Override
	public String getText()
	{
		return sessionid;
	}

	@Override
	public IStatus getStatus()
	{
		return Status.OK;
	}

	@Override
	public String getMimeType()
	{
		return "text/plain";
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
				int bodylen = Integer.parseInt(bodylenstr);
				session.getInputStream().skip(bodylen);
			}
			session.getCookies().set("session", sessionid, 1);
			uriResource.initParameter(SessionStub.class).setSession(sessionid);
			return Server.newFixedLengthResponse(getStatus(), getMimeType(), sessionid);
		}
		catch (Exception e)
		{
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
	}
	
}