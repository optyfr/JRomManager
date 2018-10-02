package jrm.server.handlers;

import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;

/**
 * Handling index
 */
public class IndexHandler extends DefaultHandler
{

	@Override
	public String getText()
	{
		return "<!DOCTYPE html><HTML>\r\n" +
				"<HEAD>\r\n" +
				"<META http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>\r\n" +
				"<link rel=\"shortcut icon\" type=\"image/png\" href=\"/images/rom_small.png\"/>\r\n" +
				"<title></title>\r\n" +
				"</HEAD>\r\n" +
				"<BODY>\r\n" +
				"<SCRIPT type='text/javascript'>\r\n" +
				"	window.isc_useSimpleNames = false;\r\n" +
				"	var isomorphicDir=\"/smartgwt/sc/\";\r\n" +
				"	var isomorphicSkin='Enterprise';\r\n" +
				"</SCRIPT>\r\n" +
				"<script type=\"text/javascript\" src=\"smartgwt/smartgwt.nocache.js\"></script>\r\n" +
				"</BODY>\r\n" +
				"</HTML>";
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