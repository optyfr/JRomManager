package jrm.profiler.scan;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import jrm.Messages;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.profiler.Profile;
import jrm.profiler.data.*;
import jrm.profiler.fix.actions.*;
import jrm.profiler.report.*;
import jrm.profiler.report.SubjectSet.Status;
import jrm.profiler.scan.options.FormatOptions;
import jrm.profiler.scan.options.HashCollisionOptions;
import jrm.profiler.scan.options.MergeOptions;
import jrm.ui.ProgressHandler;

public class Scan
{
	public ArrayList<ArrayList<jrm.profiler.fix.actions.ContainerAction>> actions = new ArrayList<>();
	public static Report report = new Report();
	private Profile profile;

	private MergeOptions merge_mode;
	private FormatOptions format;
	private boolean create_mode;
	private boolean createfull_mode;
	private DirScan dstscan;
	private List<DirScan> allscans = new ArrayList<>();

	private ArrayList<jrm.profiler.fix.actions.ContainerAction> create_actions = new ArrayList<>();
	private ArrayList<jrm.profiler.fix.actions.ContainerAction> rename_before_actions = new ArrayList<>();
	private ArrayList<jrm.profiler.fix.actions.ContainerAction> add_actions = new ArrayList<>();
	private ArrayList<jrm.profiler.fix.actions.ContainerAction> delete_actions = new ArrayList<>();
	private ArrayList<jrm.profiler.fix.actions.ContainerAction> rename_after_actions = new ArrayList<>();
	private ArrayList<jrm.profiler.fix.actions.ContainerAction> duplicate_actions = new ArrayList<>();

	public Scan(Profile profile, File dstdir, List<File> srcdirs, ProgressHandler handler) throws BreakException
	{
		this.profile = profile;
		report.setProfile(profile);
		format = FormatOptions.valueOf(profile.getProperty("format", FormatOptions.ZIP.toString())); //$NON-NLS-1$
		merge_mode = MergeOptions.valueOf(profile.getProperty("merge_mode", MergeOptions.SPLIT.toString())); //$NON-NLS-1$
		create_mode = profile.getProperty("create_mode", true); //$NON-NLS-1$
		createfull_mode = profile.getProperty("createfull_mode", true); //$NON-NLS-1$
		HashCollisionOptions hash_collision_mode = HashCollisionOptions.valueOf(profile.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString())); //$NON-NLS-1$

		dstscan = new DirScan(profile, dstdir, handler, true);
		for(File dir : srcdirs)
			allscans.add(new DirScan(profile, dir, handler, false));
		allscans.add(dstscan);

		ArrayList<Container> unknown = new ArrayList<>();
		for(Container c : dstscan.containers)
		{
			if(c.getType() == Container.Type.UNK)
				unknown.add(c);
			else if(!profile.machines_byname.containsKey(FilenameUtils.getBaseName(c.file.toString())))
				unknown.add(c);
		}

		try
		{
			unknown.forEach((c) -> {
				report.add(new ContainerUnknown(c));
				delete_actions.add(new DeleteContainer(c, format));
			});
			profile.suspicious_crc.forEach((crc) -> report.add(new RomSuspiciousCRC(crc)));

			int i = 0;
			handler.setProgress(Messages.getString("Scan.SearchingForFixes"), i, profile.machines.size()); //$NON-NLS-1$
			profile.machines.forEach(Machine::resetCollisionMode);
			for(Machine m : profile.machines)
			{
				SubjectSet report_subject = new SubjectSet(m);
		//		report.add(report_subject);
				
				boolean missing_set = true;
				Directory directory = new Directory(new File(dstdir, m.getDestMachine(merge_mode).name), m);
				Container archive = new Archive(new File(dstdir, m.getDestMachine(merge_mode).name + format.getExt()), m);
				if(format.getExt().isDir())
					archive = directory;
				List<Rom> roms = m.filterRoms(merge_mode, hash_collision_mode);
				List<Disk> disks = m.filterDisks(merge_mode, hash_collision_mode);
				if(!scanRoms(m, roms, archive, report_subject))
					missing_set = false;
				if(!scanDisks(m, disks, directory, report_subject))
					missing_set = false;
				if(roms.size()==0 && disks.size()==0)
				{
					if(!missing_set)
						report_subject.setUnneeded();
					else
						report_subject.setFound();
				}
				if(format == FormatOptions.DIR)
				{
					if(disks.size() == 0 && roms.size() == 0)
					{
						Machine m2 = m;
						if(!(merge_mode.isMerge() && m.isClone()))
							m2 = m.getDestMachine(merge_mode);
						Container c2 = dstscan.containers_byname.get(m2.name);
						if(c2 != null)
						{
							Optional.ofNullable(report.findSubject(m2)).ifPresent(s->((SubjectSet)s).setUnneeded());
							delete_actions.add(new DeleteContainer(c2, format));
						}
					}
				}
				else
				{
					if(disks.size() == 0)
					{
						Machine m2 = m;
						if(!(merge_mode.isMerge() && m.isClone()))
							m2 = m.getDestMachine(merge_mode);
						Container c2 = dstscan.containers_byname.get(m2.name);
						if(c2 != null)
						{
							Optional.ofNullable(report.findSubject(m2)).ifPresent(s->((SubjectSet)s).setUnneeded());
							delete_actions.add(new DeleteContainer(c2, format));
						}
					}
					if(roms.size() == 0)
					{
						Machine m2 = m;
						if(!(merge_mode.isMerge() && m.isClone()))
							m2 = m.getDestMachine(merge_mode);
						Container c2 = dstscan.containers_byname.get(m2.name + format.getExt());
						if(c2 != null)
						{
							Optional.ofNullable(report.findSubject(m2)).ifPresent(s->((SubjectSet)s).setUnneeded());
							delete_actions.add(new DeleteContainer(c2, format));
						}
					}
				}
				format.getExt().allExcept().forEach((e) -> {
					Container c2 = dstscan.containers_byname.get(m.name + e);
					if(c2 != null)
					{
						Optional.ofNullable(report.findSubject(m)).ifPresent(s->((SubjectSet)s).setUnneeded());
						delete_actions.add(new DeleteContainer(c2, format));
					}
				});
				handler.setProgress(null, ++i);
				if(handler.isCancel())
					throw new BreakException();
				if(roms.size() == 0 && disks.size() == 0)
					missing_set = false;
				else if(create_mode && report_subject.getStatus()==Status.UNKNOWN)
					report_subject.setMissing();
				if(missing_set)
					report.stats.missing_set_cnt++;
				if(report_subject.getStatus()!=Status.UNKNOWN)
					report.add(report_subject);
					
			}
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
			report.write();
			report.flush();
		}

		
		actions.add(create_actions);
		actions.add(rename_before_actions);
		actions.add(add_actions);
		actions.add(duplicate_actions);
		actions.add(delete_actions);
		actions.add(rename_after_actions);

	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean scanDisks(Machine m, List<Disk> disks, Directory directory, SubjectSet report_subject)
	{
		boolean missing_set = true;
		Container c;
		if(null != (c = dstscan.containers_byname.get(m.getDestMachine(merge_mode).name)))
		{
			missing_set = false;
			if(disks.size() > 0)
			{
				report_subject.setFound();
				
				ArrayList<Entry> disks_found = new ArrayList<>();
				Map<String, Disk> disks_byname = Disk.getDisksByName(disks);
				OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null, duplicate_set = null;
				for(Disk d : disks)
				{
					Entry found = null;
					Map<String, Entry> entries_byname = c.getEntriesByName();
					for(Entry e : c.getEntries())
					{
						if(e.equals(d))
						{
							if(!d.getName().equals(e.getName()))
							{
								if(disks_byname.containsKey(new File(e.file).getName()))
								{
									if(entries_byname.containsKey(d.getName()))
									{
										// report_w.println("["+m.name+"] "+d.getName()+" == "+e.file);
									}
									else
									{
										// we must duplicate
										report_subject.add(new EntryMissingDuplicate(d,e));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, directory, format)).addAction(new DuplicateEntry(d.getName(), e));
										found = e;
									}
								}
								else
								{
									report_subject.add(new EntryWrongName(d,e));
									(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format)).addAction(new RenameEntry(e));
									(rename_after_set = OpenContainer.getInstance(rename_after_set, directory, format)).addAction(new RenameEntry(d.getName(), e));
									found = e;
									break;
								}
							}
							else
							{
								found = e;
								break;
							}
						}
						else if(d.getName().equals(e.getName()))
						{
							report_subject.add(new EntryWrongHash(d,e));
							// found = e;
							break;
						}
					}
					if(found == null)
					{
						report.stats.missing_disks_cnt++;
						for(DirScan scan : allscans)
						{
							if(null != (found = scan.find_byhash(d)))
							{
								report_subject.add(new EntryAdd(d,found));
								(add_set = OpenContainer.getInstance(add_set, directory, format)).addAction(new AddEntry(d, found));
								break;
							}
						}
						if(found == null)
							report_subject.add(new EntryMissing(d));
					}
					else
					{
						report_subject.add(new EntryOK(d));
						// report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
						disks_found.add(found);
					}
				}
				List<Entry> unneeded = c.getEntries().stream().filter(not(new HashSet<>(disks_found)::contains)).collect(Collectors.toList());
				for(Entry e : unneeded)
				{
					report_subject.add(new EntryUnneeded(e));
					(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format)).addAction(new RenameEntry(e));
					(delete_set = OpenContainer.getInstance(delete_set, directory, format)).addAction(new DeleteEntry(e));
				}
				ContainerAction.addToList(rename_before_actions, rename_before_set);
				ContainerAction.addToList(add_actions, add_set);
				ContainerAction.addToList(duplicate_actions, duplicate_set);
				ContainerAction.addToList(delete_actions, delete_set);
				ContainerAction.addToList(rename_after_actions, rename_after_set);
			}
		}
		else if(create_mode)
		{
			if(disks.size() > 0)
			{
				int disks_found = 0;
				boolean partial = false;
				CreateContainer createset = null;
				for(Disk d : disks)
				{
					report.stats.missing_disks_cnt++;
					Entry found = null;
					for(DirScan scan : allscans)
					{
						if(null != (found = scan.find_byhash(d)))
						{
							report_subject.add(new EntryAdd(d, found));
							(createset = CreateContainer.getInstance(createset, directory, format)).addAction(new AddEntry(d, found));
							disks_found++;
							break;
						}
					}
					if(found == null)
					{
						report_subject.add(new EntryMissing(d));
						partial = true;
					}
				}
				if(disks_found > 0)
				{
					if(!createfull_mode || !partial)
					{
						report_subject.setCreateFull();
						if(partial)
							report_subject.setCreate();
						ContainerAction.addToList(create_actions, createset);
					}
				}
			}
		}
		return missing_set;
	}

	@SuppressWarnings("unlikely-arg-type")
	private boolean scanRoms(Machine m, List<Rom> roms, Container archive, SubjectSet report_subject)
	{
		boolean missing_set = true;
		Container c;
		if(null != (c = dstscan.containers_byname.get(m.getDestMachine(merge_mode).name + format.getExt())))
		{
			missing_set = false;
			if(roms.size() > 0)
			{
				report_subject.setFound();

				ArrayList<Entry> roms_found = new ArrayList<>();
				Map<String, Rom> roms_byname = Rom.getRomsByName(roms);
				OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null, duplicate_set = null;
				for(Rom r : roms)
				{
					Entry found = null;
					Map<String, Entry> entries_byname = c.getEntriesByName();
					for(Entry e : c.getEntries())
					{
						String efile = e.getName();
						if(e.equals(r)) // The entry 'e' match hash from rom 'r'
						{
							if(!r.getName().equals(efile)) // but this entry name does not match the rom name
							{
								Rom another_rom;
								if(null != (another_rom = roms_byname.get(efile)) && e.equals(another_rom)) // and entry name correspond to another rom name in the set
								{
									if(entries_byname.containsKey(r.getName())) // and rom name is in the entries
									{
										// report_w.println("[" + m.name + "] " + r.getName() + " == " + e.file);
									}
									else
									{
										// we must duplicate
										report_subject.add(new EntryMissingDuplicate(r, e));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format)).addAction(new DuplicateEntry(r.getName(), e));
										found = e;
										break;
									}
								}
								else
								{
									if(!entries_byname.containsKey(r.getName())) // and rom name is not in the entries
									{
										report_subject.add(new EntryWrongName(r, e));
										// (rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format)).addAction(new RenameEntry(e));
										// (rename_after_set = OpenContainer.getInstance(rename_after_set, archive, format)).addAction(new RenameEntry(r.getName(), e));
										(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format)).addAction(new DuplicateEntry(r.getName(), e));
										(delete_set = OpenContainer.getInstance(delete_set, archive, format)).addAction(new DeleteEntry(e));
										found = e;
										break;
									}
								}
							}
							else
							{
								found = e;
								break;
							}
						}
						else if(r.getName().equals(efile))
						{
							report_subject.add(new EntryWrongHash(r, e));
							// found = e;
							break;
						}
					}
					if(found == null)
					{
						report.stats.missing_roms_cnt++;
						for(DirScan scan : allscans)
						{
							if(null != (found = scan.find_byhash(profile, r)))
							{
								report_subject.add(new EntryAdd(r, found));
								(add_set = OpenContainer.getInstance(add_set, archive, format)).addAction(new AddEntry(r, found));
								// roms_found.add(found);
								break;
							}
						}
						if(found == null)
							report_subject.add(new EntryMissing(r));
					}
					else
					{
						// report_w.println("[" + m.name + "] " + r.getName() + " (" + found.file + ") OK ");
						report_subject.add(new EntryOK(r));
						roms_found.add(found);
					}
				}
				List<Entry> unneeded = c.getEntries().stream().filter(not(new HashSet<>(roms_found)::contains)).collect(Collectors.toList());
				for(Entry e : unneeded)
				{
					report_subject.add(new EntryUnneeded(e));
					(rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format)).addAction(new RenameEntry(e));
					(delete_set = OpenContainer.getInstance(delete_set, archive, format)).addAction(new DeleteEntry(e));
				}
				ContainerAction.addToList(rename_before_actions, rename_before_set);
				ContainerAction.addToList(add_actions, add_set);
				ContainerAction.addToList(duplicate_actions, duplicate_set);
				ContainerAction.addToList(delete_actions, delete_set);
				ContainerAction.addToList(rename_after_actions, rename_after_set);
			}
		}
		else if(create_mode)
		{
			if(roms.size() > 0)
			{
				int roms_found = 0;
				boolean partial = false;
				CreateContainer createset = null;
				for(Rom r : roms)
				{
					report.stats.missing_roms_cnt++;
					Entry found = null;
					for(DirScan scan : allscans)
					{
						if(null != (found = scan.find_byhash(profile, r)))
						{
							report_subject.add(new EntryAdd(r, found));
							(createset = CreateContainer.getInstance(createset, archive, format)).addAction(new AddEntry(r, found));
							roms_found++;
							break;
						}
					}
					if(found == null)
					{
						report_subject.add(new EntryMissing(r));
						partial = true;
					}
				}
				if(roms_found > 0)
				{
					if(!createfull_mode || !partial)
					{
						report_subject.setCreateFull();
						if(partial)
							report_subject.setCreate();
						ContainerAction.addToList(create_actions, createset);
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
