package jrm.security;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jrm.misc.Log;
import lombok.val;

/**
 * Path abstracter and security sandbox manager for the ROM manager application.
 * This class translates fully resolved absolute filesystem paths to/from
 * stylized relative abstract paths using placeholders (such as {@code %work},
 * {@code %shared}, and {@code %presets}).
 * <p>
 * It also handles path validation to guard against directory traversal/forgery
 * attacks, and checks whether abstract paths are writeable by the active user
 * session depending on roles and directory rules.
 * </p>
 *
 * @author Expert Java Code Documentation Developer
 * @since 1.0
 */
public class PathAbstractor {
    /**
     * Abstract path placeholder prefix for the shared folder.
     */
    private static final String SHARED = "%shared";

    /**
     * Abstract path placeholder prefix for the active user workspace folder.
     */
    private static final String WORK = "%work";

    /**
     * Abstract path placeholder prefix for the profile presets folder.
     */
    private static final String PRESETS = "%presets";

    /**
     * Security message used when directory traversal attempts are detected.
     */
    private static final String FORGED_PATH = "Forged path";

    /**
     * The session context used by this abstracter for user lookup, settings
     * resolution, and permission verification.
     */
    private Session session;

    /**
     * Constructs a new {@code PathAbstractor} instance bound to the specified
     * session.
     *
     * @param session the user session context
     */
    public PathAbstractor(Session session) {
        this.session = session;
    }

    /**
     * Checks if the specified abstract path string is writeable by the user
     * associated with this abstracter's session.
     *
     * @param strpath the abstract path string (e.g., starting with {@code %work} or
     *                {@code %shared})
     * @return {@code true} if the path is writeable by the user, {@code false}
     *         otherwise
     */
    public boolean isWriteable(String strpath) {
        return isWriteable(session, strpath);
    }

    /**
     * Checks if the specified abstract path string is writeable under the provided
     * session context.
     * <ul>
     * <li>Paths starting with {@code %work} are writeable by all users.</li>
     * <li>Paths starting with {@code %shared} are writeable only by
     * administrators.</li>
     * <li>All other paths default to administrator-only write access.</li>
     * </ul>
     *
     * @param session the active user session context
     * @param strpath the abstract path string
     * @return {@code true} if the path is writeable under the session,
     *         {@code false} otherwise
     */
    public static boolean isWriteable(Session session, String strpath) {
        if (strpath.startsWith(WORK))
            return true;
        if (strpath.startsWith(SHARED))
            return session.getUser().isAdmin();
        return session.getUser().isAdmin();
    }

    /**
     * Converts a fully resolved absolute {@link File} into an abstracted, relative
     * version.
     *
     * @param file the absolute {@link File} to relative-ize
     * @return a {@link File} instance containing the relative abstract path
     *         representation
     */
    public File getRelativePath(File file) {
        return getRelativePath(session, file.toPath()).toFile();
    }

    /**
     * Converts a fully resolved absolute {@link Path} into an abstracted, relative
     * version.
     *
     * @param path the absolute {@link Path} to relative-ize
     * @return the relative abstract {@link Path} representation
     */
    public Path getRelativePath(Path path) {
        return getRelativePath(session, path);
    }

    /**
     * Converts a fully resolved absolute path into an abstracted relative path
     * prefixed with the appropriate placeholder. Supported placeholders are:
     * <ul>
     * <li>{@code %presets}: Resolves within the active user settings' workpath,
     * inside a {@code presets} subdirectory.</li>
     * <li>{@code %work}: Resolves within the active user settings' workpath.</li>
     * <li>{@code %shared}: Resolves within the global {@code users/shared}
     * subdirectory of the base configuration path.</li>
     * </ul>
     * If the absolute path does not fall inside any of these predefined base
     * directories, it is returned unmodified.
     *
     * @param session the active user session context
     * @param path    the absolute path to process
     * @return the abstracted path containing a placeholder, or the original path if
     *         not matching any sub-directories
     */
    public static Path getRelativePath(Session session, Path path) {
        try {
            val pdir = session.getUser().getSettings().getWorkPath().resolve("presets");
            if (path.startsWith(pdir))
                return Paths.get(PRESETS, pdir.relativize(path).toString());
            else {
                val wdir = session.getUser().getSettings().getWorkPath();
                if (path.startsWith(wdir))
                    return Paths.get(WORK, wdir.relativize(path).toString());
                else {
                    val sdir = session.getUser().getSettings().getBasePath().resolve("users").resolve("shared");
                    if (path.startsWith(sdir))
                        return Paths.get(SHARED, sdir.relativize(path).toString());
                }
            }
        } catch (Exception e) {
            Log.err(e.getMessage(), e);
        }
        return path;
    }

    /**
     * Resolves an abstract string path containing placeholder prefixes into a fully
     * qualified absolute {@link Path}.
     *
     * @param strpath the abstract path string to resolve
     * @return the absolute, normalized {@link Path}
     * @throws SecurityException if the path attempts to traverse outside of its
     *                           allowed root boundary (forgery check)
     */
    public Path getAbsolutePath(final String strpath) throws SecurityException {
        return getAbsolutePath(session, strpath);
    }

    /**
     * Resolves an abstract string path containing placeholder prefixes into a fully
     * qualified absolute {@link Path} under the specified session context.
     * <p>
     * This method replaces the {@code %presets}, {@code %work}, and {@code %shared}
     * prefixes with their respective fully resolved filesystem counterparts. It
     * then normalizes the path and verifies that the resulting path is safely
     * nested within the target root folder to prevent directory traversal and
     * arbitrary file read/write vulnerabilities.
     * </p>
     *
     * @param session the active user session context
     * @param strpath the abstract path string to resolve
     * @return the absolute, normalized {@link Path}
     * @throws SecurityException if the resolved path does not start with the
     *                           expected base directory (forged path)
     */
    public static Path getAbsolutePath(Session session, final String strpath) throws SecurityException {
        final Path path;
        if (strpath.startsWith(PRESETS)) {
            val basepath = session.getUser().getSettings().getWorkPath().resolve("presets");
            try {
                Files.createDirectories(basepath);
            } catch (IOException e) {
                Log.err(e.getMessage(), e);
            }
            path = Paths.get(strpath.replace(PRESETS, basepath.toString())).toAbsolutePath().normalize();
            if (!path.startsWith(basepath))
                throw new SecurityException(FORGED_PATH);
        } else if (strpath.startsWith(WORK)) {
            val basepath = session.getUser().getSettings().getWorkPath();
            path = Paths.get(strpath.replace(WORK, basepath.toString())).toAbsolutePath().normalize();
            if (!path.startsWith(basepath))
                throw new SecurityException(FORGED_PATH);
        } else if (strpath.startsWith(SHARED)) {
            val basepath = session.getUser().getSettings().getBasePath().resolve("users").resolve("shared");
            path = Paths.get(strpath.replace(SHARED, basepath.toString())).toAbsolutePath().normalize();
            if (!path.startsWith(basepath))
                throw new SecurityException(FORGED_PATH);
        } else
            path = Paths.get(strpath);
        return path;
    }

}
