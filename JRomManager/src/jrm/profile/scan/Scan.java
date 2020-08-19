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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;

import JTrrntzip.TrrntZipStatus;
import jrm.aui.progress.ProgressHandler;
import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.MultiThreading;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.Archive;
import jrm.profile.data.ByName;
import jrm.profile.data.Container;
import jrm.profile.data.Directory;
import jrm.profile.data.Disk;
import jrm.profile.data.EntityStatus;
import jrm.profile.data.Entry;
import jrm.profile.data.FakeDirectory;
import jrm.profile.data.Machine;
import jrm.profile.data.Rom;
import jrm.profile.data.Sample;
import jrm.profile.data.Samples;
import jrm.profile.data.Software;
import jrm.profile.data.SoftwareList;
import jrm.profile.data.Container.Type;
import jrm.profile.fix.actions.AddEntry;
import jrm.profile.fix.actions.BackupContainer;
import jrm.profile.fix.actions.BackupEntry;
import jrm.profile.fix.actions.ContainerAction;
import jrm.profile.fix.actions.CreateContainer;
import jrm.profile.fix.actions.DeleteContainer;
import jrm.profile.fix.actions.DeleteEntry;
import jrm.profile.fix.actions.DuplicateEntry;
import jrm.profile.fix.actions.OpenContainer;
import jrm.profile.fix.actions.RenameEntry;
import jrm.profile.fix.actions.TZipContainer;
import jrm.profile.report.ContainerTZip;
import jrm.profile.report.ContainerUnknown;
import jrm.profile.report.ContainerUnneeded;
import jrm.profile.report.EntryAdd;
import jrm.profile.report.EntryMissing;
import jrm.profile.report.EntryMissingDuplicate;
import jrm.profile.report.EntryOK;
import jrm.profile.report.EntryUnneeded;
import jrm.profile.report.EntryWrongHash;
import jrm.profile.report.EntryWrongName;
import jrm.profile.report.Report;
import jrm.profile.report.RomSuspiciousCRC;
import jrm.profile.report.SubjectSet;
import jrm.profile.report.SubjectSet.Status;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.security.PathAbstractor;
import lombok.val;

/**
 * The scan class
 * @author optyfr
 */
public class Scan extends PathAbstractor
{
	/**
	 * the attached {@link Report}
	 */
	public final Report report;
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
	private final FormatOptions format;
	private final boolean create_mode;
	private final boolean createfull_mode;
	private final boolean ignore_unneeded_containers;
	private final boolean ignore_unneeded_entries;
	private final boolean ignore_unknown_containers;
	private final boolean backup;
	
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
		super(profile.session);
		this.profile = profile;
		this.report = profile.session.report;
		profile.setPropsCheckPoint();
		report.reset();
		report.setProfile(profile);
		
		/*
		 * Store locally various profile settings
		 */
		format = FormatOptions.valueOf(profile.getProperty(SettingsEnum.format, FormatOptions.ZIP.toString())); //$NON-NLS-1$
		merge_mode = MergeOptions.valueOf(profile.getProperty(SettingsEnum.merge_mode, MergeOptions.SPLIT.toString())); //$NON-NLS-1$
		create_mode = profile.getProperty(SettingsEnum.create_mode, true); //$NON-NLS-1$
		createfull_mode = profile.getProperty(SettingsEnum.createfull_mode, false); //$NON-NLS-1$
		ignore_unneeded_containers = profile.getProperty(SettingsEnum.ignore_unneeded_containers, false); //$NON-NLS-1$
		ignore_unneeded_entries = profile.getProperty(SettingsEnum.ignore_unneeded_entries, false); //$NON-NLS-1$
		ignore_unknown_containers = profile.getProperty(SettingsEnum.ignore_unknown_containers, false); //$NON-NLS-1$
		backup = profile.getProperty(SettingsEnum.backup, true); //$NON-NLS-1$
		val use_parallelism = profile.getProperty(SettingsEnum.use_parallelism, profile.session.server);
		val nThreads = use_parallelism ? profile.session.getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;

		final String dstdir_txt = profile.getProperty(SettingsEnum.roms_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (dstdir_txt.isEmpty())
		{
			System.err.println("dst dir is empty"); //$NON-NLS-1$
			return; //TODO be more informative on failure
		}
		final File roms_dstdir = getAbsolutePath(dstdir_txt).toFile();
		if (!roms_dstdir.isDirectory())
		{
			System.err.println("dst dir is not a directory"); //$NON-NLS-1$
			return; //TODO be more informative on failure
		}
		
		/*
		 * use disks dest dir if enabled otherwise it will be the same than roms dest dir
		 */
		final File disks_dstdir;
		if (profile.getProperty(SettingsEnum.disks_dest_dir_enabled, false)) //$NON-NLS-1$
		{
			final String disks_dstdir_txt = profile.getProperty(SettingsEnum.disks_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (disks_dstdir_txt.isEmpty())
				return; //TODO be more informative on failure
			disks_dstdir = getAbsolutePath(disks_dstdir_txt).toFile();
		}
		else
			disks_dstdir = new File(roms_dstdir.getAbsolutePath());
		
		/*
		 * use sw roms dest dir if enabled otherwise it will be the same than roms dest dir
		 */
		final File swroms_dstdir;
		if (profile.getProperty(SettingsEnum.swroms_dest_dir_enabled, false)) //$NON-NLS-1$
		{
			final String swroms_dstdir_txt = profile.getProperty(SettingsEnum.swroms_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (swroms_dstdir_txt.isEmpty())
				return; //TODO be more informative on failure
			swroms_dstdir = getAbsolutePath(swroms_dstdir_txt).toFile();
		}
		else
			swroms_dstdir = new File(roms_dstdir.getAbsolutePath());
		
		/*
		 * use sw disks dest dir if enabled otherwise it will be the same than disks dest dir (which in turn can be the same than roms dest dir)
		 */
		final File swdisks_dstdir;
		if (profile.getProperty(SettingsEnum.swdisks_dest_dir_enabled, false)) //$NON-NLS-1$
		{
			final String swdisks_dstdir_txt = profile.getProperty(SettingsEnum.swdisks_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (swdisks_dstdir_txt.isEmpty())
				return; //TODO be more informative on failure
			swdisks_dstdir = getAbsolutePath(swdisks_dstdir_txt).toFile();
		}
		else
			swdisks_dstdir = new File(swroms_dstdir.getAbsolutePath());

		/*
		 * use samples dest dir if enabled and valid, otherwise it's null and not used
		 */
		final String samples_dstdir_txt = profile.getProperty(SettingsEnum.samples_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
		final File samples_dstdir = profile.getProperty(SettingsEnum.samples_dest_dir_enabled, false) && samples_dstdir_txt.length() > 0 ? getAbsolutePath(samples_dstdir_txt).toFile() : null; //$NON-NLS-1$

		/*
		 * explode all src dir string into an ArrayList<File>
		 */
		final ArrayList<File> srcdirs = new ArrayList<>();
		for (final String s : StringUtils.split(profile.getProperty(SettingsEnum.src_dir, ""),'|')) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			if (!s.isEmpty())
			{
				final File f = getAbsolutePath(s).toFile();
				if (f.isDirectory())
					srcdirs.add(f);
			}
		}
		/* then add extra backup dir to that list */
		srcdirs.add(new File(profile.session.getUser().getSettings().getWorkPath().toFile(), "backup")); //$NON-NLS-1$
		/* then scan all dirs from that list */
		for (final File dir : srcdirs)
		{
			if(scancache != null)
			{
				String cachefile  = DirScan.getCacheFile(profile.session, dir, DirScan.getOptions(profile, false)).getAbsolutePath();
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
			if (swroms_dstdir.isDirectory() && !swroms_dstdir.equals(roms_dstdir))
			{
				File[] files  = swroms_dstdir.listFiles();
				if(files!=null) for (final File f : files)
				{
					if (!swroms_dstscans.containsKey(f.getName()))
						unknown.add(f.isDirectory() ? new Directory(f, getRelativePath(f), (Machine) null) : new Archive(f, getRelativePath(f), (Machine) null));
					if (handler.isCancel())
						throw new BreakException();
				}
			}
			if (!swroms_dstdir.equals(swdisks_dstdir))
			{
				if (swdisks_dstdir.isDirectory())
				{
					File[] files  = swdisks_dstdir.listFiles();
					if(files!=null) for (final File f : files)
					{
						if (!swdisks_dstscans.containsKey(f.getName()))
							unknown.add(f.isDirectory() ? new Directory(f, getRelativePath(f), (Machine) null) : new Archive(f, getRelativePath(f), (Machine) null));
						if (handler.isCancel())
							throw new BreakException();
					}
				}
			}
		}

		/* reset progress style */
		handler.setInfos(nThreads,null);

		
		try
		{
			/*
			 * process and report unknown actions if requested
			 */
			if (!ignore_unknown_containers)
			{
				unknown.stream().filter(c -> {
					if(samples_dstdir!=null && c.getRelFile().equals(samples_dstdir))
						return false;
					if(disks_dstdir!=roms_dstdir && c.getRelFile().equals(disks_dstdir))
						return false;
					if(swroms_dstdir!=roms_dstdir && c.getRelFile().equals(swroms_dstdir))
						return false;
					if(swdisks_dstdir!=swroms_dstdir && c.getRelFile().equals(swdisks_dstdir))
						return false;
					return true;
				}).forEach((c) -> {
					report.add(new ContainerUnknown(c));
					delete_actions.add(new DeleteContainer(c, format));
				});
			}
			/*
			 * process and report unneeded actions if requested
			 */
			if(!ignore_unneeded_containers)
			{
				unneeded.forEach(c->{
					report.add(new ContainerUnneeded(c));
					backup_actions.add(new BackupContainer(c));
					delete_actions.add(new DeleteContainer(c, format));
				});
			}
			/*
			 * report suspicious CRCs
			 */
			profile.suspicious_crc.forEach((crc) -> report.add(new RomSuspiciousCRC(crc)));

			/*
			 * Searching for fixes
			 */
			final AtomicInteger i = new AtomicInteger();
			final AtomicInteger j = new AtomicInteger();
			handler.setProgress(null, i.get(), profile.filteredSubsize()); //$NON-NLS-1$
			handler.setProgress2(String.format("%s %d/%d", Messages.getString("Scan.SearchingForFixes"), j.get(), profile.size()), j.get(), profile.size()); //$NON-NLS-1$
			if (profile.machinelist_list.get(0).size() > 0)
			{
				
				/* Scan all samples */
				handler.setProgress2(String.format("%s %d/%d", Messages.getString("Scan.SearchingForFixes"), j.get(), profile.size()), j.getAndIncrement(), profile.size()); //$NON-NLS-1$
				new MultiThreading<Samples>(nThreads, set ->
				{
					if (handler.isCancel())
						return;
					handler.setProgress(set.getName(), i.getAndIncrement());
					if (samples_dstscan != null)
						scanSamples(set);
				}).start(StreamSupport.stream(profile.machinelist_list.get(0).samplesets.spliterator(),false));
				/* scan all machines */ 
				profile.machinelist_list.get(0).forEach(Machine::resetCollisionMode);
				new MultiThreading<Machine>(nThreads, m ->
				{
					if (handler.isCancel())
						return;
					handler.setProgress(m.getFullName(), i.getAndIncrement());
					scanWare(m);
				}).start(profile.machinelist_list.get(0).getFilteredStream());
			}
			if (profile.machinelist_list.softwarelist_list.size() > 0)
			{
				/* scan all software lists */
				profile.machinelist_list.softwarelist_list.getFilteredStream().takeWhile(sl -> !handler.isCancel()).forEach(sl -> {
					// for each software list
					handler.setProgress2(String.format("%s %d/%d (%s)", Messages.getString("Scan.SearchingForFixes"), j.get(), profile.size(), sl.getName()), j.getAndIncrement(), profile.size()); //$NON-NLS-1$
					roms_dstscan = swroms_dstscans.get(sl.getName());
					disks_dstscan = swdisks_dstscans.get(sl.getName());
					sl.forEach(Software::resetCollisionMode);
					new MultiThreading<Software>(nThreads, s ->
					{
						if (handler.isCancel())
							return;
						handler.setProgress(s.getFullName(), i.getAndIncrement());
						scanWare(s);
					}).start(sl.getFilteredStream());
				});
			}
			handler.setProgress(null, i.get());
			handler.setProgress2(null, j.get());
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
			handler.setInfos(1,null);
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			/* save report */
			report.write(profile.session);
			report.flush();
			/* update and save stats */
			profile.nfo.stats.scanned = new Date();
			profile.nfo.stats.haveSets = Stream.concat(profile.machinelist_list.stream(), profile.machinelist_list.softwarelist_list.stream()).mapToLong(AnywareList::countHave).sum();
			profile.nfo.stats.haveRoms = Stream.concat(profile.machinelist_list.stream(), profile.machinelist_list.softwarelist_list.stream()).flatMap(AnywareList::stream).mapToLong(Anyware::countHaveRoms).sum();
			profile.nfo.stats.haveDisks = Stream.concat(profile.machinelist_list.stream(), profile.machinelist_list.softwarelist_list.stream()).flatMap(AnywareList::stream).mapToLong(Anyware::countHaveDisks).sum();
			profile.nfo.save(profile.session);
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
			if (c.getType() == Type.UNK)
				unknown.add(c);
/*			else if(c.getType() == Type.FAKE && format == FormatOptions.DIR)
				unknown.add(c);*/
			else if(c.getType() == Type.DIR && format == FormatOptions.FAKE)
				unknown.add(c);
			else if (!byname.containsFilteredName(getBaseName(c.getFile())))
			{
				if(byname.containsName(getBaseName(c.getFile())))
					unneeded.add(c);
				else
					unknown.add(c);
			}
		}
		return dstscan;
	}

	private static String getBaseName(File file)
	{
		String name = file.getName();
		final int last = name.lastIndexOf('.');
		if(last > 0)
			if(name.substring(last).indexOf(' ')==-1)
				name = name.substring(0, last);
		return name;
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
					final Container container = roms_dstscan.getContainerByName(ware.getDest().getName() + format.getExt());
					if (container != null)
					{
						if (container.lastTZipCheck < container.modified)
							tzipcontainer = container;
						else if (!container.lastTZipStatus.contains(TrrntZipStatus.ValidTrrntzip))
							tzipcontainer = container;
						else if (report_subject.hasFix())
							tzipcontainer = container;

					}
					else if (create_mode)
					{
						if(createfull_mode)
						{
							if(report_subject.isFixable())
								tzipcontainer = archive;
						}
						else
						{
							if(report_subject.hasFix())
								tzipcontainer = archive;
						}
					}
					if (tzipcontainer != null)
					{
						final long estimated_roms_size = roms.stream().mapToLong(Rom::getSize).sum();
						tzipcontainer.m = ware;
						tzip_actions.put(tzipcontainer.getFile().getAbsolutePath(), new TZipContainer(tzipcontainer, format, estimated_roms_size));
						report.add(new ContainerTZip(tzipcontainer));
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
				final Container container = samples_dstscan.getContainerByName(archive.getFile().getName());
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
					tzip_actions.put(tzipcontainer.getFile().getAbsolutePath(), new TZipContainer(tzipcontainer, format, Long.MAX_VALUE));
					report.add(new ContainerTZip(tzipcontainer));
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
				report.add(new ContainerUnneeded(c));
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
						report.add(new ContainerUnneeded(c));
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
					report.add(new ContainerUnneeded(c));
					backup_actions.add(new BackupContainer(c));
					delete_actions.add(new DeleteContainer(c, format));
				}
			}
			if (format != FormatOptions.DIR && roms.size() == 0)
			{
				final Container c = roms_dstscan.getContainerByName(ware.getName() + format.getExt());
				if (c != null)
				{
					report.add(new ContainerUnneeded(c));
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
		if (null != (container = disks_dstscan.getContainerByName(ware.getDest().getNormalizedName())))
		{
			missing_set = false;
			if (disks.size() > 0)
			{
				report_subject.setFound();

				final ArrayList<Entry> disks_found = new ArrayList<>();
				final Map<String, Disk> disks_byname = Disk.getDisksByName(disks);
				OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null, duplicate_set = null;
				
				final Set<Entry> marked_for_rename = new HashSet<>();
				
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
										if(!marked_for_rename.contains(candidate_entry))
										{
											report_subject.add(new EntryWrongName(disk, candidate_entry));
											(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format, 0L)).addAction(new RenameEntry(candidate_entry));
											(rename_after_set = OpenContainer.getInstance(rename_after_set, directory, format, 0L)).addAction(new RenameEntry(disk.getName(), candidate_entry));
											marked_for_rename.add(candidate_entry);
										}
										else
										{
											report_subject.add(new EntryAdd(disk, candidate_entry));
											(duplicate_set = OpenContainer.getInstance(duplicate_set, directory, format, 0L)).addAction(new DuplicateEntry(disk.getName(), candidate_entry));
										}
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
						report.stats.missing_disks_cnt++;
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
						report.stats.missing_disks_cnt++;
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
		final Container container;
		final long estimated_roms_size = roms.stream().mapToLong(Rom::getSize).sum();
		
		if (null != (container = roms_dstscan.getContainerByName(ware.getDest().getNormalizedName() + format.getExt())))
		{	// found container
			missing_set = false;
			if (roms.size() > 0)
			{
				report_subject.setFound();

				final ArrayList<Entry> roms_found = new ArrayList<>();
				final Map<String, Rom> roms_byname = Rom.getRomsByName(roms);
				
				BackupContainer backup_set = null;
				OpenContainer add_set = null, delete_set = null, rename_before_set = null;
				OpenContainer rename_after_set = null;
				OpenContainer duplicate_set = null;

				final Map<String, Entry> entries_byname = container.getEntriesByName();
				final Map<String, List<Entry>> entries_bysha1 = new HashMap<>();
				container.getEntries().forEach(e -> {
					if (e.sha1 != null)
						entries_bysha1.computeIfAbsent(e.sha1, k -> new ArrayList<>()).add(e);
				});
				final Map<String, List<Entry>> entries_bymd5 = new HashMap<>();
				container.getEntries().forEach(e -> {
					if (e.md5 != null)
						entries_bymd5.computeIfAbsent(e.md5, k -> new ArrayList<>()).add(e);
				});
				final Map<String, List<Entry>> entries_bycrc = new HashMap<>();
				container.getEntries().forEach(e -> {
					if (e.crc != null)
						entries_bycrc.computeIfAbsent(e.crc + '.' + e.size, k -> new ArrayList<>()).add(e);
				});

				final Set<Entry> marked_for_rename = new HashSet<>();
				
				for (final Rom rom : roms)	// check roms
				{
					rom.setStatus(EntityStatus.KO);
					Entry found_entry = null;
					Entry wrong_hash = null;
					
					List<Entry> entries = null;
					if(rom.sha1!=null)
						entries = entries_bysha1.get(rom.sha1);
					if(entries == null && rom.md5!=null)
						entries = entries_bymd5.get(rom.md5);
					if(entries == null && rom.crc!=null)
						entries = entries_bycrc.get(rom.crc+'.'+rom.size);
					if(entries != null) 
					{
						for (final Entry candidate_entry : entries)
						{
							final String efile = candidate_entry.getName();
							Log.debug(()->"The entry " + efile + " match hash from rom " + rom.getNormalizedName());
							if (!rom.getNormalizedName().equals(efile)) // but this entry name does not match the rom name
							{
								Log.debug(()->"\tbut this entry name does not match the rom name");
								final Rom another_rom;
								if (null != (another_rom = roms_byname.get(efile)) && candidate_entry.equals(another_rom))
								{
									Log.debug(()->"\t\t\tand the entry " + efile + " is ANOTHER rom");
									if (entries_byname.containsKey(rom.getNormalizedName())) // and rom name is in the entries
									{
										Log.debug(()->"\t\t\t\tand rom " + rom.getNormalizedName() + " is in the entries_byname");
										// report_w.println("[" + m.name + "] " + r.getName() + " == " + e.file);
									}
									else
									{
										Log.debug(()->"\\t\\t\\t\\twe must duplicate rom " + rom.getNormalizedName() + " to ");
										// we must duplicate
										report_subject.add(new EntryMissingDuplicate(rom, candidate_entry));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format, estimated_roms_size)).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										found_entry = candidate_entry;
										break;
									}
								}
								else
								{
									if (another_rom == null)
									{
										Log.debug(()->
										{
											final StringBuffer str = new StringBuffer("\t" + efile + " in roms_byname not found");
											roms_byname.forEach((k, v) -> str.append("\troms_byname: " + k));
											return str.toString();
										});
									}
									else Log.debug(()->"\t" + efile + " in roms_byname found but does not match hash");

									if (!entries_byname.containsKey(rom.getNormalizedName())) // and rom name is not in the entries
									{
										Log.debug(()->
										{
											final StringBuffer str = new StringBuffer("\t\tand rom " + rom.getNormalizedName() + " is NOT in the entries_byname");
											entries_byname.forEach((k, v) -> str.append("\t\tentries_byname: " + k));
											return str.toString();
										});

										if(!marked_for_rename.contains(candidate_entry))
										{
											report_subject.add(new EntryWrongName(rom, candidate_entry));
											(rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format, estimated_roms_size)).addAction(new RenameEntry(candidate_entry));
											(rename_after_set = OpenContainer.getInstance(rename_after_set, archive, format, estimated_roms_size)).addAction(new RenameEntry(rom.getName(), candidate_entry));
											marked_for_rename.add(candidate_entry);
										}
										else
										{
											report_subject.add(new EntryAdd(rom, candidate_entry));
											(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format, estimated_roms_size)).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										}
										//(backup_set = BackupContainer.getInstance(backup_set, archive)).addAction(new BackupEntry(candidate_entry));
										//(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										// (delete_set = OpenContainer.getInstance(delete_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DeleteEntry(candidate_entry));
										found_entry = candidate_entry;
										break;
									}
									else Log.debug(()->"\t\tand rom " + rom.getNormalizedName() + " is in the entries_byname");
								}
							}
							else
							{
								Log.debug(()->"\tThe entry " + efile + " match hash and name for rom " + rom.getNormalizedName());
								found_entry = candidate_entry;
								break;
							}
						}
					}
					if(found_entry == null)
					{
						final Entry candidate_entry;
						if((candidate_entry = entries_byname.get(rom.getNormalizedName()))!=null)
						{
							final String efile = candidate_entry.getName();
							Log.debug(()->"\tOups! we got wrong hash in "+efile+" for "+rom.getNormalizedName());
							//report_subject.add(new EntryWrongHash(rom, candidate_entry));
							wrong_hash = candidate_entry;
						}
					}
					
					
/*					for (final Entry candidate_entry : container.getEntries())	// compare each rom with container entries
					{
						final String efile = candidate_entry.getName();
						if (candidate_entry.equals(rom)) // The entry 'candidate_entry' match hash from 'rom'
						{
							Log.debug(()->"The entry "+efile+" match hash from rom "+rom.getNormalizedName());
							if (!rom.getNormalizedName().equals(efile)) // but this entry name does not match the rom name
							{
								Log.debug(()->"\tbut this entry name does not match the rom name");
								final Rom another_rom;
								if (null != (another_rom = roms_byname.get(efile)) && candidate_entry.equals(another_rom))
								{
									Log.debug(()->"\t\t\tand the entry "+efile+" is ANOTHER the rom");
									if (entries_byname.containsKey(rom.getNormalizedName())) // and rom name is in the entries
									{
										Log.debug(()->"\t\t\t\tand rom "+rom.getNormalizedName()+" is in the entries_byname");
										// report_w.println("[" + m.name + "] " + r.getName() + " == " + e.file);
									}
									else
									{
										Log.debug(()->"\\t\\t\\t\\twe must duplicate rom "+rom.getNormalizedName()+" to ");
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
										Log.debug(()->{
											StringBuffer str = new StringBuffer("\t"+efile+" in roms_byname not found");
											roms_byname.forEach((k,v)->str.append("\troms_byname: "+k));
											return str.toString();
										}
									}
									else
										Log.debug(()->"\t"+efile+" in roms_byname found but does not match hash");
									
									if (!entries_byname.containsKey(rom.getNormalizedName())) // and rom name is not in the entries
									{
										Log.debug(()->{
											StringBuffer str = new StringBuffer("\t\tand rom "+rom.getNormalizedName()+" is NOT in the entries_byname");
											entries_byname.forEach((k,v)->str.append("\t\tentries_byname: "+k));
											return str;
										}
										
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
										Log.debug(()->"\t\tand rom "+rom.getNormalizedName()+" is in the entries_byname");
								}
							}
							else
							{
								Log.debug(()->"\tThe entry "+efile+" match hash and name for rom "+rom.getNormalizedName());
								found_entry = candidate_entry;
								break;
							}
						}
						else if (rom.getNormalizedName().equals(efile))	// oups! we got a wrong rom hash
						{
							Log.debug(()->"\tOups! we got wrong hash in "+efile+" for "+rom.getNormalizedName());
							//report_subject.add(new EntryWrongHash(rom, candidate_entry));
							wrong_hash = candidate_entry;
							break;
						}
						else
						{
//							Log.debug(()->"\tnot found");
						}
					}
*/
					if (found_entry == null)	// did not find rom in container
					{
						report.stats.missing_roms_cnt++;
						for (final DirScan scan : allscans)	// now search for rom in all available dir scans
						{
							if (null != (found_entry = scan.find_byhash(rom)))
							{
								report_subject.add(new EntryAdd(rom, found_entry));
								(add_set = OpenContainer.getInstance(add_set, archive, format, estimated_roms_size)).addAction(new AddEntry(rom, found_entry));
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
						(rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format, estimated_roms_size)).addAction(new RenameEntry(unneeded_entry));
						(delete_set = OpenContainer.getInstance(delete_set, archive, format, estimated_roms_size)).addAction(new DeleteEntry(unneeded_entry));
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
						report.stats.missing_roms_cnt++;
						Entry entry_found = null;
						for (final DirScan scan : allscans)	// search rom in all scans
						{
							if (null != (entry_found = scan.find_byhash(rom)))
							{
								report_subject.add(new EntryAdd(rom, entry_found));
								(createset = CreateContainer.getInstance(createset, archive, format, estimated_roms_size)).addAction(new AddEntry(rom, entry_found));
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
		File f;
		if (format.getExt().isDir())
			archive = new Directory(f=new File(samples_dstscan.getDir(), set.getName()), getRelativePath(f), set);
		else
			archive = new Archive(f=new File(samples_dstscan.getDir(), set.getName() + format.getExt()), getRelativePath(f), set);
		final SubjectSet report_subject = new SubjectSet(set);
		if(!scanSamples(set, archive, report_subject))
			missing_set = false;
		if (create_mode && report_subject.getStatus() == Status.UNKNOWN)
			report_subject.setMissing();
		if (missing_set)
			report.stats.missing_set_cnt++;
		if (report_subject.getStatus() != Status.UNKNOWN)
			report.add(report_subject);
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
		if (null != (container = samples_dstscan.getContainerByName(archive.getFile().getName())))
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
					report.stats.missing_samples_cnt++;
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
					report.stats.missing_samples_cnt++;
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
		File f;
		final Directory directory = new Directory(f=new File(disks_dstscan.getDir(), ware.getDest().getName()), getRelativePath(f), ware);
		final Container archive;
		if (format==FormatOptions.DIR)
			archive = new Directory(f=new File(roms_dstscan.getDir(), ware.getDest().getName()), getRelativePath(f), ware);
		else if (format==FormatOptions.FAKE)
			archive = new FakeDirectory(f=new File(roms_dstscan.getDir(), ware.getDest().getName()), getRelativePath(f), ware);
		else
			archive = new Archive(f=new File(roms_dstscan.getDir(), ware.getDest().getName() + format.getExt()), getRelativePath(f), ware);
		final List<Rom> roms = ware.filterRoms();
		final List<Disk> disks = ware.filterDisks();
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
			report.stats.missing_set_cnt++;
		if (report_subject.getStatus() != Status.UNKNOWN)
			report.add(report_subject);
	}

}
