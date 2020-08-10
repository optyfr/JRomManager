package jrm.server.shared.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import jrm.server.shared.TempFileInputStream;
import jrm.server.shared.WebSession;
import jrm.server.shared.datasources.AnywareListListXMLResponse;
import jrm.server.shared.datasources.AnywareListXMLResponse;
import jrm.server.shared.datasources.AnywareXMLResponse;
import jrm.server.shared.datasources.BatchCompressorFRXMLResponse;
import jrm.server.shared.datasources.BatchDat2DirResultXMLResponse;
import jrm.server.shared.datasources.BatchDat2DirSDRXMLResponse;
import jrm.server.shared.datasources.BatchDat2DirSrcXMLResponse;
import jrm.server.shared.datasources.BatchTrntChkReportTreeXMLResponse;
import jrm.server.shared.datasources.BatchTrntChkSDRXMLResponse;
import jrm.server.shared.datasources.CatVerXMLResponse;
import jrm.server.shared.datasources.NPlayersXMLResponse;
import jrm.server.shared.datasources.ProfilesListXMLResponse;
import jrm.server.shared.datasources.ProfilesTreeXMLResponse;
import jrm.server.shared.datasources.RemoteFileChooserXMLResponse;
import jrm.server.shared.datasources.RemoteRootChooserXMLResponse;
import jrm.server.shared.datasources.ReportTreeXMLResponse;
import jrm.server.shared.datasources.XMLRequest;

@SuppressWarnings("serial")
public class DataSourceServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		try
		{
			if (req.getContentLengthLong() < 0)
				resp.setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);
			else if (req.getContentLength() < 0)
				resp.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
			else if (req.getContentLength() > 0)
			{
				if (req.getContentType().equalsIgnoreCase("text/xml"))
				{
					TempFileInputStream response = processResponse((WebSession) req.getSession().getAttribute("session"), req, resp);
					if(response!=null)
					{
						resp.setContentType("text/xml");
						resp.setStatus(HttpServletResponse.SC_OK);
						resp.setContentLengthLong(response.getLength());
						IOUtils.copy(response, resp.getOutputStream());
					}
					else if(resp.getStatus()==HttpServletResponse.SC_OK)
						resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
		
	protected TempFileInputStream processResponse(WebSession sess, HttpServletRequest req, HttpServletResponse resp) throws IOException, Exception
	{
		int bodylen = req.getContentLength();
		switch (req.getRequestURI())
		{
			case "/datasources/profilesTree":
				return new ProfilesTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/profilesList":
				return new ProfilesListXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/remoteFileChooser":
				return new RemoteFileChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/remoteRootChooser":
				return new RemoteRootChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/CatVer":
				return new CatVerXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/NPlayers":
				return new NPlayersXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/AnywareListList":
				return new AnywareListListXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/AnywareList":
				return new AnywareListXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/Anyware":
				return new AnywareXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/Report":
				return new ReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/BatchDat2DirSrc":
				return new BatchDat2DirSrcXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/BatchDat2DirSDR":
				return new BatchDat2DirSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/BatchDat2DirResult":
				return new BatchDat2DirResultXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/BatchTrntChkSDR":
				return new BatchTrntChkSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/BatchTrntChkReportTree":
				return new BatchTrntChkReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			case "/datasources/BatchCompressorFR":
				return new BatchCompressorFRXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
			default:
				resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		return null;
	}
}
