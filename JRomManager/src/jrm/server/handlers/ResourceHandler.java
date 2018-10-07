package jrm.server.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.server.Server;

/**
 * General nanolet to print debug info's as a html page.
 */
public class ResourceHandler extends DefaultHandler
{

	@Override
	public String getText()
	{
		throw new IllegalStateException("this method should not be called");
	}

	@Override
	public String getMimeType()
	{
		throw new IllegalStateException("this method should not be called");
	}

	@Override
	public IStatus getStatus()
	{
		return Status.OK;
	}

	private static SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z", Locale.US);
	static {
		gmtFrmt.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT")));
	}
	
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		String baseUri = uriResource.getUri();
		String realUri = Server.normalizeUri(session.getUri());
		for (int index = 0; index < Math.min(baseUri.length(), realUri.length()); index++)
		{
			if (baseUri.charAt(index) != realUri.charAt(index))
			{
				realUri = Server.normalizeUri(realUri.substring(index));
				break;
			}
		}
		URL fileOrdirectory = uriResource.initParameter(URL.class);
		try
		{
			fileOrdirectory = new URL(fileOrdirectory, realUri);
		}
		catch (MalformedURLException e1)
		{
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, null, null);
		}
		try
		{
			URLConnection connection = fileOrdirectory.openConnection();
			if(connection.getContentLength()==0)
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, null, null);
			try
			{
				String ifModifiedSince = session.getHeaders().get("if-modified-since");
				if (ifModifiedSince != null && gmtFrmt.parse(ifModifiedSince).getTime() / 1000 == connection.getLastModified() / 1000)
					return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, null, null);
			}
			catch (ParseException e)
			{
			}
			Response response = NanoHTTPD.newFixedLengthResponse(getStatus(), Server.getMimeTypeForFile(fileOrdirectory.getFile()), new BufferedInputStream(connection.getInputStream()), connection.getContentLengthLong());
			response.addHeader("Date", gmtFrmt.format(new Date(connection.getLastModified())));
			response.addHeader("Last-Modified", gmtFrmt.format(new Date(connection.getLastModified())));
			response.addHeader("Cache-Control", "max-age=86400");
			return response;

		}
		catch (IOException ioe)
		{
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.REQUEST_TIMEOUT, "text/plain", null);
		}
	}
}