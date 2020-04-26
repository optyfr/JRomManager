package jrm.fullserver.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import jrm.fullserver.datasources.AdminXMLResponse;
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
					WebSession sess = (WebSession) req.getSession().getAttribute("session");
					TempFileInputStream response = null;
					int bodylen = req.getContentLength();
					switch (req.getRequestURI())
					{
						case "/datasources/admin":
							response = new AdminXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/profilesTree":
							response = new ProfilesTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/profilesList":
							response = new ProfilesListXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/remoteFileChooser":
							response = new RemoteFileChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/remoteRootChooser":
							response = new RemoteRootChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/CatVer":
							response = new CatVerXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/NPlayers":
							response = new NPlayersXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/AnywareListList":
							response = new AnywareListListXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/AnywareList":
							response = new AnywareListXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/Anyware":
							response = new AnywareXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/Report":
							response = new ReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/BatchDat2DirSrc":
							response = new BatchDat2DirSrcXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/BatchDat2DirSDR":
							response = new BatchDat2DirSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/BatchDat2DirResult":
							response = new BatchDat2DirResultXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/BatchTrntChkSDR":
							response = new BatchTrntChkSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/BatchTrntChkReportTree":
							response = new BatchTrntChkReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						case "/datasources/BatchCompressorFR":
							response = new BatchCompressorFRXMLResponse(new XMLRequest(sess, new BufferedInputStream(req.getInputStream()), bodylen)).processRequest();
							break;
						default:
							resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
							break;
					}
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
		
}
