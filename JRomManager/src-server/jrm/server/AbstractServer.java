package jrm.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletHolder;

import jrm.misc.Log;
import jrm.server.shared.WebSession;

public abstract class AbstractServer
{

	private static final String CACHE_CONTROL = "cacheControl";
	private static final String PRECOMPRESSED = "precompressed";
	private static final String ACCEPT_RANGES = "acceptRanges";
	private static final String DIR_ALLOWED = "dirAllowed";
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	private final boolean debug;

	protected AbstractServer(boolean debug)
	{
		this.debug = debug;
	}
	
	/**
	 * @return
	 */
	protected ServletHolder holderStatic()
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
	protected ServletHolder holderStaticJS()
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
	protected ServletHolder holderStaticCache()
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
	protected ServletHolder holderStaticNoCache()
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
	 * @param jettyserver
	 * @throws InterruptedException
	 * @throws Exception
	 */
	protected void waitStop(final Server jettyserver) throws InterruptedException, JettyException
	{
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread(() -> Log.info("Server stopped.")));
			if (debug)
			{
				try (final var sc = new Scanner(System.in))
				{
					// wait until receive stop command from keyboard
					System.out.println("Enter 'stop' to halt: ");	//NOSONAR
					while (!sc.nextLine().equalsIgnoreCase("stop"))
						Thread.sleep(1000);
					if (!jettyserver.isStopped())
					{
						WebSession.closeAll();
						jettyserver.stop();
					}
				}
			}
			else
				jettyserver.join();
		}
		catch (InterruptedException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new JettyException(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	protected class JettyException extends Exception
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
	

}