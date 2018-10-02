package jrm.server.handlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.server.Server;

/**
 * General nanolet to print debug info's as a html page.
 */
public class ResourceHandler extends DefaultHandler
{

	private static String[] getPathArray(String uri)
	{
		String array[] = uri.split("/");
		ArrayList<String> pathArray = new ArrayList<String>();

		for (String s : array)
		{
			if (s.length() > 0)
				pathArray.add(s);
		}

		return pathArray.toArray(new String[] {});

	}

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
		for (String pathPart : getPathArray(realUri))
		{
			try
			{
				fileOrdirectory = new URL(fileOrdirectory, pathPart);
				if(!new File(fileOrdirectory.toURI()).exists())
					return new Error404UriHandler().get(uriResource, urlParams, session);
			}
			catch (MalformedURLException | URISyntaxException e)
			{
				return new Error404UriHandler().get(uriResource, urlParams, session);
			}
		}
		try
		{
			return NanoHTTPD.newChunkedResponse(getStatus(), Server.getMimeTypeForFile(fileOrdirectory.getFile()), URLToInputStream(fileOrdirectory));
		}
		catch (IOException ioe)
		{
			return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.REQUEST_TIMEOUT, "text/plain", null);
		}
	}

	protected BufferedInputStream URLToInputStream(URL fileOrdirectory) throws IOException
	{
		return new BufferedInputStream(fileOrdirectory.openStream());
	}
}