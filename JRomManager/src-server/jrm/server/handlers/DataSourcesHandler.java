package jrm.server.handlers;

import java.io.BufferedInputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.misc.Log;
import jrm.server.Server;
import jrm.server.datasources.AnywareListListXMLResponse;
import jrm.server.datasources.AnywareListXMLResponse;
import jrm.server.datasources.AnywareXMLResponse;
import jrm.server.datasources.BatchCompressorFRXMLResponse;
import jrm.server.datasources.BatchDat2DirResultXMLResponse;
import jrm.server.datasources.BatchDat2DirSDRXMLResponse;
import jrm.server.datasources.BatchDat2DirSrcXMLResponse;
import jrm.server.datasources.BatchTrntChkReportTreeXMLResponse;
import jrm.server.datasources.BatchTrntChkSDRXMLResponse;
import jrm.server.datasources.CatVerXMLResponse;
import jrm.server.datasources.NPlayersXMLResponse;
import jrm.server.datasources.ProfilesListXMLResponse;
import jrm.server.datasources.ProfilesTreeXMLResponse;
import jrm.server.datasources.RemoteFileChooserXMLResponse;
import jrm.server.datasources.RemoteRootChooserXMLResponse;
import jrm.server.datasources.ReportTreeXMLResponse;
import jrm.server.datasources.XMLRequest;
import jrm.server.shared.WebSession;

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
			if (bodylenstr != null)
			{
				long bodylen = Long.parseLong(bodylenstr);
				WebSession sess = Server.getSession(session.getCookies().read("session"));
				if (headers.get("content-type").equals("text/xml"))
				{
					switch (urlParams.get("action"))
					{
						case "profilesTree":
							return new ProfilesTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "profilesList":
							return new ProfilesListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "remoteFileChooser":
							return new RemoteFileChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "remoteRootChooser":
							return new RemoteRootChooserXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "CatVerCmd":
							return new CatVerXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "NPlayersCmd":
							return new NPlayersXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "AnywareListList":
							return new AnywareListListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "AnywareList":
							return new AnywareListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "Anyware":
							return new AnywareXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "Report":
							return new ReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "BatchDat2DirSrc":
							return new BatchDat2DirSrcXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "BatchDat2DirSDR":
							return new BatchDat2DirSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "BatchDat2DirResult":
							return new BatchDat2DirResultXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "BatchTrntChkSDR":
							return new BatchTrntChkSDRXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "BatchTrntChkReportTree":
							return new BatchTrntChkReportTreeXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "BatchCompressorFR":
							return new BatchCompressorFRXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						default:
							new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen);
							break;
					}
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