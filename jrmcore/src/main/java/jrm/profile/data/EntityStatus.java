/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.profile.data;

/**
 * Enumerates the dynamic scan status levels of an {@link EntityBase}. These status values track the detection results across
 * scanned file system containers.
 *
 * @author optyfr
 */
public enum EntityStatus {
    /**
     * The scan status is unknown or has not yet been processed.
     */
    UNKNOWN,
    /**
     * The entity was not found, or it was found with incorrect properties (e.g. mismatched hashes or wrong sizes).
     */
    KO,
    /**
     * The entity was successfully found and validated with perfect matching properties.
     */
    OK
}
