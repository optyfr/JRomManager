package jrm.server.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD.DefaultHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import jrm.misc.Log;
import jrm.security.PathAbstractor;
import jrm.server.Server;
import jrm.server.shared.WebSession;
import lombok.val;

public class DownloadHandler extends DefaultHandler
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

	@Override
	public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
	{
		try
		{
			final Map<String, String> headers = session.getHeaders();
			final String bodylenstr = headers.get("content-length");
			if (bodylenstr != null)
			{
				session.parseBody(new HashMap<String, String>());
				WebSession ws = Server.getSession(session.getCookies().read("session"));
				val pathAbstractor = new PathAbstractor(ws);
				val param = session.getParameters().get("path");
				if (param != null && param.size() > 0)
				{
					val path = param.get(0);
					val file = pathAbstractor.getAbsolutePath(path);
					if (Files.isRegularFile(file))
					{
						val dlfilename = file.getFileName().toString();
						val in = Files.newInputStream(file, StandardOpenOption.READ);
						Response response = NanoHTTPD.newFixedLengthResponse(Status.OK, Files.probeContentType(file), in, Files.size(file));
						response.addHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(dlfilename, "UTF-8") + "; filename=\"" + dlfilename + "\"");
						response.addHeader("Content-Transfer-Encoding", "binary");
						return response;
					}
					else
					{
						val tmpfile = File.createTempFile("JRMSRV", null);
						try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tmpfile), StandardCharsets.UTF_8))
						{
							Files.walkFileTree(file, new SimpleFileVisitor<Path>()
							{
								@Override
								public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException
								{
									zos.putNextEntry(new ZipEntry(file.relativize(f).toString()));
									Files.copy(f, zos);
									zos.closeEntry();
									return FileVisitResult.CONTINUE;
								}
							});
							zos.finish();
						}
						val in = new FileInputStream(tmpfile);
						val dlfilename = file.getFileName().toString() + ".zip";
						Response response = NanoHTTPD.newFixedLengthResponse(Status.OK, "application/zip", in, tmpfile.length());
						response.addHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(dlfilename, "UTF-8") + "; filename=\"" + dlfilename + "\"");
						response.addHeader("Content-Transfer-Encoding", "binary");
						return response;
					}
				}
				else
					System.err.println("no param");
			}
			else
				System.err.println("body len is null");
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
			return new Error500UriHandler(e).get(uriResource, urlParams, session);
		}
		return new Error404UriHandler().get(uriResource, urlParams, session);
	}

}
