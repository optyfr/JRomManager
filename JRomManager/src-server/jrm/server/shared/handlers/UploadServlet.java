package jrm.server.shared.handlers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import jrm.misc.Log;
import jrm.security.PathAbstractor;
import jrm.server.shared.WebSession;
import lombok.val;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet
{
	private static final String UTF_8 = "UTF-8";

	static class Result
	{
		int status = -1;
		String extstatus = "";
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		if ("/upload/".equals(req.getRequestURI()))
		{
			try
			{
				val ws = (WebSession) req.getSession().getAttribute("session");
				val pathAbstractor = new PathAbstractor(ws);
				final var result = new Result();
				String init = req.getParameter("init");
				if (init != null && init.equals("1"))
				{
					checkRequest(req, pathAbstractor, result);
				}
				else
				{
					result.status = 10;
					result.extstatus = "init error";
				}
				resp.setContentType("text/json");
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.getWriter().write(new Gson().toJson(result));
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
		else
			try
			{
				super.doPost(req, resp);
			}
			catch (ServletException | IOException e)
			{
				Log.err(e.getMessage(), e);
			}
	}

	/**
	 * @param req
	 * @param pathAbstractor
	 * @param result
	 * @throws SecurityException
	 */
	private void checkRequest(HttpServletRequest req, final PathAbstractor pathAbstractor, final Result result)
	{
		try
		{
			result.status = 0;
			result.extstatus = "continue...";
			final String filename = URLDecoder.decode(req.getHeader("x-file-name"), UTF_8);
			final String fileparent = URLDecoder.decode(req.getHeader("x-file-parent"), UTF_8);
			if (pathAbstractor.isWriteable(fileparent))
			{
				final var filesize = getXFileSize(req);
				final var dest = pathAbstractor.getAbsolutePath(fileparent);
				if (!(Files.exists(dest) && Files.isDirectory(dest)))
				{
					result.status = 6;
					result.extstatus = "Error: destination " + dest + " must be an existing directory";
				}
				else
				{
					final var fs = Files.getFileStore(dest);
					final var free = fs.getUsableSpace();
					if (free < filesize)
					{
						result.status = 7;
						result.extstatus = "Error: not enough free space, need " + filesize + " but only " + free + " is available";
					}
					else
					{
						final var filepath = dest.resolve(filename);
						Files.getLastModifiedTime(filepath);
					}
				}
			}
			else
			{
				result.status = 11;
				result.extstatus = "Is read only";
			}
		}
		catch (NoSuchFileException e)
		{ // that's ok
		}
		catch (InvalidPathException e)
		{ // invalid filename (case 1)
			result.status = 8;
			result.extstatus = e.getMessage();
		}
		catch (IOException e)
		{ // invalid filename (case 2)
			result.status = 9;
			result.extstatus = e.getMessage();
		}
	}

	/**
	 * @param req
	 * @return
	 */
	private long getXFileSize(HttpServletRequest req)
	{
		try
		{
			return Long.parseLong(req.getHeader("x-file-size"));
		}
		catch (NumberFormatException e)
		{
			return -1;
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
	{
		if ("/upload/".equals(req.getRequestURI()))
		{
			try
			{
				val ws = (WebSession) req.getSession().getAttribute("session");
				val pathAbstractor = new PathAbstractor(ws);
				final var result = new Result();
				final String filename = URLDecoder.decode(req.getHeader("x-file-name"), UTF_8);
				final String fileparent = URLDecoder.decode(req.getHeader("x-file-parent"), UTF_8);
				if (pathAbstractor.isWriteable(fileparent))
				{
					final var dest = pathAbstractor.getAbsolutePath(fileparent);
					final var filepath = dest.resolve(filename);
					Files.createDirectories(filepath.getParent());
					doUpload(req, result, filename, filepath);
					if (result.status != 3)
					{
						Log.debug(() -> result.status + " : " + result.extstatus);
						Files.delete(filepath);
					}
				}
				else
				{
					result.status = 11;
					result.extstatus = "Is read only";
				}
				resp.getWriter().write(new Gson().toJson(result));
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
		else
			try
			{
				super.doPost(req, resp);
			}
			catch (ServletException | IOException e)
			{
				Log.err(e.getMessage(), e);
			}
	}

	/**
	 * @param req
	 * @param result
	 * @param filename
	 * @param filepath
	 */
	private void doUpload(HttpServletRequest req, final Result result, final String filename, final Path filepath)
	{
		long filesize = getXFileSize(req);
		long size = 0;
		try (final var out = new BufferedOutputStream(Files.newOutputStream(filepath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)))
		{
			size = IOUtils.copy(req.getInputStream(), out);
			result.status = 3;
			result.extstatus = filename + " done";
		}
		catch (IOException e)
		{
			result.status = 20;
			result.extstatus = filename + " : " + e.getMessage();
		}
		finally
		{
			if (filesize >= 0 && size != filesize)
			{
				result.status = 21;
				result.extstatus = "Error: " + filename + " size should be " + filesize + " bytes long but got " + size + " bytes";
			}
		}
	}
}
