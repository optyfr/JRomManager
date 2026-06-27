/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.profile.manager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.time.Instant;

import lombok.Data;

/**
 * Contains comprehensive statistics and audit tracking metadata for a ROM profile. Stores information regarding owned versus total
 * counts of sets, ROMs, and disks, as well as timestamps of profile lifecycle events like creation, scanning, and fixing. Fully
 * supports compliant and custom manual serialization.
 * 
 * @author optyfr
 */
public final @Data class ProfileNFOStats implements Serializable {
    /**
     * Serialization key constant for the last fix/repair timestamp.
     */
    private static final String FIXED_STR = "fixed";

    /**
     * Serialization key constant for the last filesystem scan timestamp.
     */
    private static final String SCANNED_STR = "scanned";

    /**
     * Serialization key constant for the profile creation timestamp.
     */
    private static final String CREATED_STR = "created";

    /**
     * Serialization key constant for the total number of disks defined.
     */
    private static final String TOTAL_DISKS_STR = "totalDisks";

    /**
     * Serialization key constant for the number of owned disks.
     */
    private static final String HAVE_DISKS_STR = "haveDisks";

    /**
     * Serialization key constant for the total number of ROMs defined.
     */
    private static final String TOTAL_ROMS_STR = "totalRoms";

    /**
     * Serialization key constant for the number of owned ROMs.
     */
    private static final String HAVE_ROMS_STR = "haveRoms";

    /**
     * Serialization key constant for the total number of game sets defined.
     */
    private static final String TOTAL_SETS_STR = "totalSets";

    /**
     * Serialization key constant for the number of owned game sets.
     */
    private static final String HAVE_SETS_STR = "haveSets";

    /**
     * Serialization key constant for the MAME or database version string.
     */
    private static final String VERSION_STR = "version";

    /**
     * Serial version UID for maintaining serialization compatibility across releases.
     */
    private static final long serialVersionUID = 3L;

    /**
     * The MAME or metadata catalog database version string.
     * 
     * @param version the catalog database version to set
     * 
     * @return the catalog database version
     */
    private String version = null;

    /**
     * The total count of game sets owned in the user's collection.
     * 
     * @param haveSets the count of owned game sets to set
     * 
     * @return the count of owned game sets
     */
    private Long haveSets = null;

    /**
     * The total count of game sets defined in the metadata profile.
     * 
     * @param totalSets the total count of defined game sets to set
     * 
     * @return the total count of defined game sets
     */
    private Long totalSets = null;

    /**
     * The total count of ROM files owned in the user's collection.
     * 
     * @param haveRoms the count of owned ROMs to set
     * 
     * @return the count of owned ROMs
     */
    private Long haveRoms = null;

    /**
     * The total count of ROM files defined in the metadata profile.
     * 
     * @param totalRoms the total count of defined ROMs to set
     * 
     * @return the total count of defined ROMs
     */
    private Long totalRoms = null;

    /**
     * The total count of CHD or disk files owned in the user's collection.
     * 
     * @param haveDisks the count of owned disks to set
     * 
     * @return the count of owned disks
     */
    private Long haveDisks = null;

    /**
     * The total count of CHD or disk files defined in the metadata profile.
     * 
     * @param totalDisks the total count of defined disks to set
     * 
     * @return the total count of defined disks
     */
    private Long totalDisks = null;

    /**
     * The timestamp of when this profile NFO metadata was originally created.
     * 
     * @param created the creation instant to set
     * 
     * @return the creation instant
     */
    private Instant created = null;

    /**
     * The timestamp of the last complete directory or filesystem scan.
     * 
     * @param scanned the last scan instant to set
     * 
     * @return the last scan instant
     */
    private Instant scanned = null;

    /**
     * The timestamp of when the last repair or repair-fix operation occurred.
     * 
     * @param fixed the last fix instant to set
     * 
     * @return the last fix instant
     */
    private Instant fixed = null;

    /**
     * Declares persistent serialization fields for compliant and predictable manual object serialization.
     * 
     * @serialField version String catalog database version
     * @serialField haveSets Long count of owned game sets
     * @serialField totalSets Long total count of defined game sets
     * @serialField haveRoms Long count of owned ROM files
     * @serialField totalRoms Long total count of defined ROM files
     * @serialField haveDisks Long count of owned disks
     * @serialField totalDisks Long total count of defined disks
     * @serialField created Instant creation instant of this metadata
     * @serialField scanned Instant last filesystem scan instant
     * @serialField fixed Instant last fix/repair operation instant
     */
    private static final ObjectStreamField[] serialPersistentFields = { // NOSONAR
            new ObjectStreamField(VERSION_STR, String.class), // $NON-NLS-1$
            new ObjectStreamField(HAVE_SETS_STR, Long.class), // $NON-NLS-1$
            new ObjectStreamField(TOTAL_SETS_STR, Long.class), // $NON-NLS-1$
            new ObjectStreamField(HAVE_ROMS_STR, Long.class), // $NON-NLS-1$
            new ObjectStreamField(TOTAL_ROMS_STR, Long.class), // $NON-NLS-1$
            new ObjectStreamField(HAVE_DISKS_STR, Long.class), // $NON-NLS-1$
            new ObjectStreamField(TOTAL_DISKS_STR, Long.class), // $NON-NLS-1$
            new ObjectStreamField(CREATED_STR, Instant.class), // $NON-NLS-1$
            new ObjectStreamField(SCANNED_STR, Instant.class), // $NON-NLS-1$
            new ObjectStreamField(FIXED_STR, Instant.class), // $NON-NLS-1$
    };

    /**
     * Default zero-argument constructor initializing an empty profile statistics container.
     */
    public ProfileNFOStats() {
        // Default constructor
    }

    /**
     * Manually serializes the state of this statistics instance to the destination stream.
     * 
     * @param stream the target {@link ObjectOutputStream}
     * 
     * @throws IOException if a physical write error occurs
     */
    private void writeObject(final java.io.ObjectOutputStream stream) throws IOException {
        final ObjectOutputStream.PutField fields = stream.putFields();
        fields.put(VERSION_STR, version); // $NON-NLS-1$
        fields.put(HAVE_SETS_STR, haveSets); // $NON-NLS-1$
        fields.put(TOTAL_SETS_STR, totalSets); // $NON-NLS-1$
        fields.put(HAVE_ROMS_STR, haveRoms); // $NON-NLS-1$
        fields.put(TOTAL_ROMS_STR, totalRoms); // $NON-NLS-1$
        fields.put(HAVE_DISKS_STR, haveDisks); // $NON-NLS-1$
        fields.put(TOTAL_DISKS_STR, totalDisks); // $NON-NLS-1$
        fields.put(CREATED_STR, created); // $NON-NLS-1$
        fields.put(SCANNED_STR, scanned); // $NON-NLS-1$
        fields.put(FIXED_STR, fixed); // $NON-NLS-1$
        stream.writeFields();
    }

    /**
     * Manually deserializes the state of this statistics instance from the source stream.
     * 
     * @param stream the source {@link ObjectInputStream}
     * 
     * @throws IOException if a physical read error occurs
     * @throws ClassNotFoundException if any serialized class representation cannot be resolved
     */
    private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        final ObjectInputStream.GetField fields = stream.readFields();
        version = (String) fields.get(VERSION_STR, null); // $NON-NLS-1$
        haveSets = (Long) fields.get(HAVE_SETS_STR, null); // $NON-NLS-1$
        totalSets = (Long) fields.get(TOTAL_SETS_STR, null); // $NON-NLS-1$
        haveRoms = (Long) fields.get(HAVE_ROMS_STR, null); // $NON-NLS-1$
        totalRoms = (Long) fields.get(TOTAL_ROMS_STR, null); // $NON-NLS-1$
        haveDisks = (Long) fields.get(HAVE_DISKS_STR, null); // $NON-NLS-1$
        totalDisks = (Long) fields.get(TOTAL_DISKS_STR, null); // $NON-NLS-1$
        created = (Instant) fields.get(CREATED_STR, null); // $NON-NLS-1$
        scanned = (Instant) fields.get(SCANNED_STR, null); // $NON-NLS-1$
        fixed = (Instant) fields.get(FIXED_STR, null); // $NON-NLS-1$
    }

    /**
     * Resets all statistics values, clearing counts and timestamps and setting the profile creation timestamp to the current system
     * time.
     */
    public void reset() {
        version = null;
        haveSets = null;
        totalSets = null;
        haveRoms = null;
        totalRoms = null;
        haveDisks = null;
        totalDisks = null;
        created = Instant.now();
        scanned = null;
        fixed = null;
    }

    /**
     * Nested immutable record-like structure pairing the number of owned items ("have") with the total expected items ("total").
     * 
     * @author optyfr
     */
    public static @Data class HaveNTotal {
        /**
         * The count of successfully acquired/owned physical elements.
         * 
         * @param have the count of owned items
         * 
         * @return the count of owned items
         */
        private final Long have;

        /**
         * The total target count of expected elements in the profile.
         * 
         * @param total the total count of items
         * 
         * @return the total count of items
         */
        private final Long total;
    }

    /**
     * Returns the game sets completion statistics container.
     * 
     * @return a {@link HaveNTotal} instance representing owned sets vs total sets
     */
    public HaveNTotal getSets() {
        return new HaveNTotal(haveSets, totalSets);
    }

    /**
     * Returns the ROM files completion statistics container.
     * 
     * @return a {@link HaveNTotal} instance representing owned ROMs vs total ROMs
     */
    public HaveNTotal getRoms() {
        return new HaveNTotal(haveRoms, totalRoms);
    }

    /**
     * Returns the CHD/disk files completion statistics container.
     * 
     * @return a {@link HaveNTotal} instance representing owned disks vs total disks
     */
    public HaveNTotal getDisks() {
        return new HaveNTotal(haveDisks, totalDisks);
    }

}
