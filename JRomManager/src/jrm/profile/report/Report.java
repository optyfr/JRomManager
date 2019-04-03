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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import javax.swing.tree.TreeNode;

import jrm.locale.Messages;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.security.Session;
import jrm.ui.profile.report.ReportTreeDefaultHandler;
import jrm.ui.profile.report.ReportTreeHandler;
import jrm.ui.progress.StatusHandler;
import one.util.streamex.IntStreamEx;

/**
 * The report node root
 * @author optyfr
 *
 */
public class Report extends AbstractList<Subject> implements TreeNode, HTMLRenderer, Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * the related {@link Profile}
	 */
	private transient Profile profile = null;
	private transient File file = null;
	private transient long file_modified = 0L;
	/**
	 * the {@link List} of {@link Subject} nodes
	 */
	private List<Subject> subjects;

	/**
	 * a {@link Map} of {@link Subject} by fullname {@link String}
	 */
	private transient Map<String, Subject> subject_hash;


	private transient int id;
	private transient AtomicInteger id_cnt;
	private transient Map<Integer,Object> all;
	
	/**
	 * The {@link Stats} object
	 */
	public Stats stats;

	/**
	 * the linked UI tree model
	 */
	private transient ReportTreeHandler handler = null;

	
	private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("subjects", List.class), new ObjectStreamField("stats", Stats.class)}; //$NON-NLS-1$ //$NON-NLS-2$

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("subjects", subjects); //$NON-NLS-1$
		fields.put("stats", stats); //$NON-NLS-1$
		stream.writeFields();
	}

	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		subjects = (List<Subject>) fields.get("subjects", Collections.synchronizedList(new ArrayList<>())); //$NON-NLS-1$
		stats = (Stats) fields.get("stats", new Stats()); //$NON-NLS-1$
		subject_hash = subjects.stream().peek(s->s.parent=this).collect(Collectors.toMap(Subject::getWareName, Function.identity(), (o, n) -> null));
		filterPredicate = new FilterPredicate(new ArrayList<>());
		handler = new ReportTreeDefaultHandler(this);
	}

	public class Stats implements Serializable,Cloneable
	{
		private static final long serialVersionUID = 1L;
		
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

		@Override
		public Stats clone() throws CloneNotSupportedException
		{
			return (Stats)super.clone();
		}
		
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
		handler = new ReportTreeDefaultHandler(this);
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
	private transient FilterPredicate filterPredicate = new FilterPredicate(new ArrayList<>());

	/**
	 * The internal constructor aimed for cloning
	 * @param report The {@link Report} to copy
	 * @param filterOptions the {@link FilterOptions} {@link List} to apply
	 */
	private Report(final Report report, final List<FilterOptions> filterOptions)
	{
		filterPredicate = new FilterPredicate(filterOptions);
		id_cnt = new AtomicInteger();
		all = new HashMap<>();
		id = id_cnt.getAndIncrement();
		all.put(id, this);
		handler = report.handler;
		profile = report.profile;
		subjects = report.filter(filterOptions);
		for(Subject s : subjects)
		{
			all.put(s.id = id_cnt.getAndIncrement(), s);
			for(Note n : s)
				all.put(n.id = id_cnt.getAndIncrement(), n);
		}
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
		if(handler != null)
			handler.filter(filterPredicate.filterOptions);
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
	public ReportTreeHandler getHandler()
	{
		return handler;
	}

	public void setHandler(ReportTreeHandler handler)
	{
		this.handler = handler;
	}
	
	/**
	 * find {@link Subject} from an id
	 * @param id the id to find {@link Subject}
	 * @return the found {@link Subject} or null
	 */
	public Subject findSubject(final Integer id)
	{
		Object obj = all.get(id);
		if(obj instanceof Subject)
			return (Subject) obj;
		return null;
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
	 * cache made to trigger ui notification events as few as possible
	 */
	private transient final Map<Integer, Subject> insert_object_cache = Collections.synchronizedMap(new LinkedHashMap<>(250));

	/**
	 * add a {@link Subject} to the Report
	 * @param subject the {@link Subject} to add
	 * @return true if success
	 */
	@Override
	public synchronized boolean add(final Subject subject)
	{
		subject.parent = this;	// initialize subject.parent
		if(all!=null)
		{
			all.put(subject.id = id_cnt.getAndIncrement(),subject);
			for(Note n : subject)
				n.id = id_cnt.getAndIncrement();
		}
		if(subject.ware != null)	// add to subject_hash if there is a subject.ware
			subject_hash.put(subject.ware.getFullName(), subject);
		final boolean result = subjects.add(subject); // add to subjects list and keep result
		final Report clone = handler.getFilteredReport();	// get model Report clone (filtered one)
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
	 * flush the current object cache by generating a ui insertion event to send to all listeners available
	 */
	public synchronized void flush()
	{
		if(statusHandler != null)
			statusHandler.setStatus(stats.getStatus());
		if(insert_object_cache.size() > 0)
		{
			if(handler.hasListeners())
				handler.notifyInsertion(IntStreamEx.of(insert_object_cache.keySet()).toArray(), insert_object_cache.values().toArray());
			insert_object_cache.clear();
		}
	}

	/**
	 * write a textual report to reports/report.log
	 */
	public void write(final Session session)
	{
		final File workdir = session.getUser().settings.getWorkPath().toFile(); //$NON-NLS-1$
		final File reportdir = new File(workdir, "reports"); //$NON-NLS-1$
		reportdir.mkdirs();
		final File report_file = new File(reportdir, "report-"+new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+".log"); //$NON-NLS-1$
		try(PrintWriter report_w = new PrintWriter(report_file))
		{
			report_w.println("=== Scanned Profile ===");
			report_w.println(profile.nfo.file);
			report_w.println();
			report_w.println("=== Used Profile Properties ===");
			profile.settings.getProperties().store(report_w, null);
			report_w.println();
			report_w.println("=== Scanner Report ===");
			subjects.forEach(subject -> {
				report_w.println(subject);
				subject.notes.forEach(note -> {
					report_w.println("\t" + note); //$NON-NLS-1$
				});
			});
			report_w.println();
			report_w.println("=== Statistics ===");
			report_w.println(String.format(Messages.getString("Report.MissingSets"), stats.missing_set_cnt, profile.machines_cnt)); //$NON-NLS-1$
			report_w.println(String.format(Messages.getString("Report.MissingRoms"), stats.missing_roms_cnt, profile.roms_cnt)); //$NON-NLS-1$
			report_w.println(String.format(Messages.getString("Report.MissingDisks"), stats.missing_disks_cnt, profile.disks_cnt)); //$NON-NLS-1$
			int total = stats.set_create + stats.set_found + stats.set_missing;
			int ok = stats.set_create_complete + stats.set_found_fixcomplete + stats.set_found_ok;
			report_w.println(String.format("Missing sets after Fix : %d\n", total - ok)); //$NON-NLS-1$
		}
		catch(final FileNotFoundException e)
		{
			Log.err(e.getMessage(),e);
		}
		catch(final IOException e)
		{
			Log.err(e.getMessage(),e);
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

	public File getFile()
	{
		return this.profile!=null?this.profile.nfo.file:this.file;
	}
	
	public long getFileModified()
	{
		return file_modified;
	}
	
	public File getReportFile(final Session session)
	{
		return getReportFile(session, getFile());
	}

	public static File getReportFile(final Session session, final File file)
	{
		final CRC32 crc = new CRC32();
		crc.update(file.getAbsolutePath().getBytes());
		final File reports = session.getUser().settings.getWorkPath().resolve("reports").toFile(); //$NON-NLS-1$
		reports.mkdirs();
		return new File(reports, String.format("%08x", crc.getValue()) + ".report"); //$NON-NLS-1$
	}

	public void save(final Session session)
	{
		save(getReportFile(session));
	}
	
	public void save(File file)
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
		{
			oos.writeObject(Report.this);
		}
		catch (final Throwable e)
		{
		}
	}
	
	public static Report load(final Session session, final File file)
	{
		try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getReportFile(session, file)))))
		{
			Report report = (Report)ois.readObject();
			report.file = file;
			report.file_modified = getReportFile(session, file).lastModified();
			report.handler = new ReportTreeDefaultHandler(report);
			return report;
		}
		catch (final Throwable e)
		{
			// may fail to load because serialized classes did change since last cache save 
		}
		return null;
	}

	public int getId()
	{
		return id;
	}

	@Override
	public Subject get(int index)
	{
		return subjects.get(index);
	}

	@Override
	public int size()
	{
		return subjects.size();
	}
	
	@Override
	public String toString()
	{
		return "Report";
	}
	
}
