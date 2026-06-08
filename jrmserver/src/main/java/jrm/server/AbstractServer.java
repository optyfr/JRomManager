package jrm.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jetty.ee9.servlet.DefaultServlet;
import org.eclipse.jetty.ee9.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.resource.Resources;

import jrm.fullserver.FullServer;
import jrm.misc.Log;
import jrm.server.shared.WebSession;

/**
 * AbstractServer is the base class for both FullServer and AdminServer, providing common functionalities such as resource loading, server management, and shutdown handling.
 */
public abstract class AbstractServer implements Daemon {

    /** Constants for servlet initialization parameters */
    private static final String PRECOMPRESSED = "precompressed";
    private static final String ACCEPT_RANGES = "acceptRanges";
    private static final String DIR_ALLOWED = "dirAllowed";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    /** The path to the client resources, which can be set via configuration or defaults to embedded resources. */
    protected static String clientPath;
    /** Shared Server instance used by both FullServer and AdminServer to manage the HTTP server lifecycle. */
    protected static Server jettyserver = null;
    /** Flag to enable debug mode, which allows for easier shutdown during development and testing. */
    protected static boolean debug;

    /**
     * Protected constructor to prevent direct instantiation of AbstractServer, as it is intended to be subclassed by specific server implementations.
     */
    protected AbstractServer() { /* No implementation needed for abstract class */
    }

    /**
     * Creates and configures a ServletHolder for serving static content, with parameters set to disable directory listing, enable range requests, and support precompressed files.
     * @return
     */
    protected static ServletHolder holderStatic() {
        final var holderStatic = new ServletHolder("static", DefaultServlet.class);
        holderStatic.setInitParameter(DIR_ALLOWED, FALSE);
        holderStatic.setInitParameter(ACCEPT_RANGES, TRUE);
        holderStatic.setInitParameter(PRECOMPRESSED, TRUE);
        return holderStatic;
    }

    /**
     * @return {@link GzipHandler}
     */
    @SuppressWarnings("removal")
    protected static GzipHandler gzipHandler() {
        final var gzipHandler = new GzipHandler();
        gzipHandler.setIncludedMethods("POST", "GET");
        gzipHandler.setIncludedMimeTypes("text/html", "text/plain", "text/xml", "text/css", "application/javascript", "text/javascript", "application/json");
        gzipHandler.setInflateBufferSize(2048);
        gzipHandler.setMinGzipSize(2048);
        return gzipHandler;
    }

    /**
     * get working path
     * @return {@link Path}
     */
    protected static Path getWorkPath() {
        String base = System.getProperty("jrommanager.dir");
        if (base == null)
            base = System.getProperty("user.dir");
        return Paths.get(base);
    }

    /**
     * Creates the logs directory if it does not exist and returns its path as a string.
     * @return {@link String}
     * @throws IOException
     */
    protected static String getLogPath() throws IOException {
        final var path = getWorkPath().resolve("logs");
        Files.createDirectories(path);
        return path.toString();
    }

    /**
     * Adds a shutdown hook to gracefully stop the server when the application is terminated. In debug mode or on Windows, it waits for a "stop" command from the console to halt the server, while in production it waits for the server thread to join.
     * @throws InterruptedException
     * @throws JettyException
     */
    protected static void waitStop() throws InterruptedException, JettyException {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (isStarted()) {
                    try {
                        terminate();
                        Log.info("Server stopped.");
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }));
            if (debug || SystemUtils.IS_OS_WINDOWS) {
                try (final var sc = new Scanner(System.in)) {
                    // wait until receive stop command from keyboard
                    System.out.println("Enter 'stop' to halt: "); // NOSONAR
                    while (!sc.nextLine().equalsIgnoreCase("stop"))
                        Thread.sleep(1000);
                    System.exit(0);
                }
            } else if (isStarted())
                jettyserver.join();
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            throw new JettyException(e.getMessage(), e);
        }
    }

    /**
     * terminate the server
     * @throws Exception
     */
    public static synchronized void terminate() throws Exception {
        WebSession.closeAll();
        if (jettyserver != null) {
            if (jettyserver.isStarted())
                jettyserver.stop();
            jettyserver = null;
        }
    }

    /**
     *  Checks if the server is currently started by verifying that the jettyserver instance is not null and is in a started state.
     *  @return {@link boolean}
     */
    public static final synchronized boolean isStarted() {
        return jettyserver != null && jettyserver.isStarted();

    }

    /**
     * Checks if the server is currently stopped by verifying that the jettyserver instance is null, indicating that it has not been initialized or has been terminated.
     * @return {@link boolean}
     */
    public static final synchronized boolean isStopped() {
        return jettyserver == null;

    }

    /**
     * Custom exception class for handling Jetty-related errors, providing constructors for various error scenarios including message-only and message with cause.
     */
    @SuppressWarnings("serial")
    protected static class JettyException extends Exception {
        /** Default constructor for JettyException, allowing for instantiation without a message or cause. */
        public JettyException() {
            super();
        }

        /** Constructor for JettyException that accepts a message describing the error. */
        public JettyException(String message) {
            super(message);
        }

        /** Constructor for JettyException that accepts both a message and a cause, allowing for chaining exceptions. */
        public JettyException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Retrieves a Resource object representing the client path, checking various locations such as the provided path, classloader resources, and embedded resources within the JRT filesystem. If the resource cannot be found in any of these locations, a FileNotFoundException is thrown.
     * @param resourceFactory The ResourceFactory used to create Resource instances.
     * @param path The optional path to the client resources, which can be specified by the user or configuration.
     * @return {@link Resource}
     * @throws IOException
     * @throws URISyntaxException
     */
    protected static Resource getClientPath(ResourceFactory resourceFactory, String path) throws IOException, URISyntaxException {
        Resource resource;

        if (path != null) {
            resource = resourceFactory.newResource(path);
            if (Resources.exists(resource))
                return resource;
            resource = resourceFactory.newClassLoaderResource(path, true);
            if (Resources.exists(resource))
                return resource;
        }

        resource = resourceFactory.newResource("jrt:/jrm.merged.module/webclient/");
        if (Resources.exists(resource))
            return resource;
        URL url = FullServer.class.getResource("/webclient/");
        if (url != null) {
            resource = resourceFactory.newResource(URIUtil.correctURI(url.toURI()));
            if (Resources.exists(resource))
                return resource;
        }
        throw new FileNotFoundException("Unable to find webclient path");
    }

    /**
     * Retrieves a Resource object representing the certificates path, checking various locations such as the provided path, classloader resources, and embedded resources within the JRT filesystem. If the resource cannot be found in any of these locations, a FileNotFoundException is thrown.
     * @param path The optional path to the certificate file, which can be specified by the user or configuration.
     * @return {@link Resource}
     * @throws IOException
     * @throws URISyntaxException
     */
    protected static Resource getCertsPath(String path) throws URISyntaxException, IOException {
        Resource resource;
        final var resourceFactory = ResourceFactory.root();
        if (path != null) {
            resource = resourceFactory.newResource(path);
            if (Resources.exists(resource))
                return resource;
            resource = resourceFactory.newClassLoaderResource(path, true);
            if (Resources.exists(resource))
                return resource;
        }
        resource = resourceFactory.newResource("jrt:/jrm.merged.module/certs/localhost.pfx");
        if (Resources.exists(resource))
            return resource;
        URL url = FullServer.class.getResource("/certs/localhost.pfx");
        if (url != null) {
            resource = resourceFactory.newResource(URIUtil.correctURI(url.toURI()));
            if (Resources.exists(resource))
                return resource;
        }
        throw new FileNotFoundException("Unable to find webclient path");
    }

    /**
     * Converts a given string path to a Path object, handling various URI schemes such as "jrt:", "file:", and "jar:". If the path cannot be converted directly, it attempts to create a new filesystem for the URI and then returns the corresponding Path. If all attempts fail, it returns null.
     * @param path The string representation of the path to be converted to a Path object.
     * @return {@link Path}
     */
    protected static Path getPath(String path) {
        try {
            return path.startsWith("jrt:") || path.startsWith("file:") || path.startsWith("jar:") ? Path.of(URI.create(path)) : Paths.get(path);
        } catch (FileSystemNotFoundException e) {
            final var uri = URI.create(path);
            try {
                FileSystems.newFileSystem(uri, Collections.emptyMap());
                return Path.of(uri);
            } catch (IOException e1) {
                return null;
            }
        }
    }

}
