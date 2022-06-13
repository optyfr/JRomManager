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

public class TorrentFile
{
    private final Long fileLength;
    private final List<String> fileDirs;

    public TorrentFile(Long fileLength, List<String> fileDirs)
    {
        this.fileLength = fileLength;
        this.fileDirs = fileDirs;
    }

    @Override
    public String toString()
    {
        return "TorrentFile{" + //$NON-NLS-1$
                "fileLength=" + fileLength + //$NON-NLS-1$
                ", fileDirs=" + fileDirs + //$NON-NLS-1$
                '}';
    }

    ////////////////////////////////////////////////////////////////////////////
    //// GETTERS AND SETTERS ///////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public Long getFileLength()
    {
        return fileLength;
    }

    public List<String> getFileDirs()
    {
        return fileDirs;
    }
}