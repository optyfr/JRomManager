package jrm.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class Server extends RouterNanoHTTPD
{
	public Server()
	{
		super(8080);
		addMappings();
	}

	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		ServerRunner.run(Server.class);
	}

	@Override
	public void addMappings()
	{
		super.addMappings();
		addRoute("/", IndexHandler.class);
		addRoute("/index.html", IndexHandler.class);
		addRoute("/smartgwt/(.)+", StaticPageTestHandler.class, new File("E:\\Src\\Java\\JRomManager\\WebClient\\build\\gwt\\out\\smartgwt"));
		addRoute("/images/(.)+", ResourceHandler.class, Server.class.getResource("/jrm/resources/"));
	}

	/**
	 * Handling index
	 */
	public static class IndexHandler extends DefaultHandler
	{

		@Override
		public String getText()
		{
			return "<!DOCTYPE html><HTML>\r\n" + "<HEAD>\r\n" + "<META http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\r\n" + "<title></title>\r\n" + "</HEAD>\r\n" + "<BODY>\r\n" + "<SCRIPT type='text/javascript'>\r\n" + "	window.isc_useSimpleNames = false;\r\n" + "	var isomorphicDir=\"/smartgwt/sc/\";\r\n" + "	var isomorphicSkin='Enterprise';\r\n" + "</SCRIPT>\r\n" + "<script type=\"text/javascript\" src=\"smartgwt/smartgwt.nocache.js\"></script>\r\n" + "</BODY>\r\n" + "</HTML>";
		}

		@Override
		public String getMimeType()
		{
			return "text/html";
		}

		@Override
		public IStatus getStatus()
		{
			return Status.OK;
		}

	}

	public static class StaticPageTestHandler extends StaticPageHandler
	{

		@Override
		protected BufferedInputStream fileToInputStream(File fileOrdirectory) throws IOException
		{
			if ("exception.html".equals(fileOrdirectory.getName()))
			{
				throw new IOException("trigger something wrong");
			}
			return super.fileToInputStream(fileOrdirectory);
		}
	}

	/**
	 * General nanolet to print debug info's as a html page.
	 */
	public static class ResourceHandler extends DefaultHandler
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
			String realUri = normalizeUri(session.getUri());
			for (int index = 0; index < Math.min(baseUri.length(), realUri.length()); index++)
			{
				if (baseUri.charAt(index) != realUri.charAt(index))
				{
					realUri = normalizeUri(realUri.substring(index));
					break;
				}
			}
			URL fileOrdirectory = uriResource.initParameter(URL.class);
			for (String pathPart : getPathArray(realUri))
			{
				try
				{
					fileOrdirectory = new URL(fileOrdirectory, pathPart);
				}
				catch (MalformedURLException e)
				{
					return new Error404UriHandler().get(uriResource, urlParams, session);
				}
			}
			try
			{
				System.out.println(fileOrdirectory);
				return NanoHTTPD.newChunkedResponse(getStatus(), getMimeTypeForFile(fileOrdirectory.getFile()), URLToInputStream(fileOrdirectory));
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
}
