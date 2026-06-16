package jrm.server.shared;

import jrm.server.shared.actions.ProgressActions;
import lombok.Getter;

/**
 * Worker thread that can report progress to the client. The progress actions are set by the ProgressReporter when the worker is
 * started.
 */
public class Worker extends Thread {
    /**
     * The progress actions to report progress to the client. Set by the ProgressReporter when the worker is started. Note that this
     * is not thread-safe, but it is only set once when the worker is started, so it should be fine.
     * 
     * @return the progress actions to report progress to the client
     */
    public @Getter ProgressActions progress = null;

    /**
     * Creates a new worker thread with the given target. The progress actions will be set by the ProgressReporter when the worker
     * is started.
     * 
     * @param target the target to run in the worker thread
     */
    public Worker(Runnable target) {
        super(target);
    }

}
