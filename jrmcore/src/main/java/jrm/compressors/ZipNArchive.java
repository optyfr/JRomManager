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

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.security.Session;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * * Zip native archive class, should not be used directly.
 * 
 * @author optyfr
 */
class ZipNArchive extends NArchive {

    /**
     * Constructs a new ZipNArchive instance with the specified session and archive file. This constructor initializes the archive
     * in read-write mode by default.
     * 
     * @param session the Session object representing the current user session, used for authentication and access control during
     *        archive operations
     * @param archive the File object representing the archive file to be opened and managed by this instance
     * 
     * @throws IOException if an I/O error occurs while accessing the archive file
     * @throws SevenZipNativeInitializationException if an error occurs during the initialization of the SevenZip native library
     */
    public ZipNArchive(final Session session, final File archive) throws IOException, SevenZipNativeInitializationException {
        super(session, archive);
    }

    /**
     * Constructs a new ZipNArchive instance with the specified session, archive file, and read-only flag. This constructor allows
     * for specifying whether the archive should be opened in read-only mode or not.
     * 
     * @param session the Session object representing the current user session, used for authentication and access control during
     *        archive operations
     * @param archive the File object representing the archive file to be opened and managed by this instance
     * @param readonly a boolean flag indicating whether the archive should be opened in read-only mode (true) or read-write mode
     *        (false)
     * 
     * @throws IOException if an I/O error occurs while accessing the archive file
     * @throws SevenZipNativeInitializationException if an error occurs during the initialization of the SevenZip native library
     */
    public ZipNArchive(final Session session, final File archive, final boolean readonly, ProgressNarchiveCallBack cb) throws IOException, SevenZipNativeInitializationException {
        super(session, archive, readonly, cb);
    }
}
