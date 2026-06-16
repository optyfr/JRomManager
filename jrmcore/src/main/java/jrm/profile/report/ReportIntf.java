package jrm.profile.report;

import java.io.File;
import java.util.Set;
import java.util.zip.CRC32;

import jrm.aui.profile.report.ReportTreeHandler;
import jrm.security.Session;

/**
 * Defines the core operations and behaviors for a Report system.
 * <p>
 * This interface establishes structural cloning mechanisms based on filtering options, handler synchronization for tree model
 * updates, and target reports filesystem resolution.
 *
 * @param <T> the concrete type of the report implementation
 * 
 * @author optyfr
 * 
 * @since 1.0
 */
public interface ReportIntf<T> {
    /**
     * Clones this report instance according to the provided active filtering options.
     *
     * @param filterOptions the set of active filtering options to apply during cloning
     * 
     * @return the cloned report instance containing only elements passing the filter criteria
     */
    public T clone(final Set<FilterOptions> filterOptions);

    /**
     * Sets the report tree handler representing the UI tree model context.
     *
     * @param handler the active report tree handler to register
     */
    public void setHandler(ReportTreeHandler<T> handler);

    /**
     * Gets the registered report tree handler representing the UI tree model context.
     *
     * @return the registered report tree handler, or {@code null} if none is set
     */
    public ReportTreeHandler<T> getHandler();

    /**
     * Gets the file path pointing to the original scanned profile database or configuration file.
     *
     * @return the file object for the scanned profile
     */
    public File getFile();

    /**
     * Gets the last modified timestamp of the original scanned profile file on disk.
     *
     * @return the file modification time in milliseconds
     */
    public long getFileModified();

    /**
     * Resolves the serialized report file location within the active user session directory.
     *
     * @param session the user session context containing settings and paths
     * 
     * @return the resolved file object where the report should be saved or loaded
     */
    public default File getReportFile(final Session session) {
        return getReportFile(session, getFile());
    }

    /**
     * Resolves a serialized report file location based on a specific input file path and session.
     * <p>
     * The output file name is derived from the hexadecimal representation of the CRC32 checksum calculated over the absolute path
     * of the target file.
     *
     * @param session the user session context containing settings and paths
     * @param file the source file path used to compute the report target identifier
     * 
     * @return the resolved serialized report file located under the reports folder
     */
    public static File getReportFile(final Session session, final File file) {
        final CRC32 crc = new CRC32();
        crc.update(file.getAbsolutePath().getBytes());
        final File reports = session.getUser().getSettings().getWorkPath().resolve("reports").toFile(); //$NON-NLS-1$
        reports.mkdirs();
        return new File(reports, String.format("%08x", crc.getValue()) + ".report"); //$NON-NLS-1$
    }
}
