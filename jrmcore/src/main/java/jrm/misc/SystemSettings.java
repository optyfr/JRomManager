package jrm.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Interface defining standard system paths and properties for the ROM Manager
 * backend. Provides default configurations for database classes, base system
 * paths, and working directory paths.
 * 
 * @author optyfr
 */
public interface SystemSettings {
    /**
     * Retrieves the fully qualified name of the database driver class.
     * 
     * @return the database driver class name, defaults to
     *         {@code "jrm.fullserver.db.H2"}
     */
    public default String getDBClass() {
        return "jrm.fullserver.db.H2";
    }

    /**
     * Retrieves the base system configuration directory path. Resolves the path
     * using the {@code jrommanager.dir} system property, or falls back to the
     * current user directory.
     * 
     * @return the resolved base directory path
     */
    public default Path getBasePath() {
        final String prop = System.getProperty("jrommanager.dir");
        final Path work = (prop != null ? Paths.get(prop) : Paths.get(System.getProperty("user.dir"))).toAbsolutePath().normalize(); //$NON-NLS-1$ //$NON-NLS-2$
        if (!Files.exists(work)) {
            try {
                Files.createDirectories(work);
            } catch (IOException e) {
                Log.err(e.getMessage(), e);
            }
        }
        return work;
    }

    /**
     * Returns the current workspace directory path used for storing logs, cache,
     * XML configurations, and backups.
     * 
     * @return the active workspace directory path, defaults to the base system
     *         configuration directory
     */
    public default Path getWorkPath() {
        return getBasePath();
    }

}
