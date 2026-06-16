package jrm.misc;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Utility class for common Input/Output operations.
 * <p>
 * Provides helpers to create temporary files, temporary directories, and recursively build paths while applying POSIX file
 * permissions if supported by the host file system.
 * </p>
 * 
 * @author optyfr
 */
public class IOUtils {
    /**
     * Flag indicating whether the underlying filesystem supports POSIX file attributes.
     */
    private static final boolean POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix"); //$NON-NLS-1$

    /**
     * Standard POSIX file attributes used for newly created files/folders: {@code rwxr-x---}.
     */
    private static final FileAttribute<Set<PosixFilePermission>> POSIX_ATTR = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---")); //$NON-NLS-1$

    /**
     * Private constructor to prevent external instantiation.
     */
    private IOUtils() {
    }

    /**
     * Creates a new temporary file in the specified directory.
     * 
     * @param dir the directory where the file should be created
     * @param prefix the prefix string to be used in generating the file's name
     * @param suffix the suffix string to be used in generating the file's name
     * 
     * @return the path to the newly created temporary file
     * 
     * @throws IOException if an I/O error occurs
     */
    public static Path createTempFile(Path dir, String prefix, String suffix) throws IOException {
        if (POSIX)
            return Files.createTempFile(dir, prefix, suffix, POSIX_ATTR); // $NON-NLS-1$
        return Files.createTempFile(dir, prefix, suffix);
    }

    /**
     * Creates a new temporary file in the system default temporary directory.
     * 
     * @param prefix the prefix string to be used in generating the file's name
     * @param suffix the suffix string to be used in generating the file's name
     * 
     * @return the path to the newly created temporary file
     * 
     * @throws IOException if an I/O error occurs
     */
    public static Path createTempFile(String prefix, String suffix) throws IOException {
        if (POSIX)
            return Files.createTempFile(prefix, suffix, POSIX_ATTR); // $NON-NLS-1$
        return Files.createTempFile(prefix, suffix);
    }

    /**
     * Creates a new temporary directory under the default system temporary directory.
     * 
     * @param prefix the prefix string to be used in generating the directory's name
     * 
     * @return the path to the newly created temporary directory
     * 
     * @throws IOException if an I/O error occurs
     */
    public static Path createTempDirectory(String prefix) throws IOException {
        if (POSIX)
            return Files.createTempDirectory(prefix, POSIX_ATTR); // $NON-NLS-1$
        return Files.createTempDirectory(prefix);
    }

    /**
     * Recursively creates directories along the specified target path, applying POSIX permissions if supported.
     * 
     * @param target the target path to create
     * 
     * @return the resolved target path
     * 
     * @throws IOException if an I/O error occurs
     */
    public static Path createDirectories(Path target) throws IOException {
        if (POSIX)
            return Files.createDirectories(target, POSIX_ATTR); // $NON-NLS-1$
        return Files.createDirectories(target);
    }

    /**
     * Checks if POSIX attributes are supported by the active host file system.
     * 
     * @return {@code true} if POSIX is supported, {@code false} otherwise
     */
    public static boolean isPosix() {
        return POSIX;
    }

}
