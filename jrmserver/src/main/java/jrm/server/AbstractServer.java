package jrm.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jetty.ee9.servlet.DefaultServlet;
import org.eclipse.jetty.ee9.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.PathResourceFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.URLResourceFactory;

import jrm.fullserver.FullServer;
import jrm.misc.Log;
import jrm.server.shared.WebSession;

public abstract class AbstractServer implements Daemon
{

	private static final String CACHE_CONTROL = "cacheControl";
	private static final String PRECOMPRESSED = "precompressed";
	private static final String ACCEPT_RANGES = "acceptRanges";
	private static final String DIR_ALLOWED = "dirAllowed";
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	protected static Resource clientPath;
	protected static Server jettyserver = null;
	protected static boolean debug;

	protected AbstractServer()
	{
	}

	/**
	 * @return
	 */
	protected static ServletHolder holderStatic()
	{
		final var holderStatic = new ServletHolder("static", DefaultServlet.class);
		holderStatic.setInitParameter(DIR_ALLOWED, FALSE);
		holderStatic.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStatic.setInitParameter(PRECOMPRESSED, TRUE);
		return holderStatic;
	}

	/**
	 * @return
	 */
	protected static ServletHolder holderStaticJS()
	{
		final var holderStaticJS = new ServletHolder("static_js", DefaultServlet.class);
		holderStaticJS.setInitParameter(DIR_ALLOWED, FALSE);
		holderStaticJS.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStaticJS.setInitParameter(PRECOMPRESSED, TRUE);
		holderStaticJS.setInitParameter(CACHE_CONTROL, "public, max-age=0, must-revalidate");
		return holderStaticJS;
	}

	/**
	 * @return
	 */
	protected static ServletHolder holderStaticCache()
	{
		final var holderStaticCache = new ServletHolder("static_cache", DefaultServlet.class);
		holderStaticCache.setInitParameter(DIR_ALLOWED, FALSE);
		holderStaticCache.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStaticCache.setInitParameter(PRECOMPRESSED, TRUE);
		return holderStaticCache;
	}

	/**
	 * @return
	 */
	protected static ServletHolder holderStaticNoCache()
	{
		final var holderStaticNoCache = new ServletHolder("static_nocache", DefaultServlet.class);
		holderStaticNoCache.setInitParameter(DIR_ALLOWED, FALSE);
		holderStaticNoCache.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStaticNoCache.setInitParameter(PRECOMPRESSED, FALSE);
		holderStaticNoCache.setInitParameter(CACHE_CONTROL, "no-store");
		return holderStaticNoCache;
	}

	protected static Path getWorkPath()
	{
		String base = System.getProperty("jrommanager.dir");
		if (base == null)
			base = System.getProperty("user.dir");
		return Paths.get(base);
	}

	protected static String getLogPath() throws IOException
	{
		final var path = getWorkPath().resolve("logs");
		Files.createDirectories(path);
		return path.toString();
	}

	/**
	 * @throws InterruptedException
	 * @throws JettyException
	 */
	protected static void waitStop() throws InterruptedException, JettyException
	{
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if (isStarted())
				{
					try
					{
						terminate();
						Log.info("Server stopped.");
					}
					catch (Exception e)
					{
						// ignore
					}
				}
			}));
			if (debug || SystemUtils.IS_OS_WINDOWS)
			{
				try (final var sc = new Scanner(System.in))
				{
					// wait until receive stop command from keyboard
					System.out.println("Enter 'stop' to halt: "); // NOSONAR
					while (!sc.nextLine().equalsIgnoreCase("stop"))
						Thread.sleep(1000);
					System.exit(0);
				}
			}
			else if (isStarted())
				jettyserver.join();
		}
		catch (InterruptedException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new JettyException(e.getMessage(), e);
		}
	}

	/**
	 * @throws Exception
	 */
	public static synchronized void terminate() throws Exception
	{
		WebSession.closeAll();
		if (jettyserver != null)
		{
			if (jettyserver.isStarted())
				jettyserver.stop();
			jettyserver = null;
		}
	}

	public static final synchronized boolean isStarted()
	{
		return jettyserver != null && jettyserver.isStarted();

	}

	public static final synchronized boolean isStopped()
	{
		return jettyserver == null;

	}

	@SuppressWarnings("serial")
	protected static class JettyException extends Exception
	{
		@SuppressWarnings("unused")
		public JettyException()
		{
			super();
		}

		public JettyException(String message)
		{
			super(message);
		}

		public JettyException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

	private static ResourceFactory prf = new PathResourceFactory();
	private static ResourceFactory urf = new URLResourceFactory();
	
	protected static Resource getClientPath(String path) throws URISyntaxException
	{
		if (path != null)
			return prf.newResource(getPath(path));
		final var p = getPath("jrt:/jrm.merged.module/webclient/");
		if (Files.exists(p))
			return prf.newResource(p);
		return urf.newResource(FullServer.class.getResource("/webclient/"));
	}

	protected static Resource getCertsPath(String path) throws URISyntaxException
	{
		if (path != null)
			return prf.newResource(getPath(path));
		final var p = getPath("jrt:/jrm.merged.module/certs/localhost.pfx");
		if (Files.exists(p))
			return prf.newResource(p);
		return urf.newResource(FullServer.class.getResource("/certs/localhost.pfx"));
	}

	protected static Path getPath(String path)
	{
		try
		{
			return path.startsWith("jrt:") || path.startsWith("file:") || path.startsWith("jar:") ? Path.of(URI.create(path)) : Paths.get(path);
		}
		catch (FileSystemNotFoundException e)
		{
			final var uri = URI.create(path);
			try
			{
				FileSystems.newFileSystem(uri, Collections.emptyMap());
				return Path.of(uri);
			}
			catch (IOException e1)
			{
				return null;
			}
		}
	}

}
