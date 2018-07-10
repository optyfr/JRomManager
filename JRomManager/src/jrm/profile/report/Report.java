package jrm.profile.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;

import jrm.Messages;
import jrm.misc.HTMLRenderer;
import jrm.misc.Settings;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.ui.ReportTreeModel;
import jrm.ui.StatusHandler;
import one.util.streamex.IntStreamEx;

public class Report implements TreeNode, HTMLRenderer
{
	private Profile profile;
	private final List<Subject> subjects;
	private final Map<String, Subject> subject_hash;
	public final Stats stats;

	private ReportTreeModel model = null;

	public class Stats
	{
		public int missing_set_cnt = 0;
		public int missing_roms_cnt = 0;
		public int missing_disks_cnt = 0;
		public int missing_samples_cnt = 0;

		public int set_unneeded = 0;
		public int set_missing = 0;
		public int set_found = 0;
		public int set_found_ok = 0;
		public int set_found_fixpartial = 0;
		public int set_found_fixcomplete = 0;
		public int set_create = 0;
		public int set_create_partial = 0;
		public int set_create_complete = 0;

		public void clear()
		{
			missing_set_cnt = 0;
			missing_roms_cnt = 0;
			missing_disks_cnt = 0;
			missing_samples_cnt = 0;

			set_unneeded = 0;
			set_missing = 0;
			set_found = 0;
			set_found_ok = 0;
			set_found_fixpartial = 0;
			set_found_fixcomplete = 0;
			set_create = 0;
			set_create_partial = 0;
			set_create_complete = 0;
		}

		public String getStatus()
		{
			return String.format(Messages.getString("Report.Status"), set_found, set_found_ok, set_found_fixpartial, set_found_fixcomplete, set_create, set_create_partial, set_create_complete, set_missing, set_unneeded, set_found + set_create, set_found + set_create + set_missing); //$NON-NLS-1$
		}
	}

	public Report()
	{
		subjects = Collections.synchronizedList(new ArrayList<>());
		subject_hash = Collections.synchronizedMap(new HashMap<>());
		stats = new Stats();
		model = new ReportTreeModel(this);
		model.initClone();
	}

	class FilterPredicate implements Predicate<Subject>
	{
		List<FilterOptions> filterOptions;

		public FilterPredicate(final List<FilterOptions> filterOptions)
		{
			this.filterOptions = filterOptions;
		}

		@Override
		public boolean test(final Subject t)
		{
			if(!filterOptions.contains(FilterOptions.SHOWOK) && t instanceof SubjectSet && ((SubjectSet) t).isOK())
				return false;
			if(filterOptions.contains(FilterOptions.HIDEMISSING) && t instanceof SubjectSet && ((SubjectSet) t).isMissing())
				return false;
			return true;
		}

	}

	private FilterPredicate filterPredicate = new FilterPredicate(new ArrayList<>());

	private Report(final Report report, final List<FilterOptions> filterOptions)
	{
		filterPredicate = new FilterPredicate(filterOptions);
		model = report.model;
		profile = report.profile;
		subjects = report.filter(filterOptions);
		subject_hash = subjects.stream().collect(Collectors.toMap(Subject::getWareName, Function.identity(), (o, n) -> null));
		stats = report.stats;
	}

	public Report clone(final List<FilterOptions> filterOptions)
	{
		return new Report(this, filterOptions);
	}

	public List<Subject> filter(final List<FilterOptions> filterOptions)
	{
		filterPredicate = new FilterPredicate(filterOptions);
		return subjects.stream().filter(filterPredicate).map(s -> s.clone(filterOptions)).collect(Collectors.toList());
	}

	public void setProfile(final Profile profile)
	{
		this.profile = profile;
		reset();
	}

	public void reset()
	{
		subject_hash.clear();
		subjects.clear();
		insert_object_cache.clear();
		stats.clear();
		if(model != null)
			model.filter(filterPredicate.filterOptions);
		flush();
	}

	private StatusHandler statusHandler = null;

	public void setStatusHandler(final StatusHandler handler)
	{
		statusHandler = handler;
	}

	public ReportTreeModel getModel()
	{
		return model;
	}

	public Subject findSubject(final Anyware ware)
	{
		return ware != null ? subject_hash.get(ware.getFullName()) : null;
	}

	public Subject findSubject(final Anyware ware, final Subject def)
	{
		if(ware != null)
		{
			if(subject_hash.containsKey(ware.getFullName()))
				return subject_hash.get(ware.getFullName());
			add(def);
			return def;
		}
		return null;
	}

	private final Map<Integer, Subject> insert_object_cache = Collections.synchronizedMap(new LinkedHashMap<>(250));

	public synchronized boolean add(final Subject subject)
	{
		subject.parent = this;
		if(subject.ware != null)
			subject_hash.put(subject.ware.getFullName(), subject);
		final boolean result = subjects.add(subject);
		final Report clone = (Report) model.getRoot();
		if(this != clone)
		{
			subject.updateStats();
			if(filterPredicate.test(subject))
			{
				final Subject cloned_subject = subject.clone(filterPredicate.filterOptions);
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
			final TreeModelEvent event = new TreeModelEvent(model, model.getPathToRoot((Report) model.getRoot()), IntStreamEx.of(insert_object_cache.keySet()).toArray(), insert_object_cache.values().toArray());
			for(final TreeModelListener l : model.getTreeModelListeners())
				l.treeNodesInserted(event);
		}
		insert_object_cache.clear();
	}

	public void write()
	{
		final File workdir = Settings.getWorkPath().toFile(); //$NON-NLS-1$
		final File reportdir = new File(workdir, "reports"); //$NON-NLS-1$
		reportdir.mkdirs();
		final File report_file = new File(reportdir, "report.log"); //$NON-NLS-1$
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
		catch(final FileNotFoundException e)
		{
			e.printStackTrace();
		}

	}

	@Override
	public TreeNode getChildAt(final int childIndex)
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
	public int getIndex(final TreeNode node)
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
