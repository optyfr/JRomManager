package jrm.profiler.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import jrm.profiler.Profile;
import jrm.profiler.data.Machine;

public class Report implements TreeNode
{
	private Profile profile;
	private List<Subject> subjects = Collections.synchronizedList(new ArrayList<>());
	private Map<String, Subject> subject_hash = Collections.synchronizedMap(new HashMap<>());
	public Stats stats = new Stats();
	
	DefaultTreeModel model = null;
	
	public class Stats
	{
		public int missing_set_cnt = 0;
		public int missing_roms_cnt = 0;
		public int missing_disks_cnt = 0;
	}
	
	public Report()
	{
		model = new DefaultTreeModel(this);
	}

	public void setProfile(Profile profile)
	{
		this.profile = profile;
		subject_hash.clear();
		subjects.clear();
		if(model!=null)
			model.reload();
	}
	
	public DefaultTreeModel getModel()
	{
		return model;
	}
	
	public Subject findSubject(Machine m)
	{
		return m != null ? subject_hash.get(m.name) : null;
	}

	public boolean add(Subject subject)
	{
		subject.parent = this;
		if(subject.machine != null)
			subject_hash.put(subject.machine.name, subject);
		boolean result = subjects.add(subject);
		if(0==(subjects.size()%100))
			model.reload();
		return result;
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

	@Override
	public TreeNode getChildAt(int childIndex)
	{
		return subjects.get(childIndex);
	}

	@Override
	public int getChildCount()
	{
		return subjects.size();
	}

	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return subjects.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return subjects.size()==0;
	}

	@Override
	public Enumeration<Subject> children()
	{
		return Collections.enumeration(subjects);
	}

}
