package jrm.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import jrm.misc.Log;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

public class FullServer
{
	private String clientPath;
	private static boolean debug = false;


	public FullServer(int port, String clientPath) throws Exception
	{
		this.clientPath = clientPath;
		
		Server server = new Server(8080);
		
		ServletContextHandler context = new ServletContextHandler(null,"/",ServletContextHandler.SESSIONS);
		server.setHandler(context);
		ServerContainer container = WebSocketServerContainerInitializer.configureContext(context);
		container.addEndpoint(Socket.class);
		
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

	@ServerEndpoint("/socket")
	class Socket
	{
		private Session session;
		private RemoteEndpoint.Async remote;

		@OnOpen
		public void onOpen(Session session)
		{
			this.session = session;
			this.remote = this.session.getAsyncRemote();
		}
		
		@OnMessage
		public String onMessage(String message)
		{
			return message;
		}

	}
}
