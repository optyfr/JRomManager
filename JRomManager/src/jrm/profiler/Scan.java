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
import jrm.ui.ProgressHandler;

public class Scan
{

	public ArrayList<ArrayList<jrm.actions.ContainerAction>> actions = new ArrayList<>();

	public Scan(Profile profile, File dstdir, List<File> srcdirs, ProgressHandler handler) throws BreakException
	{
		DirScan dstscan = new DirScan(profile, dstdir, handler);
		List<DirScan> allscans = new ArrayList<>();
		allscans.add(dstscan);
		for(File dir : srcdirs)
			allscans.add(new DirScan(profile, dir, handler));

		ArrayList<Container> unknown = new ArrayList<>();
		for (Container c : dstscan.containers)
		{
			if(c.getType()==Container.Type.UNK)
				unknown.add(c);
			else if (!profile.machines_byname.containsKey(FilenameUtils.getBaseName(c.file.toString())))
				unknown.add(c);
		}

		ArrayList<jrm.actions.ContainerAction> create_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> rename_before_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> update_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> delete_actions = new ArrayList<>();
		ArrayList<jrm.actions.ContainerAction> rename_after_actions = new ArrayList<>();

		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File reportdir = new File(workdir, "reports");
		reportdir.mkdirs();
		File report = new File(reportdir, "report.log");
		try (PrintWriter report_w = new PrintWriter(report))
		{
			unknown.forEach((c)->{
				report_w.println("Uneeded " + (c.getType()==Container.Type.DIR?"Directory":"File") + " : " + c.file);
				delete_actions.add(new DeleteContainer(c));
			});
			profile.suspicious_crc.forEach((crc)->report_w.println("Detected suspicious CRC : " + crc + " (SHA1 has been calculated for theses roms"));

			int i = 0;
			int missing_set_cnt = 0;
			int missing_roms_cnt = 0;
			int missing_disks_cnt = 0;
			handler.setProgress("Searching for fixes...", i, profile.machines.size());
			for (Machine m : profile.machines)
			{
				boolean missing_set = true;
			//	if (!m.isdevice)
				{
					Container c;
					List<Disk> disks = m.filterDisks();
					List<Rom> roms = m.filterRoms();
					Directory directory  = new Directory(new File(dstdir, m.name));
					if (null != (c = dstscan.containers_byname.get(m.name)))
					{
						missing_set = false;
						if(disks.size() > 0)
						{
							ArrayList<Entry> disks_found = new ArrayList<>();
							Map<String, Disk> disks_byname = Disk.getDisksByName(disks);
							OpenContainer update_set = null, delete_set = null, rename_before_set = null, rename_after_set = null;
							for(Disk d : disks)
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
												if (!entries_byname.containsKey(d.getName()))
												{
													report_w.println("[" + m.name + "] " + d.getName() + " <- " + e.file);
													(update_set = OpenContainer.getInstance(update_set, directory)).addAction(new DuplicateEntry(d.getName(), e));
												}
												else
												{
												//	report_w.println("["+m.name+"] "+d.getName()+" == "+e.file);
												}
											}
											else
											{
												report_w.println("[" + m.name + "] wrong named rom (" + e.getName() + "->" + d.getName() + ")");
												(rename_before_set = OpenContainer.getInstance(rename_before_set, directory)).addAction(new RenameEntry(e));
												(rename_after_set = OpenContainer.getInstance(rename_after_set, directory)).addAction(new RenameEntry(d.getName(), e));
												found = e;
												break;
											}
										}
										else
										{
										//	report_w.println("["+m.name+"] "+d.getName()+" ("+e.file+") OK ");
											found = e;
											break;
										}
									}
									else if (d.getName().equals(e.getName()))
									{
										if(e.sha1==null)
											report_w.println("[" + m.name + "] "+e.file+" has wrong md5 (got " + e.md5 + " vs " + d.md5 + ")");
										else
											report_w.println("[" + m.name + "] "+e.file+" has wrong sha1 (got " + e.sha1 + " vs " + d.sha1 + ")");
										//found = e;
										break;
									}
								}								
								if (found == null)
								{
									missing_disks_cnt++;
									for(DirScan scan : allscans)
									{
										if (null != (found = scan.find_byhash(d)))
										{
											report_w.println("[" + m.name + "] " + d.getName() + " <- " + found.parent.file.getName() + "@" + found.file);
											(update_set = OpenContainer.getInstance(update_set, directory)).addAction(new AddEntry(d, found));
											break;
										}
									}
									if(found==null)
										report_w.println("[" + m.name + "] " + d.getName() + " is missing and not fixable");
								}
								else
								{
								//	report_w.println("["+m.name+"] "+d.getName()+" ("+found.file+") OK ");
									disks_found.add(found);
								}
							}
							List<Entry> unneeded = c.getEntries().stream().filter(not(new HashSet<>(disks_found)::contains)).collect(Collectors.toList());
							for (Entry e : unneeded)
							{
								report_w.println("[" + m.name + "] " + e.file + " unneeded (sha1=" + e.sha1 + ")");
								(rename_before_set = OpenContainer.getInstance(rename_before_set, directory)).addAction(new RenameEntry(e));
								(delete_set = OpenContainer.getInstance(delete_set, directory)).addAction(new DeleteEntry(e));
							}
							ContainerAction.addToList(rename_before_actions, rename_before_set);
							ContainerAction.addToList(update_actions, update_set);
							ContainerAction.addToList(delete_actions, delete_set);
							ContainerAction.addToList(rename_after_actions, rename_after_set);
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
							int disks_found = 0;
							boolean partial = false;
							String submsg = "";
							CreateContainer createset = null;
							for (Disk d : disks)
							{
								missing_disks_cnt++;
								Entry found = null;
								for(DirScan scan : allscans)
								{
									if (null != (found = scan.find_byhash(d)))
									{
										submsg += "\t[" + m.name + "] " + d.getName() + " <- " + found.parent.file.getName() + "@" + found.file + "\n";
										(createset = CreateContainer.getInstance(createset, directory)).addAction(new AddEntry(d, found));
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
							ContainerAction.addToList(create_actions, createset);
							String msg = "[" + m.name + "] is missing";
							if (disks_found > 0)
								msg += ", but can " + (partial ? "partially" : "totally") + " be recreated :\n" + submsg;
							report_w.println(msg);
						}
					}
					Archive archive = new Archive(new File(dstdir, m.name + ".zip"));
					if (null != (c = dstscan.containers_byname.get(m.name + ".zip")))
					{
						missing_set = false;
						if (roms.size() > 0)
						{
							ArrayList<Entry> roms_found = new ArrayList<>();
							Map<String, Rom> roms_byname = Rom.getRomsByName(roms);
							OpenContainer update_set = null, delete_set = null, rename_before_set = null, rename_after_set = null;
							for (Rom r : roms)
							{
								Entry found = null;
								Map<String, Entry> entries_byname = c.getEntriesByName();
								for (Entry e : c.getEntries())
								{
									String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
									if (e.equals(r))
									{
										if (!r.getName().equals(efile))
										{
											if (roms_byname.containsKey(new File(e.file).getName()))
											{
												if (!entries_byname.containsKey(r.getName()))
												{
													report_w.println("[" + m.name + "] " + r.getName() + " <- " + e.file);
													(update_set = OpenContainer.getInstance(update_set, archive)).addAction(new DuplicateEntry(r.getName(), e));
												}
												else
												{
													// report_w.println("["+m.name+"] "+r.name+" == "+e.file);
												}
											}
											else
											{
												report_w.println("[" + m.name + "] wrong named rom (" + efile + "->" + r.getName() + ")");
												(rename_before_set = OpenContainer.getInstance(rename_before_set, archive)).addAction(new RenameEntry(e));
												(rename_after_set = OpenContainer.getInstance(rename_after_set, archive)).addAction(new RenameEntry(r.getName(), e));
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
										if(e.md5==null && e.sha1==null)
											report_w.println("[" + m.name + "] "+e.file+" has wrong crc (got " + e.crc + " vs " + r.crc + ")");
										else if(e.sha1==null)
											report_w.println("[" + m.name + "] "+e.file+" has wrong md5 (got " + e.md5 + " vs " + r.md5 + ")");
										else
											report_w.println("[" + m.name + "] "+e.file+" has wrong sha1 (got " + e.sha1 + " vs " + r.sha1 + ")");
										//found = e;
										break;
									}
								}
								if (found == null)
								{
									missing_roms_cnt++;
									for(DirScan scan : allscans)
									{
										if (null != (found = scan.find_byhash(r)))
										{
											report_w.println("[" + m.name + "] " + r.getName() + " <- " + found.parent.file.getName() + "@" + found.file);
											(update_set = OpenContainer.getInstance(update_set, archive)).addAction(new AddEntry(r, found));
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
								(rename_before_set = OpenContainer.getInstance(rename_before_set, archive)).addAction(new RenameEntry(e));
								(delete_set = OpenContainer.getInstance(delete_set, archive)).addAction(new DeleteEntry(e));
							}
							ContainerAction.addToList(rename_before_actions, rename_before_set);
							ContainerAction.addToList(update_actions, update_set);
							ContainerAction.addToList(delete_actions, delete_set);
							ContainerAction.addToList(rename_after_actions, rename_after_set);
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
							int roms_found = 0;
							boolean partial = false;
							String submsg = "";
							CreateContainer createset = null;
							for (Rom r : roms)
							{
								missing_roms_cnt++;
								Entry found = null;
								for(DirScan scan : allscans)
								{
									if (null != (found = scan.find_byhash(r)))
									{
										submsg += "\t[" + m.name + "] " + r.getName() + " <- " + found.parent.file.getName() + "@" + found.file + "\n";
										(createset = CreateContainer.getInstance(createset, archive)).addAction(new AddEntry(r, found));
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
							ContainerAction.addToList(create_actions, createset);
							String msg = "[" + m.name + "] is missing";
							if (roms_found > 0)
								msg += ", but can " + (partial ? "partially" : "totally") + " be recreated :\n" + submsg;
							report_w.println(msg);
						}
					}
					handler.setProgress(null, ++i);
					if(handler.isCancel())
						throw new BreakException();
					if(roms.size()==0 && disks.size()==0)
						missing_set=false;
					if(missing_set)
						missing_set_cnt++; 
				}
			}
			report_w.println("Missing sets : " + missing_set_cnt + "/" + profile.machines_cnt);
			report_w.println("Missing roms : " + missing_roms_cnt + "/" + profile.roms_cnt);
			report_w.println("Missing disks : " + missing_disks_cnt + "/" + profile.disks_cnt);
		}
		catch (FileNotFoundException e)
		{
			Log.err("Report Exception", e);
		}
		catch(BreakException e)
		{
			throw e;
		}
		catch (Throwable e)
		{
			Log.err("Other Exception when listing", e);
		}

		actions.add(create_actions);
		actions.add(rename_before_actions);
		actions.add(update_actions);
		actions.add(delete_actions);
		actions.add(rename_after_actions);

	}

	private static <T> Predicate<T> not(Predicate<T> predicate)
	{
		return predicate.negate();
	}

}
