package jrm.fullserver.handlers;

import java.io.IOException;
import java.net.URL;
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

import jrm.fullserver.FullServer;
import lombok.val;

@SuppressWarnings("serial")
public class ImageServlet extends HttpServlet
{
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		val url = new URL(FullServer.class.getResource("/jrm/resicons/"), req.getRequestURI().substring(8));
		val urlconn = url.openConnection();
		urlconn.setDoInput(true);
		try
		{
			if (urlconn.getContentLength() == 0)
			{
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			try
			{
				String ifModifiedSince = req.getHeader("if-modified-since");
				if (ifModifiedSince != null && dateParse(ifModifiedSince).getTime() / 1000 == urlconn.getLastModified() / 1000)
				{
					resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
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
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
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
