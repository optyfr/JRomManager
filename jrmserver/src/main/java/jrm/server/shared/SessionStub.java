package jrm.server.shared;

/** Interface for classes that need to be notified when a WebSession is created or destroyed. This is used by the SessionListener to notify classes that implement this interface when a session is created or destroyed. */
public interface SessionStub {
    /** Called when a WebSession is created. This is called by the SessionListener when a session is created. @param session the WebSession that was created */
    public void setSession(WebSession session);
    /** Called when a WebSession is destroyed. This is called by the SessionListener when a session is destroyed. @param session the WebSession that was destroyed */
    public void unsetSession(WebSession session);
}
