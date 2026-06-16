/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.profile.fix.actions;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.status.StatusRendererFactory;
import jrm.compressors.Archive;
import jrm.profile.data.Entry;
import jrm.security.Session;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;

/**
 * Base abstract class representing a corrective action applied to an individual entry nested inside a game/ROM container (e.g.,
 * adding, deleting, renaming, or duplicating files).
 * <p>
 * Subclasses implement specific methods to perform actions on various targets: general archives, standard directories/paths, zip
 * files, or virtual zip file systems.
 * </p>
 * 
 * @author optyfr
 * 
 * @since 1.0
 */
public abstract class EntryAction implements StatusRendererFactory {
    /**
     * Default fallback estimated entry action size in bytes.
     */
    private static final long ESTIMATED_SIZE = 0L;

    /**
     * The target entry on which this action is applied.
     */
    final Entry entry;

    /**
     * The parent {@link ContainerAction} containing this entry action.
     */
    ContainerAction parent;

    /**
     * Constructs a new {@code EntryAction} associated with the specified target entry.
     * 
     * @param entry the target game/ROM file entry
     */
    protected EntryAction(final Entry entry) {
        this.entry = entry;
    }

    /**
     * Performs the specific action on the entry in a general compressed archive.
     * 
     * @param session the current active session
     * @param archive the target compressed archive wrapper
     * @param handler the UI progress status indicator
     * @param i the current progression step
     * @param max the total progression steps
     * 
     * @return {@code true} if the action succeeded, otherwise {@code false}
     */
    public abstract boolean doAction(final Session session, Archive archive, ProgressHandler handler, int i, int max);

    /**
     * Performs the specific action on the entry in a target directory path on disk.
     * 
     * @param session the current active session
     * @param target the target parent folder path
     * @param handler the UI progress status indicator
     * @param i the current progression step
     * @param max the total progression steps
     * 
     * @return {@code true} if the action succeeded, otherwise {@code false}
     */
    public abstract boolean doAction(final Session session, Path target, ProgressHandler handler, int i, int max);

    /**
     * Performs the specific action on the entry in a ZipFile archive.
     * 
     * @param session the current active session
     * @param zipf the target zip file instance
     * @param zipp the configuration parameters for the zip file
     * @param handler the UI progress status indicator
     * @param i the current progression step
     * @param max the total progression steps
     * 
     * @return {@code true} if the action succeeded, otherwise {@code false}
     */
    public abstract boolean doAction(final Session session, final ZipFile zipf, final ZipParameters zipp, ProgressHandler handler, int i, int max);

    /**
     * Performs the specific action on the entry in a virtual zip FileSystem.
     * 
     * @param session the current active session
     * @param fs the target virtual zip file system context
     * @param handler the UI progress status indicator
     * @param i the current progression step
     * @param max the total progression steps
     * 
     * @return {@code true} if the action succeeded, otherwise {@code false}
     */
    public abstract boolean doAction(final Session session, FileSystem fs, ProgressHandler handler, int i, int max);

    /**
     * Returns the estimated size in bytes of the entry to process.
     * 
     * @return the estimated size in bytes
     */
    public long estimatedSize() {
        return ESTIMATED_SIZE;
    }

}
