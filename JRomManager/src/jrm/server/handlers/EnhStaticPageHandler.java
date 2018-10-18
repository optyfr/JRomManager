package jrm.server.handlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;

/**
 * General nanolet to print debug info's as a html page.
 */
public class EnhStaticPageHandler extends DefaultHandler
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

	private static SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z", Locale.US);
	static {
		gmtFrmt.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT")));
	}
	
	private static synchronized String dateFormat(final Date date)
	{
		return gmtFrmt.format(date);
	}
	
	private static synchronized Date dateParse(final String str) throws ParseException
	{
		return gmtFrmt.parse(str);
	}
	
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		String baseUri = uriResource.getUri();
		String realUri = RouterNanoHTTPD.normalizeUri(session.getUri());
		for (int index = 0; index < Math.min(baseUri.length(), realUri.length()); index++)
		{
			if (baseUri.charAt(index) != realUri.charAt(index))
			{
				realUri = RouterNanoHTTPD.normalizeUri(realUri.substring(index));
				break;
			}
		}
		File fileOrdirectory = uriResource.initParameter(File.class);
		for (String pathPart : getPathArray(realUri))
		{
			fileOrdirectory = new File(fileOrdirectory, pathPart);
		}
		if (fileOrdirectory.isDirectory())
		{
			fileOrdirectory = new File(fileOrdirectory, "index.html");
			if (!fileOrdirectory.exists())
			{
				fileOrdirectory = new File(fileOrdirectory.getParentFile(), "index.htm");
			}
		}
		if (!fileOrdirectory.exists() || !fileOrdirectory.isFile())
		{
			return new Error404UriHandler().get(uriResource, urlParams, session);
		}
		else
		{
			try
			{
				Response response = null;
				try
				{
					String ifModifiedSince = session.getHeaders().get("if-modified-since");
					if (ifModifiedSince != null)
					{
						//System.out.println(fileOrdirectory.getName()+":\n\tif-modified-since="+ifModifiedSince+" ("+(gmtFrmt.parse(ifModifiedSince).getTime() / 1000)+"),\n\tlastmodified="+(fileOrdirectory.lastModified()/1000));
						if(dateParse(ifModifiedSince).getTime() / 1000 == fileOrdirectory.lastModified() / 1000)
							response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, null, null);
					}
				}
				catch (ParseException e)
				{
				}
				if(response == null)
				{
					if (fileOrdirectory.length() > 8192)
					{
						response = NanoHTTPD.newChunkedResponse(getStatus(), NanoHTTPD.getMimeTypeForFile(fileOrdirectory.getName()), fileToInputStream(fileOrdirectory));
						response.setGzipEncoding(true);
					}
					else
						response = NanoHTTPD.newFixedLengthResponse(getStatus(), NanoHTTPD.getMimeTypeForFile(fileOrdirectory.getName()), fileToInputStream(fileOrdirectory), fileOrdirectory.length());
				}
				if(fileOrdirectory.getName().contains("nocache"))
					response.addHeader("Cache-Control", "no-cache");
				else
				{
					String cache_control = session.getHeaders().get("cache-control"); 
					if(cache_control!=null && cache_control.contains("no-cache"))
						response.addHeader("Cache-Control", "must-revalidate");
				}
				response.addHeader("Date", dateFormat(new Date(fileOrdirectory.lastModified())));
				response.addHeader("Last-Modified", dateFormat(new Date(fileOrdirectory.lastModified())));
				return response;
			}
			catch (IOException ioe)
			{
				return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.REQUEST_TIMEOUT, "text/plain", null);
			}
		}
	}

	protected BufferedInputStream fileToInputStream(File fileOrdirectory) throws IOException
	{
		return new BufferedInputStream(new FileInputStream(fileOrdirectory));
	}
}
