package jrm.fullserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.Security;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.daemon.DaemonContext;
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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import jrm.fullserver.handlers.FullDataSourceServlet;
import jrm.fullserver.handlers.SessionServlet;
import jrm.fullserver.security.BasicAuthenticator;
import jrm.fullserver.security.Login;
import jrm.fullserver.security.SSLReload;
import jrm.misc.Log;
import jrm.misc.URIUtils;
import jrm.server.AbstractServer;
import jrm.server.SessionListener;
import jrm.server.shared.handlers.ActionServlet;
import jrm.server.shared.handlers.DownloadServlet;
import jrm.server.shared.handlers.ImageServlet;
import jrm.server.shared.handlers.UploadServlet;

public class FullServer extends AbstractServer
{
	private static final String KEY_STORE = "jrt:/jrm.merged.module/certs/";
	private static final String KEY_STORE_PATH_DEFAULT = KEY_STORE + "localhost.pfx";
	private static final String KEY_STORE_PW_PATH_DEFAULT = KEY_STORE + "localhost.pw";
	private static final String BIND_DEFAULT = "0.0.0.0";
	private static final int HTTP_PORT_DEFAULT = 8080;
	private static final int HTTPS_PORT_DEFAULT = 8443;
	private static final int PROTOCOLS_DEFAULT = 0xff;
	private static final int CONNLIMIT_DEFAULT = 50;

	private static String keyStorePath;
	private static String keyStorePWPath;
	private static int protocols; // bit 1 = HTTP, bit 2 = HTTPS, bit 3 = HTTP2 (with bit 2)
	private static int httpPort;
	private static int httpsPort;
	private static String bind;
	private static int connlimit;

	@Parameters(separators = " =")
	private static class Args
	{
		@Parameter(names = { "-c", "--client", "--clientPath" }, arity = 1, description = "Client path")
		private String clientPath = null;
		@Parameter(names = { "-w", "--work", "--workpath" }, arity = 1, description = "Working path")
		private String workPath = null;
		@Parameter(names = { "-d", "--debug" }, description = "Activate debug mode")
		private boolean debug = false;
		@Parameter(names = { "-C", "--cert" }, arity = 1, description = "cert file, default is " + KEY_STORE_PATH_DEFAULT)
		private String cert = KEY_STORE_PATH_DEFAULT;
		@Parameter(names = { "-s", "--https" }, arity = 1, description = "https port, default is " + HTTPS_PORT_DEFAULT)
		private int httpsPort = HTTPS_PORT_DEFAULT;
		@Parameter(names = { "-p", "--http" }, arity = 1, description = "http port, default is " + HTTP_PORT_DEFAULT)
		private int httpPort = HTTP_PORT_DEFAULT;
		@Parameter(names = { "-b", "--bind" }, arity = 1, description = "bind to address or host, default is " + BIND_DEFAULT)
		private String bind = BIND_DEFAULT;
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	public static void parseArgs(String... args) throws IOException, URISyntaxException
	{
		final var jArgs = new Args();
		final var cmd = JCommander.newBuilder().addObject(jArgs).build();
		try
		{
			cmd.parse(args);
			debug = jArgs.debug;
			clientPath = getClientPath(jArgs.clientPath);
			bind = jArgs.bind;
			httpPort = jArgs.httpPort;
			httpsPort = jArgs.httpsPort;
			keyStorePath = Optional.of(jArgs.cert).filter(p -> Files.exists(getPath(p))).orElse(KEY_STORE_PATH_DEFAULT);
			if (Files.exists(getPath(keyStorePath + ".pw")))
				keyStorePWPath = keyStorePath + ".pw";
			else if (keyStorePath.equals(KEY_STORE_PATH_DEFAULT) && Files.exists(getPath(KEY_STORE_PW_PATH_DEFAULT)))
				keyStorePWPath = KEY_STORE_PW_PATH_DEFAULT;
			else
				keyStorePWPath = null;
			Optional.ofNullable(jArgs.workPath).map(s -> s.replace("%HOMEPATH%", System.getProperty("user.home"))).ifPresent(s -> System.setProperty("jrommanager.dir", s));
			protocols = PROTOCOLS_DEFAULT;
			connlimit = CONNLIMIT_DEFAULT;
			
			Locale.setDefault(Locale.US);
			System.setProperty("file.encoding", "UTF-8");
			Log.init(getLogPath() + "/Server.%g.log", false, 1024 * 1024, 5);
		}
		catch(ParameterException e)
		{
			Log.err(e.getMessage(), e);
			e.printStackTrace();
			cmd.usage();
			System.exit(1);
		}
	}

	/**
	 * @param jettyserver
	 * @return
	 * @throws IOException
	 */
	private static ServerConnector httpsConnector(final Server jettyserver) throws IOException
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
	private static ServerConnector http2Connector(final Server jettyserver) throws IOException
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
	private static HttpConfiguration httpsConfig()
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
	private static org.eclipse.jetty.util.ssl.SslContextFactory.Server sslContext() throws IOException
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
	private static ServerConnector httpConnector(final Server jettyserver, final HttpConfiguration config)
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
	private static GzipHandler gzipHandler()
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
	private static void setSecurity(final ServletContextHandler context) throws IOException, SQLException
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
			jettyserver = new Server();
	
			final var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setBaseResource(Resource.newResource(clientPath));
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
			context.getSessionHandler().addEventListener(new SessionListener(true));
	
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
	
			jettyserver.start();
			Log.config("Start server");
			for (final var connector : jettyserver.getConnectors())
				Log.config(((ServerConnector) connector).getName() + " with port on " + ((ServerConnector) connector).getPort() + " binded to " + ((ServerConnector) connector).getHost());
			Log.config("clientPath: " + clientPath);
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

}
