package jrm.server;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.cli.*;

import fi.iki.elonen.NanoWSD.WebSocket;

public class Server extends EnhRouterNanoHTTPD implements SessionStub
{
	private static String clientPath;
	private static int port = 8080;
	
	public Server()
	{
		super(Server.port);
		addMappings();
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
		options.addOption(new Option("p", "port", true, "Server Port"));
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try
		{
			cmd = parser.parse(options, args);
			if (null == (Server.clientPath = cmd.getOptionValue('c')))
			{
				try
				{
					Server.clientPath = new File(new File(Server.class.getProtectionDomain().getCodeSource().getLocation().toURI()), "smartgwt").getPath();
				}
				catch (URISyntaxException e)
				{
					e.printStackTrace();
				}
			}
			try
			{
				Server.port = Integer.parseInt(cmd.getOptionValue('p'));
			}
			catch (NumberFormatException e)
			{
			}
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			formatter.printHelp("Server", options);
			System.exit(1);
		}
		try
		{
			Server server = Server.class.newInstance();
			server.start(0);
			try
			{
				System.out.println("Start server");
				System.out.println("port: "+port);
				System.out.println("clientPath: "+clientPath);
				System.in.read();
			}
			catch (Throwable ignored)
			{
			}
	        server.stop();
	        System.out.println("Server stopped.\n");
		}
		catch (InstantiationException | IllegalAccessException | IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addMappings()
	{
		super.addMappings();
		addRoute("/", jrm.server.IndexHandler.class);
		addRoute("/index.html", jrm.server.IndexHandler.class);
		addRoute("/smartgwt/(.)+", StaticPageHandler.class, new File(Server.clientPath));
		addRoute("/images/(.)+", ResourceHandler.class, Server.class.getResource("/jrm/resources/"));
		addRoute("/datasources/:action/", DataSourcesHandler.class);
		addRoute("/session/", SessionHandler.class, this);
	}
	
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake)
	{
		return new WebSckt(handshake);
	}

	private String session = null;
	
	@Override
	public String getSession()
	{
		return session;
	}

	@Override
	public void setSession(String session)
	{
		this.session = session;
	}
}
