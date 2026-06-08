package jrm.server.shared.actions;

import java.io.IOException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;

import jrm.misc.Log;
import jrm.server.shared.SessionStub;
import jrm.server.shared.WebSession;
import lombok.RequiredArgsConstructor;

/**
 * Interface for managing and routing JSON actions and commands within a web
 * session.
 */
public interface ActionsMgr extends SessionStub {

    /**
     * Sends a message to the client. This is used by the actions to send responses
     * back to the client. @param msg the message to send to the client
     * 
     * @param msg the message to send to the client
     */
    public void send(String msg) throws IOException;

    /**
     * Sends a message to the client if the session is open. This is used by the
     * actions to send responses back to the client without throwing an exception if
     * the session is closed. @param msg the message to send to the client
     * 
     * @param msg the message to send to the client
     */
    public void sendOptional(String msg) throws IOException;

    /**
     * Checks if the session is open. This is used by the actions to check if the
     * session is open before sending a message to the client. @return true if the
     * session is open, false otherwise
     * 
     * @return true if the session is open, false otherwise
     */
    public boolean isOpen();

    /**
     * Gets the WebSession associated with this ActionsMgr. This is used by the
     * actions to access the session information and data. @return the WebSession
     * associated with this ActionsMgr
     * 
     * @return the WebSession associated with this ActionsMgr
     */
    public WebSession getSession();

    /**
     * Processes a JSON object containing a command and its parameters, routing it
     * to the appropriate action handler based on the "cmd" field. This method
     * updates the last action time in the session and handles various commands such
     * as setting properties, importing/exporting profiles, managing reports, and
     * starting specific actions. If an unknown command is received, it logs an
     * error message. @param mgr the ActionsMgr instance to use for processing the
     * actions
     * 
     * @param mgr the ActionsMgr instance to use for processing the actions
     * @param jso the JsonObject containing the command and its parameters
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

    @RequiredArgsConstructor
    static class UpdateResult {
        final String cmd;
        final Params params;

        @RequiredArgsConstructor
        static class Params {
            final int row;
            final String result;
        }
    }

    @RequiredArgsConstructor
    static class SingleCmd {
        final String cmd;
    }

}
