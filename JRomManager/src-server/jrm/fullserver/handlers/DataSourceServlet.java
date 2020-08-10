package jrm.fullserver.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jrm.fullserver.datasources.AdminXMLResponse;
import jrm.server.shared.TempFileInputStream;
import jrm.server.shared.WebSession;
import jrm.server.shared.datasources.XMLRequest;

@SuppressWarnings("serial")
public class DataSourceServlet extends jrm.server.shared.handlers.DataSourceServlet
{
	@Override
	protected TempFileInputStream processResponse(WebSession sess, HttpServletRequest req, HttpServletResponse resp) throws IOException, Exception
	{
		int bodylen = req.getContentLength();
		switch (req.getRequestURI())
		{
			case "/datasources/admin":
				return new AdminXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			default:
				return super.processResponse(sess, req, resp);
		}
	}
	
}
