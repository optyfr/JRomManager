package jrm.fullserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.Collections;
import java.util.Locale;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import jrm.fullserver.handlers.DataSourceServlet;
import jrm.fullserver.handlers.SessionServlet;
import jrm.fullserver.security.BasicAuthenticator;
import jrm.fullserver.security.Login;
import jrm.fullserver.security.SSLReload;
import jrm.misc.Log;
import jrm.misc.URIUtils;
import jrm.server.shared.WebSession;
import jrm.server.shared.handlers.ActionServlet;
import jrm.server.shared.handlers.DownloadServlet;
import jrm.server.shared.handlers.ImageServlet;
import jrm.server.shared.handlers.UploadServlet;

public class FullServer
{
	private Path clientPath;
	private static boolean debug = false;

	private static final String KEY_STORE = "jrt:/jrm.merged.module/certs/";
	
	private static String KEY_STORE_PATH = KEY_STORE + "localhost.pfx";
	private static String KEY_STORE_PW_PATH = KEY_STORE + "localhost.pw";

	private static int PROTOCOLS = 0xff; // bit 1 = HTTP, bit 2 = HTTPS, bit 3 = HTTP2 (with bit 2)
	private static int HTTP_PORT = 8080;
	private static int HTTPS_PORT = 8443;
	private static String BIND = "0.0.0.0";
	private static int CONNLIMIT = 50;

	public FullServer(Path clientPath) throws Exception
	{
		this.clientPath = clientPath;

		final var jettyserver = new Server();

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

		final var holderStaticNoCache = new ServletHolder("static_nocache", DefaultServlet.class);
		holderStaticNoCache.setInitParameter("dirAllowed", "false");
		holderStaticNoCache.setInitParameter("acceptRanges", "true");
		holderStaticNoCache.setInitParameter("precompressed", "false");
		holderStaticNoCache.setInitParameter("cacheControl", "no-store");
		context.addServlet(holderStaticNoCache, "*.nocache.js");

		final var holderStaticCache = new ServletHolder("static_cache", DefaultServlet.class);
		holderStaticCache.setInitParameter("dirAllowed", "false");
		holderStaticCache.setInitParameter("acceptRanges", "true");
		holderStaticCache.setInitParameter("precompressed", "true");
		context.addServlet(holderStaticCache, "*.cache.js");

		final var holderStaticJS = new ServletHolder("static_js", DefaultServlet.class);
		holderStaticJS.setInitParameter("dirAllowed", "false");
		holderStaticJS.setInitParameter("acceptRanges", "true");
		holderStaticJS.setInitParameter("precompressed", "true");
		holderStaticJS.setInitParameter("cacheControl", "public, max-age=0, must-revalidate");
		context.addServlet(holderStaticJS, "*.js");

		final var holderStatic = new ServletHolder("static", DefaultServlet.class);
		holderStatic.setInitParameter("dirAllowed", "false");
		holderStatic.setInitParameter("acceptRanges", "true");
		holderStatic.setInitParameter("precompressed", "true");
		context.addServlet(holderStatic, "/");

		context.getSessionHandler().setMaxInactiveInterval(300);

		// Authentification server by login & password
		final var security = new ConstraintSecurityHandler();
		security.setAuthenticator(new BasicAuthenticator());
		security.setLoginService(new Login());

		final var constraint = new Constraint();
		constraint.setName("auth");
		constraint.setAuthenticate(true);
		constraint.setRoles(new String[] { "admin", "user" });
		final var constraintMapping = new ConstraintMapping();
		constraintMapping.setConstraint(constraint);
		constraintMapping.setPathSpec("/*");
		security.setConstraintMappings(Collections.singletonList(constraintMapping));
		context.setSecurityHandler(security);

		context.getSessionHandler().addEventListener(new SessionListener());

		jettyserver.setHandler(context);
		jettyserver.setStopAtShutdown(true);

		// LetsEncrypt certs with embedded Jetty on
		// HTTP Configuration
		final var config = new HttpConfiguration();
		config.addCustomizer(new SecureRequestCustomizer());
		config.addCustomizer(new ForwardedRequestCustomizer());

		if ((PROTOCOLS & 0x1) != 0)
		{
			// Create the HTTP connection
			final var httpConnectionFactory = new HttpConnectionFactory(config);
			final var httpConnector = new ServerConnector(jettyserver, httpConnectionFactory);
			httpConnector.setPort(HTTP_PORT);
			httpConnector.setHost(BIND);
			httpConnector.setName("HTTP");
			jettyserver.addConnector(httpConnector);
		}

		if ((PROTOCOLS & 0x2) == 0x2 && URIUtils.URIExists(KEY_STORE_PATH))
		{
			// Create the HTTPS end point
			final var httpConfig = new HttpConfiguration();
			httpConfig.setSecureScheme("https");
			httpConfig.setSecurePort(HTTPS_PORT);

			// SSL Context Factory for HTTPS and HTTP/2
			var sslContextFactory = new SslContextFactory.Server();
			sslContextFactory.setKeyStoreType("PKCS12");
			sslContextFactory.setKeyStorePath(KEY_STORE_PATH);
			sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
			sslContextFactory.setUseCipherSuitesOrder(true);

			String keyStorePassword = (KEY_STORE_PW_PATH != null && URIUtils.URIExists(KEY_STORE_PW_PATH)) ? URIUtils.readString(KEY_STORE_PW_PATH).trim() : "";
			sslContextFactory.setKeyStorePassword(keyStorePassword);
			sslContextFactory.setKeyManagerPassword(keyStorePassword);

			// Reload certificate every day at midnight (used for certificate renewal)
			SSLReload.getInstance(sslContextFactory).start();

			// HTTPS Configuration
			final var httpsConfig = new HttpConfiguration(httpConfig);
			httpsConfig.addCustomizer(new SecureRequestCustomizer());

			if ((PROTOCOLS & 0x4) == 0x4)
			{

				// HTTP/2 Connection Factory
				final var h2 = new HTTP2ServerConnectionFactory(httpsConfig);

				Security.insertProviderAt(new OpenSSLProvider(), 1); // Temporary fix for conflicting SSL providers
				final var alpn = new ALPNServerConnectionFactory();
				alpn.setDefaultProtocol(HttpVersion.HTTP_1_1.asString());

				// SSL Connection Factory
				final var ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

				// HTTP/2 Connector
				final var http2Connector = new ServerConnector(jettyserver, ssl, alpn, h2, new HttpConnectionFactory(httpsConfig));
				http2Connector.setPort(HTTPS_PORT);
				http2Connector.setHost(BIND);
				http2Connector.setName("HTTP2");
				jettyserver.addConnector(http2Connector);
			}
			else
			{
				// HTTPS Connector
				final var httpsConnector = new ServerConnector(jettyserver, new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()), new HttpConnectionFactory(httpsConfig));
				httpsConnector.setPort(HTTPS_PORT);
				httpsConnector.setHost(BIND);
				httpsConnector.setName("HTTPS");
				jettyserver.addConnector(httpsConnector);
			}
		}

		jettyserver.addBean(new ConnectionLimit(CONNLIMIT, jettyserver)); // limit simultaneous connections

		jettyserver.start();
		Log.config("Start server");
		for (final var connector : jettyserver.getConnectors())
			Log.config(((ServerConnector) connector).getName() + " with port on " + ((ServerConnector) connector).getPort() + " binded to " + ((ServerConnector) connector).getHost());
		Log.config("clientPath: " + clientPath);
		Log.config("workPath: " + getWorkPath());
		waitStop(jettyserver);
	}

	/**
	 * @param jettyserver
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private void waitStop(final Server jettyserver) throws Exception
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() -> Log.info("Server stopped.")));
		if (debug)
		{
			try (final var sc = new Scanner(System.in))
			{
				// wait until receive stop command from keyboard
				System.out.println("Enter 'stop' to halt: ");
				while (!sc.nextLine().equalsIgnoreCase("stop"))
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

	public static void main(String[] args)
	{
		final var options = new Options();
		options.addOption(new Option("c", "client", true, "Client Path"));
		options.addOption(new Option("w", "workpath", true, "Working Path"));
		options.addOption(new Option("d", "debug", false, "Debug"));
		options.addOption(new Option("C", "cert", true, "cert file, default is " + KEY_STORE_PATH));
		options.addOption(new Option("s", "https", true, "https port, default is " + HTTPS_PORT));
		options.addOption(new Option("p", "http", true, "http port, default is " + HTTP_PORT));
		options.addOption(new Option("b", "bind", true, "bind to address or host, default is " + BIND));

		Path clientPath = null;
		try
		{
			CommandLine cmd = new DefaultParser().parse(options, args);
			String cpath;
			if (null == (cpath = cmd.getOptionValue('c')))
				clientPath = URIUtils.getPath("jrt:/jrm.merged.module/webclient/");
			else
				clientPath = Paths.get(cpath);
			if (cmd.hasOption('b'))
				BIND = cmd.getOptionValue('b');
			if (cmd.hasOption('p'))
				HTTP_PORT = Integer.parseInt(cmd.getOptionValue('p'));
			if (cmd.hasOption('s'))
				HTTPS_PORT = Integer.parseInt(cmd.getOptionValue('s'));
			if (cmd.hasOption('C') && Files.exists(Paths.get(cmd.getOptionValue('C'))))
			{
				KEY_STORE_PATH = cmd.getOptionValue('C');
				if (Files.exists(Paths.get(KEY_STORE_PATH + ".pw")))
					KEY_STORE_PW_PATH = KEY_STORE_PATH + ".pw";
				else
					KEY_STORE_PW_PATH = null;
			}
			if (cmd.hasOption('w'))
				System.setProperty("jrommanager.dir", cmd.getOptionValue('w').replace("%HOMEPATH%", System.getProperty("user.home")));
			if (cmd.hasOption('d'))
				debug = true;
			Locale.setDefault(Locale.US);
			System.setProperty("file.encoding", "UTF-8");
			Log.init(getLogPath() + "/Server.%g.log", false, 1024 * 1024, 5);
			new FullServer(clientPath);
		}
		catch (ParseException e)
		{
			Log.err(e.getMessage(), e);
			new HelpFormatter().printHelp("Server", options);
			System.exit(1);
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
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
		final var path = getWorkPath().resolve("logs");
		Files.createDirectories(path);
		return path.toString();
	}

}
