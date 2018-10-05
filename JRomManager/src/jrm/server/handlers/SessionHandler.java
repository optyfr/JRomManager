package jrm.server.handlers;

import java.util.*;
import java.util.Locale.LanguageRange;

import com.eclipsesource.json.JsonObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.locale.Messages;
import jrm.security.Session;
import jrm.server.Server;
import jrm.server.SessionStub;

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
		return "text/json";
	}
	
	@SuppressWarnings("serial")
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
			uriResource.initParameter(SessionStub.class).setSession(new Session(sessionid));
			return Server.newFixedLengthResponse(getStatus(), getMimeType(), new JsonObject()
			{{
				add("session", sessionid);
				add("msgs", new JsonObject()
				{{
					List<LanguageRange> lr = LanguageRange.parse(headers.get("accept-language"));
					ResourceBundle rb = Messages.loadBundle(lr.size() > 0 ? Locale.lookup(lr, Arrays.asList(Locale.getAvailableLocales())) : Locale.getDefault());
					rb.keySet().forEach(k -> {
						if (k != null && !k.isEmpty())
							add(k, rb.getString(k));
					});
				}});
			}}.toString());
		}
		catch (Exception e)
		{
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
	}
	
}