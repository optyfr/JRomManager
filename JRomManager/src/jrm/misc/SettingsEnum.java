package jrm.misc;

public enum SettingsEnum
{
	need_sha1_or_md5,	//NOSONAR
	use_parallelism,	//NOSONAR
	thread_count,	//NOSONAR
	create_mode,	//NOSONAR
	createfull_mode,	//NOSONAR
	ignore_unneeded_containers,	//NOSONAR
	ignore_unneeded_entries,	//NOSONAR
	ignore_unknown_containers,	//NOSONAR
	implicit_merge,	//NOSONAR
	ignore_merge_name_roms,	//NOSONAR
	ignore_merge_name_disks,	//NOSONAR
	exclude_games,	//NOSONAR
	exclude_machines,	//NOSONAR
	backup,	//NOSONAR
	format,	//NOSONAR
	merge_mode,	//NOSONAR
	archives_and_chd_as_roms,	//NOSONAR
	hash_collision_mode,	//NOSONAR
	zip_cmd,	//NOSONAR
	zip_temp_threshold,	//NOSONAR
	zip_compression_level,	//NOSONAR
	sevenzip_cmd,	//NOSONAR
	sevenzip_solid,	//NOSONAR
	sevenzip_level,	//NOSONAR
	sevenzip_threads,	//NOSONAR
	zip_level,	//NOSONAR
	zip_threads,	//NOSONAR
	debug_nocache,	//NOSONAR
	debug_level,	//NOSONAR
	filter_catver_ini("filter.catver.ini"),	//NOSONAR
	filter_nplayers_ini("filter.nplayers.ini"),	//NOSONAR
	filter_InclClones("filter.InclClones"),	//NOSONAR
	filter_InclDisks("filter.InclDisks"),	//NOSONAR
	filter_InclSamples("filter.InclSamples"),	//NOSONAR
	filter_DriverStatus("filter.DriverStatus"),	//NOSONAR
	filter_DisplayOrientation("filter.DisplayOrientation"),	//NOSONAR
	filter_CabinetType("filter.CabinetType"),	//NOSONAR
	filter_YearMin("filter.YearMin"),	//NOSONAR
	filter_YearMax("filter.YearMax"),	//NOSONAR
	filter_MinSoftwareSupportedLevel("filter.MinSoftwareSupportedLevel"),	//NOSONAR
	roms_dest_dir,	//NOSONAR
	disks_dest_dir_enabled,	//NOSONAR
	disks_dest_dir,	//NOSONAR
	swroms_dest_dir_enabled,	//NOSONAR
	swroms_dest_dir,	//NOSONAR
	swdisks_dest_dir_enabled,	//NOSONAR
	swdisks_dest_dir,	//NOSONAR
	samples_dest_dir_enabled,	//NOSONAR
	samples_dest_dir,	//NOSONAR
	backup_dest_dir_enabled,	//NOSONAR
	backup_dest_dir,	//NOSONAR
	src_dir,	//NOSONAR
	dat2dir_srcdirs("dat2dir.srcdirs"),	//NOSONAR
	dat2dir_sdr("dat2dir.sdr"),	//NOSONAR
	dat2dir_dry_run("dat2dir.dry_run"),	//NOSONAR
	trntchk_sdr("trntchk.sdr"),	//NOSONAR
	trntchk_mode("trntchk.mode"),	//NOSONAR
	trntchk_detect_archived_folders("trntchk.detect_archived_folders"),	//NOSONAR
	trntchk_remove_unknown_files("trntchk.remove_unknown_files"),	//NOSONAR
	trntchk_remove_wrong_sized_files("trntchk.remove_wrong_sized_files"),	//NOSONAR
	compressor_format("compressor.format"),	//NOSONAR
	compressor_force("compressor.force"),	//NOSONAR
	compressor_parallelism("compressor.parallelism"),	//NOSONAR
	dir2dat_src_dir("dir2dat.src_dir"),	//NOSONAR
	dir2dat_dst_file("dir2dat.dst_file"),	//NOSONAR
	dir2dat_format("dir2dat.format"),	//NOSONAR
	dir2dat_scan_subfolders("dir2dat.scan_subfolders"),	//NOSONAR
	dir2dat_deep_scan("dir2dat.deep_scan"),	//NOSONAR
	dir2dat_add_md5("dir2dat.add_md5"),	//NOSONAR
	dir2dat_add_sha1("dir2dat.add_sha1"),	//NOSONAR
	dir2dat_junk_folders("dir2dat.junk_folders"),	//NOSONAR
	dir2dat_do_not_scan_archives("dir2dat.do_not_scan_archives"),	//NOSONAR
	dir2dat_match_profile("dir2dat.match_profile"),	//NOSONAR
	dir2dat_include_empty_dirs("dir2dat.include_empty_dirs"),	//NOSONAR
	automation_scan("automation.scan");	//NOSONAR
	
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
