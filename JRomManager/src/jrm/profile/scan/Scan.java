package jrm.profile.scan;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FilenameUtils;

import JTrrntzip.TrrntZipStatus;
import jrm.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.data.*;
import jrm.profile.fix.actions.*;
import jrm.profile.report.*;
import jrm.profile.report.SubjectSet.Status;
import jrm.profile.scan.options.FormatOptions;
import jrm.profile.scan.options.HashCollisionOptions;
import jrm.profile.scan.options.MergeOptions;
import jrm.ui.MainFrame;
import jrm.ui.ProgressHandler;

public class Scan
{
	public ArrayList<Collection<jrm.profile.fix.actions.ContainerAction>> actions = new ArrayList<>();
	public static Report report = new Report();
	private Profile profile;

	private MergeOptions merge_mode;
	private boolean implicit_merge;
	private FormatOptions format;
	private boolean create_mode;
	private boolean createfull_mode;
	private boolean ignore_unneeded_containers;
	private boolean ignore_unneeded_entries;
	private boolean ignore_unknown_containers;
	private HashCollisionOptions hash_collision_mode;
	private DirScan roms_dstscan = null;
	private DirScan disks_dstscan = null;
	private Map<String, DirScan> swroms_dstscans = new HashMap<String, DirScan>();
	private Map<String, DirScan> swdisks_dstscans = new HashMap<String, DirScan>();
	private List<DirScan> allscans = new ArrayList<>();

	private ArrayList<jrm.profile.fix.actions.ContainerAction> create_actions = new ArrayList<>();
	private ArrayList<jrm.profile.fix.actions.ContainerAction> rename_before_actions = new ArrayList<>();
	private ArrayList<jrm.profile.fix.actions.ContainerAction> add_actions = new ArrayList<>();
	private ArrayList<jrm.profile.fix.actions.ContainerAction> delete_actions = new ArrayList<>();
	private ArrayList<jrm.profile.fix.actions.ContainerAction> rename_after_actions = new ArrayList<>();
	private ArrayList<jrm.profile.fix.actions.ContainerAction> duplicate_actions = new ArrayList<>();
	private Map<String, jrm.profile.fix.actions.ContainerAction> tzip_actions = new HashMap<>();

	public Scan(Profile profile, ProgressHandler handler) throws BreakException
	{
		this.profile = profile;
		report.reset();
		format = FormatOptions.valueOf(profile.getProperty("format", FormatOptions.ZIP.toString())); //$NON-NLS-1$
		merge_mode = MergeOptions.valueOf(profile.getProperty("merge_mode", MergeOptions.SPLIT.toString())); //$NON-NLS-1$
		implicit_merge = profile.getProperty("implicit_merge", false); //$NON-NLS-1$
		create_mode = profile.getProperty("create_mode", true); //$NON-NLS-1$
		createfull_mode = profile.getProperty("createfull_mode", true); //$NON-NLS-1$
		hash_collision_mode = HashCollisionOptions.valueOf(profile.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString())); //$NON-NLS-1$
		ignore_unneeded_containers = profile.getProperty("ignore_unneeded_containers", false); //$NON-NLS-1$
		ignore_unneeded_entries = profile.getProperty("ignore_unneeded_entries", false); //$NON-NLS-1$
		ignore_unknown_containers = profile.getProperty("ignore_unknown_containers", false); //$NON-NLS-1$

		String dstdir_txt = profile.getProperty("roms_dest_dir", "");
		if(dstdir_txt.isEmpty())
			return;
		File roms_dstdir = new File(dstdir_txt);
		if(!roms_dstdir.isDirectory())
			return;
		File disks_dstdir = new File(roms_dstdir.getAbsolutePath());
		if(profile.getProperty("disks_dest_dir_enabled", false))
		{
			String disks_dstdir_txt = profile.getProperty("disks_dest_dir", "");
			if(disks_dstdir_txt.isEmpty())
				return;
			disks_dstdir = new File(disks_dstdir_txt);
		}
		File swroms_dstdir = new File(roms_dstdir.getAbsolutePath());
		if(profile.getProperty("swroms_dest_dir_enabled", false))
		{
			String swroms_dstdir_txt = profile.getProperty("swroms_dest_dir", "");
			if(swroms_dstdir_txt.isEmpty())
				return;
			swroms_dstdir = new File(swroms_dstdir_txt);
		}
		File swdisks_dstdir = new File(swroms_dstdir.getAbsolutePath());
		if(profile.getProperty("swdisks_dest_dir_enabled", false))
		{
			String swdisks_dstdir_txt = profile.getProperty("swdisks_dest_dir", "");
			if(swdisks_dstdir_txt.isEmpty())
				return;
			swdisks_dstdir = new File(swdisks_dstdir_txt);
		}
		ArrayList<File> srcdirs = new ArrayList<>();
		for(String s : profile.getProperty("src_dir", "").split("\\|")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{
			if(!s.isEmpty())
			{
				File f = new File(s);
				if(f.isDirectory())
					srcdirs.add(f);
			}
		}

		ArrayList<Container> unknown = new ArrayList<>();
		for(File dir : srcdirs)
		{
			allscans.add(new DirScan(profile, dir, handler, false));
			if(handler.isCancel())
				throw new BreakException();
		}
		if(profile.machinelist_list.get(0).size() > 0)
		{
			roms_dstscan = dirscan(profile.machinelist_list.get(0), roms_dstdir, unknown, handler);
			if(roms_dstdir.equals(disks_dstdir))
				disks_dstscan = roms_dstscan;
			else
				disks_dstscan = dirscan(profile.machinelist_list.get(0), disks_dstdir, unknown, handler);
			if(handler.isCancel())
				throw new BreakException();
		}
		if(profile.machinelist_list.softwarelist_list.size() > 0)
		{
			AtomicInteger j = new AtomicInteger();
			handler.setProgress2(String.format("%d/%d", j.get(), profile.machinelist_list.softwarelist_list.size()), j.get(), profile.machinelist_list.softwarelist_list.size()); //$NON-NLS-1$
			for(SoftwareList sl : profile.machinelist_list.softwarelist_list.getFilteredStream().collect(Collectors.toList()))
			{
				File sldir = new File(swroms_dstdir, sl.name);
				if(!sldir.exists())
					sldir.mkdirs();
				if(sldir.isDirectory())
					swroms_dstscans.put(sl.name, dirscan(sl, sldir, unknown, handler));
				if(swroms_dstdir.equals(swdisks_dstdir))
					swdisks_dstscans = swroms_dstscans;
				else
				{
					sldir = new File(swdisks_dstdir, sl.name);
					if(!sldir.exists())
						sldir.mkdirs();
					if(sldir.isDirectory())
						swdisks_dstscans.put(sl.name, dirscan(sl, sldir, unknown, handler));
				}
				handler.setProgress2(String.format("%d/%d (%s)", j.incrementAndGet(), profile.machinelist_list.softwarelist_list.size(), sl.name), j.get(), profile.machinelist_list.softwarelist_list.size()); //$NON-NLS-1$
				if(handler.isCancel())
					throw new BreakException();
			}
			handler.setProgress2(null, null);
			for(File f : swroms_dstdir.listFiles())
			{
				if(!swroms_dstscans.containsKey(f.getName()))
					unknown.add(f.isDirectory() ? new Directory(f, (Machine) null) : new Archive(f, (Machine) null));
				if(handler.isCancel())
					throw new BreakException();
			}
			if(!swroms_dstdir.equals(swdisks_dstdir))
			{
				for(File f : swdisks_dstdir.listFiles())
				{
					if(!swdisks_dstscans.containsKey(f.getName()))
						unknown.add(f.isDirectory() ? new Directory(f, (Machine) null) : new Archive(f, (Machine) null));
					if(handler.isCancel())
						throw new BreakException();
				}
			}
		}

		try
		{
			if(!ignore_unknown_containers)
			{
				unknown.forEach((c) -> {
					report.add(new ContainerUnknown(c));
					delete_actions.add(new DeleteContainer(c, format));
				});
			}
			profile.suspicious_crc.forEach((crc) -> report.add(new RomSuspiciousCRC(crc)));

			AtomicInteger i = new AtomicInteger();
			AtomicInteger j = new AtomicInteger();
			handler.setProgress(Messages.getString("Scan.SearchingForFixes"), i.get(), profile.machinelist_list.get(0).size() + profile.machinelist_list.softwarelist_list.stream().flatMapToInt(sl -> IntStream.of(sl.size())).sum()); //$NON-NLS-1$
			handler.setProgress2(String.format("%d/%d", j.get(), profile.machinelist_list.softwarelist_list.size() + 1), j.get(), profile.machinelist_list.softwarelist_list.size() + 1); //$NON-NLS-1$
			if(profile.machinelist_list.get(0).size() > 0)
			{
				handler.setProgress2(String.format("%d/%d", j.incrementAndGet(), profile.machinelist_list.softwarelist_list.size() + 1), j.get(), profile.machinelist_list.softwarelist_list.size() + 1); //$NON-NLS-1$
				profile.machinelist_list.get(0).forEach(Machine::resetCollisionMode);
				profile.machinelist_list.get(0).getFilteredStream().forEach(m -> {
					handler.setProgress(null, i.incrementAndGet(), null, m.getFullName());
					scanWare(m);
					if(handler.isCancel())
						throw new BreakException();
				});
			}
			if(profile.machinelist_list.softwarelist_list.size() > 0)
			{
				profile.machinelist_list.softwarelist_list.getFilteredStream().forEach(sl -> {
					handler.setProgress2(String.format("%d/%d (%s)", j.incrementAndGet(), profile.machinelist_list.softwarelist_list.size() + 1, sl.name), j.get(), profile.machinelist_list.softwarelist_list.size() + 1); //$NON-NLS-1$
					roms_dstscan = swroms_dstscans.get(sl.name);
					disks_dstscan = swdisks_dstscans.get(sl.name);
					sl.getFilteredStream().forEach(Software::resetCollisionMode);
					sl.getFilteredStream().forEach(s -> {
						handler.setProgress(null, i.incrementAndGet(), null, s.getFullName());
						scanWare(s);
						if(handler.isCancel())
							throw new BreakException();
					});
				});
			}
			handler.setProgress2(null, null);
		}
		catch(BreakException e)
		{
			throw e;
		}
		catch(Throwable e)
		{
			Log.err("Other Exception when listing", e); //$NON-NLS-1$
		}
		finally
		{
			handler.setProgress(Messages.getString("Profile.SavingCache"), -1); //$NON-NLS-1$
			report.write();
			report.flush();
			if(MainFrame.profile_viewer != null)
				MainFrame.profile_viewer.reload(); // update entries in profile viewer
			profile.nfo.stats.scanned = new Date();
			profile.nfo.stats.haveSets = (profile.machinelist_list.softwarelist_list.size() > 0 ? profile.machinelist_list.softwarelist_list : profile.machinelist_list).stream().mapToLong(AnywareList::countHave).sum();
			profile.nfo.stats.haveRoms = (profile.machinelist_list.softwarelist_list.size() > 0 ? profile.machinelist_list.softwarelist_list : profile.machinelist_list).stream().flatMap(l -> l.stream()).mapToLong(Anyware::countHaveRoms).sum();
			profile.nfo.stats.haveDisks = (profile.machinelist_list.softwarelist_list.size() > 0 ? profile.machinelist_list.softwarelist_list : profile.machinelist_list).stream().flatMap(l -> l.stream()).mapToLong(Anyware::countHaveDisks).sum();
			profile.nfo.save();
			profile.save(); // save again profile cache with scan entity status
		}

		actions.add(create_actions);
		actions.add(rename_before_actions);
		actions.add(add_actions);
		actions.add(duplicate_actions);
		actions.add(delete_actions);
		actions.add(rename_after_actions);
		actions.add(tzip_actions.values());

	}

	private DirScan dirscan(AnywareList<?> wl, File dstdir, List<Container> unknown, ProgressHandler handler)
	{
		DirScan dstscan;
		allscans.add(dstscan = new DirScan(profile, dstdir, handler, true));
		for(Container c : dstscan.containers)
		{
			if(c.getType() == Container.Type.UNK)
				unknown.add(c);
			else if(!wl.containsName(FilenameUtils.getBaseName(c.file.toString())))
				unknown.add(c);
		}
		return dstscan;
	}

	private void scanWare(Anyware ware)
	{
		SubjectSet report_subject = new SubjectSet(ware);

		boolean missing_set = true;
		Directory directory = new Directory(new File(disks_dstscan.dir, ware.getDest(merge_mode, implicit_merge).getName()), ware);
		Container archive = new Archive(new File(roms_dstscan.dir, ware.getDest(merge_mode, implicit_merge).getName() + format.getExt()), ware);
		if(format.getExt().isDir())
			archive = new Directory(new File(roms_dstscan.dir, ware.getDest(merge_mode, implicit_merge).getName()), ware);
		List<Rom> roms = ware.filterRoms(merge_mode, hash_collision_mode);
		List<Disk> disks = ware.filterDisks(merge_mode, hash_collision_mode);
		if(!scanRoms(ware, roms, archive, report_subject))
			missing_set = false;
		if(!scanDisks(ware, disks, directory, report_subject))
			missing_set = false;
		if(roms.size() == 0 && disks.size() == 0)
		{
			if(!(merge_mode.isMerge() && ware.isClone()))
			{
				if(!missing_set)
					report_subject.setUnneeded();
				else
					report_subject.setFound();
			}
			missing_set = false;
		}
		else if(create_mode && report_subject.getStatus() == Status.UNKNOWN)
			report_subject.setMissing();
		if(!ignore_unneeded_containers)
		{
			removeUnneededClone(ware, disks, roms);
			removeOtherFormats(ware);
		}
		prepTZip(report_subject, archive, ware, roms);
		if(missing_set)
			report.stats.missing_set_cnt++;
		if(report_subject.getStatus() != Status.UNKNOWN)
			report.add(report_subject);

	}

	public void removeUnneededClone(Anyware ware, List<Disk> disks, List<Rom> roms)
	{
		if(merge_mode.isMerge() && ware.isClone())
		{
			if(format == FormatOptions.DIR && disks.size() == 0 && roms.size() == 0)
			{
				Arrays.asList(roms_dstscan.containers_byname.get(ware.getName()), disks_dstscan.containers_byname.get(ware.getName())).forEach(c -> {
					if(c != null)
					{
						report.add(new ContainerUnneeded(c));
						delete_actions.add(new DeleteContainer(c, format));
					}
				});
			}
			else if(disks.size() == 0)
			{
				Container c = disks_dstscan.containers_byname.get(ware.getName());
				if(c != null)
				{
					if(c != null)
					{
						report.add(new ContainerUnneeded(c));
						delete_actions.add(new DeleteContainer(c, format));
					}
				}
			}
			if(format != FormatOptions.DIR && roms.size() == 0)
			{
				Container c = roms_dstscan.containers_byname.get(ware.getName() + format.getExt());
				if(c != null)
				{
					report.add(new ContainerUnneeded(c));
					delete_actions.add(new DeleteContainer(c, format));
				}
			}
		}
	}

	public void removeOtherFormats(Anyware ware)
	{
		format.getExt().allExcept().forEach((e) -> { // set other formats with the same set name as unneeded
			Container c = roms_dstscan.containers_byname.get(ware.getName() + e);
			if(c != null)
			{
				report.add(new ContainerUnneeded(c));
				delete_actions.add(new DeleteContainer(c, format));
			}
		});
	}

	private void prepTZip(SubjectSet report_subject, Container archive, Anyware ware, List<Rom> roms)
	{
		if(format == FormatOptions.TZIP)
		{
			if(!merge_mode.isMerge() || !ware.isClone())
			{
				if(!report_subject.isMissing() && !report_subject.isUnneeded() && roms.size()>0)
				{
					Container tzipcontainer = null;
					Container container = roms_dstscan.containers_byname.get(ware.getDest(merge_mode, implicit_merge).getName() + format.getExt());
					if(container != null)
					{
						if(container.lastTZipCheck < container.file.lastModified())
							tzipcontainer = container;
						else if(!container.lastTZipStatus.contains(TrrntZipStatus.ValidTrrntzip))
							tzipcontainer = container;
							
					}
					else if(create_mode)
						tzipcontainer = archive;
					if(tzipcontainer != null)
						tzip_actions.put(tzipcontainer.file.getAbsolutePath(), new TZipContainer(tzipcontainer, format));
				}
			}
		}
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean scanDisks(Anyware ware, List<Disk> disks, Directory directory, SubjectSet report_subject)
	{
		boolean missing_set = true;
		Container container;
		if(null != (container = disks_dstscan.containers_byname.get(ware.getDest(merge_mode, implicit_merge).getName())))
		{
			missing_set = false;
			if(disks.size() > 0)
			{
				report_subject.setFound();

				ArrayList<Entry> disks_found = new ArrayList<>();
				Map<String, Disk> disks_byname = Disk.getDisksByName(disks);
				OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null, duplicate_set = null;
				for(Disk disk : disks)
				{
					disk.own_status = EntityStatus.KO;
					Entry found_entry = null;
					Map<String, Entry> entries_byname = container.getEntriesByName();
					for(Entry candidate_entry : container.getEntries())
					{
						if(candidate_entry.equals(disk))
						{
							if(!disk.getName().equals(candidate_entry.getName())) // but this entry name does not match the rom name
							{
								Disk another_disk;
								if(null != (another_disk = disks_byname.get(candidate_entry.getName())) && candidate_entry.equals(another_disk)) // and entry name correspond to another disk name in the set
								{
									if(entries_byname.containsKey(disk.getName()))
									{
										// report_w.println("["+m.name+"] "+d.getName()+" == "+e.file);
									}
									else
									{
										// we must duplicate
										report_subject.add(new EntryMissingDuplicate(disk, candidate_entry));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, directory, format)).addAction(new DuplicateEntry(disk.getName(), candidate_entry));
										found_entry = candidate_entry;
									}
								}
								else
								{
									if(!entries_byname.containsKey(disk.getName())) // and disk name is not in the entries
									{
										report_subject.add(new EntryWrongName(disk, candidate_entry));
										(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format)).addAction(new RenameEntry(candidate_entry));
										(rename_after_set = OpenContainer.getInstance(rename_after_set, directory, format)).addAction(new RenameEntry(disk.getName(), candidate_entry));
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
						else if(disk.getName().equals(candidate_entry.getName()))
						{
							report_subject.add(new EntryWrongHash(disk, candidate_entry));
							// found = e;
							break;
						}
					}
					if(found_entry == null)
					{
						report.stats.missing_disks_cnt++;
						for(DirScan scan : allscans)
						{
							if(null != (found_entry = scan.find_byhash(disk)))
							{
								report_subject.add(new EntryAdd(disk, found_entry));
								(add_set = OpenContainer.getInstance(add_set, directory, format)).addAction(new AddEntry(disk, found_entry));
								break;
							}
						}
						if(found_entry == null)
							report_subject.add(new EntryMissing(disk));
					}
					else
					{
						disk.own_status = EntityStatus.OK;
						report_subject.add(new EntryOK(disk));
						// report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
						disks_found.add(found_entry);
					}
				}
				if(!ignore_unneeded_entries)
				{
					List<Entry> unneeded = container.getEntries().stream().filter(not(new HashSet<>(disks_found)::contains)).collect(Collectors.toList());
					for(Entry unneeded_entry : unneeded)
					{
						report_subject.add(new EntryUnneeded(unneeded_entry));
						(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format)).addAction(new RenameEntry(unneeded_entry));
						(delete_set = OpenContainer.getInstance(delete_set, directory, format)).addAction(new DeleteEntry(unneeded_entry));
					}
				}
				ContainerAction.addToList(rename_before_actions, rename_before_set);
				ContainerAction.addToList(add_actions, add_set);
				ContainerAction.addToList(duplicate_actions, duplicate_set);
				ContainerAction.addToList(delete_actions, delete_set);
				ContainerAction.addToList(rename_after_actions, rename_after_set);
			}
		}
		else
		{
			for(Disk disk : disks)
				disk.own_status = EntityStatus.KO;
			if(create_mode)
			{
				if(disks.size() > 0)
				{
					int disks_found = 0;
					boolean partial_set = false;
					CreateContainer createset = null;
					for(Disk disk : disks)
					{
						report.stats.missing_disks_cnt++;
						Entry found_entry = null;
						for(DirScan scan : allscans)
						{
							if(null != (found_entry = scan.find_byhash(disk)))
							{
								report_subject.add(new EntryAdd(disk, found_entry));
								(createset = CreateContainer.getInstance(createset, directory, format)).addAction(new AddEntry(disk, found_entry));
								disks_found++;
								break;
							}
						}
						if(found_entry == null)
						{
							report_subject.add(new EntryMissing(disk));
							partial_set = true;
						}
					}
					if(disks_found > 0)
					{
						if(!createfull_mode || !partial_set)
						{
							report_subject.setCreateFull();
							if(partial_set)
								report_subject.setCreate();
							ContainerAction.addToList(create_actions, createset);
						}
					}
				}
			}
		}
		return missing_set;
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean scanRoms(Anyware ware, List<Rom> roms, Container archive, SubjectSet report_subject)
	{
		boolean missing_set = true;
		Container container;
		if(null != (container = roms_dstscan.containers_byname.get(ware.getDest(merge_mode, implicit_merge).getName() + format.getExt())))
		{
			missing_set = false;
			if(roms.size() > 0)
			{
				report_subject.setFound();

				ArrayList<Entry> roms_found = new ArrayList<>();
				Map<String, Rom> roms_byname = Rom.getRomsByName(roms);
				OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null, duplicate_set = null;
				for(Rom rom : roms)
				{
					rom.own_status = EntityStatus.KO;
					Entry found_entry = null;
					Map<String, Entry> entries_byname = container.getEntriesByName();
					for(Entry candidate_entry : container.getEntries())
					{
						String efile = candidate_entry.getName();
						if(candidate_entry.equals(rom)) // The entry 'e' match hash from rom 'r'
						{
							if(!rom.getName().equals(efile)) // but this entry name does not match the rom name
							{
								Rom another_rom;
								if(null != (another_rom = roms_byname.get(efile)) && candidate_entry.equals(another_rom)) // and entry name correspond to another rom name in the set
								{
									if(entries_byname.containsKey(rom.getName())) // and rom name is in the entries
									{
										// report_w.println("[" + m.name + "] " + r.getName() + " == " + e.file);
									}
									else
									{
										// we must duplicate
										report_subject.add(new EntryMissingDuplicate(rom, candidate_entry));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format)).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										found_entry = candidate_entry;
										break;
									}
								}
								else
								{
									if(!entries_byname.containsKey(rom.getName())) // and rom name is not in the entries
									{
										report_subject.add(new EntryWrongName(rom, candidate_entry));
										// (rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format)).addAction(new RenameEntry(e));
										// (rename_after_set = OpenContainer.getInstance(rename_after_set, archive, format)).addAction(new RenameEntry(r.getName(), e));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format)).addAction(new DuplicateEntry(rom.getName(), candidate_entry));
										(delete_set = OpenContainer.getInstance(delete_set, archive, format)).addAction(new DeleteEntry(candidate_entry));
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
						else if(rom.getName().equals(efile))
						{
							report_subject.add(new EntryWrongHash(rom, candidate_entry));
							break;
						}
					}
					if(found_entry == null)
					{
						report.stats.missing_roms_cnt++;
						for(DirScan scan : allscans)
						{
							if(null != (found_entry = scan.find_byhash(profile, rom)))
							{
								report_subject.add(new EntryAdd(rom, found_entry));
								(add_set = OpenContainer.getInstance(add_set, archive, format)).addAction(new AddEntry(rom, found_entry));
								// roms_found.add(found);
								break;
							}
						}
						if(found_entry == null)
							report_subject.add(new EntryMissing(rom));
					}
					else
					{
						// report_w.println("[" + m.name + "] " + r.getName() + " (" + found.file + ") OK ");
						rom.own_status = EntityStatus.OK;
						report_subject.add(new EntryOK(rom));
						roms_found.add(found_entry);
					}
				}
				if(!ignore_unneeded_entries)
				{
					List<Entry> unneeded = container.getEntries().stream().filter(not(new HashSet<>(roms_found)::contains)).collect(Collectors.toList());
					for(Entry unneeded_entry : unneeded)
					{
						report_subject.add(new EntryUnneeded(unneeded_entry));
						(rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format)).addAction(new RenameEntry(unneeded_entry));
						(delete_set = OpenContainer.getInstance(delete_set, archive, format)).addAction(new DeleteEntry(unneeded_entry));
					}
				}
				ContainerAction.addToList(rename_before_actions, rename_before_set);
				ContainerAction.addToList(add_actions, add_set);
				ContainerAction.addToList(duplicate_actions, duplicate_set);
				ContainerAction.addToList(delete_actions, delete_set);
				ContainerAction.addToList(rename_after_actions, rename_after_set);
			}
		}
		else
		{
			for(Rom rom : roms)
				rom.own_status = EntityStatus.KO;
			if(create_mode)
			{
				if(roms.size() > 0)
				{
					int roms_found = 0;
					boolean partial_set = false;
					CreateContainer createset = null;
					for(Rom rom : roms)
					{
						report.stats.missing_roms_cnt++;
						Entry entry_found = null;
						for(DirScan scan : allscans)
						{
							if(null != (entry_found = scan.find_byhash(profile, rom)))
							{
								report_subject.add(new EntryAdd(rom, entry_found));
								(createset = CreateContainer.getInstance(createset, archive, format)).addAction(new AddEntry(rom, entry_found));
								roms_found++;
								break;
							}
						}
						if(entry_found == null)
						{
							report_subject.add(new EntryMissing(rom));
							partial_set = true;
						}
					}
					if(roms_found > 0)
					{
						if(!createfull_mode || !partial_set)
						{
							report_subject.setCreateFull();
							if(partial_set)
								report_subject.setCreate();
							ContainerAction.addToList(create_actions, createset);
						}
					}
				}
			}
		}
		return missing_set;
	}

	private static <T> Predicate<T> not(Predicate<T> predicate)
	{
		return predicate.negate();
	}

}
