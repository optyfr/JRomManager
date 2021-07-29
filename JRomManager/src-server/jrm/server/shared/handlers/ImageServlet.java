package jrm.server.shared.handlers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import jrm.misc.Log;
import jrm.misc.URIUtils;
import lombok.val;

@SuppressWarnings("serial")
public class ImageServlet extends HttpServlet
{
	private static URI uri = null;
	private static Boolean isModule = null;

	private static URI getURI() throws URISyntaxException
	{
		if (isModule == null)
		{
			uri = URI.create("jrt:/res.icons/jrm/resicons/");
			isModule = URIUtils.URIExists(uri);
			if (!isModule)
				uri = ImageServlet.class.getResource("/jrm/resicons/").toURI();
		}
		return uri;
	}

	private boolean ifModifiedSince(HttpServletRequest req, URLConnection urlconn)
	{
		String ifModifiedSince = req.getHeader("if-modified-since");
		try
		{
			if (ifModifiedSince != null && dateParse(ifModifiedSince).getTime() / 1000 == urlconn.getLastModified() / 1000)
			{
				return false;
			}
		}
		catch (ParseException e)
		{
			// ignore
		}
		return true;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		try
		{
			val url = getURI().resolve(req.getRequestURI().substring(8)).toURL();
			val urlconn = url.openConnection();
			urlconn.setDoInput(true);
			if (urlconn.getContentLength() == 0)
			{
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Empty result");
				return;
			}
			if(!ifModifiedSince(req, urlconn))
			{
				resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLengthLong(urlconn.getContentLengthLong());
			resp.setContentType(urlconn.getContentType());
			resp.setDateHeader("Last-Modified", urlconn.getLastModified());
			resp.setHeader("Cache-Control", "max-age=86400");
			IOUtils.copy(urlconn.getInputStream(), resp.getOutputStream());
		}
		catch (URISyntaxException|IOException e)
		{
			try
			{
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
			}
			catch (IOException e1)
			{
				Log.err(e1.getMessage(), e1);
			}
		}
	}

	private static Date dateParse(final String str) throws ParseException
	{
		final var gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT")));
		return gmtFrmt.parse(str);
	}
}
