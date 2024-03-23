package jrm.misc;

import jrm.batch.CompressorFormat;
import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipLevel;
import jrm.compressors.ZipOptions;
import jrm.compressors.ZipTempThreshold;
import jrm.io.torrent.options.TrntChkMode;

public enum SettingsEnum implements EnumWithDefault
{
	use_parallelism(true), // NOSONAR
	thread_count(-1), // NOSONAR
	zip_cmd(FindCmd.findZip()), // NOSONAR
	zip_temp_threshold(ZipTempThreshold._10MB), // NOSONAR
	zip_compression_level(ZipLevel.DEFAULT), // NOSONAR
	zip_level(ZipOptions.NORMAL), // NOSONAR
	zip_threads(-1), // NOSONAR
	sevenzip_cmd(FindCmd.find7z()), // NOSONAR
	sevenzip_solid(true), // NOSONAR
	sevenzip_level(SevenZipOptions.NORMAL), // NOSONAR
	sevenzip_threads(-1), // NOSONAR
	debug_nocache(false), // NOSONAR
	debug_level(Log.getLevel()), // NOSONAR
	dat2dir_lastsrcdir(null, "dat2dir.lastsrcdir"), // NOSONAR
	dat2dir_lastdstdatdir(null, "dat2dir.lastdstdatdir"), // NOSONAR
	dat2dir_lastdstdir(null, "dat2dir.lastdstdir"), // NOSONAR
	dat2dir_srcdirs("", "dat2dir.srcdirs"), // NOSONAR
	dat2dir_sdr("[]", "dat2dir.sdr"), // NOSONAR
	dat2dir_dry_run(false, "dat2dir.dry_run"), // NOSONAR
	trntchk_lasttrntdir(null, "trntchk.lasttrntdir"), // NOSONAR
	trntchk_lastdstdir(null, "trntchk.lastdstdir"), // NOSONAR
	trntchk_sdr("[]", "trntchk.sdr"), // NOSONAR
	trntchk_mode(TrntChkMode.FILENAME, "trntchk.mode"), // NOSONAR
	trntchk_detect_archived_folders(false, "trntchk.detect_archived_folders"), // NOSONAR
	trntchk_remove_unknown_files(false, "trntchk.remove_unknown_files"), // NOSONAR
	trntchk_remove_wrong_sized_files(false, "trntchk.remove_wrong_sized_files"), // NOSONAR
	compressor_lastdir(null, "compressor.lastdir"), // NOSONAR
	compressor_format(CompressorFormat.TZIP, "compressor.format"), // NOSONAR
	compressor_force(false, "compressor.force"), // NOSONAR
	compressor_parallelism(true, "compressor.parallelism"), // NOSONAR
	dir2dat_lastsrcdir(null, "dir2dat.lastsrcdir"), // NOSONAR
	dir2dat_src_dir(null, "dir2dat.src_dir"), // NOSONAR
	dir2dat_lastdstdir(null, "dir2dat.lastdstdir"), // NOSONAR
	dir2dat_dst_file(null, "dir2dat.dst_file"), // NOSONAR
	dir2dat_format("MAME", "dir2dat.format"), // NOSONAR
	dir2dat_scan_subfolders(true, "dir2dat.scan_subfolders"), // NOSONAR
	dir2dat_deep_scan(false, "dir2dat.deep_scan"), // NOSONAR
	dir2dat_add_md5(false, "dir2dat.add_md5"), // NOSONAR
	dir2dat_add_sha1(false, "dir2dat.add_sha1"), // NOSONAR
	dir2dat_junk_folders(false, "dir2dat.junk_folders"), // NOSONAR
	dir2dat_do_not_scan_archives(false, "dir2dat.do_not_scan_archives"), // NOSONAR
	dir2dat_match_profile(false, "dir2dat.match_profile"), // NOSONAR
	dir2dat_include_empty_dirs(false, "dir2dat.include_empty_dirs"); // NOSONAR
	
	private String name = null;
	private Object dflt = null;
	
	private SettingsEnum(Object dflt)
	{
		this.dflt = dflt;
	}
	
	private SettingsEnum(Object dflt, final String name)
	{
		this.dflt = dflt;
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		if(name!=null)
			return name;
		return name();
	}
	
	@Override
	public Object getDefault()
	{
		return dflt;
	}
	
	public static SettingsEnum from(String name)
	{
		for(final var option : values())
		{
			if(option.name!=null && option.name.equals(name))
				return option;
			if(option.name().equals(name))
				return option;
		}
		return null;
	}
}
