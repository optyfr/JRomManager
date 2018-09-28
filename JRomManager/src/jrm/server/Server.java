package jrm.server;

import java.io.File;
import java.net.URISyntaxException;

import org.apache.commons.cli.*;

import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

public class Server extends RouterNanoHTTPD
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
			if(null == (Server.clientPath = cmd.getOptionValue('c')))
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
			System.out.println(Server.clientPath);
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			formatter.printHelp("Server", options);
			System.exit(1);
		}
		ServerRunner.run(Server.class);
	}

	@Override
	public void addMappings()
	{
		super.addMappings();
		addRoute("/", IndexHandler.class);
		addRoute("/index.html", IndexHandler.class);
		addRoute("/smartgwt/(.)+", StaticPageHandler.class, new File(Server.clientPath));
		addRoute("/images/(.)+", ResourceHandler.class, Server.class.getResource("/jrm/resources/"));
	}
}
