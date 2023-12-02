package jrm.misc;

import jrm.profile.data.Driver;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.profile.data.Software;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.profile.scan.options.ScanAutomation;

public enum ProfileSettingsEnum implements EnumWithDefault
{
	need_sha1_or_md5(false), // NOSONAR
	use_parallelism(true), // NOSONAR
	create_mode(true), // NOSONAR
	createfull_mode(false), // NOSONAR
	ignore_unneeded_containers(false), // NOSONAR
	ignore_unneeded_entries(false), // NOSONAR
	ignore_unknown_containers(false), // NOSONAR
	implicit_merge(false), // NOSONAR
	ignore_merge_name_roms(false), // NOSONAR
	ignore_merge_name_disks(false), // NOSONAR
	exclude_games(false), // NOSONAR
	exclude_machines(false), // NOSONAR
	backup(false), // NOSONAR
	format(FormatOptions.ZIP), // NOSONAR
	merge_mode(MergeOptions.SPLIT), // NOSONAR
	archives_and_chd_as_roms(false), // NOSONAR
	hash_collision_mode(HashCollisionOptions.SINGLEFILE), // NOSONAR
	filter_catver_ini(null, "filter.catver.ini"), // NOSONAR
	filter_nplayers_ini(null, "filter.nplayers.ini"), // NOSONAR
	filter_InclClones(true, "filter.InclClones"), // NOSONAR
	filter_InclDisks(true, "filter.InclDisks"), // NOSONAR
	filter_InclSamples(true, "filter.InclSamples"), // NOSONAR
	filter_DriverStatus(Driver.StatusType.preliminary, "filter.DriverStatus"), // NOSONAR
	filter_DisplayOrientation(DisplayOrientation.any, "filter.DisplayOrientation"), // NOSONAR
	filter_CabinetType(CabinetType.any, "filter.CabinetType"), // NOSONAR
	filter_YearMin("", "filter.YearMin"), // NOSONAR
	filter_YearMax("????", "filter.YearMax"), // NOSONAR
	filter_MinSoftwareSupportedLevel(Software.Supported.no, "filter.MinSoftwareSupportedLevel"), // NOSONAR
	roms_dest_dir(""), // NOSONAR
	disks_dest_dir_enabled(false), // NOSONAR
	disks_dest_dir(""), // NOSONAR
	swroms_dest_dir_enabled(false), // NOSONAR
	swroms_dest_dir(""), // NOSONAR
	swdisks_dest_dir_enabled(false), // NOSONAR
	swdisks_dest_dir(""), // NOSONAR
	samples_dest_dir_enabled(false), // NOSONAR
	samples_dest_dir(""), // NOSONAR
	backup_dest_dir_enabled(false), // NOSONAR
	backup_dest_dir("%work/backup"), // NOSONAR
	src_dir("|"), // NOSONAR
	automation_scan(ScanAutomation.SCAN, "automation.scan"), // NOSONAR
	exclusion_glob_list("|"); // NOSONAR

	private String name = null;
	private Object dflt = null;

	private ProfileSettingsEnum(Object dflt)
	{
		this.dflt = dflt;
	}

	private ProfileSettingsEnum(Object dflt, final String name)
	{
		this.dflt = dflt;
		this.name = name;
	}

	@Override
	public String toString()
	{
		if (name != null)
			return name;
		return name();
	}

	@Override
	public Object getDefault()
	{
		return dflt;
	}

	public static ProfileSettingsEnum from(String name)
	{
		for (final var option : values())
		{
			if (option.name != null && option.name.equals(name))
				return option;
			if (option.name().equals(name))
				return option;
		}
		return null;
	}
}
