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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jrm.aui.profile.report.ReportTreeDefaultHandler;
import jrm.aui.profile.report.ReportTreeHandler;
import jrm.aui.progress.StatusHandler;
import jrm.locale.Messages;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.security.Session;
import lombok.Getter;
import lombok.Setter;
import one.util.streamex.IntStreamEx;

/**
 * The report node root
 * @author optyfr
 *
 */
public class Report extends AbstractList<Subject> implements HTMLRenderer, Serializable, ReportFile
{
	private static final String STATS_STR = "stats";
	private static final String SUBJECTS_STR = "subjects";
	private static final long serialVersionUID = 2L;
	/**
	 * the related {@link Profile}
	 */
	private transient Profile profile = null;
	private transient File file = null;
	private transient long fileModified = 0L;
	/**
	 * the {@link List} of {@link Subject} nodes
	 */
	private @Getter List<Subject> subjects;

	/**
	 * a {@link Map} of {@link Subject} by fullname {@link String}
	 */
	private transient Map<String, Subject> subjectHash;


	private transient int id;
	private transient AtomicInteger idCnt;
	private transient Map<Integer,Object> all;
	
	/**
	 * The {@link Stats} object
	 */
	private @Getter Stats stats;

	/**
	 * the linked UI tree model
	 */
	private transient ReportTreeHandler handler = null;

	
	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
		new ObjectStreamField(SUBJECTS_STR, List.class),
		new ObjectStreamField(STATS_STR, Stats.class)
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put(SUBJECTS_STR, subjects); //$NON-NLS-1$
		fields.put(STATS_STR, stats); //$NON-NLS-1$
		stream.writeFields();
	}

	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		subjects = (List<Subject>) fields.get(SUBJECTS_STR, Collections.synchronizedList(new ArrayList<>())); //$NON-NLS-1$
		stats = (Stats) fields.get(STATS_STR, new Stats()); //$NON-NLS-1$
		subjectHash = subjects.stream()
				.peek(s -> s.parent = this)	//NOSONAR
				.collect(Collectors.toMap(Subject::getWareName, Function.identity(), (o, n) -> null));
		filterPredicate = new FilterPredicate(new ArrayList<>());
		handler = new ReportTreeDefaultHandler(this);
	}

	public static class Stats implements Serializable
	{
		private static final long serialVersionUID = 2L;
		
		private @Getter @Setter int missingSetCnt = 0;
		private @Getter @Setter int missingRomsCnt = 0;
		private @Getter @Setter int missingDisksCnt = 0;
		private @Getter @Setter int missingSamplesCnt = 0;

		private @Getter @Setter int setUnneeded = 0;
		private @Getter @Setter int setMissing = 0;
		private @Getter @Setter int setFound = 0;
		private @Getter @Setter int setFoundOk = 0;
		private @Getter @Setter int setFoundFixPartial = 0;
		private @Getter @Setter int setFoundFixComplete = 0;
		private @Getter @Setter int setCreate = 0;
		private @Getter @Setter int setCreatePartial = 0;
		private @Getter @Setter int setCreateComplete = 0;

		public Stats(Stats org)
		{
			this.missingSetCnt = org.missingSetCnt;
			this.missingRomsCnt = org.missingRomsCnt;
			this.missingDisksCnt = org.missingDisksCnt;
			this.missingSamplesCnt = org.missingSamplesCnt;
			this.setUnneeded = org.setUnneeded;
			this.setMissing = org.setMissing;
			this.setFound = org.setFound;
			this.setFoundOk = org.setFoundOk;
			this.setFoundFixPartial = org.setFoundFixPartial;
			this.setFoundFixComplete = org.setFoundFixComplete;
			this.setCreate = org.setCreate;
			this.setCreatePartial = org.setCreatePartial;
			this.setCreateComplete = org.setCreateComplete;
		}
		
		public Stats()
		{
			
		}

		public void incMissingSetCnt()
		{
			++missingSetCnt;
		}
		
		public void incMissingRomsCnt()
		{
			++missingRomsCnt;
		}
		
		public void incMissingDisksCnt()
		{
			++missingDisksCnt;
		}
		
		public void incMissingSamplesCnt()
		{
			++missingSamplesCnt;
		}
		
		public void incSetUnneeded()
		{
			++setUnneeded;
		}
		
		public void incSetMissing()
		{
			++setMissing;
		}
		
		public void incSetFound()
		{
			++setFound;
		}
		
		public void incSetFoundOk()
		{
			++setFoundOk;
		}
		
		public void incSetFoundFixPartial()
		{
			++setFoundFixPartial;
		}
		
		public void incSetFoundFixComplete()
		{
			++setFoundFixComplete;
		}
		
		public void incSetCreate()
		{
			++setCreate;
		}
		
		public void incSetCreatePartial()
		{
			++setCreatePartial;
		}
		
		public void incSetCreateComplete()
		{
			++setCreateComplete;
		}
		
		/**
		 * clear stats
		 */
		public void clear()
		{
			/**
			 * number of missing sets
			 */
			missingSetCnt = 0;
			/**
			 * number of missing roms
			 */
			missingRomsCnt = 0;
			/**
			 * number of missing disks
			 */
			missingDisksCnt = 0;
			/**
			 * number of missing samples
			 */
			missingSamplesCnt = 0;

			/**
			 * number of unneeded set
			 */
			setUnneeded = 0;
			/**
			 * number of missing set
			 */
			setMissing = 0;
			/**
			 * number of set found
			 */
			setFound = 0;
			/**
			 * number of set found ok
			 */
			setFoundOk = 0;
			/**
			 * number of set found with partial fix
			 */
			setFoundFixPartial = 0;
			/**
			 * number of set found with complete fix
			 */
			setFoundFixComplete = 0;
			/**
			 * number of set that will be created
			 */
			setCreate = 0;
			/**
			 * number of set that will be created partially
			 */
			setCreatePartial = 0;
			/**
			 * number of set that will be fully created
			 */
			setCreateComplete = 0;
		}

		/**
		 * get a {@link String} status with all stats to be show in a status bar
		 * @return a {@link String} containing all stats
		 */
		public String getStatus()
		{
			return String.format(Messages.getString("Report.Status"), setFound, setFoundOk, setFoundFixPartial, setFoundFixComplete, setCreate, setCreatePartial, setCreateComplete, setMissing, setUnneeded, setFound + setCreate, setFound + setCreate + setMissing); //$NON-NLS-1$
		}
	}

	/**
	 * The constructor (init data)
	 */
	public Report()
	{
		subjects = Collections.synchronizedList(new ArrayList<>());
		subjectHash = Collections.synchronizedMap(new HashMap<>());
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
			if(filterOptions.contains(FilterOptions.HIDEMISSING) && t instanceof SubjectSet && ((SubjectSet) t).isMissing())	//NOSONAR
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
		idCnt = new AtomicInteger();
		all = new HashMap<>();
		id = idCnt.getAndIncrement();
		all.put(id, this);
		handler = report.handler;
		profile = report.profile;
		subjects = report.filter(filterOptions);
		for(Subject s : subjects)
		{
			s.id = idCnt.getAndIncrement();
			all.put(s.id, s);
			for(Note n : s)
			{
				n.id = idCnt.getAndIncrement();
				all.put(n.id, n);
			}
		}
		subjectHash = subjects.stream().collect(Collectors.toMap(Subject::getWareName, Function.identity(), (o, n) -> null));
		stats = report.stats;
		file = report.file;
		fileModified = report.fileModified;
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
		subjectHash.clear();
		subjects.clear();
		insertObjectCache.clear();
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
	 * get the current {@link ReportTreeHandler}
	 * @return a {@link ReportTreeHandler}
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
		return null;	//NOSONAR
	}

	/**
	 * find {@link Subject} from an {@link Anyware}
	 * @param ware the {@link Anyware} to find {@link Subject}
	 * @return the found {@link Subject} or null
	 */
	public Subject findSubject(final Anyware ware)
	{
		return ware != null ? subjectHash.get(ware.getFullName()) : null;
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
			if(subjectHash.containsKey(ware.getFullName()))
				return subjectHash.get(ware.getFullName());
			add(def);
			return def;
		}
		return null;	//NOSONAR
	}

	/**
	 * cache made to trigger ui notification events as few as possible
	 */
	private final transient Map<Integer, Subject> insertObjectCache = Collections.synchronizedMap(new LinkedHashMap<>(250));

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
			subject.id = idCnt.getAndIncrement();
			all.put(subject.id,subject);
			for(Note n : subject)
				n.id = idCnt.getAndIncrement();
		}
		if(subject.ware != null)	// add to subject_hash if there is a subject.ware
			subjectHash.put(subject.ware.getFullName(), subject);
		final boolean result = subjects.add(subject); // add to subjects list and keep result
		final Report clone = handler.getFilteredReport();	// get model Report clone (filtered one)
		if(this != clone)	// if this report is not already the clone itself then update clone
		{
			subject.updateStats();
			if(filterPredicate.test(subject))	// manually test predicate
			{
				final Subject clonedSubject = subject.clone(filterPredicate.filterOptions);	// clone the subject according filterPredicate
				clone.add(clonedSubject);	// then call this method on clone object
				insertObjectCache.put(clone.subjects.size() - 1, clonedSubject);	// insert cloned subject into insert event cache
				if(insertObjectCache.size() >= 250)	// and call flush only if the event cache is at least 250 objects
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
		if(insertObjectCache.size() > 0)
		{
			if(handler.hasListeners())
				handler.notifyInsertion(IntStreamEx.of(insertObjectCache.keySet()).toArray(), insertObjectCache.values().toArray());
			insertObjectCache.clear();
		}
	}

	/**
	 * write a textual report to reports/report.log
	 */
	public void write(final Session session)
	{
		final File workdir = session.getUser().getSettings().getWorkPath().toFile(); //$NON-NLS-1$
		final File reportdir = new File(workdir, "reports"); //$NON-NLS-1$
		reportdir.mkdirs();
		final File reportFile = new File(reportdir, "report-"+new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+".log"); //$NON-NLS-1$
		try(PrintWriter reportWriter = new PrintWriter(reportFile))
		{
			reportWriter.println("=== Scanned Profile ===");
			reportWriter.println(profile.getNfo().getFile());
			reportWriter.println();
			reportWriter.println("=== Used Profile Properties ===");
			profile.getSettings().getProperties().store(reportWriter, null);
			reportWriter.println();
			reportWriter.println("=== Scanner Report ===");
			subjects.forEach(subject -> {
				reportWriter.println(subject);
				subject.notes.forEach(note -> reportWriter.println("\t" + note));	//$NON-NLS-1$
			});
			reportWriter.println();
			reportWriter.println("=== Statistics ===");
			reportWriter.println(String.format(Messages.getString("Report.MissingSets"), stats.missingSetCnt, profile.getMachinesCnt())); //$NON-NLS-1$
			reportWriter.println(String.format(Messages.getString("Report.MissingRoms"), stats.missingRomsCnt, profile.getRomsCnt())); //$NON-NLS-1$
			reportWriter.println(String.format(Messages.getString("Report.MissingDisks"), stats.missingDisksCnt, profile.getDisksCnt())); //$NON-NLS-1$
			int total = stats.setCreate + stats.setFound + stats.setMissing;
			int ok = stats.setCreateComplete + stats.setFoundFixComplete + stats.setFoundOk;
			reportWriter.println(String.format("Missing sets after Fix : %d%n", total - ok)); //$NON-NLS-1$
		}
		catch(final IOException e)
		{
			Log.err(e.getMessage(),e);
		}

	}

	@Override
	public File getFile()
	{
		return this.profile!=null?this.profile.getNfo().getFile():this.file;
	}
	
	@Override
	public long getFileModified()
	{
		return fileModified;
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
		catch (final Exception e)
		{
			// do nothing
		}
	}
	
	public static Report load(final Session session, final File file)
	{
		try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(ReportFile.getReportFile(session, file)))))
		{
			Report report = (Report)ois.readObject();
			report.file = file;
			report.fileModified = ReportFile.getReportFile(session, file).lastModified();
			report.handler = new ReportTreeDefaultHandler(report);
			return report;
		}
		catch (final Exception e)
		{
			// may fail to load because serialized classes did change since last cache save 
		}
		return null;	//NOSONAR
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
	
	@Override
	public boolean equals(Object o)
	{
		return super.equals(o);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
}
