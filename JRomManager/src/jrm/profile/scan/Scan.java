/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.scan;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

import JTrrntzip.TrrntZipStatus;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.Settings;
import jrm.profile.Profile;
import jrm.profile.data.*;
import jrm.profile.fix.actions.*;
import jrm.profile.report.*;
import jrm.profile.report.SubjectSet.Status;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.MainFrame;
import jrm.ui.progress.ProgressHandler;

/**
 * The scan class
 * @author optyfr
 */
public class Scan
{
	/**
	 * the attached {@link Report}
	 */
	public final static Report report = new Report();
	/**
	 * All the actions to take for fixing the set after scan
	 */
	public final ArrayList<Collection<jrm.profile.fix.actions.ContainerAction>> actions = new ArrayList<>();

	/**
	 * The current profile
	 */
	private final Profile profile;
	
	/*
	 * All options variables
	 */
	private final MergeOptions merge_mode;
	private final boolean implicit_merge;
	private final FormatOptions format;
	private final boolean create_mode;
	private final boolean createfull_mode;
	private final boolean ignore_unneeded_containers;
	private final boolean ignore_unneeded_entries;
	private final boolean ignore_unknown_containers;
	private final boolean backup;
	private final HashCollisionOptions hash_collision_mode;
	
	/*
	 * All Dir Scans variables
	 */
	/**
	 * Roms dst scan result
	 */
	private DirScan roms_dstscan = null;
	/**
	 * Disks dst scan result
	 */
	private DirScan disks_dstscan = null;
	/**
	 * Samples dst scan result
	 */
	private DirScan samples_dstscan = null;
	/**
	 * Software lists roms dst scans
	 */
	private final Map<String, DirScan> swroms_dstscans = new HashMap<>();
	/**
	 * Software lists disks dst scans
	 */
	private Map<String, DirScan> swdisks_dstscans = new HashMap<>();
	/**
	 * Contains all src and dst scans
	 */
	private final List<DirScan> allscans = new ArrayList<>();
	
	/**
	 * backup actions, always made first on entries that will be removed
	 */
	private final ArrayList<jrm.profile.fix.actions.ContainerAction> backup_actions = new ArrayList<>();
	/**
	 * create actions, only for entries on totally new sets
	 */
	private final ArrayList<jrm.profile.fix.actions.ContainerAction> create_actions = new ArrayList<>();
	/**
	 * rename before actions, all entries that will be delete are renamed first, to avoid collision from add and because they can be used for another add elsewhere during fix
	 */
	private final ArrayList<jrm.profile.fix.actions.ContainerAction> rename_before_actions = new ArrayList<>();
	/**
	 * add actions
	 */
	private final ArrayList<jrm.profile.fix.actions.ContainerAction> add_actions = new ArrayList<>();
	/**
	 * delete actions
	 */
	private final ArrayList<jrm.profile.fix.actions.ContainerAction> delete_actions = new ArrayList<>();
	/**
	 * rename after actions, for entries that need to replace another entry that have to be delete first
	 */
	private final ArrayList<jrm.profile.fix.actions.ContainerAction> rename_after_actions = new ArrayList<>();
	/**
	 * duplicate actions
	 */
	private final ArrayList<jrm.profile.fix.actions.ContainerAction> duplicate_actions = new ArrayList<>();
	/**
	 * torrentzip actions, always the last actions when there is no more to do on zip archive
	 */
	private final Map<String, jrm.profile.fix.actions.ContainerAction> tzip_actions = new HashMap<>();

	/**
	 * get a negated {@link Predicate} from a provided {@link Predicate}
	 * @param predicate the {@link Predicate} to negate
	 * @param <T> the type of the input to the predicate
	 * @return the negated {@link Predicate}
	 */
	private static <T> Predicate<T> not(final Predicate<T> predicate)
	{
		return predicate.negate();
	}
	
	/**
	 * The constructor
	 * @param profile the current {@link Profile}
	 * @param handler the {@link ProgressHandler} to show progression on UI
	 * @throws BreakException
	 */
	public Scan(final Profile profile, final ProgressHandler handler) throws BreakException
	{
		this(profile, handler, null);
	}
	
	/**
	 * The constructor
	 * @param profile the current {@link Profile}
	 * @param handler the {@link ProgressHandler} to show progression on UI
	 * @param scancache a cache for src {@link DirScan}
	 * @throws BreakException
	 */
	public Scan(final Profile profile, final ProgressHandler handler, Map<String, DirScan> scancache) throws BreakException
	{
		this.profile = profile;
		profile.setPropsCheckPoint();
		Scan.report.reset();
		
		/*
		 * Store locally various profile settings
		 */
		format = FormatOptions.valueOf(profile.getProperty("format", FormatOptions.ZIP.toString())); //$NON-NLS-1$
		merge_mode = MergeOptions.valueOf(profile.getProperty("merge_mode", MergeOptions.SPLIT.toString())); //$NON-NLS-1$
		implicit_merge = profile.getProperty("implicit_merge", false); //$NON-NLS-1$
		create_mode = profile.getProperty("create_mode", true); //$NON-NLS-1$
		createfull_mode = profile.getProperty("createfull_mode", true); //$NON-NLS-1$
		hash_collision_mode = HashCollisionOptions.valueOf(profile.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString())); //$NON-NLS-1$
		ignore_unneeded_containers = profile.getProperty("ignore_unneeded_containers", false); //$NON-NLS-1$
		ignore_unneeded_entries = profile.getProperty("ignore_unneeded_entries", false); //$NON-NLS-1$
		ignore_unknown_containers = profile.getProperty("ignore_unknown_containers", false); //$NON-NLS-1$
		backup = profile.getProperty("backup", true); //$NON-NLS-1$

		final String dstdir_txt = profile.getProperty("roms_dest_dir", ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (dstdir_txt.isEmpty())
		{
			System.err.println("dst dir is empty"); //$NON-NLS-1$
			return; //TODO be more informative on failure
		}
		final File roms_dstdir = new File(dstdir_txt);
		if (!roms_dstdir.isDirectory())
		{
			System.err.println("dst dir is not a directory"); //$NON-NLS-1$
			return; //TODO be more informative on failure
		}
		
		/*
		 * use disks dest dir if enabled otherwise it will be the same than roms dest dir
		 */
		File disks_dstdir = new File(roms_dstdir.getAbsolutePath());
		if (profile.getProperty("disks_dest_dir_enabled", false)) //$NON-NLS-1$
		{
			final String disks_dstdir_txt = profile.getProperty("disks_dest_dir", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (disks_dstdir_txt.isEmpty())
				return; //TODO be more informative on failure
			disks_dstdir = new File(disks_dstdir_txt);
		}
		
		/*
		 * use sw roms dest dir if enabled otherwise it will be the same than roms dest dir
		 */
		File swroms_dstdir = new File(roms_dstdir.getAbsolutePath());
		if (profile.getProperty("swroms_dest_dir_enabled", false)) //$NON-NLS-1$
		{
			final String swroms_dstdir_txt = profile.getProperty("swroms_dest_dir", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (swroms_dstdir_txt.isEmpty())
				return; //TODO be more informative on failure
			swroms_dstdir = new File(swroms_dstdir_txt);
		}
		
		/*
		 * use sw disks dest dir if enabled otherwise it will be the same than disks dest dir (which in turn can be the same than roms dest dir)
		 */
		File swdisks_dstdir = new File(swroms_dstdir.getAbsolutePath());
		if (profile.getProperty("swdisks_dest_dir_enabled", false)) //$NON-NLS-1$
		{
			final String swdisks_dstdir_txt = profile.getProperty("swdisks_dest_dir", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (swdisks_dstdir_txt.isEmpty())
				return; //TODO be more informative on failure
			swdisks_dstdir = new File(swdisks_dstdir_txt);
		}

		/*
		 * use samples dest dir if enabled and valid, otherwise it's null and not used
		 */
		final String samples_dstdir_txt = profile.getProperty("samples_dest_dir", ""); //$NON-NLS-1$ //$NON-NLS-2$
		final File samples_dstdir = profile.getProperty("samples_dest_dir_enabled", false) && samples_dstdir_txt.length() > 0 ? new File(samples_dstdir_txt) : null; //$NON-NLS-1$

		/*
		 * explode all src dir string into an ArrayList<File>
		 */
		final ArrayList<File> srcdirs = new ArrayList<>();
		for (final String s : profile.getProperty("src_dir", "").split("\\|")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			if (!s.isEmpty())
			{
				final File f = new File(s);
				if (f.isDirectory())
					srcdirs.add(f);
			}
		}
		/* then add extra backup dir to that list */
		srcdirs.add(new File(Settings.getWorkPath().toFile(), "backup")); //$NON-NLS-1$
		/* then scan all dirs from that list */
		for (final File dir : srcdirs)
		{
			if(scancache != null)
			{
				String cachefile  = DirScan.getCacheFile(dir, DirScan.getOptions(profile, false)).getAbsolutePath();
				if(!scancache.containsKey(cachefile))
					scancache.put(cachefile, new DirScan(profile, dir, handler, false));
				allscans.add(scancache.get(cachefile));
			}
			else
				allscans.add(new DirScan(profile, dir, handler, false));
			if (handler.isCancel())
				throw new BreakException();
		}
		
		/*
		 * scan all dst dirs according machines and softwares in profile, and determinate what is unknown and what is unneeded
		 */
		final ArrayList<Container> unknown = new ArrayList<>();
		final ArrayList<Container> unneeded = new ArrayList<>();
		final ArrayList<Container> samples_unknown = new ArrayList<>();
		final ArrayList<Container> samples_unneeded = new ArrayList<>();
		if (profile.machinelist_list.get(0).size() > 0)
		{
			profile.machinelist_list.get(0).resetFilteredName();
			roms_dstscan = dirscan(profile.machinelist_list.get(0), roms_dstdir, unknown, unneeded, handler);
			if (roms_dstdir.equals(disks_dstdir))
				disks_dstscan = roms_dstscan;
			else
				disks_dstscan = dirscan(profile.machinelist_list.get(0), disks_dstdir, unknown, unneeded, handler);
			if (samples_dstdir != null && samples_dstdir.isDirectory())
				samples_dstscan = dirscan(profile.machinelist_list.get(0).samplesets, samples_dstdir, samples_unknown, samples_unneeded, handler);
			if (handler.isCancel())
				throw new BreakException();
		}
		if (profile.machinelist_list.softwarelist_list.size() > 0)
		{
			final AtomicInteger j = new AtomicInteger();
			handler.setProgress2(String.format("%d/%d", j.get(), profile.machinelist_list.softwarelist_list.size()), j.get(), profile.machinelist_list.softwarelist_list.size()); //$NON-NLS-1$
			for (final SoftwareList sl : profile.machinelist_list.softwarelist_list.getFilteredStream().collect(Collectors.toList()))
			{
				sl.resetFilteredName();
				File sldir = new File(swroms_dstdir, sl.getName());
				swroms_dstscans.put(sl.getName(), dirscan(sl, sldir, unknown, unneeded, handler));
				if (swroms_dstdir.equals(swdisks_dstdir))
					swdisks_dstscans = swroms_dstscans;
				else
				{
					sldir = new File(swdisks_dstdir, sl.getName());
					swdisks_dstscans.put(sl.getName(), dirscan(sl, sldir, unknown, unneeded, handler));
				}
				handler.setProgress2(String.format("%d/%d (%s)", j.incrementAndGet(), profile.machinelist_list.softwarelist_list.size(), sl.getName()), j.get(), profile.machinelist_list.softwarelist_list.size()); //$NON-NLS-1$
				if (handler.isCancel())
					throw new BreakException();
			}
			handler.setProgress2(null, null);
			if(swroms_dstdir.isDirectory() && !swroms_dstdir.equals(roms_dstdir)) for (final File f : swroms_dstdir.listFiles())
			{
				if (!swroms_dstscans.containsKey(f.getName()))
					unknown.add(f.isDirectory() ? new Directory(f, (Machine) null) : new Archive(f, (Machine) null));
				if (handler.isCancel())
					throw new BreakException();
			}
			if (!swroms_dstdir.equals(swdisks_dstdir))
			{
				if(swdisks_dstdir.isDirectory()) for (final File f : swdisks_dstdir.listFiles())
				{
					if (!swdisks_dstscans.containsKey(f.getName()))
						unknown.add(f.isDirectory() ? new Directory(f, (Machine) null) : new Archive(f, (Machine) null));
					if (handler.isCancel())
						throw new BreakException();
				}
			}
		}

		/* reset progress style */
		handler.setInfos(1,false);

		
		try
		{
			/*
			 * process and report unknown actions if requested
			 */
			if (!ignore_unknown_containers)
			{
				unknown.forEach((c) -> {
					Scan.report.add(new ContainerUnknown(c));
					delete_actions.add(new DeleteContainer(c, format));
				});
			}
			/*
			 * process and report unneeded actions if requested
			 */
			if(!ignore_unneeded_containers)
			{
				unneeded.forEach(c->{
					Scan.report.add(new ContainerUnneeded(c));
					backup_actions.add(new BackupContainer(c));
					delete_actions.add(new DeleteContainer(c, format));					
				});
			}
			/*
			 * report suspicious CRCs
			 */
			profile.suspicious_crc.forEach((crc) -> Scan.report.add(new RomSuspiciousCRC(crc)));

			/*
			 * Searching for fixes
			 */
			final AtomicInteger i = new AtomicInteger();
			final AtomicInteger j = new AtomicInteger();
			handler.setProgress(Messages.getString("Scan.SearchingForFixes"), i.get(), profile.subsize()); //$NON-NLS-1$
			handler.setProgress2(String.format("%d/%d", j.get(), profile.size()), j.get(), profile.size()); //$NON-NLS-1$
			if (profile.machinelist_list.get(0).size() > 0)
			{
				
				/* Scan all samples */
				handler.setProgress2(String.format("%d/%d", j.incrementAndGet(), profile.size()), j.get(), profile.size()); //$NON-NLS-1$
				for(final Samples set : profile.machinelist_list.get(0).samplesets)
				{
					// for each sample
					handler.setProgress(null, i.incrementAndGet(), null, set.getName());
					if (samples_dstscan != null)
						scanSamples(set);
					if (handler.isCancel())
						throw new BreakException();
				}
				/* scan all machines */ 
				profile.machinelist_list.get(0).forEach(Machine::resetCollisionMode);
				profile.machinelist_list.get(0).getFilteredStream().forEach(m -> {
					// for each machine
					handler.setProgress(null, i.incrementAndGet(), null, m.getFullName());
					scanWare(m);
					if (handler.isCancel())
						throw new BreakException();
				});
			}
			if (profile.machinelist_list.softwarelist_list.size() > 0)
			{
				/* scan all software lists */
				profile.machinelist_list.softwarelist_list.getFilteredStream().forEach(sl -> {
					// for each software list
					handler.setProgress2(String.format("%d/%d (%s)", j.incrementAndGet(), profile.size(), sl.getName()), j.get(), profile.size()); //$NON-NLS-1$
					roms_dstscan = swroms_dstscans.get(sl.getName());
					disks_dstscan = swdisks_dstscans.get(sl.getName());
					sl.getFilteredStream().forEach(Software::resetCollisionMode);
					sl.getFilteredStream().forEach(s -> {
						// for each software
						handler.setProgress(null, i.incrementAndGet(), null, s.getFullName());
						scanWare(s);
						if (handler.isCancel())
							throw new BreakException();
					});
				});
			}
			handler.setProgress2(null, null);
		}
		catch (final BreakException e)
		{
			throw e;
		}
		catch (final Throwable e)
		{
			Log.err("Other Exception when listing", e); //$NON-NLS-1$
		}
		finally
		{
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			/* save report */
			Scan.report.write();
			Scan.report.flush();
			/* update entries in profile viewer */ 
			if (MainFrame.profile_viewer != null)
				MainFrame.profile_viewer.reload();
			/* update and save stats */
			profile.nfo.stats.scanned = new Date();
			profile.nfo.stats.haveSets = Stream.concat(profile.machinelist_list.stream(), profile.machinelist_list.softwarelist_list.stream()).mapToLong(AnywareList::countHave).sum();
			profile.nfo.stats.haveRoms = Stream.concat(profile.machinelist_list.stream(), profile.machinelist_list.softwarelist_list.stream()).flatMap(AnywareList::stream).mapToLong(Anyware::countHaveRoms).sum();
			profile.nfo.stats.haveDisks = Stream.concat(profile.machinelist_list.stream(), profile.machinelist_list.softwarelist_list.stream()).flatMap(AnywareList::stream).mapToLong(Anyware::countHaveDisks).sum();
			profile.nfo.save();
			/* save again profile cache with scan entity status */
			profile.save(); 
		}

		/*
		 * add all actions lists, to the main actions list
		 */
		if(backup)
			actions.add(backup_actions);
		actions.add(create_actions);
		actions.add(rename_before_actions);
		actions.add(duplicate_actions);
		actions.add(add_actions);
		actions.add(delete_actions);
		actions.add(rename_after_actions);
		actions.add(tzip_actions.values());

	}

	/**
	 * scan dir, and determinate what is unknown and what is unneeded according {@link ByName} (Software or Machine)
	 * @param byname the {@link ByName} (Software or Machine)
	 * @param dstdir the dir {@link File} to scan with {@link DirScan}
	 * @param unknown the {@link List} that will receive unknown {@link Container}s
	 * @param unneeded the {@link List} that will receive unneeded {@link Container}s
	 * @param handler the {@link ProgressHandler} to show progression on UI
	 * @return a {@link DirScan} object
	 */
	private DirScan dirscan(final ByName<?> byname, final File dstdir, final List<Container> unknown, final List<Container> unneeded, final ProgressHandler handler)
	{
		final DirScan dstscan;
		allscans.add(dstscan = new DirScan(profile, dstdir, handler, true));
		for (final Container c : dstscan.getContainersIterable())
		{
			if (c.getType() == Container.Type.UNK)
				unknown.add(c);
			else if (!byname.containsFilteredName(FilenameUtils.getBaseName(c.file.toString())))
			{
				if(byname.containsName(FilenameUtils.getBaseName(c.file.toString())))
					unneeded.add(c);
				else
					unknown.add(c);
			}
		}
		return dstscan;
	}

	/**
	 * Determinate if a container need to be torrentzipped
	 * @param report_subject a SubjectSet containing the report about this archive
	 * @param archive the {@link Container} to eventually torrentzip
	 * @param ware the {@link Anyware} corresponding to the machine or Software of the archive
	 * @param roms the filtered {@link Rom} {@link List}
	 */
	private void prepTZip(final SubjectSet report_subject, final Container archive, final Anyware ware, final List<Rom> roms)
	{
		if (format == FormatOptions.TZIP)
		{
			if (!merge_mode.isMerge() || !ware.isClone())
			{
				if (!report_subject.isMissing() && !report_subject.isUnneeded() && roms.size() > 0)
				{
					Container tzipcontainer = null;
					final Container container = roms_dstscan.getContainerByName(ware.getDest(merge_mode, implicit_merge).getName() + format.getExt());
					if (container != null)
					{
						if (container.lastTZipCheck < container.modified)
							tzipcontainer = container;
						else if (!container.lastTZipStatus.contains(TrrntZipStatus.ValidTrrntzip))
							tzipcontainer = container;
						else if (report_subject.hasFix())
							tzipcontainer = container;

					}
					else if (create_mode && report_subject.hasFix())
						tzipcontainer = archive;
					if (tzipcontainer != null)
					{
						tzipcontainer.m = ware;
						tzip_actions.put(tzipcontainer.file.getAbsolutePath(), new TZipContainer(tzipcontainer, format));
						Scan.report.add(new ContainerTZip(tzipcontainer));
					}
				}
			}
		}
	}

	/**
	 * Determinate if a samples container need to be torrentzipped
	 * @param report_subject report_subject a SubjectSet containing the report about this archive
	 * @param archive the {@link Container} to eventually torrentzip
	 * @param set the set of samples
	 */
	private void prepTZip(final SubjectSet report_subject, final Container archive, final Samples set)
	{
		if (format == FormatOptions.TZIP)
		{
			if (!report_subject.isMissing() && !report_subject.isUnneeded() && set.samples.size()>0)
			{
				Container tzipcontainer = null;
				final Container container = samples_dstscan.getContainerByName(archive.file.getName());
				if (container != null)
				{
					if (container.lastTZipCheck < container.modified)
						tzipcontainer = container;
					else if (!container.lastTZipStatus.contains(TrrntZipStatus.ValidTrrntzip))
						tzipcontainer = container;
					else if (report_subject.hasFix())
						tzipcontainer = container;
				}
				else if (create_mode && report_subject.hasFix())
					tzipcontainer = archive;
				if (tzipcontainer != null)
				{
					tzipcontainer.m = set;
					tzip_actions.put(tzipcontainer.file.getAbsolutePath(), new TZipContainer(tzipcontainer, format));
					Scan.report.add(new ContainerTZip(tzipcontainer));
				}
			}
		}
	}


	/**
	 * Remove archive formats of a set that are not the current format target
	 * @param ware the {@link Anyware}, a machine or software from which to remove unneeded format archives
	 */
	private void removeOtherFormats(final Anyware ware)
	{
		format.getExt().allExcept().forEach((e) -> { // set other formats with the same set name as unneeded
			final Container c = roms_dstscan.getContainerByName(ware.getName() + e);
			if (c != null)
			{
				Scan.report.add(new ContainerUnneeded(c));
				backup_actions.add(new BackupContainer(c));
				delete_actions.add(new DeleteContainer(c, format));
			}
		});
	}

	/**
	 * Remove unneeded clones archives (case we switched from split or non-merged to merged mode)
	 * @param ware the {@link Anyware}, a machine or software from which to verify if it's a clone and remove its archive
	 * @param disks the filtered {@link Disk} {@link List}
	 * @param roms the filtered {@link Rom} {@link List}
	 */
	private void removeUnneededClone(final Anyware ware, final List<Disk> disks, final List<Rom> roms)
	{
		if (merge_mode.isMerge() && ware.isClone())
		{
			if (format == FormatOptions.DIR && disks.size() == 0 && roms.size() == 0)
			{
				Arrays.asList(roms_dstscan.getContainerByName(ware.getName()), disks_dstscan.getContainerByName(ware.getName())).forEach(c -> {
					if (c != null)
					{
						Scan.report.add(new ContainerUnneeded(c));
						backup_actions.add(new BackupContainer(c));
						delete_actions.add(new DeleteContainer(c, format));
					}
				});
			}
			else if (disks.size() == 0)
			{
				final Container c = disks_dstscan.getContainerByName(ware.getName());
				if (c != null)
				{
					Scan.report.add(new ContainerUnneeded(c));
					backup_actions.add(new BackupContainer(c));
					delete_actions.add(new DeleteContainer(c, format));
				}
			}
			if (format != FormatOptions.DIR && roms.size() == 0)
			{
				final Container c = roms_dstscan.getContainerByName(ware.getName() + format.getExt());
				if (c != null)
				{
					Scan.report.add(new ContainerUnneeded(c));
					backup_actions.add(new BackupContainer(c));
					delete_actions.add(new DeleteContainer(c, format));
				}
			}
		}
	}

	/**
	 * Scan disks
	 * @param ware the current {@link Anyware} we are processing
	 * @param disks the filtered {@link Disk} {@link List}
	 * @param directory the {@link Directory} in which the disks will reside
	 * @param report_subject the {@link SubjectSet} report related to this {@link Anyware}
	 * @return true if set is currently missing
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean scanDisks(final Anyware ware, final List<Disk> disks, final Directory directory, final SubjectSet report_subject)
	{
		boolean missing_set = true;
		final Container container;
		if (null != (container = disks_dstscan.getContainerByName(ware.getDest(merge_mode, implicit_merge).getNormalizedName())))
		{
			missing_set = false;
			if (disks.size() > 0)
			{
				report_subject.setFound();

				final ArrayList<Entry> disks_found = new ArrayList<>();
				final Map<String, Disk> disks_byname = Disk.getDisksByName(disks);
				OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null,
						duplicate_set = null;
				for (final Disk disk : disks)
				{
					disk.setStatus(EntityStatus.KO);
					Entry found_entry = null;
					final Map<String, Entry> entries_byname = container.getEntriesByName();
					for (final Entry candidate_entry : container.getEntries())
					{
						if (candidate_entry.equals(disk))
						{
							if (!disk.getNormalizedName().equals(candidate_entry.getName())) // but this entry name does not match the rom name
							{
								final Disk another_disk;
								if (null != (another_disk = disks_byname.get(candidate_entry.getName())) && candidate_entry.equals(another_disk))
								{
									if (entries_byname.containsKey(disk.getNormalizedName()))
									{
										// report_w.println("["+m.name+"] "+d.getName()+" == "+e.file);
									}
									else
									{
										// we must duplicate
										report_subject.add(new EntryMissingDuplicate(disk, candidate_entry));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, directory, format, 0L)).addAction(new DuplicateEntry(disk.getName(), candidate_entry));
										found_entry = candidate_entry;
									}
								}
								else
								{
									if (!entries_byname.containsKey(disk.getNormalizedName())) // and disk name is not in the entries
									{
										report_subject.add(new EntryWrongName(disk, candidate_entry));
										(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format, 0L)).addAction(new RenameEntry(candidate_entry));
										(rename_after_set = OpenContainer.getInstance(rename_after_set, directory, format, 0L)).addAction(new RenameEntry(disk.getName(), candidate_entry));
										found_entry = candidate_entry;
										break;
									}
								}
							}
							else
							{
								found_entry = candidate_entry;
								break;
							}
						}
						else if (disk.getNormalizedName().equals(candidate_entry.getName()))
						{
							report_subject.add(new EntryWrongHash(disk, candidate_entry));
							// found = e;
							break;
						}
					}
					if (found_entry == null)
					{
						Scan.report.stats.missing_disks_cnt++;
						for (final DirScan scan : allscans)
						{
							if (null != (found_entry = scan.find_byhash(disk)))
							{
								report_subject.add(new EntryAdd(disk, found_entry));
								(add_set = OpenContainer.getInstance(add_set, directory, format, 0L)).addAction(new AddEntry(disk, found_entry));
								break;
							}
						}
						if (found_entry == null)
							report_subject.add(new EntryMissing(disk));
					}
					else
					{
						disk.setStatus(EntityStatus.OK);
						report_subject.add(new EntryOK(disk));
						// report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
						disks_found.add(found_entry);
					}
				}
				if (!ignore_unneeded_entries)
				{
					final List<Entry> unneeded = container.getEntries().stream().filter(Scan.not(new HashSet<>(disks_found)::contains)).collect(Collectors.toList());
					for (final Entry unneeded_entry : unneeded)
					{
						report_subject.add(new EntryUnneeded(unneeded_entry));
						(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format, 0L)).addAction(new RenameEntry(unneeded_entry));
						(delete_set = OpenContainer.getInstance(delete_set, directory, format, 0L)).addAction(new DeleteEntry(unneeded_entry));
					}
				}
				ContainerAction.addToList(rename_before_actions, rename_before_set);
				ContainerAction.addToList(duplicate_actions, duplicate_set);
				ContainerAction.addToList(add_actions, add_set);
				ContainerAction.addToList(delete_actions, delete_set);
				ContainerAction.addToList(rename_after_actions, rename_after_set);
			}
		}
		else
		{
			for (final Disk disk : disks)
				disk.setStatus(EntityStatus.KO);
			if (create_mode)
			{
				if (disks.size() > 0)
				{
					int disks_found = 0;
					boolean partial_set = false;
					CreateContainer createset = null;
					for (final Disk disk : disks)
					{
						Scan.report.stats.missing_disks_cnt++;
						Entry found_entry = null;
						for (final DirScan scan : allscans)
						{
							if (null != (found_entry = scan.find_byhash(disk)))
							{
								report_subject.add(new EntryAdd(disk, found_entry));
								(createset = CreateContainer.getInstance(createset, directory, format, 0L)).addAction(new AddEntry(disk, found_entry));
								disks_found++;
								break;
							}
						}
						if (found_entry == null)
						{
							report_subject.add(new EntryMissing(disk));
							partial_set = true;
						}
					}
					if (disks_found > 0)
					{
						if (!createfull_mode || !partial_set)
						{
							report_subject.setCreateFull();
							if (partial_set)
								report_subject.setCreate();
							ContainerAction.addToList(create_actions, createset);
						}
					}
				}
			}
		}
		return missing_set;
	}

	/**
	 * Scan roms
	 * @param ware the current {@link Anyware} we are processing
	 * @param roms the filtered {@link Rom} {@link List}
	 * @param archive the {@link Container} in which the roms will reside
	 * @param report_subject the {@link SubjectSet} report related to this {@link Anyware}
	 * @return true if set is currently missing
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean scanRoms(final Anyware ware, final List<Rom> roms, final Container archive, final SubjectSet report_subject)
	{
		boolean missing_set = true;
		final boolean debug = false;
		final Container container;
		if (null != (container = roms_dstscan.getContainerByName(ware.getDest(merge_mode, implicit_merge).getNormalizedName() + format.getExt())))
		{	// found container
			missing_set = false;
			if (roms.size() > 0)
			{
				report_subject.setFound();

				final ArrayList<Entry> roms_found = new ArrayList<>();
				final Map<String, Rom> roms_byname = Rom.getRomsByName(roms);
				BackupContainer backup_set = null;
				OpenContainer add_set = null, delete_set = null, rename_before_set = null;
				final OpenContainer rename_after_set = null;
				OpenContainer duplicate_set = null;
				for (final Rom rom : roms)	// check roms
				{
					rom.setStatus(EntityStatus.KO);
					Entry found_entry = null;
					final Map<String, Entry> entries_byname = container.getEntriesByName();
					Entry wrong_hash = null;
					for (final Entry candidate_entry : container.getEntries())	// compare each rom with container entries
					{
						final String efile = candidate_entry.getName();
						if (candidate_entry.equals(rom)) // The entry 'candidate_entry' match hash from 'rom'
						{
							if(debug) System.out.println("The entry "+efile+" match hash from rom "+rom.getNormalizedName());
							if (!rom.getNormalizedName().equals(efile)) // but this entry name does not match the rom name
							{
								if(debug) System.out.println("\tbut this entry name does not match the rom name");
								final Rom another_rom;
								if (null != (another_rom = roms_byname.get(efile)) && candidate_entry.equals(another_rom))
								{
									if(debug) System.out.println("\t\t\tand the entry "+efile+" is ANOTHER the rom");
									if (entries_byname.containsKey(rom.getNormalizedName())) // and rom name is in the entries
									{
										if(debug) System.out.println("\t\t\t\tand rom "+rom.getNormalizedName()+" is in the entries_byname");
										// report_w.println("[" + m.name + "] " + r.getName() + " == " + e.file);
									}
									else
									{
										if(debug) System.out.println("\\t\\t\\t\\twe must duplicate rom "+rom.getNormalizedName()+" to ");
										// we must duplicate
										report_subject.add(new EntryMissingDuplicate(rom, candidate_entry));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										found_entry = candidate_entry;
										break;
									}
								}
								else
								{
									if(another_rom==null)
									{
										if(debug) System.out.println("\t"+efile+" in roms_byname not found");
										roms_byname.forEach((k,v)->System.out.println("\troms_byname: "+k));
									}
									else
										if(debug) System.out.println("\t"+efile+" in roms_byname found but does not match hash");
									
									if (!entries_byname.containsKey(rom.getNormalizedName())) // and rom name is not in the entries
									{
										if(debug) System.out.println("\t\tand rom "+rom.getNormalizedName()+" is NOT in the entries_byname");
										entries_byname.forEach((k,v)->System.out.println("\t\tentries_byname: "+k));
										
										report_subject.add(new EntryWrongName(rom, candidate_entry));
										// (rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format)).addAction(new RenameEntry(e));
										// (rename_after_set = OpenContainer.getInstance(rename_after_set, archive, format)).addAction(new RenameEntry(r.getName(), e));
										(backup_set = BackupContainer.getInstance(backup_set, archive)).addAction(new BackupEntry(candidate_entry));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
									//	(delete_set = OpenContainer.getInstance(delete_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DeleteEntry(candidate_entry));
										found_entry = candidate_entry;
										break;
									}
									else
										if(debug) System.out.println("\t\tand rom "+rom.getNormalizedName()+" is in the entries_byname");
								}
							}
							else
							{
								if(debug) System.out.println("\tThe entry "+efile+" match hash and name for rom "+rom.getNormalizedName());
								found_entry = candidate_entry;
								break;
							}
						}
						else if (rom.getNormalizedName().equals(efile))	// oups! we got a wrong rom hash
						{
							if(debug) System.out.println("\tOups! we got wrong hash in "+efile+" for "+rom.getNormalizedName());
							//report_subject.add(new EntryWrongHash(rom, candidate_entry));
							wrong_hash = candidate_entry;
							break;
						}
						else
						{
//							System.out.println("\tnot found");
						}
					}
					if (found_entry == null)	// did not find rom in container
					{
						Scan.report.stats.missing_roms_cnt++;
						for (final DirScan scan : allscans)	// now search for rom in all available dir scans
						{
							if (null != (found_entry = scan.find_byhash(rom)))
							{
								report_subject.add(new EntryAdd(rom, found_entry));
								(add_set = OpenContainer.getInstance(add_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new AddEntry(rom, found_entry));
								// roms_found.add(found);
								break;
							}
						}
						if (found_entry == null)	// we did not found this rom anywhere
							report_subject.add(wrong_hash!=null?new EntryWrongHash(rom, wrong_hash):new EntryMissing(rom));
					}
					else
					{
						// report_w.println("[" + m.name + "] " + r.getName() + " (" + found.file + ") OK ");
						rom.setStatus(EntityStatus.OK);
						report_subject.add(new EntryOK(rom));
						roms_found.add(found_entry);
					}
				}
				if (!ignore_unneeded_entries)
				{	// remove unneeded entries
					final List<Entry> unneeded = container.getEntries().stream().filter(Scan.not(new HashSet<>(roms_found)::contains)).collect(Collectors.toList());
					for (final Entry unneeded_entry : unneeded)
					{
						report_subject.add(new EntryUnneeded(unneeded_entry));
						(backup_set = BackupContainer.getInstance(backup_set, archive)).addAction(new BackupEntry(unneeded_entry));
						(rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new RenameEntry(unneeded_entry));
						(delete_set = OpenContainer.getInstance(delete_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DeleteEntry(unneeded_entry));
					}
				}
				ContainerAction.addToList(backup_actions, backup_set);
				ContainerAction.addToList(rename_before_actions, rename_before_set);
				ContainerAction.addToList(duplicate_actions, duplicate_set);
				ContainerAction.addToList(add_actions, add_set);
				ContainerAction.addToList(delete_actions, delete_set);
				ContainerAction.addToList(rename_after_actions, rename_after_set);
			}
		}
		else	// container is missing
		{
			for (final Rom rom : roms)
				rom.setStatus(EntityStatus.KO);
			if (create_mode)
			{
				if (roms.size() > 0)
				{
					int roms_found = 0;
					boolean partial_set = false;
					CreateContainer createset = null;
					for (final Rom rom : roms)
					{
						Scan.report.stats.missing_roms_cnt++;
						Entry entry_found = null;
						for (final DirScan scan : allscans)	// search rom in all scans
						{
							if (null != (entry_found = scan.find_byhash(rom)))
							{
								report_subject.add(new EntryAdd(rom, entry_found));
								(createset = CreateContainer.getInstance(createset, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new AddEntry(rom, entry_found));
								roms_found++;
								break;
							}
						}
						if (entry_found == null)	// We did not find all roms to create a full set
						{
							report_subject.add(new EntryMissing(rom));
							partial_set = true;
						}
					}
					if (roms_found > 0)
					{
						if (!createfull_mode || !partial_set)
						{
							report_subject.setCreateFull();
							if (partial_set)
								report_subject.setCreate();
							ContainerAction.addToList(create_actions, createset);
						}
					}
				}
			}
		}
		return missing_set;
	}

	/**
	 * Scan samples
	 * @param set the {@link Samples} set to scan
	 */
	private void scanSamples(final Samples set)
	{
		boolean missing_set = true;
		final Container archive;
		if (format.getExt().isDir())
			archive = new Directory(new File(samples_dstscan.getDir(), set.getName()), set);
		else
			archive = new Archive(new File(samples_dstscan.getDir(), set.getName() + format.getExt()), set);
		final SubjectSet report_subject = new SubjectSet(set);
		if(!scanSamples(set, archive, report_subject))
			missing_set = false;
		if (create_mode && report_subject.getStatus() == Status.UNKNOWN)
			report_subject.setMissing();
		if (missing_set)
			Scan.report.stats.missing_set_cnt++;
		if (report_subject.getStatus() != Status.UNKNOWN)
			Scan.report.add(report_subject);
		prepTZip(report_subject, archive, set);
	}

	/**
	 * Scan samples
	 * @param set the Samples set to scan
	 * @param archive the {@link Container} in which the samples will reside
	 * @param report_subject the {@link SubjectSet} report related to this {@link Anyware}
	 * @return true if set is currently missing
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean scanSamples(final Samples set, final Container archive, final SubjectSet report_subject)
	{
		boolean missing_set = true;
		final Container container;
		if (null != (container = samples_dstscan.getContainerByName(archive.file.getName())))
		{
			missing_set = false;
			report_subject.setFound();
			final ArrayList<Entry> samples_found = new ArrayList<>();
			final OpenContainer add_set = null;
			OpenContainer delete_set = null, rename_before_set = null;
			final OpenContainer rename_after_set = null, duplicate_set = null;
			for (final Sample sample : set)
			{
				sample.setStatus(EntityStatus.KO);
				Entry found_entry = null;
				for (final Entry candidate_entry : container.getEntries())
				{
					if (candidate_entry.equals(sample))
					{
						found_entry = candidate_entry;
						break;
					}
				}
				if (found_entry == null)
				{
					Scan.report.stats.missing_samples_cnt++;
					for (final DirScan scan : allscans)
					{
						for (final FormatOptions.Ext ext : EnumSet.allOf(FormatOptions.Ext.class))
						{
							final Container found_container;
							if (null != (found_container = scan.getContainerByName(set.getName() + ext)))
							{
								for (final Entry entry : found_container.entries_byname.values())
								{
									if (entry.getName().equals(sample.getNormalizedName()))
										found_entry = entry;
									if (null != found_entry)
										break;
								}
							}
							if (null != found_entry)
								break;
						}
					}
					if (found_entry == null)
						report_subject.add(new EntryMissing(sample));
				}
				else
				{
					sample.setStatus(EntityStatus.OK);
					report_subject.add(new EntryOK(sample));
					// report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
					samples_found.add(found_entry);
				}
			}
			if (!ignore_unneeded_entries)
			{
				final List<Entry> unneeded = container.getEntries().stream().filter(Scan.not(new HashSet<>(samples_found)::contains)).collect(Collectors.toList());
				for (final Entry unneeded_entry : unneeded)
				{
					report_subject.add(new EntryUnneeded(unneeded_entry));
					(rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format, Long.MAX_VALUE)).addAction(new RenameEntry(unneeded_entry));
					(delete_set = OpenContainer.getInstance(delete_set, archive, format, Long.MAX_VALUE)).addAction(new DeleteEntry(unneeded_entry));
				}
			}
			ContainerAction.addToList(rename_before_actions, rename_before_set);
			ContainerAction.addToList(duplicate_actions, duplicate_set);
			ContainerAction.addToList(add_actions, add_set);
			ContainerAction.addToList(delete_actions, delete_set);
			ContainerAction.addToList(rename_after_actions, rename_after_set);
		}
		else
		{
			for (final Sample sample : set)
				sample.setStatus(EntityStatus.KO);
			if (create_mode)
			{
				int samples_found = 0;
				boolean partial_set = false;
				CreateContainer createset = null;
				for (final Sample sample : set)
				{
					Scan.report.stats.missing_samples_cnt++;
					Entry entry_found = null;
					for (final DirScan scan : allscans)
					{
						for (final FormatOptions.Ext ext : EnumSet.allOf(FormatOptions.Ext.class))
						{
							final Container found_container;
							if (null != (found_container = scan.getContainerByName(set.getName() + ext)))
							{
								for (final Entry entry : found_container.entries_byname.values())
								{
									if (entry.getName().equals(sample.getNormalizedName()))
										entry_found = entry;
									if (null != entry_found)
										break;
								}
							}
							if (null != entry_found)
								break;
						}
						if (null != entry_found)
						{
							report_subject.add(new EntryAdd(sample, entry_found));
							(createset = CreateContainer.getInstance(createset, archive, format, Long.MAX_VALUE)).addAction(new AddEntry(sample, entry_found));
							samples_found++;
							break;
						}
					}
					if (entry_found == null)
					{
						report_subject.add(new EntryMissing(sample));
						partial_set = true;
					}
				}
				if (samples_found > 0)
				{
					if (!createfull_mode || !partial_set)
					{
						report_subject.setCreateFull();
						if (partial_set)
							report_subject.setCreate();
						ContainerAction.addToList(create_actions, createset);
					}
				}
			}
		}
		return missing_set;
	}

	/**
	 * Scan a Machine or a Software
	 * @param ware the {@link Anyware} to scan
	 */
	private void scanWare(final Anyware ware)
	{
		final SubjectSet report_subject = new SubjectSet(ware);

		boolean missing_set = true;
		final Directory directory = new Directory(new File(disks_dstscan.getDir(), ware.getDest(merge_mode, implicit_merge).getName()), ware);
		final Container archive;
		if (format.getExt().isDir())
			archive = new Directory(new File(roms_dstscan.getDir(), ware.getDest(merge_mode, implicit_merge).getName()), ware);
		else
			archive = new Archive(new File(roms_dstscan.getDir(), ware.getDest(merge_mode, implicit_merge).getName() + format.getExt()), ware);
		final List<Rom> roms = ware.filterRoms(merge_mode, hash_collision_mode);
		final List<Disk> disks = ware.filterDisks(merge_mode, hash_collision_mode);
		if (!scanRoms(ware, roms, archive, report_subject))
			missing_set = false;
		prepTZip(report_subject, archive, ware, roms);
		if (!scanDisks(ware, disks, directory, report_subject))
			missing_set = false;
		if (roms.size() == 0 && disks.size() == 0)
		{
			if (!(merge_mode.isMerge() && ware.isClone()))
			{
				if (!missing_set)
					report_subject.setUnneeded();
				else
					report_subject.setFound();
			}
			missing_set = false;
		}
		else if (create_mode && report_subject.getStatus() == Status.UNKNOWN)
			report_subject.setMissing();
		if (!ignore_unneeded_containers)
		{
			removeUnneededClone(ware, disks, roms);
			removeOtherFormats(ware);
		}
		if (missing_set)
			Scan.report.stats.missing_set_cnt++;
		if (report_subject.getStatus() != Status.UNKNOWN)
			Scan.report.add(report_subject);
	}

}
