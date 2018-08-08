/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
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
import jrm.misc.Settings;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.ui.profile.report.ReportTreeModel;
import jrm.ui.progress.StatusHandler;
import one.util.streamex.IntStreamEx;

/**
 * The report node root
 * @author optyfr
 *
 */
public class Report implements TreeNode, HTMLRenderer
{
	/**
	 * the related {@link Profile}
	 */
	private Profile profile;
	/**
	 * the {@link List} of {@link Subject} nodes
	 */
	private final List<Subject> subjects;
	/**
	 * a {@link Map} of {@link Subject} by fullname {@link String}
	 */
	private final Map<String, Subject> subject_hash;
	/**
	 * The {@link Stats} object
	 */
	public final Stats stats;

	/**
	 * the linked UI tree model
	 */
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

		/**
		 * clear stats
		 */
		public void clear()
		{
			/**
			 * number of missing sets
			 */
			missing_set_cnt = 0;
			/**
			 * number of missing roms
			 */
			missing_roms_cnt = 0;
			/**
			 * number of missing disks
			 */
			missing_disks_cnt = 0;
			/**
			 * number of missing samples
			 */
			missing_samples_cnt = 0;

			/**
			 * number of unneeded set
			 */
			set_unneeded = 0;
			/**
			 * number of missing set
			 */
			set_missing = 0;
			/**
			 * number of set found
			 */
			set_found = 0;
			/**
			 * number of set found ok
			 */
			set_found_ok = 0;
			/**
			 * number of set found with partial fix
			 */
			set_found_fixpartial = 0;
			/**
			 * number of set found with complete fix
			 */
			set_found_fixcomplete = 0;
			/**
			 * number of set that will be created
			 */
			set_create = 0;
			/**
			 * number of set that will be created partially
			 */
			set_create_partial = 0;
			/**
			 * number of set that will be fully created
			 */
			set_create_complete = 0;
		}

		/**
		 * get a {@link String} status with all stats to be show in a status bar
		 * @return a {@link String} containing all stats
		 */
		public String getStatus()
		{
			return String.format(Messages.getString("Report.Status"), set_found, set_found_ok, set_found_fixpartial, set_found_fixcomplete, set_create, set_create_partial, set_create_complete, set_missing, set_unneeded, set_found + set_create, set_found + set_create + set_missing); //$NON-NLS-1$
		}
	}

	/**
	 * The constructor (init data)
	 */
	public Report()
	{
		subjects = Collections.synchronizedList(new ArrayList<>());
		subject_hash = Collections.synchronizedMap(new HashMap<>());
		stats = new Stats();
		model = new ReportTreeModel(this);
		model.initClone();
	}

	/**
	 * A class that is a filter {@link Predicate} for {@link Subject}
	 * @author optyfr
	 *
	 */
	class FilterPredicate implements Predicate<Subject>
	{
		/**
		 * {@link List} of {@link FilterOptions}
		 */
		List<FilterOptions> filterOptions;

		/**
		 * The predicate constructor
		 * @param filterOptions {@link List} of {@link FilterOptions} to test against
		 */
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

	/**
	 * the current filter predicate (initialized with an empty {@link List} of {@link FilterOptions})
	 */
	private FilterPredicate filterPredicate = new FilterPredicate(new ArrayList<>());

	/**
	 * The internal constructor aimed for cloning
	 * @param report The {@link Report} to copy
	 * @param filterOptions the {@link FilterOptions} {@link List} to apply
	 */
	private Report(final Report report, final List<FilterOptions> filterOptions)
	{
		filterPredicate = new FilterPredicate(filterOptions);
		model = report.model;
		profile = report.profile;
		subjects = report.filter(filterOptions);
		subject_hash = subjects.stream().collect(Collectors.toMap(Subject::getWareName, Function.identity(), (o, n) -> null));
		stats = report.stats;
	}

	/**
	 * Clone this {@link Report} according a {@link List} of {@link FilterOptions}
	 * @param filterOptions the {@link FilterOptions} {@link List} to apply
	 * @return the cloned {@link Report}
	 */
	public Report clone(final List<FilterOptions> filterOptions)
	{
		return new Report(this, filterOptions);
	}

	/**
	 * Filter subjects using current {@link FilterPredicate}
	 * @param filterOptions the {@link FilterOptions} {@link List} to apply
	 * @return a {@link List} of {@link Subject}
	 */
	public List<Subject> filter(final List<FilterOptions> filterOptions)
	{
		filterPredicate = new FilterPredicate(filterOptions);
		return subjects.stream().filter(filterPredicate).map(s -> s.clone(filterOptions)).collect(Collectors.toList());
	}

	/**
	 * Set the current profile
	 * @param profile {@link Profile}
	 */
	public void setProfile(final Profile profile)
	{
		this.profile = profile;
		reset();
	}

	/**
	 * Reset the report
	 */
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

	/**
	 * the link to UI status handler
	 */
	private StatusHandler statusHandler = null;

	/**
	 * Set the {@link StatusHandler}
	 * @param handler the {@link StatusHandler} that will be used
	 */
	public void setStatusHandler(final StatusHandler handler)
	{
		statusHandler = handler;
	}

	/**
	 * get the current {@link ReportTreeModel}
	 * @return a {@link ReportTreeModel}
	 */
	public ReportTreeModel getModel()
	{
		return model;
	}

	/**
	 * find {@link Subject} from an {@link Anyware}
	 * @param ware the {@link Anyware} to find {@link Subject}
	 * @return the found {@link Subject} or null
	 */
	public Subject findSubject(final Anyware ware)
	{
		return ware != null ? subject_hash.get(ware.getFullName()) : null;
	}

	/**
	 * find {@link Subject} from an {@link Anyware} or return a default {@link Subject}
	 * @param ware the {@link Anyware} to find {@link Subject}
	 * @param def a default {@link Subject} to return in case there is no {@link Subject} for this {@link Anyware}
	 * @return a {@link Subject} or null if ware is null
	 */
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

	/**
	 * cache made to trigger {@link TreeModelEvent} as few as possible
	 */
	private final Map<Integer, Subject> insert_object_cache = Collections.synchronizedMap(new LinkedHashMap<>(250));

	/**
	 * add a {@link Subject} to the Report
	 * @param subject the {@link Subject} to add
	 * @return true if success
	 */
	public synchronized boolean add(final Subject subject)
	{
		subject.parent = this;	// initialize subject.parent
		if(subject.ware != null)	// add to subject_hash if there is a subject.ware
			subject_hash.put(subject.ware.getFullName(), subject);
		final boolean result = subjects.add(subject); // add to subjects list and keep result
		final Report clone = (Report) model.getRoot();	// get model Report clone (filtered one)
		if(this != clone)	// if this report is not already the clone itself then update clone
		{
			subject.updateStats();
			if(filterPredicate.test(subject))	// manually test predicate
			{
				final Subject cloned_subject = subject.clone(filterPredicate.filterOptions);	// clone the subject according filterPredicate
				clone.add(cloned_subject);	// then call this method on clone object
				insert_object_cache.put(clone.subjects.size() - 1, cloned_subject);	// insert cloned subject into insert event cache
				if(insert_object_cache.size() >= 250)	// and call flush only if the event cache is at least 250 objects
					flush();
			}
		}
		return result;
	}

	/**
	 * flush the current object cache by generating a {@link TreeModelEvent} to send to all {@link TreeModelListener}s available
	 */
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

	/**
	 * write a textual report to reports/report.log
	 */
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
