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

import actions.AddRom;
import actions.CreateSet;
import actions.DeleteRom;
import actions.DeleteSet;
import actions.DuplicateRom;
import actions.OpenSet;
import actions.RenameRom;
import data.Archive;
import data.Container;
import data.Entry;
import data.Machine;
import data.Rom;
import misc.Log;
import ui.ProgressHandler;

public class Scan
{

	public ArrayList<actions.SetAction> actions = new ArrayList<>();

	public Scan(Profile profile, File dstdir, List<File> srcdirs, ProgressHandler handler)
	{
		DirScan dstscan = new DirScan(dstdir, handler);
		List<DirScan> allscans = new ArrayList<>();
		allscans.add(dstscan);
		for(File dir : srcdirs)
			allscans.add(new DirScan(dir, handler));

		ArrayList<Container> unknown = new ArrayList<>();
		for (Container c : dstscan.containers)
			if (!profile.machines_byname.containsKey(FilenameUtils.getBaseName(c.file.toString())))
				unknown.add(c);

		ArrayList<actions.SetAction> create_actions = new ArrayList<>();
		ArrayList<actions.SetAction> rename_before_actions = new ArrayList<>();
		ArrayList<actions.SetAction> update_actions = new ArrayList<>();
		ArrayList<actions.SetAction> delete_actions = new ArrayList<>();
		ArrayList<actions.SetAction> rename_after_actions = new ArrayList<>();

		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File reportdir = new File(workdir, "reports");
		reportdir.mkdirs();
		File report = new File(reportdir, "report.log");
		try (PrintWriter report_w = new PrintWriter(report))
		{
			int i = 0;
			int missing_set = 0;
			int missing_roms = 0;
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
							report_w.println("[" + m.name + "] " + r.name + " has no crc");
							return false;
						}
						return m.isbios || m.romof == null || r.merge == null;
					}).collect(Collectors.toList());
					if (null != (c = dstscan.containers_byname.get(m.name + ".zip")))
					{
						if (roms.size() > 0)
						{
							OpenSet update_set = null, delete_set = null, rename_before_set = null,
									rename_after_set = null;
							ArrayList<Entry> roms_found = new ArrayList<>();
							Map<String, Rom> roms_byname = roms.stream().collect(Collectors.toMap(Rom::getName, Function.identity(), (n, r) -> n));
							for (Rom r : roms)
							{
								Entry found = null;
								Map<String, Entry> entries_byname = c.entries.stream().collect(Collectors.toMap(Entry::getName, Function.identity(), (n, e) -> n));
								for (Entry e : c.entries)
								{
									String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
									if (r.crc.equals(e.crc) && (e.sha1==null || e.sha1.equals(r.sha1)))
									{
										if (!r.name.equals(efile))
										{
											if (roms_byname.containsKey(new File(e.file).getName()))
											{
												if (!entries_byname.containsKey(r.name))
												{
													report_w.println("[" + m.name + "] " + r.name + " <- " + e.file);
													if (update_set == null)
														update_set = new OpenSet(new Archive(new File(dstdir, m.name + ".zip")));
													update_set.addRomAction(new DuplicateRom(r.name, e));
												}
												else
												{
													// report_w.println("["+m.name+"] "+r.name+" == "+e.file);
												}
											}
											else
											{
												report_w.println("[" + m.name + "] wrong named rom (" + efile + "->" + r.name + ")");
												if (rename_before_set == null)
													rename_before_set = new OpenSet(new Archive(new File(dstdir, m.name + ".zip")));
												rename_before_set.addRomAction(new RenameRom(e));
												if (rename_after_set == null)
													rename_after_set = new OpenSet(new Archive(new File(dstdir, m.name + ".zip")));
												rename_after_set.addRomAction(new RenameRom(r.name, e));
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
									else if (r.name.equals(efile))
									{
										if(e.sha1==null)
											report_w.println("[" + m.name + "] wrong crc (got " + e.crc + " vs " + r.crc + ")");
										else
											report_w.println("[" + m.name + "] wrong sha1 (got " + e.sha1 + " vs " + r.sha1 + ")");
										found = e;
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
											report_w.println("[" + m.name + "] " + r.name + " <- " + found.parent.file.getName() + "@" + found.file);
											if (update_set == null)
												update_set = new OpenSet(new Archive(new File(dstdir, m.name + ".zip")));
											update_set.addRomAction(new AddRom(r, found));
											// roms_found.add(found);
											break;
										}
									}
									if(found==null)
										report_w.println("[" + m.name + "] " + r.name + " is missing and not fixable");
								}
								else
								{
									//report_w.println("["+m.name+"] "+r.name+" ("+found.file+") OK ");
									roms_found.add(found);
								}
							}
							List<Entry> unneeded = c.entries.stream().filter(not(new HashSet<>(roms_found)::contains)).collect(Collectors.toList());
							for (Entry e : unneeded)
							{
								String efile = Paths.get(e.file).subpath(0, Paths.get(e.file).getNameCount()).toString();
								report_w.println("[" + m.name + "] " + efile + " unneeded (crc=" + e.crc + ", sha1=" + e.sha1 + ")");
								if (rename_before_set == null)
									rename_before_set = new OpenSet(new Archive(new File(dstdir, m.name + ".zip")));
								rename_before_set.addRomAction(new RenameRom(e));
								if (delete_set == null)
									delete_set = new OpenSet(new Archive(new File(dstdir, m.name + ".zip")));
								delete_set.addRomAction(new DeleteRom(e));
							}
							if (rename_before_set != null && rename_before_set.roms.size() > 0)
								rename_before_actions.add(rename_before_set);
							if (update_set != null && update_set.roms.size() > 0)
								update_actions.add(update_set);
							if (delete_set != null && delete_set.roms.size() > 0)
								delete_actions.add(delete_set);
							if (rename_after_set != null && rename_after_set.roms.size() > 0)
								rename_after_actions.add(rename_after_set);
						}
						else
						{
							report_w.println("[" + m.name + "] is unneeded");
							delete_actions.add(new DeleteSet(c));
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
							CreateSet createset = null;
							for (Rom r : roms)
							{
								missing_roms++;
								Entry found = null;
								for(DirScan scan : allscans)
								{
									if (null != (found = scan.find_byhash(r)))
									{
										submsg += "\t[" + m.name + "] " + r.name + " <- " + found.parent.file.getName() + "@" + found.file + "\n";
										if (createset == null)
											createset = new CreateSet(new Archive(new File(dstdir, m.name + ".zip"), true));
										createset.addRomAction(new AddRom(r, found));
										roms_found++;
										break;
									}
								}
								if(found==null)
								{
									submsg += "\t[" + m.name + "] " + r.name + " is missing and not fixable\n";
									partial = true;
								}
							}
							if (createset != null && createset.roms.size() > 0)
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
			report_w.println("Missing sets : " + missing_set + "/" + profile.machines.size());
			report_w.println("Missing roms : " + missing_roms + "/" + profile.roms_bysha1.size());
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
