package jrm.fullserver.handlers;

import java.io.BufferedInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jrm.fullserver.datasources.AdminXMLResponse;
import jrm.server.shared.TempFileInputStream;
import jrm.server.shared.WebSession;
import jrm.server.shared.datasources.XMLRequest;
import jrm.server.shared.handlers.DataSourceServlet;

@SuppressWarnings("serial")
public class FullDataSourceServlet extends DataSourceServlet
{
	@Override
	protected TempFileInputStream processResponse(WebSession sess, HttpServletRequest req, HttpServletResponse resp) throws Exception
	{
		if ("/datasources/admin".equals(req.getRequestURI()))
		{
			try (final var in = new BufferedInputStream(req.getInputStream()))
			{
				try(final var response = new AdminXMLResponse(new XMLRequest(sess, in, req.getContentLength())))
				{
					return response.processRequest();
				}
			}
		}
		else
			return super.processResponse(sess, req, resp);
	}

}
