/**
 * Provides HTTP servlet handlers for the JRM web server's client-server communication layer.
 * <p>
 * This package contains the core servlets responsible for processing web client requests and serving various types of content in
 * the JRM retro-gaming ROM manager application.
 * <h2>Servlet Overview</h2>
 * <ul>
 * <li><b>ActionServlet</b> - Handles client action commands and implements long polling for efficient server-to-client
 * notifications</li>
 * <li><b>DataSourceServlet</b> - Processes XML-based data source requests and routes them to appropriate response handlers for
 * profile, ROM, and batch operation data</li>
 * <li><b>DownloadServlet</b> - Manages file and directory downloads with support for single file streaming and ZIP archive
 * generation</li>
 * <li><b>ImageServlet</b> - Serves static resource icons and images with HTTP caching and conditional GET support</li>
 * </ul>
 * <h2>Key Features</h2>
 * <ul>
 * <li>Session-based authentication and authorization via {@link jrm.server.shared.WebSession}</li>
 * <li>Secure path resolution using {@link jrm.security.PathAbstractor}</li>
 * <li>Efficient streaming of large data sets via temporary files</li>
 * <li>HTTP caching and conditional request handling</li>
 * <li>Long polling mechanism for real-time server notifications</li>
 * </ul>
 * 
 * @see jrm.server.shared.WebSession
 * @see jrm.security.PathAbstractor
 * @see jrm.server.shared.datasources.XMLRequest
 * @see jrm.server.shared.datasources.XMLResponse
 */
package jrm.server.shared.handlers;
