package jrm.fullserver;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.servlet.annotation.WebServlet;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.Session;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import jrm.misc.Log;

public class FullServer
{
	private String clientPath;
	private static boolean debug = false;


	public FullServer(int port, String clientPath) throws Exception
	{
		this.clientPath = clientPath;
		
		Server server = new Server(8080);
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setBaseResource(Resource.newResource(this.clientPath));
		context.setContextPath("/");

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

		server.setHandler(context);
		server.setStopAtShutdown(true);
		server.start();
		Log.config("Start server");
		Log.config("port: " + port);
		Log.config("clientPath: " + clientPath);
		Log.config("workPath: " + getWorkPath());
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			Log.info("Server stopped.");
		}));
		if(debug)
		{
			System.in.read();
			System.exit(0);
		}
		else
			server.join();
	}

	public static void main(String[] args)
	{
		Options options = new Options();
		options.addOption(new Option("c", "client", true, "Client Path"));
		options.addOption(new Option("p", "port", true, "Server Port"));
		options.addOption(new Option("w", "workpath", true, "Working Path"));
		options.addOption(new Option("d", "debug", false, "Debug"));

		String clientPath = null;
		int port = 8080;
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
					Log.err(e.getMessage(),e);
				}
			}
			try
			{
				if(cmd.hasOption('p'))
					port = Integer.parseInt(cmd.getOptionValue('p'));
			}
			catch (NumberFormatException e)
			{
			}
			if(cmd.hasOption('w'))
				System.setProperty("jrommanager.dir", cmd.getOptionValue('w').replace("%HOMEPATH%", System.getProperty("user.home")));
			if(cmd.hasOption('d'))
				debug = true;
			try
			{
				Locale.setDefault(Locale.US);
				System.setProperty("file.encoding", "UTF-8");
				Log.init(getLogPath() + "/Server.%g.log", debug, 1024 * 1024, 5);
				new FullServer(port, clientPath);
			}
			catch (Exception e)
			{
				Log.err(e.getMessage(), e);
			}
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(),e);
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

	@SuppressWarnings("serial")
	@WebServlet(name = "WebSocket Servlet", urlPatterns = {"/socket"})
	public class SocketServlet extends WebSocketServlet
	{
	    @Override
	    public void configure(WebSocketServletFactory factory)
	    {
	        // set a 10 second timeout
	        factory.getPolicy().setIdleTimeout(10000);

	        // register MyEchoSocket as the WebSocket to create on Upgrade
	        factory.register(Socket.class);
	    }
	}
	
	@WebSocket
	class Socket
	{
		@OnWebSocketConnect
		public void connect(Session session)
		{
			System.out.println("Socket connect for session  "+session.getId()); 
		}
		
		@OnWebSocketClose
		public void close(Session session, int statusCode, String reason)
		{
			System.out.println("Socket close for session  " + session.getId() + " with status " + statusCode + " and reason " + reason);
		}
		
		@OnWebSocketError
		public void error(Session session, Throwable error)
		{
			System.out.println("Socket error for session  " + session.getId() + " with error message " + error.getMessage());
		}
		
		@OnWebSocketMessage
		public void message(Session session, String msg)
		{
			System.out.println("Socket message for session  " + session.getId() + " with message " + msg);
		}
	}
	
}
