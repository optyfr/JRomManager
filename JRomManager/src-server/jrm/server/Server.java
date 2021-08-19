package jrm.server;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;

import jrm.misc.Log;
import jrm.misc.URIUtils;
import jrm.server.handlers.SessionServlet;
import jrm.server.shared.WebSession;
import jrm.server.shared.handlers.ActionServlet;
import jrm.server.shared.handlers.DataSourceServlet;
import jrm.server.shared.handlers.DownloadServlet;
import jrm.server.shared.handlers.ImageServlet;
import jrm.server.shared.handlers.UploadServlet;
import lombok.val;

public class Server extends AbstractServer
{
	private Path clientPath;
	private boolean debug = false;
	private static final int HTTP_PORT_DEFAULT = 8080;
	private int httpPort = HTTP_PORT_DEFAULT;
	private static final String BIND_DEFAULT = "0.0.0.0";
	private String bind = BIND_DEFAULT;
	private int connLimit = 50;

	static final Map<String, WebSession> sessions = new HashMap<>();
	
		
	@SuppressWarnings("exports")
	public Server(CommandLine cmd) throws IOException, JettyException, InterruptedException
	{
		super(cmd.hasOption('d'));
		this.clientPath = Optional.ofNullable(cmd.getOptionValue('c')).map(Paths::get).orElse(URIUtils.getPath("jrt:/jrm.merged.module/webclient/"));
		if (cmd.hasOption('b'))
			bind = cmd.getOptionValue('b');
		if (cmd.hasOption('p'))
			httpPort = Integer.parseInt(cmd.getOptionValue('p'));
		if (cmd.hasOption('w'))
			System.setProperty("jrommanager.dir", cmd.getOptionValue('w').replace("%HOMEPATH%", System.getProperty("user.home")));
		Locale.setDefault(Locale.US);
		System.setProperty("file.encoding", "UTF-8");
		Log.init(getLogPath() + "/Server.%g.log", debug, 1024 * 1024, 5);

		val jettyserver = new org.eclipse.jetty.server.Server();

		final var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setBaseResource(Resource.newResource(this.clientPath));
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

		context.getSessionHandler().addEventListener(new SessionListener());

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

		try
		{
			jettyserver.start();
			Log.config("Start server");
			for (val connector : jettyserver.getConnectors())
				Log.config(((ServerConnector) connector).getName() + " with port on " + ((ServerConnector) connector).getPort()+ " binded to " +((ServerConnector) connector).getHost());
			Log.config("clientPath: " + clientPath);
			Log.config("workPath: " + getWorkPath());
			waitStop(jettyserver);
		}
		catch (InterruptedException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new JettyException(e.getMessage());
		}
	}
	
	/**
	 * Main entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		final var options = new Options();
		options.addOption(new Option("c", "client", true, "Client Path"));
		options.addOption(new Option("w", "workpath", true, "Working Path"));
		options.addOption(new Option("d", "debug", false, "Debug"));
		options.addOption(new Option("p", "http", true, "http port, default is " + HTTP_PORT_DEFAULT));
		options.addOption(new Option("b", "bind", true, "bind to address or host, default is " + BIND_DEFAULT));

		try
		{
			CommandLine cmd = new DefaultParser().parse(options, args);
			new Server(cmd);
		}
		catch (InterruptedException e)
		{
			Log.err(e.getMessage(), e);
			System.exit(1);
			Thread.currentThread().interrupt();
		}
		catch (ParseException | IOException | JettyException e)
		{
			Log.err(e.getMessage(), e);
			new HelpFormatter().printHelp("Server", options);
			System.exit(1);
		}
	}
}
