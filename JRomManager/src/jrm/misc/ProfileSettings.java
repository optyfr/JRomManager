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
		settings.setProperty(SettingsEnum.need_sha1_or_md5, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.use_parallelism, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.create_mode, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.createfull_mode, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_unneeded_containers, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_unneeded_entries, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_unknown_containers, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.implicit_merge, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_merge_name_roms, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_merge_name_disks, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.exclude_games, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.exclude_machines, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.backup, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.format, FormatOptions.DIR.toString()); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.merge_mode, MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.archives_and_chd_as_roms, true); //$NON-NLS-1$
		session.getUser().getSettings().saveProfileSettings(src, settings);
	}
	
	public static void TZIP(Session session, File src) throws IOException
	{
		ProfileSettings settings = new ProfileSettings();
		settings.setProperty(SettingsEnum.need_sha1_or_md5, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.use_parallelism, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.create_mode, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.createfull_mode, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_unneeded_containers, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_unneeded_entries, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_unknown_containers, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.implicit_merge, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_merge_name_roms, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.ignore_merge_name_disks, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.exclude_games, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.exclude_machines, false); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.backup, true); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.format, FormatOptions.TZIP.toString()); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.merge_mode, MergeOptions.NOMERGE.toString()); //$NON-NLS-1$
		settings.setProperty(SettingsEnum.archives_and_chd_as_roms, false); //$NON-NLS-1$
		session.getUser().getSettings().saveProfileSettings(src, settings);
	}

	@Override
	protected void propagate(Enum<?> property, String value)
	{
		if(SettingsEnum.merge_mode==property)
			merge_mode = value!=null?MergeOptions.valueOf(value):null;
		else if(SettingsEnum.implicit_merge==property)
			implicit_merge = value!=null?Boolean.parseBoolean(value):null;
		else if(SettingsEnum.hash_collision_mode==property)
			hash_collision_mode = value!=null?HashCollisionOptions.valueOf(value):null;
	}

}
