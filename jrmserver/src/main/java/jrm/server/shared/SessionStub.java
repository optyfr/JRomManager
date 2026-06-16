package jrm.server.shared;

/**
 * Interface for classes that need to be notified when a WebSession is created or destroyed. This is used by the SessionListener to
 * notify classes that implement this interface when a session is created or destroyed.
 * <p>
 * Implementing classes can use this interface to perform any necessary setup or cleanup when a WebSession is created or destroyed,
 * such as initializing resources, setting up session-specific data, or releasing resources when a session is destroyed. This allows
 * for better management of session-related resources and ensures that any necessary actions are taken when sessions are created or
 * destroyed.
 */
public interface SessionStub {
    /**
     * Called when a WebSession is created. This is called by the SessionListener when a session is created. @param session the
     * WebSession that was created
     * <p>
     * Implementing classes can use this method to perform any necessary setup or initialization when a WebSession is created, such
     * as initializing resources, setting up session-specific data, or performing any other actions that are required when a new
     * session is established.
     * 
     * @param session the WebSession that was created
     */
    public void setSession(WebSession session);

    /**
     * Called when a WebSession is destroyed. This is called by the SessionListener when a session is destroyed. @param session the
     * WebSession that was destroyed
     * <p>
     * Implementing classes can use this method to perform any necessary cleanup or resource release when a WebSession is destroyed,
     * such as closing database connections, releasing memory, or performing any other actions that are required to properly clean
     * up resources associated with the session that is being terminated.
     * 
     * @param session the WebSession that was destroyed
     */
    public void unsetSession(WebSession session);
}
