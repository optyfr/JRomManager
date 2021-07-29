package jrm.fullserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
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

import jrm.fullserver.handlers.FullDataSourceServlet;
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
	private static final String CACHE_CONTROL = "cacheControl";
	private static final String PRECOMPRESSED = "precompressed";
	private static final String ACCEPT_RANGES = "acceptRanges";
	private static final String DIR_ALLOWED = "dirAllowed";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	
	private Path clientPath;
	private final boolean debug;

	private static final String KEY_STORE = "jrt:/jrm.merged.module/certs/";
	private static final String KEY_STORE_PATH_DEFAULT = KEY_STORE + "localhost.pfx";
	private static final String KEY_STORE_PW_PATH_DEFAULT = KEY_STORE + "localhost.pw";
	private static final String BIND_DEFAULT = "0.0.0.0";
	private static final int HTTP_PORT_DEFAULT = 8080;
	private static final int HTTPS_PORT_DEFAULT = 8443;
	private static final int PROTOCOLS_DEFAULT = 0xff;
	private static final int CONNLIMIT_DEFAULT = 50;

	private final String keyStorePath;
	private final String keyStorePWPath;
	private final int protocols; // bit 1 = HTTP, bit 2 = HTTPS, bit 3 = HTTP2 (with bit 2)
	private final int httpPort;
	private final int httpsPort;
	private final String bind;
	private final int connlimit;

	public FullServer(CommandLine cmd) throws IOException, SQLException, InterruptedException, JettyException
	{
		clientPath = getOption(cmd, 'c').map(Paths::get).orElse(URIUtils.getPath("jrt:/jrm.merged.module/webclient/"));
		bind = getOption(cmd, 'b').orElse(BIND_DEFAULT);
		httpPort = getOption(cmd, 'p').map(Integer::parseInt).orElse(HTTP_PORT_DEFAULT);
		httpsPort = getOption(cmd, 's').map(Integer::parseInt).orElse(HTTPS_PORT_DEFAULT);
		keyStorePath = getOption(cmd, 'C').filter(p->Files.exists(Paths.get(p))).orElse(KEY_STORE_PATH_DEFAULT);
		if (Files.exists(Paths.get(keyStorePath + ".pw")))
			keyStorePWPath = keyStorePath + ".pw";
		else if(keyStorePath.equals(KEY_STORE_PATH_DEFAULT) && Files.exists(Paths.get(KEY_STORE_PW_PATH_DEFAULT)))
			keyStorePWPath = KEY_STORE_PW_PATH_DEFAULT;
		else
			keyStorePWPath = null;
		getOption(cmd, 'w').map(s -> s.replace("%HOMEPATH%", System.getProperty("user.home"))).ifPresent(s -> System.setProperty("jrommanager.dir", s));
		debug = cmd.hasOption('d');
		protocols = PROTOCOLS_DEFAULT;
		connlimit = CONNLIMIT_DEFAULT;
		
		Locale.setDefault(Locale.US);
		System.setProperty("file.encoding", "UTF-8");
		Log.init(getLogPath() + "/Server.%g.log", false, 1024 * 1024, 5);

		final var jettyserver = new Server();

		final var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setBaseResource(Resource.newResource(this.clientPath));
		context.setContextPath("/");

		context.setGzipHandler(gzipHandler());

		context.addServlet(new ServletHolder("datasources", FullDataSourceServlet.class), "/datasources/*");
		context.addServlet(new ServletHolder("images", ImageServlet.class), "/images/*");
		context.addServlet(new ServletHolder("session", SessionServlet.class), "/session");
		context.addServlet(new ServletHolder("actions", ActionServlet.class), "/actions/*");
		context.addServlet(new ServletHolder("upload", UploadServlet.class), "/upload/*");
		context.addServlet(new ServletHolder("download", DownloadServlet.class), "/download/*");
		context.addServlet(holderStaticNoCache(), "*.nocache.js");
		context.addServlet(holderStaticCache(), "*.cache.js");
		context.addServlet(holderStaticJS(), "*.js");
		context.addServlet(holderStatic(), "/");

		setSecurity(context);

		context.getSessionHandler().setMaxInactiveInterval(300);
		context.getSessionHandler().addEventListener(new SessionListener());

		jettyserver.setHandler(context);
		jettyserver.setStopAtShutdown(true);

		// LetsEncrypt certs with embedded Jetty on
		// HTTP Configuration
		final var config = new HttpConfiguration();
		config.addCustomizer(new SecureRequestCustomizer());
		config.addCustomizer(new ForwardedRequestCustomizer());

		if ((protocols & 0x1) == 0x1)
		{
			// Create the HTTP connection
			jettyserver.addConnector(httpConnector(jettyserver, config));
		}

		if ((protocols & 0x2) == 0x2 && URIUtils.URIExists(keyStorePath))
		{
			if ((protocols & 0x4) == 0x4)
				jettyserver.addConnector(http2Connector(jettyserver));
			else
				jettyserver.addConnector(httpsConnector(jettyserver));
		}

		jettyserver.addBean(new ConnectionLimit(connlimit, jettyserver)); // limit simultaneous connections

		try
		{
			jettyserver.start();
			Log.config("Start server");
			for (final var connector : jettyserver.getConnectors())
				Log.config(((ServerConnector) connector).getName() + " with port on " + ((ServerConnector) connector).getPort() + " binded to " + ((ServerConnector) connector).getHost());
			Log.config("clientPath: " + clientPath);
			Log.config("workPath: " + getWorkPath());
			waitStop(jettyserver);
		}
		catch (InterruptedException|JettyException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new JettyException(e.getMessage(),e);
		}
	}

	private Optional<String> getOption(CommandLine cmd, char c)
	{
		if(cmd.hasOption(c))
			return Optional.ofNullable(cmd.getOptionValue(c));
		return Optional.empty();
	}
	
	/**
	 * @param jettyserver
	 * @return
	 * @throws IOException
	 */
	private ServerConnector httpsConnector(final Server jettyserver) throws IOException
	{
		// SSL Context Factory for HTTPS and HTTP/2
		final var sslContextFactory = sslContext();

		// Reload certificate every day at midnight (used for certificate renewal)
		SSLReload.getInstance(sslContextFactory).start();

		// Create the HTTPS end point
		final var httpsConfig = httpsConfig();

		// SSL Connection Factory
		final SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());

		// HTTPS Connector
		final var httpsConnector = new ServerConnector(jettyserver, ssl, new HttpConnectionFactory(httpsConfig));
		httpsConnector.setPort(httpsPort);
		httpsConnector.setHost(bind);
		httpsConnector.setName("HTTPS");
		return httpsConnector;
	}

	/**
	 * @param jettyserver
	 * @return
	 * @throws IOException
	 */
	private ServerConnector http2Connector(final Server jettyserver) throws IOException
	{
		// SSL Context Factory for HTTPS and HTTP/2
		final var sslContextFactory = sslContext();

		// Reload certificate every day at midnight (used for certificate renewal)
		SSLReload.getInstance(sslContextFactory).start();

		// Create the HTTPS end point
		final var httpsConfig = httpsConfig();

		// HTTP/2 Connection Factory
		final var h2 = new HTTP2ServerConnectionFactory(httpsConfig);

		Security.insertProviderAt(new OpenSSLProvider(), 1); // Temporary fix for conflicting SSL providers
		final var alpn = new ALPNServerConnectionFactory();
		alpn.setDefaultProtocol(HttpVersion.HTTP_1_1.asString());

		// SSL Connection Factory
		final var ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

		// HTTP/2 Connector
		final var http2Connector = new ServerConnector(jettyserver, ssl, alpn, h2, new HttpConnectionFactory(httpsConfig));
		http2Connector.setPort(httpsPort);
		http2Connector.setHost(bind);
		http2Connector.setName("HTTP2");
		return http2Connector;
	}

	/**
	 * @return
	 */
	private HttpConfiguration httpsConfig()
	{
		final var httpsConfig = new HttpConfiguration();
		httpsConfig.setSecureScheme("https");
		httpsConfig.setSecurePort(httpsPort);
		httpsConfig.addCustomizer(new SecureRequestCustomizer());
		return httpsConfig;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private org.eclipse.jetty.util.ssl.SslContextFactory.Server sslContext() throws IOException
	{
		var sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStoreType("PKCS12");
		sslContextFactory.setKeyStorePath(keyStorePath);
		sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
		sslContextFactory.setUseCipherSuitesOrder(true);

		String keyStorePassword = (keyStorePWPath != null && URIUtils.URIExists(keyStorePWPath)) ? URIUtils.readString(keyStorePWPath).trim() : "";
		sslContextFactory.setKeyStorePassword(keyStorePassword);
		sslContextFactory.setKeyManagerPassword(keyStorePassword);
		return sslContextFactory;
	}

	/**
	 * @param jettyserver
	 * @param config
	 * @return
	 */
	private ServerConnector httpConnector(final Server jettyserver, final HttpConfiguration config)
	{
		final var httpConnectionFactory = new HttpConnectionFactory(config);
		final var httpConnector = new ServerConnector(jettyserver, httpConnectionFactory);
		httpConnector.setPort(httpPort);
		httpConnector.setHost(bind);
		httpConnector.setName("HTTP");
		return httpConnector;
	}

	/**
	 * @return
	 */
	private GzipHandler gzipHandler()
	{
		final var gzipHandler = new GzipHandler();
		gzipHandler.setIncludedMethods("POST", "GET");
		gzipHandler.setIncludedMimeTypes("text/html", "text/plain", "text/xml", "text/css", "application/javascript", "text/javascript", "application/json");
		gzipHandler.setInflateBufferSize(2048);
		gzipHandler.setMinGzipSize(2048);
		return gzipHandler;
	}

	/**
	 * @param context
	 * @throws IOException
	 * @throws SQLException
	 */
	private void setSecurity(final ServletContextHandler context) throws IOException, SQLException
	{
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
	}

	/**
	 * @return
	 */
	private ServletHolder holderStatic()
	{
		final var holderStatic = new ServletHolder("static", DefaultServlet.class);
		holderStatic.setInitParameter(DIR_ALLOWED, FALSE);
		holderStatic.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStatic.setInitParameter(PRECOMPRESSED, TRUE);
		return holderStatic;
	}

	/**
	 * @return
	 */
	private ServletHolder holderStaticJS()
	{
		final var holderStaticJS = new ServletHolder("static_js", DefaultServlet.class);
		holderStaticJS.setInitParameter(DIR_ALLOWED, FALSE);
		holderStaticJS.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStaticJS.setInitParameter(PRECOMPRESSED, TRUE);
		holderStaticJS.setInitParameter(CACHE_CONTROL, "public, max-age=0, must-revalidate");
		return holderStaticJS;
	}

	/**
	 * @return
	 */
	private ServletHolder holderStaticCache()
	{
		final var holderStaticCache = new ServletHolder("static_cache", DefaultServlet.class);
		holderStaticCache.setInitParameter(DIR_ALLOWED, FALSE);
		holderStaticCache.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStaticCache.setInitParameter(PRECOMPRESSED, TRUE);
		return holderStaticCache;
	}

	/**
	 * @return
	 */
	private ServletHolder holderStaticNoCache()
	{
		final var holderStaticNoCache = new ServletHolder("static_nocache", DefaultServlet.class);
		holderStaticNoCache.setInitParameter(DIR_ALLOWED, FALSE);
		holderStaticNoCache.setInitParameter(ACCEPT_RANGES, TRUE);
		holderStaticNoCache.setInitParameter(PRECOMPRESSED, FALSE);
		holderStaticNoCache.setInitParameter(CACHE_CONTROL, "no-store");
		return holderStaticNoCache;
	}

	/**
	 * @param jettyserver
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private void waitStop(final Server jettyserver) throws InterruptedException, JettyException
	{
		try
		{
			Runtime.getRuntime().addShutdownHook(new Thread(() -> Log.info("Server stopped.")));
			if (debug)
			{
				try (final var sc = new Scanner(System.in))
				{
					// wait until receive stop command from keyboard
					System.out.println("Enter 'stop' to halt: ");	//NOSONAR
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
		catch (InterruptedException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new JettyException(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	private class JettyException extends Exception
	{
		@SuppressWarnings("unused")
		public JettyException()
		{
			super();
		}
		
		public JettyException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
	
	public static void main(String[] args)
	{
		final var options = new Options();
		options.addOption(new Option("c", "client", true, "Client Path"));
		options.addOption(new Option("w", "workpath", true, "Working Path"));
		options.addOption(new Option("d", "debug", false, "Debug"));
		options.addOption(new Option("C", "cert", true, "cert file, default is " + KEY_STORE_PATH_DEFAULT));
		options.addOption(new Option("s", "https", true, "https port, default is " + HTTPS_PORT_DEFAULT));
		options.addOption(new Option("p", "http", true, "http port, default is " + HTTP_PORT_DEFAULT));
		options.addOption(new Option("b", "bind", true, "bind to address or host, default is " + BIND_DEFAULT));

		try
		{
			CommandLine cmd = new DefaultParser().parse(options, args);
			new FullServer(cmd);
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
			e.printStackTrace();
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
		final var path = getWorkPath().resolve("logs");
		Files.createDirectories(path);
		return path.toString();
	}

}
