package jrm.profiler.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jrm.profiler.Profile;
import jrm.profiler.data.Machine;

public class Report
{
	private Profile profile;
	private List<Subject> subjects = Collections.synchronizedList(new ArrayList<>());
	private Map<String, Subject> subject_hash = Collections.synchronizedMap(new HashMap<>());
	public Stats stats = new Stats();

	public class Stats
	{
		public int missing_set_cnt = 0;
		public int missing_roms_cnt = 0;
		public int missing_disks_cnt = 0;
	}
	
	public Report(Profile profile)
	{
		this.profile = profile;
	}

	public Subject findSubject(Machine m)
	{
		return m != null ? subject_hash.get(m.name) : null;
	}

	public boolean add(Subject subject)
	{
		if(subject.machine != null)
			subject_hash.put(subject.machine.name, subject);
		return subjects.add(subject);
	}
	
	public void write()
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile();
		File reportdir = new File(workdir, "reports");
		reportdir.mkdirs();
		File report_file = new File(reportdir, "report.log");
		try(PrintWriter report_w = new PrintWriter(report_file))
		{
			subjects.forEach(subject->{
				report_w.println(subject);
				subject.notes.forEach(note->{
					report_w.println("\t"+note);
				});
			});
			report_w.println();
			report_w.println("Missing sets : " + stats.missing_set_cnt + "/" + profile.machines_cnt);
			report_w.println("Missing roms : " + stats.missing_roms_cnt + "/" + profile.roms_cnt);
			report_w.println("Missing disks : " + stats.missing_disks_cnt + "/" + profile.disks_cnt);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
	}

}
