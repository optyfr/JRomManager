package jrm.profiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import jrm.actions.AddEntry;
import jrm.actions.ContainerAction;
import jrm.actions.CreateContainer;
import jrm.actions.DeleteContainer;
import jrm.actions.DeleteEntry;
import jrm.actions.DuplicateEntry;
import jrm.actions.OpenContainer;
import jrm.actions.RenameEntry;
import jrm.data.Archive;
import jrm.data.Container;
import jrm.data.Directory;
import jrm.data.Disk;
import jrm.data.Entry;
import jrm.data.Machine;
import jrm.data.Rom;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.profiler.scan.FormatOptions;
import jrm.profiler.scan.HashCollisionOptions;
import jrm.profiler.scan.MergeOptions;
import jrm.ui.ProgressHandler;

public class Scan
{

	public ArrayList<ArrayList<jrm.actions.ContainerAction>> actions = new ArrayList<>();

	public Scan(Profile profile, File dstdir, List<File> srcdirs, ProgressHandler handler) throws BreakException
	{
		FormatOptions format = FormatOptions.valueOf(profile.getProperty("format", FormatOptions.ZIP.toString()));
		MergeOptions merge_mode = MergeOptions.valueOf(profile.getProperty("merge_mode", MergeOptions.SPLIT.toString()));
		boolean create_mode = profile.getProperty("create_mode", true);
		boolean createfull_mode = profile.getProperty("createfull_mode", true);
		HashCollisionOptions hash_collision_mode = HashCollisionOptions.valueOf(profile.getProperty("hash_collision_mode", HashCollisionOptions.SINGLEFILE.toString()));

		DirScan dstscan = new DirScan(profile, dstdir, handler, true);
		List<DirScan> allscans = new ArrayList<>();
		for (File dir : srcdirs)
			allscans.add(new DirScan(profile, dir, handler, false));
		allscans.add(dstscan);

		ArrayList<Container> unknown = new ArrayList<>();
		for (Container c : dstscan.containers)
		{
			if (c.getType() == Container.Type.UNK)
				unknown.add(c);
			else if (!profile.machines_byname.containsKey(FilenameUtils.getBaseName(c.file.toString())))
				unknown.add(c);
		}

		ArrayList<jrm.actions.ContainerAction> create_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> rename_before_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> add_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> delete_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> rename_after_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> duplicate_actions = new ArrayList<>();

		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File reportdir = new File(workdir, "reports");
		reportdir.mkdirs();
		File report = new File(reportdir, "report.log");
		try (PrintWriter report_w = new PrintWriter(report))
		{
			unknown.forEach((c) -> {
				report_w.println("Uneeded " + (c.getType() == Container.Type.DIR ? "Directory" : "File") + " : " + c.file);
				delete_actions.add(new DeleteContainer(c, format));
			});
			profile.suspicious_crc.forEach((crc) -> report_w.println("Detected suspicious CRC : " + crc + " (SHA1 has been calculated for theses roms)"));

			int i = 0;
			int missing_set_cnt = 0;
			int missing_roms_cnt = 0;
			int missing_disks_cnt = 0;
			handler.setProgress("Searching for fixes...", i, profile.machines.size());
			profile.machines.forEach(Machine::resetCollisionMode);
			for (Machine m : profile.machines)
			{
				boolean missing_set = true;
				Container c;
				List<Disk> disks = m.filterDisks(merge_mode, hash_collision_mode);
				List<Rom> roms = m.filterRoms(merge_mode, hash_collision_mode);
				Directory directory = new Directory(new File(dstdir, m.getDestMachine(merge_mode).name), m);
				if (null != (c = dstscan.containers_byname.get(m.getDestMachine(merge_mode).name)))
				{
					missing_set = false;
					if (disks.size() > 0)
					{
						ArrayList<Entry> disks_found = new ArrayList<>();
						Map<String, Disk> disks_byname = Disk.getDisksByName(disks);
						OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null, duplicate_set = null;
						for (Disk d : disks)
						{
							Entry found = null;
							Map<String, Entry> entries_byname = c.getEntriesByName();
							for (Entry e : c.getEntries())
							{
								if (e.equals(d))
								{
									if (!d.getName().equals(e.getName()))
									{
										if (disks_byname.containsKey(new File(e.file).getName()))
										{
											if (entries_byname.containsKey(d.getName()))
											{
												// report_w.println("["+m.name+"] "+d.getName()+" == "+e.file);
											}
											else
											{
												// we must duplicate
												report_w.println("[" + m.name + "] duplicate " + e.file + " >>> " + d.getName());
												(duplicate_set = OpenContainer.getInstance(duplicate_set, directory, format)).addAction(new DuplicateEntry(d.getName(), e));
												found = e;
											}
										}
										else
										{
											report_w.println("[" + m.name + "] wrong named rom (" + e.getName() + "->" + d.getName() + ")");
											(rename_before_set = OpenContainer.getInstance(rename_before_set, directory, format)).addAction(new RenameEntry(e));
											(rename_after_set = OpenContainer.getInstance(rename_after_set, directory, format)).addAction(new RenameEntry(d.getName(), e));
											found = e;
											break;
										}
									}
									else
									{
										// report_w.println("["+m.name+"] "+d.getName()+" ("+e.file+") OK ");
										found = e;
										break;
									}
								}
								else if (d.getName().equals(e.getName()))
								{
									if (e.sha1 == null)
										report_w.println("[" + m.name + "] " + e.file + " has wrong md5 (got " + e.md5 + " vs " + d.md5 + ")");
									else
										report_w.println("[" + m.name + "] " + e.file + " has wrong sha1 (got " + e.sha1 + " vs " + d.sha1 + ")");
									// found = e;
									break;
								}
							}
							if (found == null)
							{
								missing_disks_cnt++;
								for (DirScan scan : allscans)
								{
									if (null != (found = scan.find_byhash(d)))
									{
										report_w.println("[" + m.name + "] " + d.getName() + " <- " + found.parent.file.getName() + "@" + found.file);
										(add_set = OpenContainer.getInstance(add_set, directory, format)).addAction(new AddEntry(d, found));
										break;
									}
								}
								if (found == null)
									report_w.println("[" + m.name + "] " + d.getName() + " is missing and not fixable");
							}
							else
							{
								// report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
								disks_found.add(found);
							}
						}
						List<Entry> unneeded = c.getEntries().stream().filter(not(new HashSet<>(disks_found)::contains)).collect(Collectors.toList());
						for (Entry e : unneeded)
						{
							report_w.println("[" + m.name + "] " + e.file + " unneeded (sha1=" + e.sha1 + ")");
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
					if (disks.size() > 0)
					{
						int disks_found = 0;
						boolean partial = false;
						String submsg = "";
						CreateContainer createset = null;
						for (Disk d : disks)
						{
							missing_disks_cnt++;
							Entry found = null;
							for (DirScan scan : allscans)
							{
								if (null != (found = scan.find_byhash(d)))
								{
									submsg += "\t[" + m.name + "] " + d.getName() + " <- " + found.parent.file.getName() + "@" + found.file + "\n";
									(createset = CreateContainer.getInstance(createset, directory, format)).addAction(new AddEntry(d, found));
									disks_found++;
									break;
								}
							}
							if (found == null)
							{
								submsg += "\t[" + m.name + "] " + d.getName() + " is missing and not fixable\n";
								partial = true;
							}
						}
						String msg = "[" + m.name + "] is missing";
						if (disks_found > 0)
						{
							if(!createfull_mode || !partial)
							{
								ContainerAction.addToList(create_actions, createset);
								msg += ", but can " + (partial ? "partially" : "totally") + " be recreated :\n" + submsg;
							}
						}
						report_w.println(msg);
					}
				}
				Container archive = new Archive(new File(dstdir, m.getDestMachine(merge_mode).name + format.getExt()), m);
				if(format.getExt().isDir()) archive = directory;
				if (null != (c = dstscan.containers_byname.get(m.getDestMachine(merge_mode).name + format.getExt())))
				{
					missing_set = false;
					if (roms.size() > 0)
					{
						ArrayList<Entry> roms_found = new ArrayList<>();
						Map<String, Rom> roms_byname = Rom.getRomsByName(roms);
						OpenContainer add_set = null, delete_set = null, rename_before_set = null, rename_after_set = null, duplicate_set = null;
						for (Rom r : roms)
						{
							Entry found = null;
							Map<String, Entry> entries_byname = c.getEntriesByName();
							for (Entry e : c.getEntries())
							{
								String efile = e.getName();
								if (e.equals(r))	// The entry 'e' match hash from rom 'r'
								{
									if (!r.getName().equals(efile))	// but this entry name does not match the rom name
									{
										Rom another_rom;
										if (null!=(another_rom=roms_byname.get(efile)) && e.equals(another_rom))	// and entry name correspond to another rom name in the set
										{
											if (entries_byname.containsKey(r.getName()))	// and rom name is in the entries
											{
										//		report_w.println("[" + m.name + "] " + r.getName() + " == " + e.file);
											}
											else
											{
												// we must duplicate
												report_w.println("[" + m.name + "] duplicate " + e.file + " >>> " + r.getName());
												(duplicate_set = OpenContainer.getInstance(duplicate_set, archive, format)).addAction(new DuplicateEntry(r.getName(), e));
												found = e;
												break;
											}
										}
										else
										{
											if (!entries_byname.containsKey(r.getName()))	// and rom name is not in the entries
											{
												report_w.println("[" + m.name + "] wrong named rom (" + archive.file.getName() + "@" + efile + "->" + r.getName() + ")");
											//	(rename_before_set = OpenContainer.getInstance(rename_before_set, archive, format)).addAction(new RenameEntry(e));
											//	(rename_after_set = OpenContainer.getInstance(rename_after_set, archive, format)).addAction(new RenameEntry(r.getName(), e));
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
								else if (r.getName().equals(efile))
								{
									if (e.md5 == null && e.sha1 == null)
										report_w.println("[" + m.name + "] " + archive.file.getName() + "@" + efile + " has wrong crc (got " + e.crc + " vs " + r.crc + ")");
									else if (e.sha1 == null)
										report_w.println("[" + m.name + "] " + archive.file.getName() + "@" + efile + " has wrong md5 (got " + e.md5 + " vs " + r.md5 + ")");
									else
										report_w.println("[" + m.name + "] " + archive.file.getName() + "@" + efile + " has wrong sha1 (got " + e.sha1 + " vs " + r.sha1 + ")");
									// found = e;
									break;
								}
							}
							if (found == null)
							{
								missing_roms_cnt++;
								for (DirScan scan : allscans)
								{
									if (null != (found = scan.find_byhash(profile,r)))
									{
										report_w.println("[" + m.name + "] " + archive.file.getName() + "@" + r.getName() + " <- " + found.parent.file.getName() + "@" + found.file);
										(add_set = OpenContainer.getInstance(add_set, archive, format)).addAction(new AddEntry(r, found));
										// roms_found.add(found);
										break;
									}
								}
								if (found == null)
									report_w.println("[" + m.name + "] " + archive.file.getName() + "@" + r.getName() + " is missing and not fixable");
							}
							else
							{
							//	report_w.println("[" + m.name + "] " + r.getName() + " (" + found.file + ") OK ");
								roms_found.add(found);
							}
						}
						List<Entry> unneeded = c.getEntries().stream().filter(not(new HashSet<>(roms_found)::contains)).collect(Collectors.toList());
						for (Entry e : unneeded)
						{
							String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
							report_w.println("[" + m.name + "] " + archive.file.getName() + "@" + efile + " unneeded (crc=" + e.crc + ", sha1=" + e.sha1 + ")");
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
					if (roms.size() > 0)
					{
						int roms_found = 0;
						boolean partial = false;
						String submsg = "";
						CreateContainer createset = null;
						for (Rom r : roms)
						{
							missing_roms_cnt++;
							Entry found = null;
							for (DirScan scan : allscans)
							{
								if (null != (found = scan.find_byhash(profile,r)))
								{
									submsg += "\t[" + m.name + "] " + archive.file.getName() + "@" + r.getName() + " <- " + found.parent.file.getName() + "@" + found.file + "\n";
									(createset = CreateContainer.getInstance(createset, archive, format)).addAction(new AddEntry(r, found));
									roms_found++;
									break;
								}
							}
							if (found == null)
							{
								submsg += "\t[" + m.name + "] " + archive.file.getName() + "@" + r.getName() + " is missing and not fixable\n";
								partial = true;
							}
						}
						String msg = "[" + m.name + "] is missing";
						if (roms_found > 0)
						{
							if(!createfull_mode || !partial)
							{
								ContainerAction.addToList(create_actions, createset);
								msg += ", but can " + (partial ? "partially" : "totally") + " be recreated :\n" + submsg;
							}
						}
						report_w.println(msg);
					}
				}
				if(format==FormatOptions.DIR)
				{
					if(disks.size()==0 && roms.size()==0)
					{
						Machine m2 = m;
						if(!(merge_mode.isMerge() && m.isClone()))
							m2 = m.getDestMachine(merge_mode);
						Container c2 = dstscan.containers_byname.get(m2.name);
						if(c2 != null)
						{
							report_w.println("[" + m2.name + "] " + c2.file.getName() + " is unneeded");
							delete_actions.add(new DeleteContainer(c2, format));
						}
					}
				}
				else
				{
					if(disks.size()==0)
					{
						Machine m2 = m;
						if(!(merge_mode.isMerge() && m.isClone()))
							m2 = m.getDestMachine(merge_mode);
						Container c2 = dstscan.containers_byname.get(m2.name);
						if(c2 != null)
						{
							report_w.println("[" + m2.name + "] " + c2.file.getName() + " is unneeded");
							delete_actions.add(new DeleteContainer(c2, format));
						}
					}
					if(roms.size()==0)
					{
						Machine m2 = m;
						if(!(merge_mode.isMerge() && m.isClone()))
							m2 = m.getDestMachine(merge_mode);
						Container c2 = dstscan.containers_byname.get(m2.name+format.getExt());
						if(c2 != null)
						{
							report_w.println("[" + m2.name + "] " + c2.file.getName() + " is unneeded");
							delete_actions.add(new DeleteContainer(c2, format));
						}
					}
				}
				format.getExt().allExcept().forEach((e)->{
					Container c2 = dstscan.containers_byname.get(m.name+e);
					if(c2 != null)
					{
						report_w.println("[" + m.name + "] " + c2.file.getName() + " is unneeded");
						delete_actions.add(new DeleteContainer(c2, format));
					}
				});
				handler.setProgress(null, ++i);
				if (handler.isCancel())
					throw new BreakException();
				if (roms.size() == 0 && disks.size() == 0)
					missing_set = false;
				if (missing_set)
					missing_set_cnt++;
			}
			report_w.println("Missing sets : " + missing_set_cnt + "/" + profile.machines_cnt);
			report_w.println("Missing roms : " + missing_roms_cnt + "/" + profile.roms_cnt);
			report_w.println("Missing disks : " + missing_disks_cnt + "/" + profile.disks_cnt);
		}
		catch (FileNotFoundException e)
		{
			Log.err("Report Exception", e);
		}
		catch (BreakException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			Log.err("Other Exception when listing", e);
		}

		actions.add(create_actions);
		actions.add(rename_before_actions);
		actions.add(add_actions);
		actions.add(duplicate_actions);
		actions.add(delete_actions);
		actions.add(rename_after_actions);

	}

	private static <T> Predicate<T> not(Predicate<T> predicate)
	{
		return predicate.negate();
	}

}
