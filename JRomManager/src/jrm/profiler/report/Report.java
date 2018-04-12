package jrm.profiler.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.tree.TreeNode;

import jrm.Messages;
import jrm.profiler.Profile;
import jrm.profiler.data.Machine;
import jrm.ui.ReportTreeModel;

public class Report implements TreeNode
{
	private Profile profile;
	private List<Subject> subjects = Collections.synchronizedList(new ArrayList<>());
	private Map<String, Subject> subject_hash = Collections.synchronizedMap(new HashMap<>());
	public Stats stats = new Stats();
	
	ReportTreeModel model = null;
	
	public class Stats
	{
		public int missing_set_cnt = 0;
		public int missing_roms_cnt = 0;
		public int missing_disks_cnt = 0;
	}
	
	public Report()
	{
		model = new ReportTreeModel(this);
	}

	public Report(Report report, FilterOptions... filterOptions)
	{
		this(report,Arrays.asList(filterOptions));
	}
	
	public Report(Report report, List<FilterOptions> filterOptions)
	{
		this.model = report.model;
		this.profile = report.profile;
		this.subjects = report.filter(filterOptions);
		this.subject_hash = this.subjects.stream().collect(Collectors.toMap(Subject::getMachineName, Function.identity(), (o,n)->null));
		this.stats = report.stats;
		this.model = report.model;
	}
	
	public List<Subject> filter(FilterOptions... filterOptions)
	{
		return filter(Arrays.asList(filterOptions));
		
	}
	
	public List<Subject> filter(List<FilterOptions> filterOptions)
	{
		return subjects.stream().filter(s -> {
			if(!filterOptions.contains(FilterOptions.SHOWOK) && s instanceof SubjectSet && ((SubjectSet)s).isOK())
				return false;
			if(filterOptions.contains(FilterOptions.HIDEMISSING) && s instanceof SubjectSet && ((SubjectSet)s).isMissing())
				return false;
			return true;
		}).map(s->s.clone(filterOptions)).collect(Collectors.toList());
	}
	
	public void setProfile(Profile profile)
	{
		this.profile = profile;
		subject_hash.clear();
		subjects.clear();
		if(model!=null)
			model.reload();
	}
	
	public ReportTreeModel getModel()
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
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
		File reportdir = new File(workdir, "reports"); //$NON-NLS-1$
		reportdir.mkdirs();
		File report_file = new File(reportdir, "report.log"); //$NON-NLS-1$
		try(PrintWriter report_w = new PrintWriter(report_file))
		{
			subjects.forEach(subject->{
				report_w.println(subject);
				subject.notes.forEach(note->{
					report_w.println("\t"+note); //$NON-NLS-1$
				});
			});
			report_w.println();
			report_w.println(String.format(Messages.getString("Report.MissingSets"), stats.missing_set_cnt, profile.machines_cnt)); //$NON-NLS-1$
			report_w.println(String.format(Messages.getString("Report.MissingRoms"), stats.missing_roms_cnt, profile.roms_cnt)); //$NON-NLS-1$
			report_w.println(String.format(Messages.getString("Report.MissingDisks"), stats.missing_disks_cnt, profile.disks_cnt)); //$NON-NLS-1$
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
