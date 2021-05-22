package jrm.misc;

public enum SettingsEnum
{
	need_sha1_or_md5,
	use_parallelism,
	thread_count,
	create_mode,
	createfull_mode,
	ignore_unneeded_containers,
	ignore_unneeded_entries,
	ignore_unknown_containers,
	implicit_merge,
	ignore_merge_name_roms,
	ignore_merge_name_disks,
	exclude_games,
	exclude_machines,
	backup,
	format,
	merge_mode,
	archives_and_chd_as_roms,
	hash_collision_mode,
	zip_cmd,
	zip_temp_threshold,
	zip_compression_level,
	sevenzip_cmd,
	sevenzip_solid,
	sevenzip_level,
	sevenzip_threads,
	zip_level,
	zip_threads,
	debug_nocache,
	debug_level,
	filter_catver_ini("filter.catver.ini"),
	filter_nplayers_ini("filter.nplayers.ini"),
	filter_InclClones("filter.InclClones"),
	filter_InclDisks("filter.InclDisks"),
	filter_InclSamples("filter.InclSamples"),
	filter_DriverStatus("filter.DriverStatus"),
	filter_DisplayOrientation("filter.DisplayOrientation"),
	filter_CabinetType("filter.CabinetType"),
	filter_YearMin("filter.YearMin"),
	filter_YearMax("filter.YearMax"),
	filter_MinSoftwareSupportedLevel("filter.MinSoftwareSupportedLevel"),
	roms_dest_dir,
	disks_dest_dir_enabled,
	disks_dest_dir,
	swroms_dest_dir_enabled,
	swroms_dest_dir,
	swdisks_dest_dir_enabled,
	swdisks_dest_dir,
	samples_dest_dir_enabled,
	samples_dest_dir,
	src_dir,
	dat2dir_srcdirs("dat2dir.srcdirs"),
	dat2dir_sdr("dat2dir.sdr"),
	dat2dir_dry_run("dat2dir.dry_run"),
	trntchk_sdr("trntchk.sdr"),
	trntchk_mode("trntchk.mode"),
	trntchk_detect_archived_folders("trntchk.detect_archived_folders"),
	trntchk_remove_unknown_files("trntchk.remove_unknown_files"),
	trntchk_remove_wrong_sized_files("trntchk.remove_wrong_sized_files"),
	compressor_format("compressor.format"),
	compressor_force("compressor.force"),
	compressor_parallelism("compressor.parallelism"),
	dir2dat_src_dir("dir2dat.src_dir"),
	dir2dat_dst_file("dir2dat.dst_file"),
	dir2dat_format("dir2dat.format"),
	dir2dat_scan_subfolders("dir2dat.scan_subfolders"),
	dir2dat_deep_scan("dir2dat.deep_scan"),
	dir2dat_add_md5("dir2dat.add_md5"),
	dir2dat_add_sha1("dir2dat.add_sha1"),
	dir2dat_junk_folders("dir2dat.junk_folders"),
	dir2dat_do_not_scan_archives("dir2dat.do_not_scan_archives"),
	dir2dat_match_profile("dir2dat.match_profile"),
	dir2dat_include_empty_dirs("dir2dat.include_empty_dirs"),
	automation_scan("automation.scan");
	
	private String name = null;
	private SettingsEnum()
	{
		
	}
	
	private SettingsEnum(final String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		if(name!=null)
			return name;
		return name();
	}
	
	public static SettingsEnum from(String name)
	{
		for(SettingsEnum option : values())
		{
			if(option.name!=null && option.name.equals(name))
				return option;
			if(option.name().equals(name))
				return option;
		}
		return null;
	}
}
