package jrm.misc;

import java.io.File;

import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.security.Session;
import lombok.Getter;
import lombok.Setter;

/**
 * Settings configuration holder specific to a ROM scan profile. Extends
 * {@link Settings} to incorporate custom options such as merge strategies,
 * implicit scanning, and hash collision conflict resolution.
 * 
 * @author optyfr
 */
public class ProfileSettings extends Settings {
    /**
     * The merge mode used while filtering roms/disks.
     * 
     * @param mergeMode the merge options to set
     * @return the active merge options
     */
    private @Getter @Setter MergeOptions mergeMode;

    /**
     * Must we strictly conform to merge tag (explicit), or search merge-able ROMs
     * by ourselves (implicit).
     * 
     * @param implicitMerge the implicit merge flag to set
     * @return the active implicit merge flag
     */
    private @Getter @Setter Boolean implicitMerge;

    /**
     * What hash collision mode is used?
     * 
     * @param hashCollisionMode the hash collision options to set
     * @return the active hash collision options
     */
    private @Getter @Setter HashCollisionOptions hashCollisionMode;

    /**
     * Constructs a new empty {@code ProfileSettings} instance with default
     * configuration.
     */
    public ProfileSettings() {
        super();
    }

    /**
     * Configures and saves directory-based format profile settings on the session
     * user settings.
     * 
     * @param session the active session context
     * @param src     the source profile file
     */
    public static void DIR(Session session, File src) // NOSONAR
    {
        final var settings = common();
        settings.setProperty(ProfileSettingsEnum.format, FormatOptions.DIR.toString()); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.merge_mode, MergeOptions.NOMERGE.toString()); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.archives_and_chd_as_roms, true); // $NON-NLS-1$
        session.getUser().getSettings().saveProfileSettings(src, settings);
    }

    /**
     * Configures and saves TorrentZip-based format profile settings on the session
     * user settings.
     * 
     * @param session the active session context
     * @param src     the source profile file
     */
    public static void TZIP(Session session, File src) // NOSONAR
    {
        final var settings = common();
        settings.setProperty(ProfileSettingsEnum.format, FormatOptions.TZIP.toString()); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.merge_mode, MergeOptions.NOMERGE.toString()); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.archives_and_chd_as_roms, false); // $NON-NLS-1$
        session.getUser().getSettings().saveProfileSettings(src, settings);
    }

    /**
     * Factory method producing a standard base profile settings configuration.
     * 
     * @return a pre-configured profile settings instance with standard settings
     */
    private static ProfileSettings common() {
        final var settings = new ProfileSettings();
        settings.setProperty(ProfileSettingsEnum.need_sha1_or_md5, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.use_parallelism, true); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.create_mode, true); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.createfull_mode, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.zero_entry_matters, true); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.ignore_unneeded_containers, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.ignore_unneeded_entries, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.ignore_unknown_containers, true); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.implicit_merge, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.ignore_merge_name_roms, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.ignore_merge_name_disks, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.exclude_games, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.exclude_machines, false); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.backup, true); // $NON-NLS-1$
        settings.setProperty(ProfileSettingsEnum.zero_entry_matters, true); // $NON-NLS-1$
        return settings;
    }

    @Override
    protected void propagate(Enum<?> property, String value) {
        if (ProfileSettingsEnum.merge_mode == property)
            setMergeMode(value != null ? MergeOptions.valueOf(value) : null);
        else if (ProfileSettingsEnum.implicit_merge == property)
            setImplicitMerge(value != null ? Boolean.parseBoolean(value) : null);
        else if (ProfileSettingsEnum.hash_collision_mode == property)
            setHashCollisionMode(value != null ? HashCollisionOptions.valueOf(value) : null);
    }
}
