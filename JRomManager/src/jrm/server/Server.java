package jrm.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.router.RouterNanoHTTPD;
import fi.iki.elonen.util.ServerRunner;
import jrm.misc.GlobalSettings;
import jrm.ui.profile.manager.DirNode;

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
		addRoute("/", jrm.server.IndexHandler.class);
		addRoute("/index.html", jrm.server.IndexHandler.class);
		addRoute("/smartgwt/(.)+", StaticPageHandler.class, new File(Server.clientPath));
		addRoute("/images/(.)+", ResourceHandler.class, Server.class.getResource("/jrm/resources/"));
		addRoute("/datasources/:action/", UserHandler.class);
	}

	public static class UserHandler extends DefaultHandler
	{

		@Override
		public String getText()
		{
			return "not implemented";
		}

		public String getText(Map<String, String> urlParams, IHTTPSession session)
		{
			switch(urlParams.get("action"))
			{
				case "profilesTree":
					DirNode root = new DirNode(GlobalSettings.getWorkPath().resolve("xmlfiles").toAbsolutePath().normalize().toFile());
					break;
			}

			return "";
		}

		@Override
		public String getMimeType()
		{
			return "text/html";
		}

		@Override
		public IStatus getStatus()
		{
			return Status.OK;
		}

		public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
		{
			String text = getText(urlParams, session);
			ByteArrayInputStream inp = new ByteArrayInputStream(text.getBytes());
			int size = text.getBytes().length;
			return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), inp, size);
		}

	}
}
