package jrm.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import fi.iki.elonen.router.RouterNanoHTTPD.Error404UriHandler;
import fi.iki.elonen.router.RouterNanoHTTPD.UriResource;
import fi.iki.elonen.util.ServerRunner;

public class Server extends EnhRouterNanoHTTPD
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
		addRoute("/session/", SessionHandler.class);
	}
	
	public static class SessionHandler extends DefaultHandler
	{

		@Override
		public String getText()
		{
			return UUID.randomUUID().toString();
		}

		@Override
		public IStatus getStatus()
		{
			return Status.OK;
		}

		@Override
		public String getMimeType()
		{
			return "text/plain";
		}
		
		@Override
		public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session)
		{
			try
			{
				final Map<String, String> headers = session.getHeaders();
				final String bodylenstr = headers.get("content-length");
				if (bodylenstr != null)
				{
					int bodylen = Integer.parseInt(bodylenstr);
					session.getInputStream().skip(bodylen);
				}
				return newFixedLengthResponse(getStatus(), getMimeType(), getText());
			}
			catch (Exception e)
			{
				return new Error500UriHandler(e).get(uriResource, urlParams, session);
			}
		}
		
	}
	
	class Ws extends WebSocket
	{
		public Ws(IHTTPSession handshakeRequest)
		{
			super(handshakeRequest);
			System.out.println("websocket created......");
		}

		@Override
		protected void onPong(WebSocketFrame pongFrame)
		{

		}

		@Override
		protected void onMessage(WebSocketFrame messageFrame)
		{
			try
			{
				send("Received "+messageFrame.getTextPayload());
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		@Override
		protected void onClose(CloseCode code, String reason, boolean initiatedByRemote)
		{
			System.out.println("websocket closed......");
		}

		@Override
		protected void onException(IOException e)
		{

		}

		@Override
		protected void onOpen()
		{
			System.out.println("websocket opened......");
		}

	}

	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake)
	{
		return new Ws(handshake);
	}
}
