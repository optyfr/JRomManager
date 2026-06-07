/* Copyright (C) 2018  optyfr
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
package jrm.profile.scan.options;

import jrm.locale.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enumeration of available hash collision management options. These options
 * define the behavior of the rebuilder when identical checksums (hash
 * collisions) are detected between different romsets or parent/clone
 * relationships.
 * 
 * @author optyfr
 * @since 1.0
 */
public @RequiredArgsConstructor enum HashCollisionOptions implements Descriptor {
    /**
     * Only the collisioned entry is placed in the clone's subfolder.
     */
    SINGLEFILE(Messages.getString("HashCollisionOptions.SingleFile")), //$NON-NLS-1$
    /**
     * All entries in the collisioning clone are placed in the clone's subfolder.
     */
    SINGLECLONE(Messages.getString("HashCollisionOptions.SingleClone")), //$NON-NLS-1$
    /**
     * All clones are placed in their respective subfolders as soon as a single
     * collision occurs.
     */
    ALLCLONES(Messages.getString("HashCollisionOptions.AllClones")), //$NON-NLS-1$
    /**
     * All clones are placed in their subfolders as soon as a collision is detected,
     * with specific optimization rules.
     */
    HALFDUMB(Messages.getString("HashCollisionOptions.AllClonesHalfDumb")), //$NON-NLS-1$
    /**
     * All clones are placed in subfolders unconditionally, regardless of whether a
     * hash collision actually exists.
     */
    DUMB(Messages.getString("HashCollisionOptions.AllClonesDumb")), //$NON-NLS-1$
    /**
     * All clones are unconditionally placed in subfolders, including disk images
     * (CHDs).
     */
    DUMBER(Messages.getString("HashCollisionOptions.AllClonesDumber")); //$NON-NLS-1$

    /**
     * The descriptive name of the collision management option.
     * 
     * @return the localized description string.
     */
    private final @Getter String desc;
}
