package jrm.profile.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;

import jrm.Messages;
import jrm.misc.HTMLRenderer;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.ui.ReportTreeModel;
import jrm.ui.StatusHandler;
import one.util.streamex.IntStreamEx;

public class Report implements TreeNode, HTMLRenderer
{
	private Profile profile;
	private List<Subject> subjects = Collections.synchronizedList(new ArrayList<>());
	private Map<String, Subject> subject_hash = Collections.synchronizedMap(new HashMap<>());
	public Stats stats = new Stats();

	private ReportTreeModel model = null;

	public class Stats
	{
		public int missing_set_cnt = 0;
		public int missing_roms_cnt = 0;
		public int missing_disks_cnt = 0;

		public int set_unneeded = 0;
		public int set_missing = 0;
		public int set_found = 0;
		public int set_found_ok = 0;
		public int set_found_fixpartial = 0;
		public int set_found_fixcomplete = 0;
		public int set_create = 0;
		public int set_create_partial = 0;
		public int set_create_complete = 0;

		public String getStatus()
		{
			return String.format(Messages.getString("Report.Status"), set_found, set_found_ok, set_found_fixpartial, set_found_fixcomplete, set_create, set_create_partial, set_create_complete, set_missing, set_unneeded, set_found + set_create, set_found + set_create + set_missing); //$NON-NLS-1$
		}
	}

	public Report()
	{
		model = new ReportTreeModel(this);
		model.initClone();
	}

	class FilterPredicate implements Predicate<Subject>
	{
		List<FilterOptions> filterOptions;

		public FilterPredicate(List<FilterOptions> filterOptions)
		{
			this.filterOptions = filterOptions;
		}

		@Override
		public boolean test(Subject t)
		{
			if(!filterOptions.contains(FilterOptions.SHOWOK) && t instanceof SubjectSet && ((SubjectSet) t).isOK())
				return false;
			if(filterOptions.contains(FilterOptions.HIDEMISSING) && t instanceof SubjectSet && ((SubjectSet) t).isMissing())
				return false;
			return true;
		}

	}

	private FilterPredicate filterPredicate = new FilterPredicate(new ArrayList<>());

	private Report(Report report, List<FilterOptions> filterOptions)
	{
		this.filterPredicate = new FilterPredicate(filterOptions);
		this.model = report.model;
		this.profile = report.profile;
		this.subjects = report.filter(filterOptions);
		this.subject_hash = this.subjects.stream().collect(Collectors.toMap(Subject::getWareName, Function.identity(), (o, n) -> null));
		this.stats = report.stats;
	}

	public Report clone(List<FilterOptions> filterOptions)
	{
		return new Report(this, filterOptions);
	}

	public List<Subject> filter(List<FilterOptions> filterOptions)
	{
		this.filterPredicate = new FilterPredicate(filterOptions);
		return subjects.stream().filter(filterPredicate).map(s -> s.clone(filterOptions)).collect(Collectors.toList());
	}

	public void setProfile(Profile profile)
	{
		this.profile = profile;
		reset();
	}

	public void reset()
	{
		subject_hash.clear();
		subjects.clear();
		insert_object_cache.clear();
		stats = new Stats();
		if(model != null)
			model.filter(filterPredicate.filterOptions);
		flush();
	}

	private StatusHandler statusHandler = null;

	public void setStatusHandler(StatusHandler handler)
	{
		statusHandler = handler;
	}

	public ReportTreeModel getModel()
	{
		return model;
	}

	public Subject findSubject(Anyware ware)
	{
		return ware != null ? subject_hash.get(ware.getFullName()) : null;
	}

	private Map<Integer, Subject> insert_object_cache = Collections.synchronizedMap(new LinkedHashMap<>(250));

	public synchronized boolean add(Subject subject)
	{
		subject.parent = this;
		if(subject.ware != null)
			subject_hash.put(subject.ware.getFullName(), subject);
		boolean result = subjects.add(subject);
		Report clone = (Report) model.getRoot();
		if(this != clone)
		{
			subject.updateStats();
			if(filterPredicate.test(subject))
			{
				Subject cloned_subject = subject.clone(filterPredicate.filterOptions);
				clone.add(cloned_subject);
				insert_object_cache.put(clone.subjects.size() - 1, cloned_subject);
				if(insert_object_cache.size() >= 250)
					flush();
			}
		}
		return result;
	}

	public synchronized void flush()
	{
		if(statusHandler != null)
			statusHandler.setStatus(stats.getStatus());
		if(insert_object_cache.size() > 0)
		{
			TreeModelEvent event = new TreeModelEvent(model, model.getPathToRoot((Report) model.getRoot()), IntStreamEx.of(insert_object_cache.keySet()).toArray(), insert_object_cache.values().toArray());
			for(TreeModelListener l : model.getTreeModelListeners())
				l.treeNodesInserted(event);
		}
		insert_object_cache.clear();
	}

	public void write()
	{
		File workdir = Paths.get(".").toAbsolutePath().normalize().toFile(); //$NON-NLS-1$
		File reportdir = new File(workdir, "reports"); //$NON-NLS-1$
		reportdir.mkdirs();
		File report_file = new File(reportdir, "report.log"); //$NON-NLS-1$
		try(PrintWriter report_w = new PrintWriter(report_file))
		{
			subjects.forEach(subject -> {
				report_w.println(subject);
				subject.notes.forEach(note -> {
					report_w.println("\t" + note); //$NON-NLS-1$
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
		return false;
	}

	@Override
	public Enumeration<Subject> children()
	{
		return Collections.enumeration(subjects);
	}

}
