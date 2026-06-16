/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.compressors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.misc.Log;
import jrm.security.Session;
import lombok.Getter;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * The external SevenZip archive class.<br>
 * If possible, wrap over {@link NArchive} via {@link SevenZipNArchive} to use SevenZipJBinding.<br>
 * Otherwise will try to use external 7z executable if available...<br>
 * If command line is used, the archive will be extracted to temporary directory upon first write operation, then entirely recreated
 * from temporary directory upon archive's {@link #close()} operation
 * 
 * @author optyfr
 */
public class SevenZipArchive implements Archive {
    /**
     * The underlying SevenZipNArchive instance that provides the actual implementation of archive operations using
     * SevenZipJBinding. This field is initialized in the constructor and is used to delegate all archive-related operations to the
     * native implementation.
     * 
     * @return the SevenZipNArchive instance used for archive operations
     */
    private @Getter SevenZipNArchive native7Zip = null;

    /**
     * Constructs a new SevenZipArchive instance with the specified session and archive file. This constructor initializes the
     * underlying SevenZipNArchive instance using the provided session and archive file, and sets the readonly flag to false by
     * default. If an error occurs during initialization (e.g., if 7-Zip is not supported on the platform), an IOException is thrown
     * with an appropriate message.
     * 
     * @param session the Session object representing the current user session, used for accessing session-specific information and
     *        resources during archive operations
     * @param archive the File object representing the archive file to be opened or created, which will be used for performing
     *        archive operations such as extraction, addition, deletion, etc.
     * 
     * @throws IOException if an error occurs during initialization (e.g., if 7-Zip is not supported on the platform)
     */
    public SevenZipArchive(final Session session, final File archive) throws IOException {
        this(session, archive, false, null);
    }

    /**
     * Constructs a new SevenZipArchive instance with the specified session, archive file, and progress callback. This constructor
     * initializes the underlying SevenZipNArchive instance using the provided session, archive file, and progress callback, and
     * sets the readonly flag to false by default. If an error occurs during initialization (e.g., if 7-Zip is not supported on the
     * platform), an IOException is thrown with an appropriate message.
     * 
     * @param session the Session object representing the current user session, used for accessing session-specific information and
     *        resources during archive operations
     * @param archive the File object representing the archive file to be opened or created, which will be used for performing
     *        archive operations such as extraction, addition, deletion, etc.
     * @param cb the ProgressNarchiveCallBack instance used for reporting progress during archive operations, allowing for feedback
     *        on the status of ongoing operations (can be null if no progress reporting is needed)
     * 
     * @throws IOException if an error occurs during initialization (e.g., if 7-Zip is not supported on the platform)
     */
    public SevenZipArchive(final Session session, final File archive, ProgressNarchiveCallBack cb) throws IOException {
        this(session, archive, false, cb);
    }

    /**
     * Constructs a new SevenZipArchive instance with the specified session, archive file, readonly flag, and progress callback.
     * This constructor initializes the underlying SevenZipNArchive instance using the provided parameters, allowing for more
     * control over the initialization process. If an error occurs during initialization (e.g., if 7-Zip is not supported on the
     * platform), an IOException is thrown with an appropriate message.
     * 
     * @param session the Session object representing the current user session, used for accessing session-specific information and
     *        resources during archive operations
     * @param archive the File object representing the archive file to be opened or created, which will be used for performing
     *        archive operations such as extraction, addition, deletion, etc.
     * @param readonly a boolean flag indicating whether the archive should be opened in read-only mode (true) or read-write mode
     *        (false), allowing for control over whether modifications to the archive are permitted
     * @param cb the ProgressNarchiveCallBack instance used for reporting progress during archive operations, allowing for feedback
     *        on the status of ongoing operations (can be null if no progress reporting is needed)
     * 
     * @throws IOException if an error occurs during initialization (e.g., if 7-Zip is not supported on the platform)
     */
    public SevenZipArchive(final Session session, final File archive, final boolean readonly, ProgressNarchiveCallBack cb) throws IOException {
        try {
            native7Zip = new SevenZipNArchive(session, archive, readonly, cb);
        } catch (final SevenZipNativeInitializationException e) {
            Log.err(e.getMessage(), e);
            throw new IOException("7zip not supported on that platform"); //$NON-NLS-1$
        }
    }

    /**
     * Closes the archive and releases any associated resources. This method delegates the close operation to the underlying
     * SevenZipNArchive instance, ensuring that all resources are properly released when the archive is closed. If an error occurs
     * during the closing process, an IOException is thrown with an appropriate message.
     * 
     * @throws IOException if an error occurs while closing the archive or releasing resources
     */
    @Override
    public void close() throws IOException {
        native7Zip.close();
    }

    /**
     * Retrieves the temporary directory used for intermediate file storage during archive modifications. This method delegates the
     * retrieval of the temporary directory to the underlying SevenZipNArchive instance, allowing for consistent management of
     * temporary files across different archive implementations. If an error occurs while retrieving the temporary directory, an
     * IOException is thrown with an appropriate message.
     * 
     * @return the File instance representing the temporary directory used for intermediate file storage during archive
     *         modifications
     * 
     * @throws IOException if an error occurs while retrieving the temporary directory
     */
    @Override
    public File getTempDir() throws IOException {
        return native7Zip.getTempDir();
    }

    /**
     * Extracts the contents of the archive. This method delegates the extraction process to the underlying SevenZipNArchive
     * instance, allowing for consistent extraction behavior across different archive implementations. If an error occurs during
     * extraction, an IOException is thrown with an appropriate message.
     * 
     * @return an integer representing the result of the extraction operation (e.g., number of files extracted, status code, etc.)
     * 
     * @throws IOException if an error occurs during the extraction process
     */
    @Override
    public int extract() throws IOException {
        return native7Zip.extract();
    }

    /**
     * Extracts a specific entry from the archive. This method delegates the extraction of the specified entry to the underlying
     * SevenZipNArchive instance, allowing for consistent behavior when extracting individual entries across different archive
     * implementations. If an error occurs during the extraction of the specified entry, an IOException is thrown with an
     * appropriate message.
     * 
     * @param entry the String representing the entry to be extracted from the archive (e.g., file path, entry name, etc.)
     * 
     * @return a File instance representing the extracted entry, or null if the extraction was unsuccessful
     * 
     * @throws IOException if an error occurs during the extraction of the specified entry
     */
    @Override
    public File extract(final String entry) throws IOException {
        return native7Zip.extract(entry);
    }

    /**
     * Extracts the contents of a specific entry from the archive and returns it as an InputStream. This method delegates the
     * extraction process to the underlying SevenZipNArchive instance, allowing for consistent behavior when extracting individual
     * entries as streams across different archive implementations. If an error occurs during the extraction of the specified entry,
     * an IOException is thrown with an appropriate message.
     * 
     * @param entry the String representing the entry to be extracted from the archive (e.g., file path, entry name, etc.)
     * 
     * @return an InputStream representing the extracted entry, or null if the extraction was unsuccessful
     * 
     * @throws IOException if an error occurs during the extraction of the specified entry
     */
    @Override
    public InputStream extractStdOut(final String entry) throws IOException {
        return native7Zip.extractStdOut(entry);
    }

    /**
     * Adds a new entry to the archive. This method delegates the addition of the specified entry to the underlying SevenZipNArchive
     * instance, allowing for consistent behavior when adding entries across different archive implementations. If an error occurs
     * during the addition of the specified entry, an IOException is thrown with an appropriate message.
     * 
     * @param entry the String representing the entry to be added to the archive (e.g., file path, entry name, etc.)
     * 
     * @return an integer representing the result of the addition operation (e.g., status code, index of the added entry, etc.)
     * 
     * @throws IOException if an error occurs during the addition of the specified entry
     */
    @Override
    public int add(final String entry) throws IOException {
        return native7Zip.add(entry);
    }

    /**
     * Adds a new entry to the archive with a specified base directory. This method delegates the addition of the specified entry to
     * the underlying SevenZipNArchive instance, allowing for consistent behavior when adding entries with base directories across
     * different archive implementations. If an error occurs during the addition of the specified entry, an IOException is thrown
     * with an appropriate message.
     * 
     * @param baseDir the File representing the base directory for the entry being added to the archive (e.g., directory containing
     *        the file to be added, etc.)
     * @param entry the String representing the entry to be added to the archive (e.g., file path, entry name, etc.)
     * 
     * @return an integer representing the result of the addition operation (e.g., status code, index of the added entry, etc.)
     * 
     * @throws IOException if an error occurs during the addition of the specified entry
     */
    @Override
    public int add(final File baseDir, final String entry) throws IOException {
        return native7Zip.add(baseDir, entry);
    }

    /**
     * Adds a new entry to the archive using data from the provided InputStream. This method delegates the addition of the specified
     * entry to the underlying SevenZipNArchive instance, allowing for consistent behavior when adding entries from streams across
     * different archive implementations. If an error occurs during the addition of the specified entry, an IOException is thrown
     * with an appropriate message.
     * 
     * @param src the InputStream representing the data to be added as a new entry in the archive (e.g., stream containing file
     *        data, etc.)
     * @param entry the String representing the entry to be added to the archive (e.g., file path, entry name, etc.)
     * 
     * @return an integer representing the result of the addition operation (e.g., status code, index of the added entry, etc.)
     * 
     * @throws IOException if an error occurs during the addition of the specified entry
     */
    @Override
    public int addStdIn(final InputStream src, final String entry) throws IOException {
        return native7Zip.addStdIn(src, entry);
    }

    /**
     * Deletes a specific entry from the archive. This method delegates the deletion of the specified entry to the underlying
     * SevenZipNArchive instance, allowing for consistent behavior when deleting entries across different archive implementations.
     * If an error occurs during the deletion of the specified entry, an IOException is thrown with an appropriate message.
     * 
     * @param entry the String representing the entry to be deleted from the archive (e.g., file path, entry name, etc.)
     * 
     * @return an integer representing the result of the deletion operation (e.g., status code, index of the deleted entry, etc.)
     * 
     * @throws IOException if an error occurs during the deletion of the specified entry
     */
    @Override
    public int delete(final String entry) throws IOException {
        return native7Zip.delete(entry);
    }

    /**
     * Renames a specific entry in the archive. This method delegates the renaming of the specified entry to the underlying
     * SevenZipNArchive instance, allowing for consistent behavior when renaming entries across different archive implementations.
     * If an error occurs during the renaming of the specified entry, an IOException is thrown with an appropriate message.
     * 
     * @param entry the String representing the entry to be renamed in the archive (e.g., file path, entry name, etc.)
     * @param newname the String representing the new name for the entry being renamed in the archive (e.g., new file path, new
     *        entry name, etc.)
     * 
     * @return an integer representing the result of the renaming operation (e.g., status code, index of the renamed entry, etc.)
     * 
     * @throws IOException if an error occurs during the renaming of the specified entry
     */
    @Override
    public int rename(final String entry, final String newname) throws IOException {
        return native7Zip.rename(entry, newname);
    }

    /**
     * Duplicates a specific entry in the archive. This method delegates the duplication of the specified entry to the underlying
     * SevenZipNArchive instance, allowing for consistent behavior when duplicating entries across different archive
     * implementations. If an error occurs during the duplication of the specified entry, an IOException is thrown with an
     * appropriate message.
     * 
     * @param entry the String representing the entry to be duplicated in the archive (e.g., file path, entry name, etc.)
     * @param newname the String representing the new name for the duplicated entry in the archive (e.g., new file path, new entry
     *        name, etc.)
     * 
     * @return an integer representing the result of the duplication operation (e.g., status code, index of the duplicated entry,
     *         etc.)
     * 
     * @throws IOException if an error occurs during the duplication of the specified entry
     */
    @Override
    public int duplicate(final String entry, final String newname) throws IOException {
        return native7Zip.duplicate(entry, newname);
    }
}
