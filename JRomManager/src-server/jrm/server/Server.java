package jrm.server;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import jrm.misc.Log;
import jrm.misc.URIUtils;
import jrm.server.handlers.SessionServlet;
import jrm.server.shared.WebSession;
import jrm.server.shared.handlers.ActionServlet;
import jrm.server.shared.handlers.DataSourceServlet;
import jrm.server.shared.handlers.DownloadServlet;
import jrm.server.shared.handlers.ImageServlet;
import jrm.server.shared.handlers.UploadServlet;

public class Server extends AbstractServer
{
	private static final int HTTP_PORT_DEFAULT = 8080;
	private static int httpPort = HTTP_PORT_DEFAULT;
	private static final String BIND_DEFAULT = "0.0.0.0";
	private static String bind = BIND_DEFAULT;
	private static int connLimit = 50;

	static final Map<String, WebSession> sessions = new HashMap<>();
	
	@Parameters(separators = " =")
	public static class Args
	{
		@Parameter(names = { "-c", "--client", "--clientPath" }, arity = 1, description = "Client path")
		private String clientPath = null;
		@Parameter(names = { "-w", "--work", "--workpath" }, arity = 1, description = "Working path")
		private String workPath = null;
		@Parameter(names = { "-d", "--debug" }, description = "Activate debug mode")
		private boolean debug = false;
		@Parameter(names = { "-p", "--http" }, arity = 1, description = "http port")
		private int httpPort = HTTP_PORT_DEFAULT;
		@Parameter(names = { "-b", "--bind" }, arity = 1, description = "bind to address or host")
		private String bind = BIND_DEFAULT;
	}
	
	/**
	 * @param cmd
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public static void parseArgs(String... args) throws NumberFormatException, IOException
	{
		final var jArgs = new Args();
		final var cmd = JCommander.newBuilder().addObject(jArgs).build();
		try
		{
			cmd.parse(args);
			debug = jArgs.debug;
			clientPath = Optional.ofNullable(jArgs.clientPath).map(Paths::get).orElse(URIUtils.getPath("jrt:/jrm.merged.module/webclient/"));
			bind = jArgs.bind;
			httpPort = jArgs.httpPort;
			Optional.ofNullable(jArgs.workPath).map(s -> s.replace("%HOMEPATH%", System.getProperty("user.home"))).ifPresent(s -> System.setProperty("jrommanager.dir", s));
			Locale.setDefault(Locale.US);
			System.setProperty("file.encoding", "UTF-8");
			Log.init(getLogPath() + "/Server.%g.log", debug, 1024 * 1024, 5);
		}
		catch(ParameterException e)
		{
			Log.err(e.getMessage(), e);
			cmd.usage();
			System.exit(1);
		}
	}
	
	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			parseArgs(args);
			initialize();
			waitStop();
		}
		catch (InterruptedException e)
		{
			Log.err(e.getMessage(), e);
			System.exit(1);
			Thread.currentThread().interrupt();
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
			System.exit(1);
		}
	}

	/**
	 * @throws Exception
	 */
	public static void initialize() throws Exception
	{
		if(jettyserver==null)
		{
			jettyserver = new org.eclipse.jetty.server.Server();
	
			final var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setBaseResource(Resource.newResource(clientPath));
			context.setContextPath("/");
	
			final var gzipHandler = new GzipHandler();
			gzipHandler.setIncludedMethods("POST", "GET");
			gzipHandler.setIncludedMimeTypes("text/html", "text/plain", "text/xml", "text/css", "application/javascript", "text/javascript", "application/json");
			gzipHandler.setInflateBufferSize(2048);
			gzipHandler.setMinGzipSize(2048);
			context.setGzipHandler(gzipHandler);
	
			context.addServlet(new ServletHolder("datasources", DataSourceServlet.class), "/datasources/*");
			context.addServlet(new ServletHolder("images", ImageServlet.class), "/images/*");
			context.addServlet(new ServletHolder("session", SessionServlet.class), "/session");
			context.addServlet(new ServletHolder("actions", ActionServlet.class), "/actions/*");
			context.addServlet(new ServletHolder("upload", UploadServlet.class), "/upload/*");
			context.addServlet(new ServletHolder("download", DownloadServlet.class), "/download/*");
			context.addServlet(holderStaticNoCache(), "*.nocache.js");
			context.addServlet(holderStaticCache(), "*.cache.js");
			context.addServlet(holderStaticJS(), "*.js");
			context.addServlet(holderStatic(), "/");
	
			context.getSessionHandler().setMaxInactiveInterval(300);
	
			context.getSessionHandler().addEventListener(new SessionListener(false));
	
			jettyserver.setHandler(context);
			jettyserver.setStopAtShutdown(true);
	
			// Create the HTTP connection
			final var httpConnectionFactory = new HttpConnectionFactory();
			final var httpConnector = new ServerConnector(jettyserver, httpConnectionFactory);
			httpConnector.setPort(httpPort);
			httpConnector.setHost(bind);
			httpConnector.setName("HTTP");
			jettyserver.addConnector(httpConnector);
			
			jettyserver.addBean(new ConnectionLimit(connLimit, jettyserver)); // limit simultaneous connections
	
			jettyserver.start();
			Log.config("Start server");
			for (final var connector : jettyserver.getConnectors())
				Log.config(((ServerConnector) connector).getName() + " with port on " + ((ServerConnector) connector).getPort()+ " binded to " +((ServerConnector) connector).getHost());
			Log.config("clientPath: " + clientPath);
			Log.config("workPath: " + getWorkPath());
		}
		else
			Log.err("Already initialized");
	}
}
