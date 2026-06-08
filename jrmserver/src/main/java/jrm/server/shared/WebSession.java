package jrm.server.shared;

import java.io.Closeable;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import jrm.batch.Compressor.FileResult;
import jrm.batch.TrntChkReport;
import jrm.profile.report.Report;
import jrm.security.Session;
import lombok.Getter;
import lombok.Setter;

/**
 * class to represent a web session for the client. This class is used to store the state of the session, such as the messages to be sent to the client, the worker thread that is currently running for this session, and the temporary report that is being generated for the client. The session is created when the client connects to the server, and it is destroyed when the client disconnects from the server. The session is also used to store the cached profile lists and compressor lists for this session, so that they can be accessed by the worker thread when they are generated.
 */
public class WebSession extends Session implements Closeable, Serializable {
    /** The serial version UID for serialization. */
    private static final long serialVersionUID = 1L;

    /** All active sessions, mapped by session ID. */
    private static Map<String, WebSession> allSessions = new ConcurrentHashMap<>();
    /** Whether to terminate all sessions. This is set to true when the server is shutting down, and all sessions should stop their work and close themselves.
     * @return whether to terminate all sessions
     */
    private static @Getter boolean terminate = false;
    /** The messages to be sent to the client. This is a blocking deque, so that the worker thread can add messages to it, and the client can take messages from it.
     * @return the messages to be sent to the client
     */
    private @Getter BlockingDeque<String> lprMsg = new LinkedBlockingDeque<>();

    /** The worker thread that is currently running for this session. This is set by the ProgressReporter when the worker is started, and it is used to interrupt the worker thread when the session is closed. Note that this is not thread-safe, but it is only set once when the worker is started, so it should be fine.
     * @return the worker thread that is currently running for this session
     */
    private transient @Getter Worker worker = null;

    /** Sets the worker thread that is currently running for this session. This is set by the ProgressReporter when the worker is started, and it is used to interrupt the worker thread when the session is closed. Note that this is not thread-safe, but it is only set once when the worker is started, so it should be fine.
     * @param worker the worker thread that is currently running for this session
     * @return the worker thread that is currently running for this session
     */
    public Worker setWorker(Worker worker) {
        this.worker = worker;
        return this.worker;
    }

    /** The last action time for this session. This is updated whenever the client performs an action, and it is used to determine when to expire the session. Note that this is not thread-safe, but it is only updated by the client thread, so it should be fine.
     * @param lastAction the last action time for this session
     * @return the last action time for this session
     */
    private @Getter @Setter Date lastAction = new Date();

    /** The temporary report for this session. This is used to store the report that is being generated for the client, and it is cleared when the report is sent to the client. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     * @param tmpReport the temporary report for this session
     * @return the temporary report for this session
     */
    private transient @Getter @Setter Report tmpReport = null;
    /** The temporary TrntChkReport for this session. This is used to store the report that is being generated for the client, and it is cleared when the report is sent to the client. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     * @param tmpTCReport the temporary TrntChkReport for this session
     * @return the temporary TrntChkReport for this session
     */
    private transient @Getter @Setter TrntChkReport tmpTCReport = null;
    /** The cached profile list for this session. This is used to store the profile lists that are generated for the client, and it is cleared when the session is closed. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine. */
    private transient TreeMap<Integer, Path> cachedProfileList = null;
    /** The cached compressor list for this session. This is used to store the compressor lists that are generated for the client, and it is cleared when the session is closed. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     * @return the cached compressor list for this session
     */
    private transient @Getter TreeMap<String, FileResult> cachedCompressorList = new TreeMap<>();

    /** Creates a new WebSession with the given session ID. This is used for single-session WebSessions, where each session has a unique session ID. The session is added to the allSessions map, so that it can be accessed by the client thread when the session is destroyed.
     * @param sessionId the session ID for this WebSession
     */
    public WebSession(String sessionId) {
        super(sessionId);
        allSessions.put(sessionId, this);
    }

    /** Creates a new WebSession with the given session ID, user, and roles. This is used for multi-session WebSessions, where multiple sessions can share the same session ID. The session is added to the allSessions map, so that it can be accessed by the client thread when the session is destroyed.
     * @param sessionId the session ID for this WebSession
     * @param user the user for this WebSession
     * @param roles the roles for this WebSession
     */
    public WebSession(String sessionId, String user, String[] roles) {
        super(sessionId, user, roles);
        allSessions.put(sessionId, this);
    }

    @Override
    public void close() {
        if (lprMsg.isEmpty())
            lprMsg.add("");
        allSessions.remove(getSessionId());
    }

    /**
     * Closes all active sessions. This is called when the server is shutting down, and all sessions should stop their work and close themselves. The terminate flag is set to true, so that the worker threads can check it and stop their work if it is set to true.
     * note that this is not thread-safe, but it is only called when the server is shutting down, so it should be fine.
     */
    public static void closeAll() {
        terminate = true;
        allSessions.forEach((k, s) -> s.close());
    }

    /** Puts a profile list in the cache for this session. This is used to store the profile lists that are generated for the client, and it is cleared when the session is closed. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     * note that the ID for the profile list is generated by the client, and it is used to retrieve the profile list from the cache when the client requests it. The path for the profile list is generated by the worker thread, and it is used to store the profile list on the server. The client can then download the profile list from the server using the path.
     * @param id the ID of the profile list
     * @param path the path to the profile list
     */
    public void putProfileList(Integer id, Path path) {
        cachedProfileList.put(id, path);
    }

    /** Removes a profile list from the cache for this session. This is used to store the profile lists that are generated for the client, and it is cleared when the session is closed. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     * note that the ID for the profile list is generated by the client, and it is used to retrieve the profile list from the cache when the client requests it. The path for the profile list is generated by the worker thread, and it is used to store the profile list on the server. The client can then download the profile list from the server using the path.
     * @param id the ID of the profile list
     */
    public void removeProfileList(Integer id) {
        cachedProfileList.remove(id);
    }

    /**
     * Creates a new profile list cache for this session. This is used to store the profile lists that are generated for the client, and it is cleared when the session is closed. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     */
    public void newProfileList() {
        cachedProfileList = new TreeMap<>();
    }

    /** Gets the last profile list key for this session. This is used to generate a new ID for the next profile list that is generated for the client. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     * @return the last profile list key for this session
     */
    public Integer getLastProfileListKey() {
        return cachedProfileList != null && !cachedProfileList.isEmpty() ? cachedProfileList.lastKey() : 0;
    }

    /** Gets a profile list from the cache for this session. This is used to store the profile lists that are generated for the client, and it is cleared when the session is closed. Note that this is not thread-safe, but it is only set by the worker thread, so it should be fine.
     * note that the ID for the profile list is generated by the client, and it is used to retrieve the profile list from the cache when the client requests it. The path for the profile list is generated by the worker thread, and it is used to store the profile list on the server. The client can then download the profile list from the server using the path.
     * @param id the ID of the profile list
     * @return the path to the profile list
     */
    public Path getProfileList(Integer id) {
        return cachedProfileList.get(id);
    }
}
