package jrm.misc;

import java.io.File;
import java.io.IOException;

import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.security.Session;

public class ProfileSettings extends Settings
{
	/**
	 * The merge mode used while filtering roms/disks
	 */
	public transient MergeOptions merge_mode;
	/**
	 * Must we strictly conform to merge tag (explicit), or search merge-able ROMs by ourselves (implicit)
	 */
	public transient Boolean implicit_merge;
	/**
	 * What hash collision mode is used?
	 */
	public transient HashCollisionOptions hash_collision_mode;
	

	public ProfileSettings()
	{
		super();
	}

	public static void DIR(Session session, File src) throws IOException
	{
		ProfileSettings settings = new ProfileSettings();
		settings.setProperty("need_sha1_or_md5", false); //$NON-NLS-1$
		settings.setProperty("use_parallelism", true); //$NON-NLS-1$
		settings.setProperty("create_mode", true); //$NON-NLS-1$
		settings.setProperty("createfull_mode", false); //$NON-NLS-1$
		settings.setProperty("ignore_unneeded_containers", false); //$NON-NLS-1$
		settings.setProperty("ignore_unneeded_entries", false); //$NON-NLS-1$
		settings.setProperty("ignore_unknown_containers", true); //$NON-NLS-1$
		settings.setProperty("implicit_merge", false); //$NON-NLS-1$
		settings.setProperty("ignore_merge_name_roms", false); //$NON-NLS-1$
		settings.setProperty("ignore_merge_name_disks", false); //$NON-NLS-1$
		settings.setProperty("exclude_games", false); //$NON-NLS-1$
		settings.setProperty("exclude_machines", false); //$NON-NLS-1$
		settings.setProperty("backup", true); //$NON-NLS-1$
		settings.setProperty("format", FormatOptions.DIR.toString()); //$NON-NLS-1$
		settings.setProperty("merge_mode", MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
		settings.setProperty("archives_and_chd_as_roms", true); //$NON-NLS-1$
		session.getUser().settings.saveProfileSettings(src, settings);
	}
	
	public static void TZIP(Session session, File src) throws IOException
	{
		ProfileSettings settings = new ProfileSettings();
		settings.setProperty("need_sha1_or_md5", false); //$NON-NLS-1$
		settings.setProperty("use_parallelism", true); //$NON-NLS-1$
		settings.setProperty("create_mode", true); //$NON-NLS-1$
		settings.setProperty("createfull_mode", false); //$NON-NLS-1$
		settings.setProperty("ignore_unneeded_containers", false); //$NON-NLS-1$
		settings.setProperty("ignore_unneeded_entries", false); //$NON-NLS-1$
		settings.setProperty("ignore_unknown_containers", true); //$NON-NLS-1$
		settings.setProperty("implicit_merge", false); //$NON-NLS-1$
		settings.setProperty("ignore_merge_name_roms", false); //$NON-NLS-1$
		settings.setProperty("ignore_merge_name_disks", false); //$NON-NLS-1$
		settings.setProperty("exclude_games", false); //$NON-NLS-1$
		settings.setProperty("exclude_machines", false); //$NON-NLS-1$
		settings.setProperty("backup", true); //$NON-NLS-1$
		settings.setProperty("format", FormatOptions.TZIP.toString()); //$NON-NLS-1$
		settings.setProperty("merge_mode", MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
		settings.setProperty("archives_and_chd_as_roms", false); //$NON-NLS-1$
		session.getUser().settings.saveProfileSettings(src, settings);
	}

	@Override
	protected void propagate(String property, String value)
	{
		if("merge_mode".equals(property))
			merge_mode = value!=null?MergeOptions.valueOf(value):null;
		else if("implicit_merge".equals(property))
			implicit_merge = value!=null?Boolean.parseBoolean(value):null;
		else if("hash_collision_mode".equals(property))
			hash_collision_mode = value!=null?HashCollisionOptions.valueOf(value):null;
	}

}
