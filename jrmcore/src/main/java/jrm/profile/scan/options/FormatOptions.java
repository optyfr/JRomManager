/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.profile.scan.options;

import java.util.EnumSet;
import java.util.Set;

import jrm.locale.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.ZipFile;

/**
 * Enumeration of supported file container format options used during scanning and rebuilding. These format options specify whether
 * romsets are stored as standard zip files, seven-zip files, torrentzipped files, raw directories, or single files.
 * 
 * @author optyfr
 * 
 * @since 1.0
 */
public @RequiredArgsConstructor enum FormatOptions implements Descriptor {
    /**
     * Zip format internally handled via {@link ZipFile}.
     */
    ZIP(Messages.getString("FormatOptions.Zip"), Ext.ZIP), //$NON-NLS-1$
    /**
     * Zip format either handled by SevenzipJBinding or by an external tool.
     */
    ZIPE(Messages.getString("FormatOptions.ZipExternal"), Ext.ZIP), //$NON-NLS-1$
    /**
     * SevenZip format either handled by SevenzipJBinding or by an external tool.
     */
    SEVENZIP(Messages.getString("FormatOptions.SevenZip"), Ext.SEVENZIP), //$NON-NLS-1$
    /**
     * Torrentzip format (standard zip containing files sorted and compressed deterministically by jtrrntzip).
     */
    TZIP(Messages.getString("FormatOptions.TorrentZip"), Ext.ZIP), //$NON-NLS-1$
    /**
     * Standard file directory folder format.
     */
    DIR(Messages.getString("FormatOptions.Directories"), Ext.DIR), //$NON-NLS-1$
    /**
     * Single file format representing a fake directory.
     */
    FAKE(Messages.getString("FormatOptions.SingleFile"), Ext.FAKE); //$NON-NLS-1$

    /**
     * Nested enumeration of standard file container extensions associated with format options.
     */
    public @RequiredArgsConstructor enum Ext {
        /**
         * Directory extension representing no extension.
         */
        DIR(""), //$NON-NLS-1$
        /**
         * ZIP file extension.
         */
        ZIP(".zip"), //$NON-NLS-1$
        /**
         * Seven-zip file extension.
         */
        SEVENZIP(".7z"), //$NON-NLS-1$
        /**
         * Fake directory file extension.
         */
        FAKE(".$$$"); //$NON-NLS-1$

        /**
         * The literal string file extension value.
         */
        private final String value;

        /**
         * Returns the string representation of this file extension.
         * 
         * @return a {@link String} representation of the extension.
         */
        @Override
        public String toString() {
            return value;
        }

        /**
         * Returns all file extensions except this current one and the directory format.
         * 
         * @return a {@link Set} of {@link Ext} representing complementary formats.
         */
        public Set<Ext> allExcept() {
            return EnumSet.complementOf(EnumSet.of(this, DIR));
        }

        /**
         * Checks if this extension represents a standard directory format.
         * 
         * @return {@code true} if this is the directory format, {@code false} otherwise.
         */
        public boolean isDir() {
            return this == DIR;
        }
    }

    /**
     * Localized text description of the format option.
     * 
     * @return theLocalized description string.
     */
    private final @Getter String desc;
    /**
     * File container extension associated with this format option.
     * 
     * @return the corresponding {@link Ext} extension instance.
     */
    private final @Getter Ext ext;

    /**
     * Returns all format options except this current format option and the directory format.
     * 
     * @return a {@link Set} of {@link FormatOptions} representing alternative target formats.
     */
    public Set<FormatOptions> allExcept() {
        return EnumSet.complementOf(EnumSet.of(this, DIR));
    }
}
