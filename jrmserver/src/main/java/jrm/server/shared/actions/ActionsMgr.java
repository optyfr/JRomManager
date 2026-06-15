package jrm.server.shared.actions;

import java.io.IOException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;

import jrm.misc.Log;
import jrm.server.shared.SessionStub;
import jrm.server.shared.WebSession;
import lombok.RequiredArgsConstructor;

/**
 * Interface for managing and routing JSON actions and commands within a web session.
 * <p>
 * This interface extends {@link SessionStub} to provide capabilities for processing, routing, and responding to various client
 * command requests.
 * </p>
 * <p>
 * <b>Command Routing:</b> The {@link #processActions(ActionsMgr, JsonObject)} method routes incoming JSON commands to appropriate
 * action handlers based on the "cmd" field. Supported commands include:
 * </p>
 * <ul>
 * <li><b>Global.*</b> - System-wide operations (property updates, memory management, garbage collection)</li>
 * <li><b>Profile.*</b> - Profile lifecycle operations (import, load, scan, fix, settings management)</li>
 * <li><b>Report.*</b> - Report filtering and display operations</li>
 * <li><b>CatVer.*</b> - Category/version metadata management</li>
 * <li><b>NPlayers.*</b> - Player count metadata management</li>
 * <li><b>Progress.*</b> - Progress tracking and cancellation</li>
 * <li><b>Dat2Dir.*</b> - DAT file to directory conversion</li>
 * <li><b>Dir2Dat.*</b> - Directory to DAT file conversion</li>
 * <li><b>TrntChk.*</b> - Torrent file verification</li>
 * <li><b>Compressor.*</b> - ROM compression operations</li>
 * </ul>
 *
 * @author optyfr
 * 
 * @see SessionStub
 * @see WebSession
 */
public interface ActionsMgr extends SessionStub {

    /**
     * Sends a message to the client. This is used by action handlers to transmit responses back to the active client session.
     * 
     * @param msg the message string to send to the client
     * 
     * @throws IOException if an I/O error occurs during transmission
     */
    public void send(String msg) throws IOException;

    /**
     * Sends a message to the client if the session is open. This is used by action handlers to transmit responses back to the
     * client without raising errors if the connection is terminated.
     * 
     * @param msg the message string to send to the client
     * 
     * @throws IOException if an I/O error occurs
     */
    public void sendOptional(String msg) throws IOException;

    /**
     * Checks if the session is currently open and active.
     * 
     * @return {@code true} if the session is open, {@code false} otherwise
     */
    public boolean isOpen();

    /**
     * Gets the {@link WebSession} associated with this {@code ActionsMgr}. This is used by action handlers to access session
     * configurations and active states.
     * 
     * @return the associated {@link WebSession} instance
     */
    public WebSession getSession();

    /**
     * Processes a JSON object containing a command and its parameters, routing it to the appropriate action handler based on the
     * "cmd" field.
     * <p>
     * This method updates the last action timestamp in the session and handles various commands such as setting properties,
     * importing/exporting profiles, managing reports, and starting specific backend actions. If an unknown command is received, it
     * logs an error message.
     * </p>
     * 
     * @param mgr the {@code ActionsMgr} instance to use for processing the actions
     * @param jso the {@link JsonObject} containing the command and its parameters
     */
    public default void processActions(ActionsMgr mgr, JsonObject jso) {
        try {
            if (jso != null) {
                mgr.getSession().setLastAction(new Date());
                switch (jso.getString("cmd", "unknown")) {
                    case "Global.setProperty" -> new GlobalActions(this).setProperty(jso);
                    case "Global.getMemory" -> new GlobalActions(this).setMemory(jso);
                    case "Global.GC" -> new GlobalActions(this).gc(jso);
                    case "Profile.import" -> new ProfileActions(this).imprt(jso);
                    case "Profile.load" -> new ProfileActions(this).load(jso);
                    case "Profile.scan" -> new ProfileActions(this).scan(jso, true);
                    case "Profile.fix" -> new ProfileActions(this).fix(jso);
                    case "Profile.importSettings" -> new ProfileActions(this).importSettings(jso);
                    case "Profile.exportSettings" -> new ProfileActions(this).exportSettings(jso);
                    case "Profile.setProperty" -> new ProfileActions(this).setProperty(jso);
                    case "ReportLite.setFilter" -> new ReportActions(this).setFilter(jso, true);
                    case "Report.setFilter" -> new ReportActions(this).setFilter(jso, false);
                    case "CatVer.load" -> new CatVerActions(this).load(jso);
                    case "NPlayers.load" -> new NPlayersActions(this).load(jso);
                    case "Progress.cancel" -> {
                        if (mgr.getSession().getWorker() != null && mgr.getSession().getWorker().isAlive() && mgr.getSession().getWorker().progress != null) {
                            mgr.getSession().getWorker().getProgress().doCancel();
                        }
                    }
                    case "Dat2Dir.start" -> new Dat2DirActions(this).start(jso);
                    case "Dir2Dat.start" -> new Dir2DatActions(this).start(jso);
                    case "TrntChk.start" -> new TrntChkActions(this).start(jso);
                    case "Compressor.start" -> new CompressorActions(this).start(jso);
                    case "Dat2Dir.settings" -> new Dat2DirActions(this).settings(jso);
                    default -> Log.err(() -> "Unknown command : " + jso.getString("cmd", "unknown"));
                }
            }
        } catch (Exception e) {
            Log.err(e.getMessage(), e);
        }
    }

    /**
     * Represents the JSON-serializable result of an update action sent back to the client.
     */
    @RequiredArgsConstructor
    static class UpdateResult {

        /**
         * The command identifier associated with this update result.
         */
        final String cmd;

        /**
         * The parameter details nested within the update result.
         */
        final Params params;

        /**
         * Nested parameter details containing row index and action operation results.
         */
        @RequiredArgsConstructor
        static class Params {

            /**
             * The targeted data row index.
             */
            final int row;

            /**
             * The textual result or status description of the execution.
             */
            final String result;
        }
    }

    /**
     * Represents a single standalone client command request wrapper containing only the command name.
     */
    @RequiredArgsConstructor
    static class SingleCmd {

        /**
         * The name of the command to trigger.
         */
        final String cmd;
    }

}
