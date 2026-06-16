package jrm.profile.report;

/**
 * Specifies the active filtering options applied to a {@link Report} when querying or rendering its structural components.
 * <p>
 * These options control the visibility of container subjects and individual entries depending on their validity and repairable
 * status.
 *
 * @author optyfr
 * 
 * @since 1.0
 */
public enum FilterOptions {
    /**
     * Show OK containers that match database expectations perfectly and do not need to be rebuilt or repaired.
     */
    SHOWOK,

    /**
     * Hide totally missing containers that cannot be rebuilt because none of their required entries are locally available.
     */
    HIDEMISSING
}
