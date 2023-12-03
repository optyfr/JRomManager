package jrm.server.shared.handlers;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jrm.misc.Log;
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
import jrm.server.shared.datasources.XMLResponse;

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
			else if (req.getContentLength() > 0 && req.getContentType().equalsIgnoreCase("text/xml"))
			{
				TempFileInputStream response = processResponse((WebSession) req.getSession().getAttribute("session"), req, resp);
				if (response != null)
				{
					resp.setContentType("text/xml");
					resp.setStatus(HttpServletResponse.SC_OK);
					resp.setContentLengthLong(response.getLength());
					IOUtils.copy(response, resp.getOutputStream());
				}
				else if (resp.getStatus() == HttpServletResponse.SC_OK)
					resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}

		}
		catch (IOException e)
		{
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	protected TempFileInputStream processResponse(WebSession sess, HttpServletRequest req, HttpServletResponse resp) throws IOException, XMLStreamException
	{
		int bodylen = req.getContentLength();
		XMLResponse response = null;
		try(final var in = new BufferedInputStream(req.getInputStream()))
		{
			switch (req.getRequestURI())
			{
				case "/datasources/profilesTree":
					response = new ProfilesTreeXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/profilesList":
					response = new ProfilesListXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/remoteFileChooser":
					response = new RemoteFileChooserXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/remoteRootChooser":
					response = new RemoteRootChooserXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/CatVer":
					response = new CatVerXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/NPlayers":
					response = new NPlayersXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/AnywareListList":
					response = new AnywareListListXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/AnywareList":
					response = new AnywareListXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/Anyware":
					response = new AnywareXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/Report":
					response = new ReportTreeXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/BatchDat2DirSrc":
					response = new BatchDat2DirSrcXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/BatchDat2DirSDR":
					response = new BatchDat2DirSDRXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/BatchDat2DirResult":
					response = new BatchDat2DirResultXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/BatchTrntChkSDR":
					response = new BatchTrntChkSDRXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/BatchTrntChkReportTree":
					response = new BatchTrntChkReportTreeXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				case "/datasources/BatchCompressorFR":
					response = new BatchCompressorFRXMLResponse(new XMLRequest(sess, in, bodylen));
					break;
				default:
					break;
			}
			if (response != null)
			{
				return response.processRequest();
			}
			else
				resp.setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
		}
		finally
		{
			if(response!=null)
				response.close();
		}
		return null;
	}
}
