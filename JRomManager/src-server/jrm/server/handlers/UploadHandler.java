package jrm.server.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.FileStore;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.misc.Log;
import jrm.security.PathAbstractor;
import jrm.server.Server;
import jrm.server.shared.WebSession;
import lombok.val;

public class UploadHandler extends DefaultHandler
{
	static class Result
	{
		int status = -1;
		String extstatus = "";
	}

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

	@Override
	public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			WebSession sess = Server.getSession(session.getCookies().read("session"));
			val pathAbstractor = new PathAbstractor(sess);
			final Result result = new Result();
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
				result.status = 0;
				result.extstatus = "continue...";
				final String filename =  URLDecoder.decode(headers.get("x-file-name"), "UTF-8");
				final String fileparent = URLDecoder.decode(headers.get("x-file-parent"), "UTF-8");
				if(pathAbstractor.isWriteable(fileparent))
				{
					long filesize;
					try
					{
						filesize = Long.parseLong(headers.get("x-file-size"));
					}
					catch(NumberFormatException e)
					{
						filesize = -1;
					}
					try
					{
						final Path dest = pathAbstractor.getAbsolutePath(fileparent);
						if(!(Files.exists(dest) && Files.isDirectory(dest)))
						{
							result.status=6;
							result.extstatus="Error: destination " + dest + " must be an existing directory";
						}
						else
						{
							final FileStore fs = Files.getFileStore(dest);
							final long free = fs.getUsableSpace();
							if(free < filesize)
							{
								result.status=7;
								result.extstatus="Error: not enough free space, need " + filesize + " but only " + free + " is available";
							}
							else
							{
								final Path filepath = dest.resolve(filename);
								Files.getLastModifiedTime(filepath);
							}
						}
					}
					catch(NoSuchFileException e)
					{	// that's ok
					}
					catch(InvalidPathException e)
					{	// invalid filename (case 1)
						result.status=8;
						result.extstatus=e.getMessage();
					}
					catch(FileSystemException e)
					{	// invalid filename (case 2)
						result.status=9;
						result.extstatus=e.getMessage();
					}
				}
				else
				{
					result.status=10;
					result.extstatus="Is read only";
				}
				return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), new Gson().toJson(result));
			}
			else
			{
				if (bodylenstr != null)
				{
					InputStream in = new BufferedInputStream(session.getInputStream());
					final String filename =  URLDecoder.decode(headers.get("x-file-name"), "UTF-8");
					String fileparent = URLDecoder.decode(headers.get("x-file-parent"), "UTF-8");
					if(pathAbstractor.isWriteable(fileparent))
					{
						long filesize;
						try
						{
							filesize = Long.parseLong(headers.get("x-file-size"));
						}
						catch(NumberFormatException e)
						{
							filesize = -1;
						}
						final Path dest = pathAbstractor.getAbsolutePath(fileparent);
						final Path filepath = dest.resolve(filename);
						Files.createDirectories(filepath.getParent());
						long size = 0;
						try(BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(filepath, StandardOpenOption.CREATE));)
						{
							size = IOUtils.copy(in, out);
							result.status = 3;
							result.extstatus = filename + " done";
						}
						catch(IOException e)
						{
							result.status = 20;
							result.extstatus = filename + " : " + e.getMessage();
						}
						finally
						{
							if(filesize >= 0 && size != filesize)
							{
								result.status = 21;
								result.extstatus = "Error: " + filename + " size should be " + filesize + " bytes long but got " + size + " bytes";
							}
						}
						if(result.status != 3)
						{
							System.err.println(result.status + " : " + result.extstatus);
							Files.delete(filepath);
						}
					}
					else
					{
						result.status=10;
						result.extstatus="Is read only";
					}
				}
				return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), new Gson().toJson(result));
			}
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(),e);
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
	}
	
}
