package jrm.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.daemon.DaemonContext;
import org.eclipse.jetty.ee9.servlet.FilterHolder;
import org.eclipse.jetty.ee9.servlet.ServletContextHandler;
import org.eclipse.jetty.ee9.servlet.ServletHolder;
import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.ResourceFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jrm.misc.DefaultEnvironmentProperties;
import jrm.misc.Log;
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

	private static final DefaultEnvironmentProperties env = DefaultEnvironmentProperties.getInstance(Server.class);

	
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
	
	private static void initFromEnv(Args jArgs)
	{
		Optional.ofNullable(env.getProperty("jrm.server.clientpath", jArgs.clientPath)).ifPresent(v -> jArgs.clientPath = v);
		Optional.ofNullable(env.getProperty("jrm.server.workpath", jArgs.workPath)).ifPresent(v -> jArgs.workPath = v);
		Optional.ofNullable(env.getProperty("jrm.server.debug", jArgs.debug)).ifPresent(v -> jArgs.debug = v);
		Optional.ofNullable(env.getProperty("jrm.server.http", jArgs.httpPort)).ifPresent(v -> jArgs.httpPort = v);
		Optional.ofNullable(env.getProperty("jrm.server.bind", jArgs.bind)).ifPresent(v -> jArgs.bind = v);
	}
	
	/**
	 * @param args
	 * @throws NumberFormatException
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public static void parseArgs(String... args) throws NumberFormatException, IOException
	{
		final var jArgs = new Args();
		final var cmd = JCommander.newBuilder().addObject(jArgs).build();
		try
		{
			initFromEnv(jArgs);
			
			cmd.parse(args);
			debug = jArgs.debug;
			clientPath = jArgs.clientPath;
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
			final var  resourceFactory = ResourceFactory.of(context);
			context.setBaseResource(getClientPath(resourceFactory, clientPath));
			context.setContextPath("/");
	
			context.addServlet(new ServletHolder("datasources", DataSourceServlet.class), "/datasources/*");
			context.addServlet(new ServletHolder("images", ImageServlet.class), "/images/*");
			context.addServlet(new ServletHolder("session", SessionServlet.class), "/session");
			context.addServlet(new ServletHolder("actions", ActionServlet.class), "/actions/*");
			context.addServlet(new ServletHolder("upload", UploadServlet.class), "/upload/*");
			context.addServlet(new ServletHolder("download", DownloadServlet.class), "/download/*");

			context.addFilter(new FilterHolder((request, response, chain) -> {
				if (request instanceof HttpServletRequest httprequest && response instanceof HttpServletResponse httpresponse)
				{
					if(httprequest.getRequestURI().endsWith(".nocache.js"))
						httpresponse.setHeader("cache-control", "no-store");
					else if(!httprequest.getRequestURI().endsWith(".cache.js"))
						httpresponse.setHeader("cache-control", "public, max-age=0, must-revalidate");
				}
				chain.doFilter(request, response);
			}), "*.js", EnumSet.of(DispatcherType.REQUEST));
			
			context.addServlet(holderStatic(), "/");
	
			context.getSessionHandler().setMaxInactiveInterval(300);
			context.getSessionHandler().addEventListener(new SessionListener(false));
	
			final var gh = gzipHandler();
			gh.setHandler(context);
			jettyserver.setHandler(gh);
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
			Log.config("clientPath: " + context.getBaseResource());
			Log.config("workPath: " + getWorkPath());
		}
		else
			Log.err("Already initialized");
	}

	@Override
	public void init(DaemonContext context) throws Exception
	{
		parseArgs(context.getArguments());
		initialize();
	}

	@Override
	public void start() throws Exception
	{
		// do nothing
	}

	@Override
	public void stop() throws Exception
	{
		// do nothing
	}

	@Override
	public void destroy()
	{
		try
		{
			terminate();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static void windowsService(String [] args) throws Exception
	{
		Log.info(() -> "WINDOW SERVICE " + Stream.of(args).collect(Collectors.joining(" ")));
		var cmd = "start";
		if(args.length > 0) cmd = args[0];

		try
		{
			parseArgs(Arrays.copyOfRange(args, 1, args.length));
			if("start".equals(cmd))
				windowsStart();
			else
				windowsStop();
		}
		catch(Exception e)
		{
			Log.err(e.getMessage(), e);
			throw e;
		}
	}

	static void windowsStart() throws Exception
	{
		Log.info("WIN START");
		initialize();
		while(isStopped())
		{
			synchronized(Server.class)
			{
				Server.class.wait(60000); // wait 1 minute and check if stopped
			}
		}
	}

	static void windowsStop() throws Exception
	{
		Log.info("WIN STOP");
		terminate();
		synchronized(Server.class)
		{
			// stop the start loop
			Server.class.notifyAll();
		}
	}
}
