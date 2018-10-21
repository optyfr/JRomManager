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
import jrm.security.Session;
import jrm.server.Server;
import jrm.server.datasources.*;

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
				Session sess = Server.getSession(session.getCookies().read("session"));
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
						case "CatVer":
							return new CatVerXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "NPlayers":
							return new NPlayersXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "MachineListList":
							return new MachineListListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
						case "AnywareList":
							return new AnywareListXMLResponse(new XMLRequest(sess, new BufferedInputStream(session.getInputStream()), bodylen)).processRequest();
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
			e.printStackTrace();
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
		return new Error404UriHandler().get(uriResource, urlParams, session);
	}

}