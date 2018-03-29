package profiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import actions.AddEntry;
import actions.CreateContainer;
import actions.DeleteContainer;
import actions.DeleteEntry;
import actions.DuplicateEntry;
import actions.OpenContainer;
import actions.RenameEntry;
import data.Archive;
import data.Container;
import data.Directory;
import data.Disk;
import data.Entry;
import data.Machine;
import data.Rom;
import misc.Log;
import ui.ProgressHandler;

public class Scan
{

	public ArrayList<actions.ContainerAction> actions = new ArrayList<>();

	public Scan(Profile profile, File dstdir, List<File> srcdirs, ProgressHandler handler)
	{
		DirScan dstscan = new DirScan(profile, dstdir, handler);
		List<DirScan> allscans = new ArrayList<>();
		allscans.add(dstscan);
		for(File dir : srcdirs)
			allscans.add(new DirScan(profile, dir, handler));

		ArrayList<Container> unknown = new ArrayList<>();
		for (Container c : dstscan.containers)
			if (!profile.machines_byname.containsKey(FilenameUtils.getBaseName(c.file.toString())))
				unknown.add(c);

		ArrayList<actions.ContainerAction> create_actions = new ArrayList<>();
		ArrayList<actions.ContainerAction> rename_before_actions = new ArrayList<>();
		ArrayList<actions.ContainerAction> update_actions = new ArrayList<>();
		ArrayList<actions.ContainerAction> delete_actions = new ArrayList<>();
		ArrayList<actions.ContainerAction> rename_after_actions = new ArrayList<>();

		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File reportdir = new File(workdir, "reports");
		reportdir.mkdirs();
		File report = new File(reportdir, "report.log");
		try (PrintWriter report_w = new PrintWriter(report))
		{
			profile.suspicious_crc.forEach((crc)->report_w.println("Detected suspicious CRC : " + crc));

			int i = 0;
			int missing_set = 0;
			int missing_roms = 0;
			int missing_disks = 0;
			handler.setProgress("Searching for fixes...", i, profile.machines.size());
			for (Machine m : profile.machines)
			{
			//	if (!m.isdevice)
				{
					Container c;
					List<Rom> roms = m.roms.stream().filter(r -> {
						if(r.status.equals("nodump"))
							return false;
						if (r.crc == null)
						{
							report_w.println("[" + m.name + "] " + r.getName() + " has no crc");
							return false;
						}
						return m.isbios || m.romof == null || r.merge == null;
					}).collect(Collectors.toList());
					List<Disk> disks = m.disks.stream().filter(d -> {
						if(d.status.equals("nodump"))
							return false;
						return m.isbios || m.romof == null || d.merge == null;
					}).collect(Collectors.toList());
					if (null != (c = dstscan.containers_byname.get(m.name)))
					{
						if(disks.size() > 0)
						{
							ArrayList<Entry> disks_found = new ArrayList<>();
							Map<String, Disk> disks_byname = disks.stream().collect(Collectors.toMap(Disk::getName, Function.identity(), (n, r) -> n));
							OpenContainer update_set = null, delete_set = null, rename_before_set = null, rename_after_set = null;
							for(Disk d : disks)
							{
								Entry found = null;
								Map<String, Entry> entries_byname = c.getEntries().stream().collect(Collectors.toMap(Entry::getName, Function.identity(), (n, e) -> n));
								for (Entry e : c.getEntries())
								{
									String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
									if (d.sha1.equals(e.sha1))
									{
										if (!d.getName().equals(efile))
										{
											if (disks_byname.containsKey(new File(e.file).getName()))
											{
												if (!entries_byname.containsKey(d.getName()))
												{
													report_w.println("[" + m.name + "] " + d.getName() + " <- " + e.file);
													if (update_set == null)
														update_set = new OpenContainer(new Directory(new File(dstdir, m.name)));
													update_set.addAction(new DuplicateEntry(d.getName(), e));
												}
												else
												{
													report_w.println("["+m.name+"] "+d.getName()+" == "+e.file);
												}
											}
											else
											{
												report_w.println("[" + m.name + "] wrong named rom (" + efile + "->" + d.getName() + ")");
												if (rename_before_set == null)
													rename_before_set = new OpenContainer(new Directory(new File(dstdir, m.name)));
												rename_before_set.addAction(new RenameEntry(e));
												if (rename_after_set == null)
													rename_after_set = new OpenContainer(new Directory(new File(dstdir, m.name)));
												rename_after_set.addAction(new RenameEntry(d.getName(), e));
												found = e;
												break;
											}
										}
										else
										{
											report_w.println("["+m.name+"] "+d.getName()+" ("+e.file+") OK ");
											found = e;
											break;
										}
									}
									else if (d.getName().equals(efile))
									{
										report_w.println("[" + m.name + "] "+e.file+" has wrong sha1 (got " + e.sha1 + " vs " + d.sha1 + ")");
										//found = e;
										break;
									}
								}								
								if (found == null)
								{
									missing_roms++;
									for(DirScan scan : allscans)
									{
										if (null != (found = scan.find_byhash(d)))
										{
											report_w.println("[" + m.name + "] " + d.getName() + " <- " + found.parent.file.getName() + "@" + found.file);
											if (update_set == null)
												update_set = new OpenContainer(new Directory(new File(dstdir, m.name)));
											update_set.addAction(new AddEntry(d, found));
											break;
										}
									}
									if(found==null)
										report_w.println("[" + m.name + "] " + d.getName() + " is missing and not fixable");
								}
								else
								{
									report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
									disks_found.add(found);
								}
							}
							List<Entry> unneeded = c.getEntries().stream().filter(not(new HashSet<>(disks_found)::contains)).collect(Collectors.toList());
							for (Entry e : unneeded)
							{
								String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
								report_w.println("[" + m.name + "] " + efile + " unneeded (sha1=" + e.sha1 + ")");
								if (rename_before_set == null)
									rename_before_set = new OpenContainer(new Directory(new File(dstdir, m.name)));
								rename_before_set.addAction(new RenameEntry(e));
								if (delete_set == null)
									delete_set = new OpenContainer(new Directory(new File(dstdir, m.name)));
								delete_set.addAction(new DeleteEntry(e));
							}
							if (rename_before_set != null && rename_before_set.entry_actions.size() > 0)
								rename_before_actions.add(rename_before_set);
							if (update_set != null && update_set.entry_actions.size() > 0)
								update_actions.add(update_set);
							if (delete_set != null && delete_set.entry_actions.size() > 0)
								delete_actions.add(delete_set);
							if (rename_after_set != null && rename_after_set.entry_actions.size() > 0)
								rename_after_actions.add(rename_after_set);
						}
						else
						{
							report_w.println("[" + m.name + "] "+c.file.getName()+" is unneeded");
							delete_actions.add(new DeleteContainer(c));							
						}
					}
					else
					{
						if (disks.size() > 0)
						{
							missing_set++;
							int disks_found = 0;
							boolean partial = false;
							String submsg = "";
							CreateContainer createset = null;
							for (Disk d : disks)
							{
								missing_disks++;
								Entry found = null;
								for(DirScan scan : allscans)
								{
									if (null != (found = scan.find_byhash(d)))
									{
										submsg += "\t[" + m.name + "] " + d.getName() + " <- " + found.parent.file.getName() + "@" + found.file + "\n";
										if (createset == null)
											createset = new CreateContainer(new Directory(new File(dstdir, m.name)));
										createset.addAction(new AddEntry(d, found));
										disks_found++;
										break;
									}
								}
								if(found==null)
								{
									submsg += "\t[" + m.name + "] " + d.getName() + " is missing and not fixable\n";
									partial = true;
								}
							}
							if (createset != null && createset.entry_actions.size() > 0)
								create_actions.add(createset);
							String msg = "[" + m.name + "] is missing";
							if (disks_found > 0)
								msg += ", but can " + (partial ? "partially" : "totally") + " be recreated :\n" + submsg;
							report_w.println(msg);
						}
					}
					if (null != (c = dstscan.containers_byname.get(m.name + ".zip")))
					{
						if (roms.size() > 0)
						{
							ArrayList<Entry> roms_found = new ArrayList<>();
							Map<String, Rom> roms_byname = roms.stream().collect(Collectors.toMap(Rom::getName, Function.identity(), (n, r) -> n));
							OpenContainer update_set = null, delete_set = null, rename_before_set = null, rename_after_set = null;
							for (Rom r : roms)
							{
								Entry found = null;
								Map<String, Entry> entries_byname = c.getEntries().stream().collect(Collectors.toMap(Entry::getName, Function.identity(), (n, e) -> n));
								for (Entry e : c.getEntries())
								{
									String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
									if (r.crc.equals(e.crc) && (e.sha1==null || e.sha1.equals(r.sha1)))
									{
										if (!r.getName().equals(efile))
										{
											if (roms_byname.containsKey(new File(e.file).getName()))
											{
												if (!entries_byname.containsKey(r.getName()))
												{
													report_w.println("[" + m.name + "] " + r.getName() + " <- " + e.file);
													if (update_set == null)
														update_set = new OpenContainer(new Archive(new File(dstdir, m.name + ".zip")));
													update_set.addAction(new DuplicateEntry(r.getName(), e));
												}
												else
												{
													// report_w.println("["+m.name+"] "+r.name+" == "+e.file);
												}
											}
											else
											{
												report_w.println("[" + m.name + "] wrong named rom (" + efile + "->" + r.getName() + ")");
												if (rename_before_set == null)
													rename_before_set = new OpenContainer(new Archive(new File(dstdir, m.name + ".zip")));
												rename_before_set.addAction(new RenameEntry(e));
												if (rename_after_set == null)
													rename_after_set = new OpenContainer(new Archive(new File(dstdir, m.name + ".zip")));
												rename_after_set.addAction(new RenameEntry(r.getName(), e));
												found = e;
												break;
											}
										}
										else
										{
											//report_w.println("["+m.name+"] "+r.name+" ("+e.file+") OK ");
											found = e;
											break;
										}
									}
									else if (r.getName().equals(efile))
									{
										if(e.sha1==null)
											report_w.println("[" + m.name + "] "+e.file+" has wrong crc (got " + e.crc + " vs " + r.crc + ")");
										else
											report_w.println("[" + m.name + "] "+e.file+" has good crc but wrong sha1 (got " + e.sha1 + " vs " + r.sha1 + ")");
										//found = e;
										break;
									}
								}
								if (found == null)
								{
									missing_roms++;
									for(DirScan scan : allscans)
									{
										if (null != (found = scan.find_byhash(r)))
										{
											report_w.println("[" + m.name + "] " + r.getName() + " <- " + found.parent.file.getName() + "@" + found.file);
											if (update_set == null)
												update_set = new OpenContainer(new Archive(new File(dstdir, m.name + ".zip")));
											update_set.addAction(new AddEntry(r, found));
											// roms_found.add(found);
											break;
										}
									}
									if(found==null)
										report_w.println("[" + m.name + "] " + r.getName() + " is missing and not fixable");
								}
								else
								{
									//report_w.println("["+m.name+"] "+r.name+" ("+found.file+") OK ");
									roms_found.add(found);
								}
							}
							List<Entry> unneeded = c.getEntries().stream().filter(not(new HashSet<>(roms_found)::contains)).collect(Collectors.toList());
							for (Entry e : unneeded)
							{
								String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
								report_w.println("[" + m.name + "] " + efile + " unneeded (crc=" + e.crc + ", sha1=" + e.sha1 + ")");
								if (rename_before_set == null)
									rename_before_set = new OpenContainer(new Archive(new File(dstdir, m.name + ".zip")));
								rename_before_set.addAction(new RenameEntry(e));
								if (delete_set == null)
									delete_set = new OpenContainer(new Archive(new File(dstdir, m.name + ".zip")));
								delete_set.addAction(new DeleteEntry(e));
							}
							if (rename_before_set != null && rename_before_set.entry_actions.size() > 0)
								rename_before_actions.add(rename_before_set);
							if (update_set != null && update_set.entry_actions.size() > 0)
								update_actions.add(update_set);
							if (delete_set != null && delete_set.entry_actions.size() > 0)
								delete_actions.add(delete_set);
							if (rename_after_set != null && rename_after_set.entry_actions.size() > 0)
								rename_after_actions.add(rename_after_set);
						}
						else
						{
							report_w.println("[" + m.name + "] "+c.file.getName()+" is unneeded");
							delete_actions.add(new DeleteContainer(c));
						}
					}
					else
					{
						if (roms.size() > 0)
						{
							missing_set++;
							int roms_found = 0;
							boolean partial = false;
							String submsg = "";
							CreateContainer createset = null;
							for (Rom r : roms)
							{
								missing_roms++;
								Entry found = null;
								for(DirScan scan : allscans)
								{
									if (null != (found = scan.find_byhash(r)))
									{
										submsg += "\t[" + m.name + "] " + r.getName() + " <- " + found.parent.file.getName() + "@" + found.file + "\n";
										if (createset == null)
											createset = new CreateContainer(new Archive(new File(dstdir, m.name + ".zip")));
										createset.addAction(new AddEntry(r, found));
										roms_found++;
										break;
									}
								}
								if(found==null)
								{
									submsg += "\t[" + m.name + "] " + r.getName() + " is missing and not fixable\n";
									partial = true;
								}
							}
							if (createset != null && createset.entry_actions.size() > 0)
								create_actions.add(createset);
							String msg = "[" + m.name + "] is missing";
							if (roms_found > 0)
								msg += ", but can " + (partial ? "partially" : "totally") + " be recreated :\n" + submsg;
							report_w.println(msg);
						}
					}
					handler.setProgress(null, ++i);
				}
			}
			report_w.println("Missing sets : " + missing_set + "/" + profile.machines_cnt);
			report_w.println("Missing roms : " + missing_roms + "/" + profile.roms_cnt);
			report_w.println("Missing disks : " + missing_disks + "/" + profile.disks_cnt);
		}
		catch (FileNotFoundException e)
		{
			Log.err("Report Exception", e);
		}
		catch (Throwable e)
		{
			Log.err("Other Exception when listing", e);
		}

		actions.addAll(create_actions);
		actions.addAll(rename_before_actions);
		actions.addAll(update_actions);
		actions.addAll(delete_actions);
		actions.addAll(rename_after_actions);

	}

	private static <T> Predicate<T> not(Predicate<T> predicate)
	{
		return predicate.negate();
	}

}
