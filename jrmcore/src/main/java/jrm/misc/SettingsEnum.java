package jrm.misc;

import jrm.batch.CompressorFormat;
import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipLevel;
import jrm.compressors.ZipOptions;
import jrm.compressors.ZipTempThreshold;
import jrm.io.torrent.options.TrntChkMode;

/**
 * Enum defining all general application settings options and their respective
 * default values. Implements {@link EnumWithDefault} to allow lookup of default
 * configurations.
 * 
 * @author optyfr
 */
public enum SettingsEnum implements EnumWithDefault {
    /**
     * Flag enabling multi-threaded execution or parallel processing where
     * supported.
     */
    use_parallelism(true), // NOSONAR

    /**
     * Total thread count limit allocated for execution. Negative values indicate
     * adaptive or virtual threads.
     */
    thread_count(-1), // NOSONAR

    /**
     * Path or name of the zip command utility.
     */
    zip_cmd(FindCmd.findZip()), // NOSONAR

    /**
     * Threshold file size above which temporary files are utilized instead of
     * memory buffers.
     */
    zip_temp_threshold(ZipTempThreshold._10MB), // NOSONAR

    /**
     * Native ZIP compressor compression intensity level.
     */
    zip_compression_level(ZipLevel.DEFAULT), // NOSONAR

    /**
     * Native ZIP compressor performance configuration options (e.g. FAST, NORMAL,
     * MAXIMUM).
     */
    zip_level(ZipOptions.NORMAL), // NOSONAR

    /**
     * Number of concurrent compression threads allocated for ZIP operations.
     */
    zip_threads(-1), // NOSONAR

    /**
     * Path or name of the 7-Zip command utility.
     */
    sevenzip_cmd(FindCmd.find7z()), // NOSONAR

    /**
     * Flag enabling solid compression archives in 7-Zip format.
     */
    sevenzip_solid(true), // NOSONAR

    /**
     * Compression level configuration options for 7-Zip operations.
     */
    sevenzip_level(SevenZipOptions.NORMAL), // NOSONAR

    /**
     * Number of concurrent threads allocated for 7-Zip compression.
     */
    sevenzip_threads(-1), // NOSONAR

    /**
     * Debugging flag that forces scanning without reading or writing from local
     * cache registries.
     */
    debug_nocache(false), // NOSONAR

    /**
     * The active minimum logging level threshold.
     */
    debug_level(Log.getLevel()), // NOSONAR

    /**
     * The last source directory used in dat2dir translation operations.
     */
    dat2dir_lastsrcdir(null, "dat2dir.lastsrcdir"), // NOSONAR

    /**
     * The last destination DAT directory used in dat2dir translation operations.
     */
    dat2dir_lastdstdatdir(null, "dat2dir.lastdstdatdir"), // NOSONAR

    /**
     * The last destination directory used in dat2dir translation operations.
     */
    dat2dir_lastdstdir(null, "dat2dir.lastdstdir"), // NOSONAR

    /**
     * The set of source directories used in dat2dir translation.
     */
    dat2dir_srcdirs("", "dat2dir.srcdirs"), // NOSONAR

    /**
     * The source-destination mapping registry string for dat2dir translation.
     */
    dat2dir_sdr("[]", "dat2dir.sdr"), // NOSONAR

    /**
     * Dry-run flag for dat2dir translation operations to simulate actions without
     * disk changes.
     */
    dat2dir_dry_run(false, "dat2dir.dry_run"), // NOSONAR

    /**
     * The last TorrentZip directory scanned during torrent check operations.
     */
    trntchk_lasttrntdir(null, "trntchk.lasttrntdir"), // NOSONAR

    /**
     * The last destination directory utilized during torrent check operations.
     */
    trntchk_lastdstdir(null, "trntchk.lastdstdir"), // NOSONAR

    /**
     * The source-destination mapping registry string for torrent checks.
     */
    trntchk_sdr("[]", "trntchk.sdr"), // NOSONAR

    /**
     * Active comparison mode used during torrent check operations (e.g. FILENAME,
     * HASH).
     */
    trntchk_mode(TrntChkMode.FILENAME, "trntchk.mode"), // NOSONAR

    /**
     * Flag enabling search of archived folder directories inside TorrentZip
     * verification.
     */
    trntchk_detect_archived_folders(false, "trntchk.detect_archived_folders"), // NOSONAR

    /**
     * Flag allowing removal of unidentified files from TorrentZip target
     * containers.
     */
    trntchk_remove_unknown_files(false, "trntchk.remove_unknown_files"), // NOSONAR

    /**
     * Flag allowing removal of files with incorrect size attributes from
     * containers.
     */
    trntchk_remove_wrong_sized_files(false, "trntchk.remove_wrong_sized_files"), // NOSONAR

    /**
     * The last directory utilized during batch compressor operations.
     */
    compressor_lastdir(null, "compressor.lastdir"), // NOSONAR

    /**
     * Target compressor format.
     */
    compressor_format(CompressorFormat.TZIP, "compressor.format"), // NOSONAR

    /**
     * Flag forcing re-compression of archives even if they match current
     * configurations.
     */
    compressor_force(false, "compressor.force"), // NOSONAR

    /**
     * Flag enabling multi-threaded execution of batch compressor operations.
     */
    compressor_parallelism(true, "compressor.parallelism"), // NOSONAR

    /**
     * Last directory from which directories are scanned during dir2dat creation.
     */
    dir2dat_lastsrcdir(null, "dir2dat.lastsrcdir"), // NOSONAR

    /**
     * The actual source directory scanned for generating dir2dat files.
     */
    dir2dat_src_dir(null, "dir2dat.src_dir"), // NOSONAR

    /**
     * The last destination directory used to write generated dir2dat XML files.
     */
    dir2dat_lastdstdir(null, "dir2dat.lastdstdir"), // NOSONAR

    /**
     * Destination path where generated dir2dat XML files are written.
     */
    dir2dat_dst_file(null, "dir2dat.dst_file"), // NOSONAR

    /**
     * Target emulator database metadata format (e.g. MAME, Logiqx, etc.).
     */
    dir2dat_format("MAME", "dir2dat.format"), // NOSONAR

    /**
     * Flag enabling recursive scanning of subfolders during dir2dat generation.
     */
    dir2dat_scan_subfolders(true, "dir2dat.scan_subfolders"), // NOSONAR

    /**
     * Flag enabling full contents hash checking of files during dir2dat scanning.
     */
    dir2dat_deep_scan(false, "dir2dat.deep_scan"), // NOSONAR

    /**
     * Flag enabling addition of MD5 checksum values to dir2dat XML files.
     */
    dir2dat_add_md5(false, "dir2dat.add_md5"), // NOSONAR

    /**
     * Flag enabling addition of SHA-1 checksum values to dir2dat XML files.
     */
    dir2dat_add_sha1(false, "dir2dat.add_sha1"), // NOSONAR

    /**
     * Flag to disregard parent folder names when nesting ROM items under dir2dat.
     */
    dir2dat_junk_folders(false, "dir2dat.junk_folders"), // NOSONAR

    /**
     * Flag that excludes compressed files (ZIP, 7z) from deep content scanning in
     * dir2dat.
     */
    dir2dat_do_not_scan_archives(false, "dir2dat.do_not_scan_archives"), // NOSONAR

    /**
     * Flag that restricts items matching specific active profile parameters.
     */
    dir2dat_match_profile(false, "dir2dat.match_profile"), // NOSONAR

    /**
     * Flag enabling inclusion of empty directory folders into final dir2dat.
     */
    dir2dat_include_empty_dirs(false, "dir2dat.include_empty_dirs"), // NOSONAR

    /**
     * JSON representation of display configurations inside scanning and matching
     * report panels.
     */
    report_settings("[\"STATS\",\"COMPACT\",\"GROUP_BY_TYPE_AND_STATUS\"]", "report.settings"); // NOSONAR

    /**
     * Explicit custom string name of this option key. If null, the standard name()
     * is used.
     */
    private String name = null;

    /**
     * Default fallback configuration value associated with this setting option.
     */
    private Object dflt = null;

    /**
     * Constructs a new settings option with the specified default fallback value.
     * 
     * @param dflt the default fallback value
     */
    private SettingsEnum(Object dflt) {
        this.dflt = dflt;
    }

    /**
     * Constructs a new settings option with the specified default fallback value
     * and custom key name.
     * 
     * @param dflt the default fallback value
     * @param name the custom key name
     */
    private SettingsEnum(Object dflt, final String name) {
        this.dflt = dflt;
        this.name = name;
    }

    /**
     * Returns the string representation of this option, favoring any defined custom
     * name key.
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
     * Resolves a key name into its matching {@code SettingsEnum} constant.
     * 
     * @param name the raw string key name to parse
     * @return the resolved enum constant, or {@code null} if no match is found
     */
    public static SettingsEnum from(String name) {
        for (final var option : values()) {
            if (option.name != null && option.name.equals(name))
                return option;
            if (option.name().equals(name))
                return option;
        }
        return null;
    }
}
