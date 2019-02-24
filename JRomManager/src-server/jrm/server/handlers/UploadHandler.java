package jrm.server.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import com.eclipsesource.json.JsonObject;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.misc.Log;

public class UploadHandler extends DefaultHandler
{
	@Override
	public String getText()
	{
		return null;
	}

	@Override
	public IStatus getStatus()
	{
		return Status.OK;
	}

	@Override
	public String getMimeType()
	{
		return "text/json";
	}

	@SuppressWarnings("serial")
	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			final Map<String, String> headers = session.getHeaders();
			final String bodylenstr = headers.get("content-length");
			List<String> init = session.getParameters().get("init");
			if(init!=null && init.size()>0 && "1".equals(init.get(0)))
			{
				if (bodylenstr != null)
				{
					int bodylen = Integer.parseInt(bodylenstr);
					session.getInputStream().skip(bodylen);
				}
				return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), new JsonObject() {{
					add("status", 0);
					add("extstatus", "continue...");
				}}.toString());
			}
			else
			{
				if (bodylenstr != null)
				{
					long bodylen = Long.parseLong(bodylenstr);
					InputStream in = new BufferedInputStream(session.getInputStream());
					final String filename =  URLDecoder.decode(headers.get("x-file-name"), "UTF-8");
					final String fileparent = URLDecoder.decode(headers.get("x-file-parent"), "UTF-8");
					Path dstpath = Paths.get(fileparent, filename);
					Files.createDirectories(dstpath.getParent());
					BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(dstpath, StandardOpenOption.CREATE));
					int c;
					long cnt = 0;
					for(long i = 0; i < bodylen; i++, cnt++)
					{
						if((c=in.read())==-1)
							break;
						out.write(c);
					}
					out.close();
					if(cnt != bodylen)
						Files.delete(dstpath);
				}
				return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), new JsonObject() {{
					add("status", 3);
					add("extstatus", "done");
				}}.toString());
			}
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(),e);
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
	}
}
