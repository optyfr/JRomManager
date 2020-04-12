package jrm.fullserver.handlers;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.FileStore;
import java.nio.file.FileSystemException;
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

import jrm.security.PathAbstractor;
import jrm.server.shared.WebSession;
import lombok.val;

@SuppressWarnings("serial")
public class UploadServlet extends HttpServlet
{
	static class Result
	{
		int status = -1;
		String extstatus = "";
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		switch(req.getRequestURI())
		{
			case "/upload/":
			{
				val ws = (WebSession)req.getSession().getAttribute("session");
				val pathAbstractor = new PathAbstractor(ws);
				final Result result = new Result();
				String init = req.getParameter("init");
				if(init != null && init.equals("1"))
				{
					result.status=0;
					result.extstatus="continue...";
					final String filename =  URLDecoder.decode(req.getHeader("x-file-name"), "UTF-8");
					final String fileparent = URLDecoder.decode(req.getHeader("x-file-parent"), "UTF-8");
					if(pathAbstractor.isWriteable(fileparent))
					{
						long filesize;
						try
						{
							filesize = Long.parseLong(req.getHeader("x-file-size"));
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
						result.status=11;
						result.extstatus="Is read only";
					}
				}
				else
				{
					result.status=10;
					result.extstatus="init error";
				}
				resp.setContentType("text/json");
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.getWriter().write(new Gson().toJson(result));
				break;
			}
			default:
				super.doPost(req, resp);
				break;
		}
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		switch(req.getRequestURI())
		{
			case "/upload/":
			{
				val ws = (WebSession)req.getSession().getAttribute("session");
				val pathAbstractor = new PathAbstractor(ws);
				final Result result = new Result();
				final String filename =  URLDecoder.decode(req.getHeader("x-file-name"), "UTF-8");
				final String fileparent = URLDecoder.decode(req.getHeader("x-file-parent"), "UTF-8");
				if(pathAbstractor.isWriteable(fileparent))
				{
					long filesize;
					try
					{
						filesize = Long.parseLong(req.getHeader("x-file-size"));
					}
					catch(NumberFormatException e)
					{
						filesize = -1;
						System.err.println(filename + " : " + e.getMessage());
					}
					final Path dest = pathAbstractor.getAbsolutePath(fileparent);
					final Path filepath = dest.resolve(filename);
					Files.createDirectories(filepath.getParent());
					long size = 0;
					try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(filepath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)))
					{
						size = IOUtils.copy(req.getInputStream(), out);
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
					result.status=11;
					result.extstatus="Is read only";
				}
				resp.getWriter().write(new Gson().toJson(result));
				break;
			}
			default:
				super.doPut(req, resp);
				break;
		}
	}
}
