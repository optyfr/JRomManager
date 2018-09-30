package jrm.server;

import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;

/**
 * Handling error 500 - internal server error
 */
public class Error500UriHandler extends DefaultHandler
{
	private Exception e;

	public Error500UriHandler(Exception e)
	{
		this.e = e;
	}

	public String getText()
	{
		return "<html><body><h3>Error 500: " + e.getMessage() + ".</h3></body></html>";
	}

	@Override
	public String getMimeType()
	{
		return "text/html";
	}

	@Override
	public IStatus getStatus()
	{
		return Status.INTERNAL_ERROR;
	}
}