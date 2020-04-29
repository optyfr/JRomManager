package jrm.server.handlers;

import java.io.BufferedInputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.misc.Log;
import jrm.server.Server;
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

public class DataSourcesHandler extends DefaultHandler
{

	@Override
	public String getText()
	{
		return "not implemented";
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

	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			final Map<String, String> headers = session.getHeaders();
			final String bodylenstr = headers.get("content-length");
			System.out.println(bodylenstr);
			if (bodylenstr != null)
			{
				long bodylen = Long.parseLong(bodylenstr);
				WebSession sess = Server.getSession(session.getCookies().read("session"));
				System.out.println(headers.get("content-type"));
				if (headers.get("content-type").equals("text/xml"))
				{
					TempFileInputStream response = null;
					switch (urlParams.get("action"))
					{
						case "profilesTree":
							System.out.println(urlParams.get("action"));
							response = new ProfilesTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "profilesList":
							System.out.println(urlParams.get("action"));
							response = new ProfilesListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "remoteFileChooser":
							response = new RemoteFileChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "remoteRootChooser":
							response = new RemoteRootChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "CatVer":
							response = new CatVerXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "NPlayers":
							response = new NPlayersXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "AnywareListList":
							response = new AnywareListListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "AnywareList":
							response = new AnywareListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "Anyware":
							response = new AnywareXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "Report":
							response = new ReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "BatchDat2DirSrc":
							response = new BatchDat2DirSrcXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "BatchDat2DirSDR":
							response = new BatchDat2DirSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "BatchDat2DirResult":
							response = new BatchDat2DirResultXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "BatchTrntChkSDR":
							response = new BatchTrntChkSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "BatchTrntChkReportTree":
							response = new BatchTrntChkReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						case "BatchCompressorFR":
							response = new BatchCompressorFRXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
							break;
						default:
							new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen);
							break;
					}
					System.out.println(response);
					if(response!=null)
						return NanoHTTPD.newFixedLengthResponse(Status.OK, "text/xml", response, response.getLength());
				}
				else
					session.getInputStream().skip(bodylen);
			}
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(),e);
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
		return new Error404UriHandler().get(uriResource, urlParams, session);
	}

}