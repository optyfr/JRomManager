package jrm.server;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import jrm.misc.Log;
import jrm.server.shared.WebSession;
import lombok.RequiredArgsConstructor;

/**
 * Listener for session events. Creates a WebSession for each new session and closes it when the session is destroyed.
 */
@RequiredArgsConstructor
public class SessionListener implements HttpSessionListener {
    /** Whether to create a multi-session WebSession (with a shared session ID) or a single-session WebSession (with a unique session ID). */
    final boolean multi;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Log.debug(() -> "Destroying session " + se.getSession().getId());
        WebSession ws = (WebSession) se.getSession().getAttribute("session");
        if (ws != null)
            ws.close();
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        Log.debug(() -> "Creating session " + se.getSession().getId());
        se.getSession().setAttribute("session", multi ? new WebSession(se.getSession().getId(), null, null) : new WebSession(se.getSession().getId()));
    }

}
