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
import jrm.profile.data.Container.Type;
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
	private static final String MSG_SCAN_SEARCHING_FOR_FIXES = "Scan.SearchingForFixes";
	/**
	 * the attached {@link Report}
	 */
	public final Report report;
	/**
	 * All the actions to take for fixing the set after scan
	 */
	public final List<Collection<jrm.profile.fix.actions.ContainerAction>> actions = new ArrayList<>();

	/**
	 * The current profile
	 */
	private final Profile profile;
	
	/*
	 * All options variables
	 */
	private final MergeOptions mergeMode;
	private final FormatOptions format;
	private final boolean createMode;
	private final boolean createFullMode;
	private final boolean ignoreUnneededContainers;
	private final boolean ignoreUnneededEntries;
	private final boolean ignoreUnknownContainers;
	private final boolean backup;
	
	/*
	 * All Dir Scans variables
	 */
	/**
	 * Roms dst scan result
	 */
	private DirScan romsDstScan = null;
	/**
	 * Disks dst scan result
	 */
	private DirScan disksDstScan = null;
	/**
	 * Samples dst scan result
	 */
	private DirScan samplesDstScan = null;
	/**
	 * Software lists roms dst scans
	 */
	private final Map<String, DirScan> swromsDstScans = new HashMap<>();
	/**
	 * Software lists disks dst scans
	 */
	private Map<String, DirScan> swdisksDstScans = new HashMap<>();
	/**
	 * Contains all src and dst scans
	 */
	private final List<DirScan> allScans = new ArrayList<>();
	
	/**
	 * backup actions, always made first on entries that will be removed
	 */
	private final List<jrm.profile.fix.actions.ContainerAction> backupActions = new ArrayList<>();
	/**
	 * create actions, only for entries on totally new sets
	 */
	private final List<jrm.profile.fix.actions.ContainerAction> createActions = new ArrayList<>();
	/**
	 * rename before actions, all entries that will be delete are renamed first, to avoid collision from add and because they can be used for another add elsewhere during fix
	 */
	private final List<jrm.profile.fix.actions.ContainerAction> renameBeforeActions = new ArrayList<>();
	/**
	 * add actions
	 */
	private final List<jrm.profile.fix.actions.ContainerAction> addActions = new ArrayList<>();
	/**
	 * delete actions
	 */
	private final List<jrm.profile.fix.actions.ContainerAction> deleteActions = new ArrayList<>();
	/**
	 * rename after actions, for entries that need to replace another entry that have to be delete first
	 */
	private final List<jrm.profile.fix.actions.ContainerAction> renameAfterActions = new ArrayList<>();
	/**
	 * duplicate actions
	 */
	private final List<jrm.profile.fix.actions.ContainerAction> duplicateActions = new ArrayList<>();
	/**
	 * torrentzip actions, always the last actions when there is no more to do on zip archive
	 */
	private final Map<String, jrm.profile.fix.actions.ContainerAction> tzipActions = new HashMap<>();

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
		super(profile.getSession());
		this.profile = profile;
		this.report = profile.getSession().getReport();
		profile.setPropsCheckPoint();
		report.reset();
		report.setProfile(profile);
		
		/*
		 * Store locally various profile settings
		 */
		format = FormatOptions.valueOf(profile.getProperty(SettingsEnum.format, FormatOptions.ZIP.toString())); //$NON-NLS-1$
		mergeMode = MergeOptions.valueOf(profile.getProperty(SettingsEnum.merge_mode, MergeOptions.SPLIT.toString())); //$NON-NLS-1$
		createMode = profile.getProperty(SettingsEnum.create_mode, true); //$NON-NLS-1$
		createFullMode = profile.getProperty(SettingsEnum.createfull_mode, false); //$NON-NLS-1$
		ignoreUnneededContainers = profile.getProperty(SettingsEnum.ignore_unneeded_containers, false); //$NON-NLS-1$
		ignoreUnneededEntries = profile.getProperty(SettingsEnum.ignore_unneeded_entries, false); //$NON-NLS-1$
		ignoreUnknownContainers = profile.getProperty(SettingsEnum.ignore_unknown_containers, false); //$NON-NLS-1$
		backup = profile.getProperty(SettingsEnum.backup, true); //$NON-NLS-1$
		val useParallelism = profile.getProperty(SettingsEnum.use_parallelism, profile.getSession().isServer());
		val nThreads = useParallelism ? profile.getSession().getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;

		final String dstDirTxt = profile.getProperty(SettingsEnum.roms_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
		if (dstDirTxt.isEmpty())
		{
			Log.err("dst dir is empty");
			return; //TODO be more informative on failure
		}
		final File romsDstDir = getAbsolutePath(dstDirTxt).toFile();
		if (!romsDstDir.isDirectory())
		{
			Log.err("dst dir is not a directory");
			return; //TODO be more informative on failure
		}
		
		/*
		 * use disks dest dir if enabled otherwise it will be the same than roms dest dir
		 */
		final File disksDstDir;
		if (profile.getProperty(SettingsEnum.disks_dest_dir_enabled, false)) //$NON-NLS-1$
		{
			final String disksDstDirTxt = profile.getProperty(SettingsEnum.disks_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (disksDstDirTxt.isEmpty())
				return; //TODO be more informative on failure
			disksDstDir = getAbsolutePath(disksDstDirTxt).toFile();
		}
		else
			disksDstDir = new File(romsDstDir.getAbsolutePath());
		
		/*
		 * use sw roms dest dir if enabled otherwise it will be the same than roms dest dir
		 */
		final File swromsDstDir;
		if (profile.getProperty(SettingsEnum.swroms_dest_dir_enabled, false)) //$NON-NLS-1$
		{
			final String swromsDstDirTxt = profile.getProperty(SettingsEnum.swroms_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (swromsDstDirTxt.isEmpty())
				return; //TODO be more informative on failure
			swromsDstDir = getAbsolutePath(swromsDstDirTxt).toFile();
		}
		else
			swromsDstDir = new File(romsDstDir.getAbsolutePath());
		
		/*
		 * use sw disks dest dir if enabled otherwise it will be the same than disks dest dir (which in turn can be the same than roms dest dir)
		 */
		final File swdisksDstDir;
		if (profile.getProperty(SettingsEnum.swdisks_dest_dir_enabled, false)) //$NON-NLS-1$
		{
			final String swdisksDstDirTxt = profile.getProperty(SettingsEnum.swdisks_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (swdisksDstDirTxt.isEmpty())
				return; //TODO be more informative on failure
			swdisksDstDir = getAbsolutePath(swdisksDstDirTxt).toFile();
		}
		else
			swdisksDstDir = new File(swromsDstDir.getAbsolutePath());

		/*
		 * use samples dest dir if enabled and valid, otherwise it's null and not used
		 */
		final String samplesDstDirTxt = profile.getProperty(SettingsEnum.samples_dest_dir, ""); //$NON-NLS-1$ //$NON-NLS-2$
		final File samplesDstDir = profile.getProperty(SettingsEnum.samples_dest_dir_enabled, false) && samplesDstDirTxt.length() > 0 ? getAbsolutePath(samplesDstDirTxt).toFile() : null; //$NON-NLS-1$

		/*
		 * explode all src dir string into an ArrayList<File>
		 */
		final var srcdirs = new ArrayList<File>();
		for (final var s : StringUtils.split(profile.getProperty(SettingsEnum.src_dir, ""),'|')) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			if (!s.isEmpty())
			{
				final var f = getAbsolutePath(s).toFile();
				if (f.isDirectory())
					srcdirs.add(f);
			}
		}
		/* then add extra backup dir to that list */
		srcdirs.add(new File(profile.getSession().getUser().getSettings().getWorkPath().toFile(), "backup")); //$NON-NLS-1$
		/* then scan all dirs from that list */
		for (final var dir : srcdirs)
		{
			if(scancache != null)
			{
				final var cachefile  = DirScan.getCacheFile(profile.getSession(), dir, DirScan.getOptions(profile, false)).getAbsolutePath();
				if(!scancache.containsKey(cachefile))
					scancache.put(cachefile, new DirScan(profile, dir, handler, false));
				allScans.add(scancache.get(cachefile));
			}
			else
				allScans.add(new DirScan(profile, dir, handler, false));
			if (handler.isCancel())
				throw new BreakException();
		}
		
		/*
		 * scan all dst dirs according machines and softwares in profile, and determinate what is unknown and what is unneeded
		 */
		final ArrayList<Container> unknown = new ArrayList<>();
		final ArrayList<Container> unneeded = new ArrayList<>();
		final ArrayList<Container> samplesUnknown = new ArrayList<>();
		final ArrayList<Container> samplesUnneeded = new ArrayList<>();
		if (!profile.getMachineListList().get(0).isEmpty())
		{
			profile.getMachineListList().get(0).resetFilteredName();
			romsDstScan = dirscan(profile.getMachineListList().get(0), romsDstDir, unknown, unneeded, handler);
			if (romsDstDir.equals(disksDstDir))
				disksDstScan = romsDstScan;
			else
				disksDstScan = dirscan(profile.getMachineListList().get(0), disksDstDir, unknown, unneeded, handler);
			if (samplesDstDir != null && samplesDstDir.isDirectory())
				samplesDstScan = dirscan(profile.getMachineListList().get(0).samplesets, samplesDstDir, samplesUnknown, samplesUnneeded, handler);
			if (handler.isCancel())
				throw new BreakException();
		}
		if (!profile.getMachineListList().getSoftwareListList().isEmpty())
		{
			final AtomicInteger j = new AtomicInteger();
			handler.setProgress2(String.format("%d/%d", j.get(), profile.getMachineListList().getSoftwareListList().size()), j.get(), profile.getMachineListList().getSoftwareListList().size()); //$NON-NLS-1$
			for (final SoftwareList sl : profile.getMachineListList().getSoftwareListList().getFilteredStream().collect(Collectors.toList()))
			{
				sl.resetFilteredName();
				File sldir = new File(swromsDstDir, sl.getName());
				swromsDstScans.put(sl.getName(), dirscan(sl, sldir, unknown, unneeded, handler));
				if (swromsDstDir.equals(swdisksDstDir))
					swdisksDstScans = swromsDstScans;
				else
				{
					sldir = new File(swdisksDstDir, sl.getName());
					swdisksDstScans.put(sl.getName(), dirscan(sl, sldir, unknown, unneeded, handler));
				}
				handler.setProgress2(String.format("%d/%d (%s)", j.incrementAndGet(), profile.getMachineListList().getSoftwareListList().size(), sl.getName()), j.get(), profile.getMachineListList().getSoftwareListList().size()); //$NON-NLS-1$
				if (handler.isCancel())
					throw new BreakException();
			}
			handler.setProgress2(null, null);
			if (swromsDstDir.isDirectory() && !swromsDstDir.equals(romsDstDir))
			{
				File[] files  = swromsDstDir.listFiles();
				if(files!=null) for (final File f : files)
				{
					if (!swromsDstScans.containsKey(f.getName()))
						unknown.add(f.isDirectory() ? new Directory(f, getRelativePath(f), (Machine) null) : new Archive(f, getRelativePath(f), (Machine) null));
					if (handler.isCancel())
						throw new BreakException();
				}
			}
			if (!swromsDstDir.equals(swdisksDstDir) && swdisksDstDir.isDirectory())
			{
				File[] files  = swdisksDstDir.listFiles();
				if(files!=null) for (final File f : files)
				{
					if (!swdisksDstScans.containsKey(f.getName()))
						unknown.add(f.isDirectory() ? new Directory(f, getRelativePath(f), (Machine) null) : new Archive(f, getRelativePath(f), (Machine) null));
					if (handler.isCancel())
						throw new BreakException();
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
			if (!ignoreUnknownContainers)
			{
				unknown.stream().filter(c -> {
					if(samplesDstDir!=null && c.getRelFile().equals(samplesDstDir))
						return false;
					if(disksDstDir!=romsDstDir && c.getRelFile().equals(disksDstDir))
						return false;
					if(swromsDstDir!=romsDstDir && c.getRelFile().equals(swromsDstDir))
						return false;
					if(swdisksDstDir!=swromsDstDir && c.getRelFile().equals(swdisksDstDir))
						return false;
					return true;
				}).forEach(c -> {
					report.add(new ContainerUnknown(c));
					deleteActions.add(new DeleteContainer(c, format));
				});
			}
			/*
			 * process and report unneeded actions if requested
			 */
			if(!ignoreUnneededContainers)
			{
				unneeded.forEach(c->{
					report.add(new ContainerUnneeded(c));
					backupActions.add(new BackupContainer(c));
					deleteActions.add(new DeleteContainer(c, format));
				});
			}
			/*
			 * report suspicious CRCs
			 */
			profile.getSuspiciousCRC().forEach(crc -> report.add(new RomSuspiciousCRC(crc)));

			/*
			 * Searching for fixes
			 */
			final AtomicInteger i = new AtomicInteger();
			final AtomicInteger j = new AtomicInteger();
			handler.setProgress(null, i.get(), profile.filteredSubsize()); //$NON-NLS-1$
			handler.setProgress2(String.format("%s %d/%d", Messages.getString(MSG_SCAN_SEARCHING_FOR_FIXES), j.get(), profile.size()), j.get(), profile.size()); //$NON-NLS-1$
			if (!profile.getMachineListList().get(0).isEmpty())
			{
				
				/* Scan all samples */
				handler.setProgress2(String.format("%s %d/%d", Messages.getString(MSG_SCAN_SEARCHING_FOR_FIXES), j.get(), profile.size()), j.getAndIncrement(), profile.size()); //$NON-NLS-1$
				new MultiThreading<Samples>(nThreads, set ->
				{
					if (handler.isCancel())
						return;
					handler.setProgress(set.getName(), i.getAndIncrement());
					if (samplesDstScan != null)
						scanSamples(set);
				}).start(StreamSupport.stream(profile.getMachineListList().get(0).samplesets.spliterator(),false));
				/* scan all machines */ 
				profile.getMachineListList().get(0).forEach(Machine::resetCollisionMode);
				new MultiThreading<Machine>(nThreads, m ->
				{
					if (handler.isCancel())
						return;
					handler.setProgress(m.getFullName(), i.getAndIncrement());
					scanWare(m);
				}).start(profile.getMachineListList().get(0).getFilteredStream());
			}
			if (!profile.getMachineListList().getSoftwareListList().isEmpty())
			{
				/* scan all software lists */
				profile.getMachineListList().getSoftwareListList().getFilteredStream().takeWhile(sl -> !handler.isCancel()).forEach(sl -> {
					// for each software list
					handler.setProgress2(String.format("%s %d/%d (%s)", Messages.getString(MSG_SCAN_SEARCHING_FOR_FIXES), j.get(), profile.size(), sl.getName()), j.getAndIncrement(), profile.size()); //$NON-NLS-1$
					romsDstScan = swromsDstScans.get(sl.getName());
					disksDstScan = swdisksDstScans.get(sl.getName());
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
		catch (final Exception e)
		{
			Log.err("Other Exception when listing", e); //$NON-NLS-1$
		}
		finally
		{
			handler.setInfos(1,null);
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			/* save report */
			if(!profile.getSession().isServer())
				report.write(profile.getSession());
			report.flush();
			/* update and save stats */
			final var nfo = profile.getNfo();
			nfo.getStats().setScanned(new Date());
			nfo.getStats().setHaveSets(Stream.concat(profile.getMachineListList().stream(), profile.getMachineListList().getSoftwareListList().stream()).mapToLong(AnywareList::countHave).sum());
			nfo.getStats().setHaveRoms(Stream.concat(profile.getMachineListList().stream(), profile.getMachineListList().getSoftwareListList().stream()).flatMap(AnywareList::stream).mapToLong(Anyware::countHaveRoms).sum());
			nfo.getStats().setHaveDisks(Stream.concat(profile.getMachineListList().stream(), profile.getMachineListList().getSoftwareListList().stream()).flatMap(AnywareList::stream).mapToLong(Anyware::countHaveDisks).sum());
			nfo.save(profile.getSession());
			/* save again profile cache with scan entity status */
			profile.save(); 
		}

		/*
		 * add all actions lists, to the main actions list
		 */
		if(backup)
			actions.add(backupActions);
		actions.add(createActions);
		actions.add(renameBeforeActions);
		actions.add(duplicateActions);
		actions.add(addActions);
		actions.add(deleteActions);
		actions.add(renameAfterActions);
		actions.add(new ArrayList<>(tzipActions.values()));

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
		final DirScan dstScan;
		dstScan = new DirScan(profile, dstdir, handler, true);
		allScans.add(dstScan);
		for (final Container c : dstScan.getContainersIterable())
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
		return dstScan;
	}

	private static String getBaseName(File file)
	{
		String name = file.getName();
		final int last = name.lastIndexOf('.');
		if (last > 0 && name.indexOf(' ', last) == -1)
			name = name.substring(0, last);
		return name;
	}
	
	/**
	 * Determinate if a container need to be torrentzipped
	 * @param reportSubject a SubjectSet containing the report about this archive
	 * @param archive the {@link Container} to eventually torrentzip
	 * @param ware the {@link Anyware} corresponding to the machine or Software of the archive
	 * @param roms the filtered {@link Rom} {@link List}
	 */
	private void prepTZip(final SubjectSet reportSubject, final Container archive, final Anyware ware, final List<Rom> roms)
	{
		if (format == FormatOptions.TZIP && (!mergeMode.isMerge() || !ware.isClone()) && (!reportSubject.isMissing() && !reportSubject.isUnneeded() && !roms.isEmpty()))
		{
			Container tzipcontainer = null;
			final Container container = romsDstScan.getContainerByName(ware.getDest().getName() + format.getExt());
			if (container != null)
			{
				if (container.getLastTZipCheck() < container.getModified())
					tzipcontainer = container;
				else if (!container.getLastTZipStatus().contains(TrrntZipStatus.ValidTrrntzip))
					tzipcontainer = container;
				else if (reportSubject.hasFix())
					tzipcontainer = container;

			}
			else if (createMode)
			{
				if (createFullMode)
				{
					if (reportSubject.isFixable())
						tzipcontainer = archive;
				}
				else
				{
					if (reportSubject.hasFix())
						tzipcontainer = archive;
				}
			}
			if (tzipcontainer != null)
			{
				final long estimated_roms_size = roms.stream().mapToLong(Rom::getSize).sum();
				tzipcontainer.setRelAW(ware);
				tzipActions.put(tzipcontainer.getFile().getAbsolutePath(), new TZipContainer(tzipcontainer, format, estimated_roms_size));
				report.add(new ContainerTZip(tzipcontainer));
			}
		}
	}

	/**
	 * Determinate if a samples container need to be torrentzipped
	 * @param reportSubject report_subject a SubjectSet containing the report about this archive
	 * @param archive the {@link Container} to eventually torrentzip
	 * @param set the set of samples
	 */
	private void prepTZip(final SubjectSet reportSubject, final Container archive, final Samples set)
	{
		if (format == FormatOptions.TZIP && !reportSubject.isMissing() && !reportSubject.isUnneeded() && set.getSamplesMap().size() > 0)
		{
			Container tzipcontainer = null;
			final Container container = samplesDstScan.getContainerByName(archive.getFile().getName());
			if (container != null)
			{
				if (container.getLastTZipCheck() < container.getModified())
					tzipcontainer = container;
				else if (!container.getLastTZipStatus().contains(TrrntZipStatus.ValidTrrntzip))
					tzipcontainer = container;
				else if (reportSubject.hasFix())
					tzipcontainer = container;
			}
			else if (createMode && reportSubject.hasFix())
				tzipcontainer = archive;
			if (tzipcontainer != null)
			{
				tzipcontainer.setRelAW(set);
				tzipActions.put(tzipcontainer.getFile().getAbsolutePath(), new TZipContainer(tzipcontainer, format, Long.MAX_VALUE));
				report.add(new ContainerTZip(tzipcontainer));
			}
		}
	}


	/**
	 * Remove archive formats of a set that are not the current format target
	 * @param ware the {@link Anyware}, a machine or software from which to remove unneeded format archives
	 */
	private void removeOtherFormats(final Anyware ware)
	{
		format.getExt().allExcept().forEach(e -> { // set other formats with the same set name as unneeded
			final Container c = romsDstScan.getContainerByName(ware.getName() + e);
			if (c != null)
			{
				report.add(new ContainerUnneeded(c));
				backupActions.add(new BackupContainer(c));
				deleteActions.add(new DeleteContainer(c, format));
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
		if (mergeMode.isMerge() && ware.isClone())
		{
			if (format == FormatOptions.DIR && disks.isEmpty() && roms.isEmpty())
			{
				Arrays.asList(romsDstScan.getContainerByName(ware.getName()), disksDstScan.getContainerByName(ware.getName())).forEach(c -> {
					if (c != null)
					{
						report.add(new ContainerUnneeded(c));
						backupActions.add(new BackupContainer(c));
						deleteActions.add(new DeleteContainer(c, format));
					}
				});
			}
			else if (disks.isEmpty())
			{
				final Container c = disksDstScan.getContainerByName(ware.getName());
				if (c != null)
				{
					report.add(new ContainerUnneeded(c));
					backupActions.add(new BackupContainer(c));
					deleteActions.add(new DeleteContainer(c, format));
				}
			}
			if (format != FormatOptions.DIR && roms.isEmpty())
			{
				final Container c = romsDstScan.getContainerByName(ware.getName() + format.getExt());
				if (c != null)
				{
					report.add(new ContainerUnneeded(c));
					backupActions.add(new BackupContainer(c));
					deleteActions.add(new DeleteContainer(c, format));
				}
			}
		}
	}

	/**
	 * Scan disks
	 * @param ware the current {@link Anyware} we are processing
	 * @param disks the filtered {@link Disk} {@link List}
	 * @param directory the {@link Directory} in which the disks will reside
	 * @param reportSubject the {@link SubjectSet} report related to this {@link Anyware}
	 * @return true if set is currently missing
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean scanDisks(final Anyware ware, final List<Disk> disks, final Directory directory, final SubjectSet reportSubject)
	{
		boolean missingSet = true;
		final Container container;
		if (null != (container = disksDstScan.getContainerByName(ware.getDest().getNormalizedName())))
		{
			missingSet = false;
			if (!disks.isEmpty())
			{
				reportSubject.setFound();

				final var disksFound = new ArrayList<Entry>();
				final var disksByName = Disk.getDisksByName(disks);
				OpenContainer addSet = null;
				OpenContainer deleteSet = null;
				OpenContainer renameBeforeSet = null;
				OpenContainer renameAfterSet = null;
				OpenContainer duplicateSet = null;
				
				final var markedForRename = new HashSet<Entry>();
				
				for (final Disk disk : disks)
				{
					disk.setStatus(EntityStatus.KO);
					Entry foundEntry = null;
					final var entriesByName = container.getEntriesByName();
					for (final var candidateEntry : container.getEntries())
					{
						if (candidateEntry.equals(disk))
						{
							if (!disk.getNormalizedName().equals(candidateEntry.getName())) // but this entry name does not match the rom name
							{
								final Disk anotherDisk;
								if (null != (anotherDisk = disksByName.get(candidateEntry.getName())) && candidateEntry.equals(anotherDisk))
								{
									if (entriesByName.containsKey(disk.getNormalizedName()))
									{
										// report_w.println("["+m.name+"] "+d.getName()+" == "+e.file);
									}
									else
									{
										// we must duplicate
										reportSubject.add(new EntryMissingDuplicate(disk, candidateEntry));
										duplicateSet = OpenContainer.getInstance(duplicateSet, directory, format, 0L);
										duplicateSet.addAction(new DuplicateEntry(disk.getName(), candidateEntry));
										foundEntry = candidateEntry;
									}
								}
								else
								{
									if (!entriesByName.containsKey(disk.getNormalizedName())) // and disk name is not in the entries
									{
										if(!markedForRename.contains(candidateEntry))
										{
											reportSubject.add(new EntryWrongName(disk, candidateEntry));
											renameBeforeSet = OpenContainer.getInstance(renameBeforeSet, directory, format, 0L);
											renameBeforeSet.addAction(new RenameEntry(candidateEntry));
											renameAfterSet = OpenContainer.getInstance(renameAfterSet, directory, format, 0L);
											renameAfterSet.addAction(new RenameEntry(disk.getName(), candidateEntry));
											markedForRename.add(candidateEntry);
										}
										else
										{
											reportSubject.add(new EntryAdd(disk, candidateEntry));
											duplicateSet = OpenContainer.getInstance(duplicateSet, directory, format, 0L);
											duplicateSet.addAction(new DuplicateEntry(disk.getName(), candidateEntry));
										}
										foundEntry = candidateEntry;
										break;
									}
								}
							}
							else
							{
								foundEntry = candidateEntry;
								break;
							}
						}
						else if (disk.getNormalizedName().equals(candidateEntry.getName()))
						{
							reportSubject.add(new EntryWrongHash(disk, candidateEntry));
							// found = e;
							break;
						}
					}
					if (foundEntry == null)
					{
						report.getStats().incMissingDisksCnt();
						for (final DirScan scan : allScans)
						{
							if (null != (foundEntry = scan.findByHash(disk)))
							{
								reportSubject.add(new EntryAdd(disk, foundEntry));
								addSet = OpenContainer.getInstance(addSet, directory, format, 0L);
								addSet.addAction(new AddEntry(disk, foundEntry));
								break;
							}
						}
						if (foundEntry == null)
							reportSubject.add(new EntryMissing(disk));
					}
					else
					{
						disk.setStatus(EntityStatus.OK);
						reportSubject.add(new EntryOK(disk));
						// report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
						disksFound.add(foundEntry);
					}
				}
				if (!ignoreUnneededEntries)
				{
					final List<Entry> unneeded = container.getEntries().stream().filter(Scan.not(new HashSet<>(disksFound)::contains)).collect(Collectors.toList());
					for (final Entry unneeded_entry : unneeded)
					{
						reportSubject.add(new EntryUnneeded(unneeded_entry));
						renameBeforeSet = OpenContainer.getInstance(renameBeforeSet, directory, format, 0L);
						renameBeforeSet.addAction(new RenameEntry(unneeded_entry));
						deleteSet = OpenContainer.getInstance(deleteSet, directory, format, 0L);
						deleteSet.addAction(new DeleteEntry(unneeded_entry));
					}
				}
				ContainerAction.addToList(renameBeforeActions, renameBeforeSet);
				ContainerAction.addToList(duplicateActions, duplicateSet);
				ContainerAction.addToList(addActions, addSet);
				ContainerAction.addToList(deleteActions, deleteSet);
				ContainerAction.addToList(renameAfterActions, renameAfterSet);
			}
		}
		else
		{
			for (final Disk disk : disks)
				disk.setStatus(EntityStatus.KO);
			if (createMode && !disks.isEmpty())
			{
				int disksFound = 0;
				boolean partialSet = false;
				CreateContainer createSet = null;
				for (final Disk disk : disks)
				{
					report.getStats().incMissingDisksCnt();
					Entry foundEntry = null;
					for (final DirScan scan : allScans)
					{
						if (null != (foundEntry = scan.findByHash(disk)))
						{
							reportSubject.add(new EntryAdd(disk, foundEntry));
							createSet = CreateContainer.getInstance(createSet, directory, format, 0L);
							createSet.addAction(new AddEntry(disk, foundEntry));
							disksFound++;
							break;
						}
					}
					if (foundEntry == null)
					{
						reportSubject.add(new EntryMissing(disk));
						partialSet = true;
					}
				}
				if (disksFound > 0 && (!createFullMode || !partialSet))
				{
					reportSubject.setCreateFull();
					if (partialSet)
						reportSubject.setCreate();
					ContainerAction.addToList(createActions, createSet);
				}
			}
		}
		return missingSet;
	}

	/**
	 * Scan roms
	 * @param ware the current {@link Anyware} we are processing
	 * @param roms the filtered {@link Rom} {@link List}
	 * @param archive the {@link Container} in which the roms will reside
	 * @param reportSubject the {@link SubjectSet} report related to this {@link Anyware}
	 * @return true if set is currently missing
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean scanRoms(final Anyware ware, final List<Rom> roms, final Container archive, final SubjectSet reportSubject)
	{
		boolean missingSet = true;
		final Container container;
		final long estimatedRomsSize = roms.stream().mapToLong(Rom::getSize).sum();
		
		if (null != (container = romsDstScan.getContainerByName(ware.getDest().getNormalizedName() + format.getExt())))
		{	// found container
			missingSet = false;
			if (!roms.isEmpty())
			{
				reportSubject.setFound();

				final var romsFound = new ArrayList<Entry>();
				final var romsByName = Rom.getRomsByName(roms);
				
				BackupContainer backupSet = null;
				OpenContainer addSet = null;
				OpenContainer deleteSet = null;
				OpenContainer renameBeforeSet = null;
				OpenContainer renameAfterSet = null;
				OpenContainer duplicateSet = null;

				final var entriesByName = container.getEntriesByName();
				final var entriesBySha1 = new HashMap<String, List<Entry>>();
				container.getEntries().forEach(e -> {
					if (e.getSha1() != null)
						entriesBySha1.computeIfAbsent(e.getSha1(), k -> new ArrayList<>()).add(e);
				});
				final var entriesByMd5 = new HashMap<String, List<Entry>>();
				container.getEntries().forEach(e -> {
					if (e.getMd5() != null)
						entriesByMd5.computeIfAbsent(e.getMd5(), k -> new ArrayList<>()).add(e);
				});
				final Map<String, List<Entry>> entriesByCrc = new HashMap<>();
				container.getEntries().forEach(e -> {
					if (e.getCrc() != null)
						entriesByCrc.computeIfAbsent(e.getCrc() + '.' + e.getSize(), k -> new ArrayList<>()).add(e);
				});

				final Set<Entry> markedForRename = new HashSet<>();
				
				for (final Rom rom : roms)	// check roms
				{
					rom.setStatus(EntityStatus.KO);
					Entry foundEntry = null;
					Entry wrongHash = null;
					
					List<Entry> entries = null;
					if(rom.getSha1()!=null)
						entries = entriesBySha1.get(rom.getSha1());
					if(entries == null && rom.getMd5()!=null)
						entries = entriesByMd5.get(rom.getMd5());
					if(entries == null && rom.getCrc()!=null)
						entries = entriesByCrc.get(rom.getCrc()+'.'+rom.getSize());
					if(entries != null) 
					{
						for (final var candidate_entry : entries)
						{
							final String efile = candidate_entry.getName();
							Log.debug(()->"The entry " + efile + " match hash from rom " + rom.getNormalizedName());
							if (!rom.getNormalizedName().equals(efile)) // but this entry name does not match the rom name
							{
								Log.debug(()->"\tbut this entry name does not match the rom name");
								final Rom anotherRom;
								if (null != (anotherRom = romsByName.get(efile)) && candidate_entry.equals(anotherRom))
								{
									Log.debug(()->"\t\t\tand the entry " + efile + " is ANOTHER rom");
									if (entriesByName.containsKey(rom.getNormalizedName())) // and rom name is in the entries
									{
										Log.debug(()->"\t\t\t\tand rom " + rom.getNormalizedName() + " is in the entries_byname");
										// report_w.println("[" + m.name + "] " + r.getName() + " == " + e.file);
									}
									else
									{
										Log.debug(()->"\\t\\t\\t\\twe must duplicate rom " + rom.getNormalizedName() + " to ");
										// we must duplicate
										reportSubject.add(new EntryMissingDuplicate(rom, candidate_entry));
										duplicateSet = OpenContainer.getInstance(duplicateSet, archive, format, estimatedRomsSize);
										duplicateSet.addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										foundEntry = candidate_entry;
										break;
									}
								}
								else
								{
									if (anotherRom == null)
									{
										Log.debug(()->
										{
											final var str = new StringBuilder("\t" + efile + " in roms_byname not found");
											romsByName.forEach((k, v) -> str.append("\troms_byname: " + k));
											return str.toString();
										});
									}
									else Log.debug(()->"\t" + efile + " in roms_byname found but does not match hash");

									if (!entriesByName.containsKey(rom.getNormalizedName())) // and rom name is not in the entries
									{
										Log.debug(()->
										{
											final var str = new StringBuilder("\t\tand rom " + rom.getNormalizedName() + " is NOT in the entries_byname");
											entriesByName.forEach((k, v) -> str.append("\t\tentries_byname: " + k));
											return str.toString();
										});

										if(!markedForRename.contains(candidate_entry))
										{
											reportSubject.add(new EntryWrongName(rom, candidate_entry));
											renameBeforeSet = OpenContainer.getInstance(renameBeforeSet, archive, format, estimatedRomsSize);
											renameBeforeSet.addAction(new RenameEntry(candidate_entry));
											renameAfterSet = OpenContainer.getInstance(renameAfterSet, archive, format, estimatedRomsSize);
											renameAfterSet.addAction(new RenameEntry(rom.getName(), candidate_entry));
											markedForRename.add(candidate_entry);
										}
										else
										{
											reportSubject.add(new EntryAdd(rom, candidate_entry));
											duplicateSet = OpenContainer.getInstance(duplicateSet, archive, format, estimatedRomsSize);
											duplicateSet.addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										}
										//(backup_set = BackupContainer.getInstance(backup_set, archive)).addAction(new BackupEntry(candidate_entry));
										//(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										// (delete_set = OpenContainer.getInstance(delete_set, archive, format, roms.stream().mapToLong(Rom::getSize).sum())).addAction(new DeleteEntry(candidate_entry));
										foundEntry = candidate_entry;
										break;
									}
									else Log.debug(()->"\t\tand rom " + rom.getNormalizedName() + " is in the entries_byname");
								}
							}
							else
							{
								Log.debug(()->"\tThe entry " + efile + " match hash and name for rom " + rom.getNormalizedName());
								foundEntry = candidate_entry;
								break;
							}
						}
					}
					if(foundEntry == null)
					{
						final Entry candidateEntry;
						if((candidateEntry = entriesByName.get(rom.getNormalizedName()))!=null)
						{
							final String efile = candidateEntry.getName();
							Log.debug(()->"\tOups! we got wrong hash in "+efile+" for "+rom.getNormalizedName());
							//report_subject.add(new EntryWrongHash(rom, candidate_entry));
							wrongHash = candidateEntry;
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
					if (foundEntry == null)	// did not find rom in container
					{
						report.getStats().incMissingRomsCnt();
						for (final DirScan scan : allScans)	// now search for rom in all available dir scans
						{
							if (null != (foundEntry = scan.findByHash(rom)))
							{
								reportSubject.add(new EntryAdd(rom, foundEntry));
								addSet = OpenContainer.getInstance(addSet, archive, format, estimatedRomsSize);
								addSet.addAction(new AddEntry(rom, foundEntry));
								// roms_found.add(found);
								break;
							}
						}
						if (foundEntry == null)	// we did not found this rom anywhere
							reportSubject.add(wrongHash!=null?new EntryWrongHash(rom, wrongHash):new EntryMissing(rom));
					}
					else
					{
						// report_w.println("[" + m.name + "] " + r.getName() + " (" + found.file + ") OK ");
						rom.setStatus(EntityStatus.OK);
						reportSubject.add(new EntryOK(rom));
						romsFound.add(foundEntry);
					}
				}
				if (!ignoreUnneededEntries)
				{	// remove unneeded entries
					final List<Entry> unneeded = container.getEntries().stream().filter(Scan.not(new HashSet<>(romsFound)::contains)).collect(Collectors.toList());
					for (final Entry unneeded_entry : unneeded)
					{
						reportSubject.add(new EntryUnneeded(unneeded_entry));
						backupSet = BackupContainer.getInstance(backupSet, archive);
						backupSet.addAction(new BackupEntry(unneeded_entry));
						renameBeforeSet = OpenContainer.getInstance(renameBeforeSet, archive, format, estimatedRomsSize);
						renameBeforeSet.addAction(new RenameEntry(unneeded_entry));
						deleteSet = OpenContainer.getInstance(deleteSet, archive, format, estimatedRomsSize);
						deleteSet.addAction(new DeleteEntry(unneeded_entry));
					}
				}
				ContainerAction.addToList(backupActions, backupSet);
				ContainerAction.addToList(renameBeforeActions, renameBeforeSet);
				ContainerAction.addToList(duplicateActions, duplicateSet);
				ContainerAction.addToList(addActions, addSet);
				ContainerAction.addToList(deleteActions, deleteSet);
				ContainerAction.addToList(renameAfterActions, renameAfterSet);
			}
		}
		else	// container is missing
		{
			for (final Rom rom : roms)
				rom.setStatus(EntityStatus.KO);
			if (createMode && !roms.isEmpty())
			{
				int romsFound = 0;
				boolean partialSet = false;
				CreateContainer createSet = null;
				for (final Rom rom : roms)
				{
					report.getStats().incMissingRomsCnt();
					Entry entryFound = null;
					for (final DirScan scan : allScans)	// search rom in all scans
					{
						if (null != (entryFound = scan.findByHash(rom)))
						{
							reportSubject.add(new EntryAdd(rom, entryFound));
							createSet = CreateContainer.getInstance(createSet, archive, format, estimatedRomsSize);
							createSet.addAction(new AddEntry(rom, entryFound));
							romsFound++;
							break;
						}
					}
					if (entryFound == null)	// We did not find all roms to create a full set
					{
						reportSubject.add(new EntryMissing(rom));
						partialSet = true;
					}
				}
				if (romsFound > 0 && (!createFullMode || !partialSet))
				{
					reportSubject.setCreateFull();
					if (partialSet)
						reportSubject.setCreate();
					ContainerAction.addToList(createActions, createSet);
				}
			}
		}
		return missingSet;
	}

	/**
	 * Scan samples
	 * @param set the {@link Samples} set to scan
	 */
	private void scanSamples(final Samples set)
	{
		boolean missingSet = true;
		final Container archive;
		if (format.getExt().isDir())
		{
			final var f = new File(samplesDstScan.getDir(), set.getName());
			archive = new Directory(f, getRelativePath(f), set);
		}
		else
		{
			final var f = new File(samplesDstScan.getDir(), set.getName() + format.getExt());
			archive = new Archive(f, getRelativePath(f), set);
		}
		final SubjectSet reportSubject = new SubjectSet(set);
		if(!scanSamples(set, archive, reportSubject))
			missingSet = false;
		if (createMode && reportSubject.getStatus() == Status.UNKNOWN)
			reportSubject.setMissing();
		if (missingSet)
			report.getStats().incMissingSetCnt();
		if (reportSubject.getStatus() != Status.UNKNOWN)
			report.add(reportSubject);
		prepTZip(reportSubject, archive, set);
	}

	/**
	 * Scan samples
	 * @param set the Samples set to scan
	 * @param archive the {@link Container} in which the samples will reside
	 * @param reportSubject the {@link SubjectSet} report related to this {@link Anyware}
	 * @return true if set is currently missing
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean scanSamples(final Samples set, final Container archive, final SubjectSet reportSubject)
	{
		boolean missingSet = true;
		final Container container;
		if (null != (container = samplesDstScan.getContainerByName(archive.getFile().getName())))
		{
			missingSet = false;
			reportSubject.setFound();
			final ArrayList<Entry> samplesFound = new ArrayList<>();
			final OpenContainer addSet = null;
			OpenContainer deleteSet = null;
			OpenContainer renameBeforeSet = null;
			final OpenContainer renameAfterSet = null;
			final OpenContainer duplicateSet = null;
			for (final Sample sample : set)
			{
				sample.setStatus(EntityStatus.KO);
				Entry foundEntry = null;
				for (final Entry candidate_entry : container.getEntries())
				{
					if (candidate_entry.equals(sample))
					{
						foundEntry = candidate_entry;
						break;
					}
				}
				if (foundEntry == null)
				{
					report.getStats().incMissingSamplesCnt();
					for (final DirScan scan : allScans)
					{
						for (final FormatOptions.Ext ext : EnumSet.allOf(FormatOptions.Ext.class))
						{
							final Container foundContainer;
							if (null != (foundContainer = scan.getContainerByName(set.getName() + ext)))
							{
								for (final Entry entry : foundContainer.getEntriesByFName().values())
								{
									if (entry.getName().equals(sample.getNormalizedName()))
										foundEntry = entry;
									if (null != foundEntry)
										break;
								}
							}
							if (null != foundEntry)
								break;
						}
					}
					if (foundEntry == null)
						reportSubject.add(new EntryMissing(sample));
				}
				else
				{
					sample.setStatus(EntityStatus.OK);
					reportSubject.add(new EntryOK(sample));
					// report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
					samplesFound.add(foundEntry);
				}
			}
			if (!ignoreUnneededEntries)
			{
				final List<Entry> unneeded = container.getEntries().stream().filter(Scan.not(new HashSet<>(samplesFound)::contains)).collect(Collectors.toList());
				for (final Entry unneededEntry : unneeded)
				{
					reportSubject.add(new EntryUnneeded(unneededEntry));
					renameBeforeSet = OpenContainer.getInstance(renameBeforeSet, archive, format, Long.MAX_VALUE);
					renameBeforeSet.addAction(new RenameEntry(unneededEntry));
					deleteSet = OpenContainer.getInstance(deleteSet, archive, format, Long.MAX_VALUE);
					deleteSet.addAction(new DeleteEntry(unneededEntry));
				}
			}
			ContainerAction.addToList(renameBeforeActions, renameBeforeSet);
			ContainerAction.addToList(duplicateActions, duplicateSet);
			ContainerAction.addToList(addActions, addSet);
			ContainerAction.addToList(deleteActions, deleteSet);
			ContainerAction.addToList(renameAfterActions, renameAfterSet);
		}
		else
		{
			for (final Sample sample : set)
				sample.setStatus(EntityStatus.KO);
			if (createMode)
			{
				int samplesFound = 0;
				boolean partialSet = false;
				CreateContainer createSet = null;
				for (final Sample sample : set)
				{
					report.getStats().incMissingSamplesCnt();
					Entry entryFound = null;
					for (final DirScan scan : allScans)
					{
						for (final FormatOptions.Ext ext : EnumSet.allOf(FormatOptions.Ext.class))
						{
							final Container foundContainer;
							if (null != (foundContainer = scan.getContainerByName(set.getName() + ext)))
							{
								for (final Entry entry : foundContainer.getEntriesByFName().values())
								{
									if (entry.getName().equals(sample.getNormalizedName()))
										entryFound = entry;
									if (null != entryFound)
										break;
								}
							}
							if (null != entryFound)
								break;
						}
						if (null != entryFound)
						{
							reportSubject.add(new EntryAdd(sample, entryFound));
							createSet = CreateContainer.getInstance(createSet, archive, format, Long.MAX_VALUE);
							createSet.addAction(new AddEntry(sample, entryFound));
							samplesFound++;
							break;
						}
					}
					if (entryFound == null)
					{
						reportSubject.add(new EntryMissing(sample));
						partialSet = true;
					}
				}
				if (samplesFound > 0 && (!createFullMode || !partialSet))
				{
					reportSubject.setCreateFull();
					if (partialSet)
						reportSubject.setCreate();
					ContainerAction.addToList(createActions, createSet);
				}
			}
		}
		return missingSet;
	}

	/**
	 * Scan a Machine or a Software
	 * @param ware the {@link Anyware} to scan
	 */
	private void scanWare(final Anyware ware)
	{
		final SubjectSet reportSubject = new SubjectSet(ware);

		boolean missingSet = true;
		final var dd = new File(disksDstScan.getDir(), ware.getDest().getName());
		final Directory directory = new Directory(dd, getRelativePath(dd), ware);
		final Container archive;
		if (format == FormatOptions.DIR)
		{
			final var d = new File(romsDstScan.getDir(), ware.getDest().getName());
			archive = new Directory(d, getRelativePath(d), ware);
		}
		else if (format == FormatOptions.FAKE)
		{
			final var fd = new File(romsDstScan.getDir(), ware.getDest().getName());
			archive = new FakeDirectory(fd, getRelativePath(fd), ware);
		}
		else
		{
			final var af = new File(romsDstScan.getDir(), ware.getDest().getName() + format.getExt());
			archive = new Archive(af, getRelativePath(af), ware);
		}
		final List<Rom> roms = ware.filterRoms();
		final List<Disk> disks = ware.filterDisks();
		if (!scanRoms(ware, roms, archive, reportSubject))
			missingSet = false;
		prepTZip(reportSubject, archive, ware, roms);
		if (!scanDisks(ware, disks, directory, reportSubject))
			missingSet = false;
		if (roms.isEmpty() && disks.isEmpty())
		{
			if (!(mergeMode.isMerge() && ware.isClone()))
			{
				if (!missingSet)
					reportSubject.setUnneeded();
				else
					reportSubject.setFound();
			}
			missingSet = false;
		}
		else if (createMode && reportSubject.getStatus() == Status.UNKNOWN)
			reportSubject.setMissing();
		if (!ignoreUnneededContainers)
		{
			removeUnneededClone(ware, disks, roms);
			removeOtherFormats(ware);
		}
		if (missingSet)
			report.getStats().incMissingSetCnt();
		if (reportSubject.getStatus() != Status.UNKNOWN)
			report.add(reportSubject);
	}

}
