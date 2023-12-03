package jrm.fullserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.Security;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.daemon.DaemonContext;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.Constraint.Authorization;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.AcceptRateLimit;
import org.eclipse.jetty.server.ConnectionLimit;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

import jrm.fullserver.handlers.FullDataSourceServlet;
import jrm.fullserver.handlers.SessionServlet;
import jrm.fullserver.security.Login;
import jrm.fullserver.security.SSLReload;
import jrm.misc.DefaultEnvironmentProperties;
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
	private static final int RATELIMIT_DEFAULT = CONNLIMIT_DEFAULT / 10;
	private static final int MAXTHREADS_DEFAULT = CONNLIMIT_DEFAULT * 4;
	private static final int MINTHREADS_DEFAULT = CONNLIMIT_DEFAULT / 4;
	private static final int SESSIONTIMEOUT_DEFAULT = 300;

	private static Resource keyStorePath;
	private static String keyStorePWPath;
	private static int protocols; // bit 1 = HTTP, bit 2 = HTTPS, bit 3 = HTTP2 (with bit 2)
	private static int httpPort;
	private static int httpsPort;
	private static String bind;
	private static int connLimit = CONNLIMIT_DEFAULT;
	private static int rateLimit = RATELIMIT_DEFAULT;
	private static int maxThreads = MAXTHREADS_DEFAULT;
	private static int minThreads = MINTHREADS_DEFAULT;
	private static int sessionTimeOut = SESSIONTIMEOUT_DEFAULT;

	private static final DefaultEnvironmentProperties env = DefaultEnvironmentProperties.getInstance(FullServer.class);
	
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
		
		@Parameter(names = { "--conn-limit" }, arity = 1, description = "max simultaneous connection, default is " + CONNLIMIT_DEFAULT)
		private int connlimit = CONNLIMIT_DEFAULT;
		
		@Parameter(names = { "--rate-limit" }, arity = 1, description = "max connection rate per second, default is " + RATELIMIT_DEFAULT)
		private int ratelimit = RATELIMIT_DEFAULT;
		
		@Parameter(names = { "--max-threads" }, arity = 1, description = "max server threads, default is " + MAXTHREADS_DEFAULT)
		private int maxThreads = MAXTHREADS_DEFAULT;
		
		@Parameter(names = { "--min-threads" }, arity = 1, description = "min server threads, default is " + MINTHREADS_DEFAULT)
		private int minThreads = MINTHREADS_DEFAULT;
		
		@Parameter(names = { "--session-timeout" }, arity = 1, description = "session timeout, default is " + SESSIONTIMEOUT_DEFAULT)
		private int sessionTimeOut = MINTHREADS_DEFAULT;
		
	}

	private static void initFromEnv(Args jArgs)
	{
		Optional.ofNullable(env.getProperty("jrm.server.clientpath", jArgs.clientPath)).ifPresent(v -> jArgs.clientPath = v);
		Optional.ofNullable(env.getProperty("jrm.server.workpath", jArgs.workPath)).ifPresent(v -> jArgs.workPath = v);
		Optional.ofNullable(env.getProperty("jrm.server.debug", jArgs.debug)).ifPresent(v -> jArgs.debug = v);
		Optional.ofNullable(env.getProperty("jrm.server.cert", jArgs.cert)).ifPresent(v -> jArgs.cert = v);
		Optional.ofNullable(env.getProperty("jrm.server.https", jArgs.httpsPort)).ifPresent(v -> jArgs.httpsPort = v);
		Optional.ofNullable(env.getProperty("jrm.server.http", jArgs.httpPort)).ifPresent(v -> jArgs.httpPort = v);
		Optional.ofNullable(env.getProperty("jrm.server.bind", jArgs.bind)).ifPresent(v -> jArgs.bind = v);
		Optional.ofNullable(env.getProperty("jrm.server.connlimit", jArgs.connlimit)).ifPresent(v -> jArgs.connlimit = v);
		Optional.ofNullable(env.getProperty("jrm.server.ratelimit", jArgs.ratelimit)).ifPresent(v -> jArgs.ratelimit = v);
		Optional.ofNullable(env.getProperty("jrm.server.minthreads", jArgs.minThreads)).ifPresent(v -> jArgs.minThreads = v);
		Optional.ofNullable(env.getProperty("jrm.server.maxthreads", jArgs.maxThreads)).ifPresent(v -> jArgs.maxThreads = v);
		Optional.ofNullable(env.getProperty("jrm.server.sessiontimeout", jArgs.sessionTimeOut)).ifPresent(v -> jArgs.sessionTimeOut = v);
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
			initFromEnv(jArgs);
			
			cmd.parse(args);
			debug = jArgs.debug;
			clientPath = getClientPath(jArgs.clientPath);
			bind = jArgs.bind;
			httpPort = jArgs.httpPort;
			httpsPort = jArgs.httpsPort;
			keyStorePath = Optional.of(getCertsPath(jArgs.cert)).filter(p -> p.exists()).orElse(getCertsPath(null));
			if (Files.exists(getPath(keyStorePath + ".pw")))
				keyStorePWPath = keyStorePath + ".pw";
			else if (keyStorePath != null && keyStorePath.getPath() != null && KEY_STORE_PATH_DEFAULT.equals(keyStorePath.getPath()) && Files.exists(getPath(KEY_STORE_PW_PATH_DEFAULT)))
				keyStorePWPath = KEY_STORE_PW_PATH_DEFAULT;
			else
				keyStorePWPath = null;
			Optional.ofNullable(jArgs.workPath).map(s -> s.replace("%HOMEPATH%", System.getProperty("user.home"))).ifPresent(s -> System.setProperty("jrommanager.dir", s));
			protocols = PROTOCOLS_DEFAULT;
			connLimit = jArgs.connlimit;
			rateLimit = jArgs.ratelimit;
			minThreads = jArgs.minThreads;
			maxThreads = jArgs.maxThreads;
			sessionTimeOut = jArgs.sessionTimeOut;
			
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
		sslContextFactory.setKeyStorePath(keyStorePath.getURI().toString());
		sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);
		sslContextFactory.setUseCipherSuitesOrder(true);

		String keyStorePassword = (keyStorePWPath != null && Files.exists(getPath(keyStorePWPath))) ? URIUtils.readString(keyStorePWPath).trim() : "";
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

		final var constraint = Constraint.from("auth", Authorization.SPECIFIC_ROLE, "admin", "user");
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
			jettyserver = new Server(new QueuedThreadPool(maxThreads > 0?maxThreads:(connLimit * 4), minThreads > 0?minThreads:(connLimit / 4)));
	
			
			final var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
			context.setBaseResource(clientPath);
			context.setContextPath("/");
	
			context.insertHandler(gzipHandler());
	
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
	
			context.getSessionHandler().setMaxInactiveInterval(sessionTimeOut);
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
	
			if ((protocols & 0x2) == 0x2 && keyStorePath.exists())
			{
				if ((protocols & 0x4) == 0x4)
					jettyserver.addConnector(http2Connector(jettyserver));
				else
					jettyserver.addConnector(httpsConnector(jettyserver));
			}
	
			jettyserver.addBean(new ConnectionLimit(connLimit, jettyserver)); // limit simultaneous connections
			jettyserver.addBean(new AcceptRateLimit(rateLimit > 0 ? rateLimit : (connLimit / 10), 1, TimeUnit.SECONDS, jettyserver));	// rate limit
	
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
