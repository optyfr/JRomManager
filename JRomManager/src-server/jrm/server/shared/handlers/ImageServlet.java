package jrm.server.shared.handlers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import jrm.misc.URIUtils;
import lombok.val;

@SuppressWarnings("serial")
public class ImageServlet extends HttpServlet
{
	private static URI uri = null;
	private static Boolean isModule = null;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		if(isModule==null)
		{
			uri = URI.create("jrt:/res.icons/jrm/resicons/");
			if (!(isModule = URIUtils.URIExists(uri)))
			{
				try
				{
					uri = ImageServlet.class.getResource("/jrm/resicons/").toURI();
				}
				catch (URISyntaxException e)
				{
					resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Icons resource missing?");
					return;
				}
			}
		}
		val url = uri.resolve(req.getRequestURI().substring(8)).toURL();
		try
		{
			val urlconn = url.openConnection();
			urlconn.setDoInput(true);
			if (urlconn.getContentLength() == 0)
			{
				resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Empty result");
				return;
			}
			try
			{
				String ifModifiedSince = req.getHeader("if-modified-since");
				if (ifModifiedSince != null && dateParse(ifModifiedSince).getTime() / 1000 == urlconn.getLastModified() / 1000)
				{
					resp.sendError(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				}
			}
			catch (ParseException e)
			{
			}
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentLengthLong(urlconn.getContentLengthLong());
			resp.setContentType(urlconn.getContentType());
			resp.setDateHeader("Last-Modified", urlconn.getLastModified());
			resp.setHeader("Cache-Control", "max-age=86400");
			IOUtils.copy(urlconn.getInputStream(), resp.getOutputStream());
		}
		catch (Exception e)
		{
			resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		}
	}

	private static SimpleDateFormat gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z", Locale.US);
	static
	{
		gmtFrmt.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT")));
	}

	private static synchronized Date dateParse(final String str) throws ParseException
	{
		return gmtFrmt.parse(str);
	}
}
