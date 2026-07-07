/*
 * Copyright (C) 2018 optyfr
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.profile.scan;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.ProfileSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.security.Session;

/**
 * Shared reflection and fixture helpers for the {@code jrm.profile.scan} test classes.
 *
 * <p>Centralizes the reflection-based wiring needed to build a {@link Profile} outside its normal load pipeline (private
 * constructors, private fields) and to inject a scan-ready {@link ProfileSettings} instance. Keeping these helpers here avoids
 * duplicating them across {@code ScanTest} and {@code ScanFixTest}.</p>
 *
 * @author optyfr
 */
final class ScanTestSupport {

    /** System property used by {@code GlobalSettings} to locate the work directory in server mode. */
    static final String JRM_DIR_PROP = "jrommanager.dir";

    /**
     * Private constructor; this is a utility class and should not be instantiated.
     */
    private ScanTestSupport() {
    }

    /**
     * Builds an empty {@link Profile} via its private no-argument constructor.
     *
     * @return a fresh empty profile
     * @throws ReflectiveOperationException if reflection fails
     */
    static Profile newProfile() throws ReflectiveOperationException {
        final Constructor<Profile> constructor = Profile.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * Injects a value into a private field of the given profile.
     *
     * @param profile the target profile
     * @param fieldName the field name to write
     * @param value the value to inject
     * @throws ReflectiveOperationException if reflection fails
     */
    static void setField(final Profile profile, final String fieldName, final Object value) throws ReflectiveOperationException {
        final Field field = Profile.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(profile, value);
    }

    /**
     * Builds a real {@link ProfileNFO} via its private {@code ProfileNFO(File)} constructor.
     *
     * @param file the associated database file (must not be a {@code .jrm} file)
     * @return a fresh profile NFO
     * @throws ReflectiveOperationException if reflection fails
     */
    static ProfileNFO newProfileNFO(final File file) throws ReflectiveOperationException {
        final Constructor<ProfileNFO> constructor = ProfileNFO.class.getDeclaredConstructor(File.class);
        constructor.setAccessible(true);
        return constructor.newInstance(file);
    }

    /**
     * Loads a profile from a DAT file using the public {@link Profile#load(Session, ProfileNFO, ProgressHandler)} entry point so
     * the full post-parse pipeline runs, then injects the given scan-ready settings (overriding the defaults loaded by
     * {@code Profile.load}).
     *
     * @param session the real session used for loading
     * @param datFile the DAT file to parse
     * @param handler the progress handler used during parsing
     * @param settings the scan-ready settings to inject after loading
     * @return the loaded and wired profile
     * @throws ReflectiveOperationException if reflection fails
     */
    static Profile loadAndWireProfile(final Session session, final File datFile, final ProgressHandler handler, final ProfileSettings settings) throws ReflectiveOperationException {
        final ProfileNFO nfo = newProfileNFO(datFile);
        final Profile profile = Profile.load(session, nfo, handler);
        setField(profile, "settings", settings);
        return profile;
    }

    /**
     * Creates a {@link ProfileSettings} pre-populated with a valid scan configuration, using the given format, merge mode, source
     * and destination directories.
     *
     * <p>The {@code implicit_merge} key is set via the {@code String} overload because the boolean/int overloads of
     * {@code setProperty} do not propagate to the {@code implicitMerge} field (they route through {@code SettingsEnum.from(String)}
     * which does not recover the {@code ProfileSettingsEnum}).</p>
     *
     * @param format the output container format
     * @param mergeMode the active merge mode
     * @param romsDstDir the ROMs destination directory path, or {@code null} to leave it empty
     * @param srcDir the pipe-separated source directories, or {@code null} to leave it empty
     * @param createMode whether creation of missing containers is enabled
     * @param backup whether the backup phase is enabled
     * @return a scan-ready profile settings instance
     */
    static ProfileSettings baseSettings(final FormatOptions format, final MergeOptions mergeMode, final String romsDstDir, final String srcDir, final boolean createMode, final boolean backup) {
        final var settings = new ProfileSettings();
        settings.setProperty(ProfileSettingsEnum.format, format.toString());
        settings.setProperty(ProfileSettingsEnum.merge_mode, mergeMode.toString());
        settings.setProperty(ProfileSettingsEnum.implicit_merge, Boolean.FALSE.toString());
        settings.setProperty(ProfileSettingsEnum.create_mode, createMode);
        settings.setProperty(ProfileSettingsEnum.createfull_mode, false);
        settings.setProperty(ProfileSettingsEnum.use_parallelism, false);
        settings.setProperty(ProfileSettingsEnum.backup, backup);
        settings.setProperty(ProfileSettingsEnum.ignore_unneeded_containers, false);
        settings.setProperty(ProfileSettingsEnum.ignore_unneeded_entries, false);
        settings.setProperty(ProfileSettingsEnum.ignore_unknown_containers, true);
        settings.setProperty(ProfileSettingsEnum.roms_dest_dir, romsDstDir == null ? "" : romsDstDir);
        settings.setProperty(ProfileSettingsEnum.src_dir, srcDir == null ? "" : srcDir);
        return settings;
    }
}
