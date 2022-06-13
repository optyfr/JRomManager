package jrm.server.shared.handlers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jrm.misc.Log;
import jrm.security.PathAbstractor;
import jrm.server.shared.WebSession;
import lombok.val;

@SuppressWarnings("serial")
public class DownloadServlet extends HttpServlet
{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		try
		{
			if ("/download/".equals(req.getRequestURI()))
			{
				download(req, resp);
			}
			else
				super.doPost(req, resp);
		}
		catch (IOException|ServletException e)
		{
			Log.err(e.getMessage(), e);
		}

	}

	/**
	 * @param req
	 * @param resp
	 * @throws SecurityException
	 * @throws IOException
	 */
	private void download(HttpServletRequest req, HttpServletResponse resp)
	{
		try
		{
			val ws = (WebSession) req.getSession().getAttribute("session");
			val pathAbstractor = new PathAbstractor(ws);
			val path = req.getParameter("path");
			if (path != null)
			{
				val file = pathAbstractor.getAbsolutePath(path);
				if (Files.isRegularFile(file))
				{
					val dlfilename = file.getFileName().toString();
					resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(dlfilename, "UTF-8") + "; filename=\"" + dlfilename + "\"");
					resp.setHeader("Content-Transfer-Encoding", "binary");
					resp.setStatus(HttpServletResponse.SC_OK);
					resp.setContentLengthLong(Files.size(file));
					resp.setContentType(Files.probeContentType(file));
					resp.setDateHeader("Last-Modified", Files.getLastModifiedTime(file).toMillis());
					resp.setHeader("Cache-Control", "max-age=86400");
					Files.copy(file, resp.getOutputStream());

				}
				else
				{
					val dlfilename = file.getFileName().toString() + ".zip";
					resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(dlfilename, "UTF-8") + "; filename=\"" + dlfilename + "\"");
					resp.setHeader("Content-Transfer-Encoding", "binary");
					resp.setContentType("application/zip");
					resp.setStatus(HttpServletResponse.SC_OK);
					final var zos = new ZipOutputStream(resp.getOutputStream(), StandardCharsets.UTF_8);
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
			}
			else
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		catch (IOException e)
		{
			try
			{
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
			catch (IOException e1)
			{
				Log.err(e1.getMessage(), e1);
			}
		}
	}
}
