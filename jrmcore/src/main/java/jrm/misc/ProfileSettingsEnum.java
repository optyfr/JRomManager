package jrm.misc;

import jrm.profile.data.Driver;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.profile.data.Software;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.profile.scan.options.ScanAutomation;

/**
 * Enum defining all scan profile settings options and their respective default values. Implements {@link EnumWithDefault} to allow
 * lookup of default configurations.
 * 
 * @author optyfr
 */
public enum ProfileSettingsEnum implements EnumWithDefault {
    /**
     * Determines if SHA-1 or MD5 digests are required.
     */
    need_sha1_or_md5(false), // NOSONAR

    /**
     * Flag enabling multi-threaded execution or parallel scanning.
     */
    use_parallelism(true), // NOSONAR

    /**
     * Flag enabling container creation mode.
     */
    create_mode(true), // NOSONAR

    /**
     * Flag enabling full container creation mode.
     */
    createfull_mode(false), // NOSONAR

    /**
     * Flag to ignore unneeded containers.
     */
    ignore_unneeded_containers(false), // NOSONAR

    /**
     * Flag to ignore unneeded entries.
     */
    ignore_unneeded_entries(false), // NOSONAR

    /**
     * Flag to ignore unknown containers.
     */
    ignore_unknown_containers(false), // NOSONAR

    /**
     * Flag enabling implicit merge strategy checks.
     */
    implicit_merge(false), // NOSONAR

    /**
     * Flag to ignore merge name matching for ROMs.
     */
    ignore_merge_name_roms(false), // NOSONAR

    /**
     * Flag to ignore merge name matching for disks.
     */
    ignore_merge_name_disks(false), // NOSONAR

    /**
     * Flag to exclude standard games from processing.
     */
    exclude_games(false), // NOSONAR

    /**
     * Flag to exclude machine profiles from processing.
     */
    exclude_machines(false), // NOSONAR

    /**
     * Flag enabling automated file backup operations.
     */
    backup(false), // NOSONAR

    /**
     * The output compressor format.
     */
    format(FormatOptions.ZIP), // NOSONAR

    /**
     * The active merge strategy.
     */
    merge_mode(MergeOptions.SPLIT), // NOSONAR

    /**
     * Flag specifying if archives and CHD formats are treated as regular ROMs.
     */
    archives_and_chd_as_roms(false), // NOSONAR

    /**
     * Active hash collision conflict resolution strategy.
     */
    hash_collision_mode(HashCollisionOptions.SINGLEFILE), // NOSONAR

    /**
     * Filename or path of the catver.ini filter file.
     */
    filter_catver_ini(null, "filter.catver.ini"), // NOSONAR

    /**
     * Filename or path of the nplayers.ini filter file.
     */
    filter_nplayers_ini(null, "filter.nplayers.ini"), // NOSONAR

    /**
     * Filter option indicating if clone ROMs should be included.
     */
    filter_InclClones(true, "filter.InclClones"), // NOSONAR

    /**
     * Filter option indicating if disks should be included.
     */
    filter_InclDisks(true, "filter.InclDisks"), // NOSONAR

    /**
     * Filter option indicating if samples should be included.
     */
    filter_InclSamples(true, "filter.InclSamples"), // NOSONAR

    /**
     * Minimum acceptable driver status level.
     */
    filter_DriverStatus(Driver.StatusType.preliminary, "filter.DriverStatus"), // NOSONAR

    /**
     * Permissible display orientation filter values.
     */
    filter_DisplayOrientation(DisplayOrientation.any, "filter.DisplayOrientation"), // NOSONAR

    /**
     * Permissible cabinet type filter values.
     */
    filter_CabinetType(CabinetType.any, "filter.CabinetType"), // NOSONAR

    /**
     * Lower bound year limit filter.
     */
    filter_YearMin("", "filter.YearMin"), // NOSONAR

    /**
     * Upper bound year limit filter.
     */
    filter_YearMax("????", "filter.YearMax"), // NOSONAR

    /**
     * Minimum acceptable software supported level.
     */
    filter_MinSoftwareSupportedLevel(Software.Supported.no, "filter.MinSoftwareSupportedLevel"), // NOSONAR

    /**
     * Destination directory path for ROM outputs.
     */
    roms_dest_dir(""), // NOSONAR

    /**
     * Flag to enable disk destination directory mapping.
     */
    disks_dest_dir_enabled(false), // NOSONAR

    /**
     * Destination directory path for disk outputs.
     */
    disks_dest_dir(""), // NOSONAR

    /**
     * Flag to enable software ROM destination directory mapping.
     */
    swroms_dest_dir_enabled(false), // NOSONAR

    /**
     * Destination directory path for software ROM outputs.
     */
    swroms_dest_dir(""), // NOSONAR

    /**
     * Flag to enable software disk destination directory mapping.
     */
    swdisks_dest_dir_enabled(false), // NOSONAR

    /**
     * Destination directory path for software disk outputs.
     */
    swdisks_dest_dir(""), // NOSONAR

    /**
     * Flag to enable samples destination directory mapping.
     */
    samples_dest_dir_enabled(false), // NOSONAR

    /**
     * Destination directory path for samples outputs.
     */
    samples_dest_dir(""), // NOSONAR

    /**
     * Flag to enable backup destination directory mapping.
     */
    backup_dest_dir_enabled(false), // NOSONAR

    /**
     * Destination directory path for backup outputs.
     */
    backup_dest_dir("%work/backup"), // NOSONAR

    /**
     * Source directories list.
     */
    src_dir("|"), // NOSONAR

    /**
     * Active automated scan operations phase option.
     */
    automation_scan(ScanAutomation.SCAN, "automation.scan"), // NOSONAR

    /**
     * Set of glob patterns for file exclusion matches.
     */
    exclusion_glob_list("|"), // NOSONAR

    /**
     * Flag specifying if zero-length entries are treated as significant.
     */
    zero_entry_matters(true); // NOSONAR

    /**
     * Explicit custom string name of this option key. If null, the standard name() is used.
     */
    private String name = null;

    /**
     * Default fallback configuration value associated with this setting option.
     */
    private Object dflt = null;

    /**
     * Constructs a new profile setting option with the specified default fallback value.
     * 
     * @param dflt the default fallback value
     */
    private ProfileSettingsEnum(Object dflt) {
        this.dflt = dflt;
    }

    /**
     * Constructs a new profile setting option with the specified default fallback value and custom key name.
     * 
     * @param dflt the default fallback value
     * @param name the custom key name
     */
    private ProfileSettingsEnum(Object dflt, final String name) {
        this.dflt = dflt;
        this.name = name;
    }

    /**
     * Returns the string representation of this option, favoring any defined custom name key.
     * 
     * @return the string key name
     */
    @Override
    public String toString() {
        if (name != null)
            return name;
        return name();
    }

    /**
     * Retrieves the default fallback value.
     * 
     * @return the default value, can be null
     */
    @Override
    public Object getDefault() {
        return dflt;
    }

    /**
     * Resolves a key name into its matching {@code ProfileSettingsEnum} constant.
     * 
     * @param name the raw string key name to parse
     * 
     * @return the resolved enum constant, or {@code null} if no match is found
     */
    public static ProfileSettingsEnum from(String name) {
        for (final var option : values()) {
            if (option.name != null && option.name.equals(name))
                return option;
            if (option.name().equals(name))
                return option;
        }
        return null;
    }
}
