/**
 * Provides action handler classes for processing WebSocket commands in the ROM manager web server.
 * <p>
 * This package contains classes that implement the server-side action handlers for various retro-gaming ROM management operations,
 * including:
 * </p>
 * <ul>
 * <li><b>Profile management</b> - Loading, importing, scanning, and fixing ROM profiles</li>
 * <li><b>Report filtering</b> - Managing scan result filters and report generation</li>
 * <li><b>Progress tracking</b> - Broadcasting operation progress to connected clients</li>
 * <li><b>Metadata management</b> - Loading CatVer (category/version) and NPlayers (player count) metadata files</li>
 * <li><b>Utility operations</b> - Global settings, memory management, and user notifications</li>
 * </ul>
 * <p>
 * Each action handler class follows a consistent pattern:
 * </p>
 * <ol>
 * <li>Receives a JSON command from the {@link jrm.server.shared.actions.ActionsMgr} router</li>
 * <li>Parses command parameters from the JSON payload</li>
 * <li>Executes the requested operation, potentially in a background worker thread</li>
 * <li>Sends status updates and results back to the client via WebSocket messages</li>
 * </ol>
 * <p>
 * All action handlers require an {@link jrm.server.shared.actions.ActionsMgr} instance for session access and WebSocket
 * communication.
 * </p>
 *
 * @author optyfr
 * 
 * @see jrm.server.shared.actions.ActionsMgr
 * @see jrm.server.shared.WebSession
 */
package jrm.server.shared.actions;
