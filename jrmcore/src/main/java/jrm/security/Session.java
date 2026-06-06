package jrm.security;

import java.util.ResourceBundle;

import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.report.Report;
import jrm.profile.scan.Scan;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a user or server session within the Retro-Gaming ROM manager. A
 * session acts as a central execution context, maintaining reference to the
 * authenticated {@link User}, the active {@link Profile}, and the running
 * {@link Scan}. It also tracks execution modes (such as server, multi-user, and
 * update restrictions) and holds a cumulative session {@link Report}.
 *
 * @author Expert Java Code Documentation Developer
 * @since 1.0
 */
public class Session {
    /**
     * The default administrative role identifier used for fallback or
     * auto-generated users.
     */
    private static final String ADMIN = "admin";

    /**
     * The unique session identifier string.
     *
     * @param sessionId the unique session identifier to set
     */
    private @Setter String sessionId;

    /**
     * The user context associated with this session. This field can be accessed by
     * subclasses or classes within the same package.
     */
    protected User user = null;

    /**
     * The resource bundle for localized messages and UI text within this session
     * context.
     *
     * @param msgs the resource bundle to set
     * @return the resource bundle for localized messages
     */
    private @Getter @Setter ResourceBundle msgs = null;

    /**
     * Flag indicating whether the session is operating as a remote/web server
     * session.
     *
     * @return {@code true} if running in server mode, {@code false} otherwise
     */
    private @Getter boolean server = false;

    /**
     * Flag indicating whether multi-user isolation features are enabled.
     *
     * @return {@code true} if multi-user features are active, {@code false}
     *         otherwise
     */
    private @Getter boolean multiuser = false;

    /**
     * Flag indicating whether update checking is disabled during the session.
     *
     * @return {@code true} if automatic updates are disabled, {@code false}
     *         otherwise
     */
    private @Getter boolean noupdate = false;

    /**
     * The compilation report summarizing active operations and diagnostic entries.
     *
     * @return the session execution report
     */
    private final @Getter Report report = new Report();

    /**
     * The currently loaded ROM profile containing configuration rules and scan
     * settings.
     *
     * @param currProfile the active ROM profile to load/set
     * @return the currently loaded ROM profile, or {@code null} if none is active
     */
    private @Getter @Setter Profile currProfile = null;

    /**
     * The current scan process execution state.
     *
     * @param currScan the current scan context to set
     * @return the active scan execution context, or {@code null} if no scan is
     *         running
     */
    private @Getter @Setter Scan currScan;

    /**
     * Protected default constructor. Used for subclasses or internal
     * framework-driven instantiation.
     */
    protected Session() {

    }

    /**
     * Package-private constructor to build a standalone desktop session context.
     * Initializes a default user named "JRomManager" with administrative
     * privileges.
     *
     * @param multiuser {@code true} to enable multi-user environment checks,
     *                  {@code false} for standard single-user
     * @param noupdate  {@code true} to disable check for updates, {@code false} to
     *                  permit updates
     */
    Session(boolean multiuser, boolean noupdate) {
        this.multiuser = multiuser;
        this.noupdate = noupdate;
        this.sessionId = null;
        user = new User(this, "JRomManager", new String[] { ADMIN });
        msgs = Messages.getBundle();
    }

    /**
     * Constructs a single-user server session with a specific session identifier.
     *
     * @param sessionId the unique session ID representing this session on the
     *                  server
     */
    public Session(String sessionId) {
        this.server = true;
        this.sessionId = sessionId;
        msgs = Messages.getBundle();
    }

    /**
     * Constructs a multi-user server session with a specific session identifier,
     * username, and role assignments.
     *
     * @param sessionId the unique session ID representing this session on the
     *                  server
     * @param user      the username to associate with this session; if
     *                  {@code null}, defaults to "server"
     * @param roles     the roles assigned to the user; if {@code null}, defaults to
     *                  a single "admin" role
     */
    public Session(String sessionId, String user, String[] roles) {
        this.multiuser = true;
        this.server = true;
        this.sessionId = sessionId;
        this.user = new User(this, user == null ? "server" : user, roles == null ? new String[] { ADMIN } : roles);
        msgs = Messages.getBundle();
    }

    /**
     * Returns the user context associated with this session. If no user is
     * currently defined, a default administrator user is automatically created.
     *
     * @return the active {@link User} associated with this session
     */
    public User getUser() {
        if (user == null)
            user = new User(this, "JRomManager", new String[] { ADMIN });
        return user;
    }

    /**
     * Configures or overrides the user profile associated with this session.
     *
     * @param user  the username of the user
     * @param roles the roles to assign to the user
     */
    public void setUser(String user, String[] roles) {
        this.user = new User(this, user, roles);
    }

    /**
     * Retrieves the unique session identifier.
     *
     * @return the session ID, or {@code null} if this is an unassigned local
     *         session
     */
    public String getSessionId() {
        return sessionId;
    }

}
