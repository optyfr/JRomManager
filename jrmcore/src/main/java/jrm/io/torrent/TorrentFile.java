/* Copyright (C) 2015  Christophe De Troyer
 * Copyright (C) 2018  Optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.io.torrent;

import java.util.List;

/**
 * Represents an individual file description inside a multi-file torrent layout.
 * Each file contains a file size (length) and a hierarchical list of directory path segments.
 * 
 * @author Christophe De Troyer
 * @author Optyfr
 */
public class TorrentFile
{
    /**
     * The length/size of the file in bytes.
     */
    private final Long fileLength;

    /**
     * The list of subdirectory names containing the file (representing the path).
     */
    private final List<String> fileDirs;

    /**
     * Constructs a new {@code TorrentFile} description.
     *
     * @param fileLength the size of the file in bytes
     * @param fileDirs the list of directory segments representing the relative file path
     */
    public TorrentFile(Long fileLength, List<String> fileDirs)
    {
        this.fileLength = fileLength;
        this.fileDirs = fileDirs;
    }

    /**
     * Returns a string representation of this {@code TorrentFile}.
     *
     * @return the formatted string
     */
    @Override
    public String toString()
    {
        return "TorrentFile{" + //$NON-NLS-1$
                "fileLength=" + fileLength + //$NON-NLS-1$
                ", fileDirs=" + fileDirs + //$NON-NLS-1$
                '}';
    }

    // Getters and setters

    /**
     * Gets the length of the file in bytes.
     *
     * @return the size in bytes
     */
    public Long getFileLength()
    {
        return fileLength;
    }

    /**
     * Gets the list of directory segments representing the hierarchical path of this file.
     *
     * @return the list of path directories
     */
    public List<String> getFileDirs()
    {
        return fileDirs;
    }
}
