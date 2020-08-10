package jrm.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import jrm.misc.Log;
import jrm.server.handlers.SessionServlet;
import jrm.server.shared.WebSession;
import jrm.server.shared.handlers.ActionServlet;
import jrm.server.shared.handlers.DataSourceServlet;
import jrm.server.shared.handlers.DownloadServlet;
import jrm.server.shared.handlers.ImageServlet;
import jrm.server.shared.handlers.UploadServlet;
import lombok.val;

public class Server
{
	private String clientPath;
	private static boolean debug = false;
	private static int HTTP_PORT = 8080;
	private static String BIND = "0.0.0.0";
	private static int CONNLIMIT = 50;

	final static Map<String, WebSession> sessions = new HashMap<>();
	
		
	public Server(String clientPath) throws Exception
	{
		this.clientPath = clientPath;

		val jettyserver = new org.eclipse.jetty.server.Server();

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setBaseResource(Resource.newResource(this.clientPath));
		context.setContextPath("/");

		GzipHandler gzipHandler = new GzipHandler();
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

		ServletHolder holderStaticNoCache = new ServletHolder("static_nocache", DefaultServlet.class);
		holderStaticNoCache.setInitParameter("dirAllowed", "false");
		holderStaticNoCache.setInitParameter("acceptRanges", "true");
		holderStaticNoCache.setInitParameter("precompressed", "false");
		holderStaticNoCache.setInitParameter("cacheControl", "no-store");
		context.addServlet(holderStaticNoCache, "*.nocache.js");

		ServletHolder holderStaticCache = new ServletHolder("static_cache", DefaultServlet.class);
		holderStaticCache.setInitParameter("dirAllowed", "false");
		holderStaticCache.setInitParameter("acceptRanges", "true");
		holderStaticCache.setInitParameter("precompressed", "true");
		context.addServlet(holderStaticCache, "*.cache.js");

		ServletHolder holderStaticJS = new ServletHolder("static_js", DefaultServlet.class);
		holderStaticJS.setInitParameter("dirAllowed", "false");
		holderStaticJS.setInitParameter("acceptRanges", "true");
		holderStaticJS.setInitParameter("precompressed", "true");
		holderStaticJS.setInitParameter("cacheControl", "public, max-age=0, must-revalidate");
		context.addServlet(holderStaticJS, "*.js");

		ServletHolder holderStatic = new ServletHolder("static", DefaultServlet.class);
		holderStatic.setInitParameter("dirAllowed", "false");
		holderStatic.setInitParameter("acceptRanges", "true");
		holderStatic.setInitParameter("precompressed", "true");
		context.addServlet(holderStatic, "/");

		context.getSessionHandler().setMaxInactiveInterval(300);

		context.getSessionHandler().addEventListener(new SessionListener());

		jettyserver.setHandler(context);
		jettyserver.setStopAtShutdown(true);

		// Create the HTTP connection
		HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory();
		ServerConnector httpConnector = new ServerConnector(jettyserver, httpConnectionFactory);
		httpConnector.setPort(HTTP_PORT);
		httpConnector.setHost(BIND);
		httpConnector.setName("HTTP");
		jettyserver.addConnector(httpConnector);
		
		jettyserver.addBean(new ConnectionLimit(CONNLIMIT, jettyserver)); // limit simultaneous connections

		jettyserver.start();
		Log.config("Start server");
		for (val connector : jettyserver.getConnectors())
			Log.config(((ServerConnector) connector).getName() + " with port on " + ((ServerConnector) connector).getPort()+ " binded to " +((ServerConnector) connector).getHost());
		Log.config("clientPath: " + clientPath);
		Log.config("workPath: " + getWorkPath());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Log.info("Server stopped.");
		}));
		if (debug)
		{
			try (Scanner sc = new Scanner(System.in))
			{
				// wait until receive stop command from keyboard
				System.out.println("Enter 'stop' to halt: ");
				while (!sc.nextLine().toLowerCase().equals("stop"))
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

	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		Options options = new Options();
		options.addOption(new Option("c", "client", true, "Client Path"));
		options.addOption(new Option("w", "workpath", true, "Working Path"));
		options.addOption(new Option("d", "debug", false, "Debug"));
		options.addOption(new Option("p", "http", true, "http port, default is " + HTTP_PORT));
		options.addOption(new Option("b", "bind", true, "bind to address or host, default is " + BIND));

		String clientPath = null;
		try
		{
			CommandLine cmd = new DefaultParser().parse(options, args);
			if (null == (clientPath = cmd.getOptionValue('c')))
			{
				try
				{
					clientPath = new File(new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "smartgwt").getPath();
				}
				catch (URISyntaxException e)
				{
					Log.err(e.getMessage(), e);
				}
			}
			if (cmd.hasOption('b'))
				BIND = cmd.getOptionValue('b');
			if (cmd.hasOption('p'))
				HTTP_PORT = Integer.parseInt(cmd.getOptionValue('p'));
			if (cmd.hasOption('w'))
				System.setProperty("jrommanager.dir", cmd.getOptionValue('w').replace("%HOMEPATH%", System.getProperty("user.home")));
			if (cmd.hasOption('d'))
				debug = true;
			try
			{
				Locale.setDefault(Locale.US);
				System.setProperty("file.encoding", "UTF-8");
				Log.init(getLogPath() + "/Server.%g.log", debug, 1024 * 1024, 5);
				new Server(clientPath);
			}
			catch (Exception e)
			{
				Log.err(e.getMessage(), e);
			}
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(), e);
			new HelpFormatter().printHelp("Server", options);
			System.exit(1);
		}
	}
	
	private static Path getWorkPath()
	{
		String base = System.getProperty("jrommanager.dir");
		if (base == null)
			base = System.getProperty("user.dir");
		return Paths.get(base);
	}
	
	private static String getLogPath() throws IOException
	{
		Path path = getWorkPath().resolve("logs");
		Files.createDirectories(path);
		return path.toString();
	}
}
