package jrm.profile.data;

/**
 * Enumerates the available export modes supported when exporting profile databases or DAT lists.
 *
 * @author optyfr
 */
public enum ExportMode {
    /**
     * Export all entries without any filtering.
     */
    ALL,
    /**
     * Export only filtered or active selected items.
     */
    FILTERED,
    /**
     * Export only missing items (gaps/requirements that are not present).
     */
    MISSING,
    /**
     * Export only owned/present items that are completely validated.
     */
    HAVE
}
