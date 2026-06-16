/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.compressors;

import jrm.locale.Messages;
import lombok.Getter;

/**
 * Zip compression options representing different levels of compression.
 * 
 * @author optyfr
 */
public enum ZipOptions {
    /** Store compression option, no compression. */
    STORE(Messages.getString("ZipOptions.STORE"), 0), //$NON-NLS-1$
    /** Fastest compression option. */
    FASTEST(Messages.getString("ZipOptions.FASTEST"), 1), //$NON-NLS-1$
    /** Fast compression option. */
    FAST(Messages.getString("ZipOptions.FAST"), 3), //$NON-NLS-1$
    /** Normal compression option. */
    NORMAL(Messages.getString("ZipOptions.NORMAL"), 5), //$NON-NLS-1$
    /** Maximum compression option. */
    MAXIMUM(Messages.getString("ZipOptions.MAXIMUM"), 7), //$NON-NLS-1$
    /** Ultra compression option. */
    ULTRA(Messages.getString("ZipOptions.ULTRA"), 9); //$NON-NLS-1$

    /**
     * Description of the compression option, used for display purposes.
     * 
     * @return the description of the compression option, used for display purposes
     */
    private @Getter String desc;
    /**
     * Compression level associated with the option, used for configuring the compression algorithm.
     * 
     * @return the compression level associated with the option, used for configuring the compression algorithm
     */
    private @Getter int level;

    /**
     * Constructs a new ZipOptions instance with the specified description and compression level. This constructor initializes the
     * enum constant with the provided description for display purposes and the corresponding compression level for configuring the
     * compression algorithm during archive operations.
     * 
     * @param desc the description of the compression option, used for display purposes
     * @param level the compression level associated with the option, used for configuring the compression algorithm
     */
    private ZipOptions(final String desc, final int level) {
        this.desc = desc;
        this.level = level;
    }
}
