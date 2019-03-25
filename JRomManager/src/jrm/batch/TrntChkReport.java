package jrm.batch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Subject;
import jrm.security.Session;

public final class TrntChkReport implements Serializable, HTMLRenderer
{
	private static final long serialVersionUID = 2L;
	
	public List<Child> nodes = new ArrayList<>();
	public Map<Long,Child> all = new HashMap<>();
	
	private transient AtomicLong uid_cnt = new AtomicLong(); 
	private transient File file = null;
	private transient long file_modified = 0L;
	
	/**
	 * the linked UI tree model
	 */
	public TrntChkReport(File src)
	{
		this.file = src;
	}
	
	public enum Status
	{
		OK,
		SIZE,
		SHA1,
		MISSING,
		SKIPPED,
		UNKNOWN
	}
	
	/**
	 * A class that is a filter {@link Predicate} for {@link Subject}
	 * @author optyfr
	 *
	 */
	class FilterPredicate implements Predicate<Child>
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
		public boolean test(final Child t)
		{
			if(!filterOptions.contains(FilterOptions.SHOWOK) && t.data.status==Status.OK)
				return false;
			if(filterOptions.contains(FilterOptions.HIDEMISSING) && t.data.status==Status.MISSING)
				return false;
			return true;
		}

	}

	/**
	 * the current filter predicate (initialized with an empty {@link List} of {@link FilterOptions})
	 */
	private transient FilterPredicate filterPredicate = new FilterPredicate(new ArrayList<>());

	public final static class ChildData implements Serializable
	{
		private static final long serialVersionUID = 1L;
		public String title;
		public Long length = null;
		public Status status = Status.UNKNOWN;
	}
	
	public final class Child implements Serializable, HTMLRenderer
	{
		private static final long serialVersionUID = 2L;

		public List<Child> children;

		public long uid;
		public Child parent = null;
		public ChildData data = null;

		public Child()
		{
			uid = uid_cnt.incrementAndGet();
			all.put(uid, this);
			this.parent = null;
			this.data = new ChildData();
		}

		public Child(Child parent)
		{
			uid = uid_cnt.incrementAndGet();
			all.put(uid, this);
			this.parent = parent;
			this.data = new ChildData();
		}

		Child add(String title)
		{
			Child node = new Child(this);
			node.data.title = title;
			if (children == null)
				children = new ArrayList<>();
			children.add(node);
			return node;
		}
		
		Child add(Child org)
		{
			Child node = new Child(this);
			node.data = org.data;
			if (children == null)
				children = new ArrayList<>();
			children.add(node);
			return node;
		}
		
		void setStatus(TrntChkReport.Status status)
		{
			data.status = status;
			if (children != null)
			{
				children.forEach(n -> {
					if (n.data.status == Status.UNKNOWN || n.data.status == Status.OK)
						n.data.status = status;
				});
			}
		}

		@Override
		public String toString()
		{
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("%s%-50s %12d %s\n", parent==null?"":"|_ ", data.title, data.length, data.status));
			if (children != null)
				for (Child child : children)
					sb.append(child);
			return sb.toString();
		}
		
		public Child clone()
		{
			Child node = new Child();
			node.uid = this.uid;
			node.children = this.children;
			node.parent = this.parent;
			node.data = this.data;
			return node;
		}
	}
	
	Child add(String title)
	{
		Child node = new Child();
		node.data.title = title;
		nodes.add(node);
		return node;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (Child node : nodes)
			sb.append(node);
		return sb.toString();
	}

	public File getFile()
	{
		return this.file;
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
		return new File(reports, String.format("%08x", crc.getValue()) + ".tc_report"); //$NON-NLS-1$
	}

	public void save(File file)
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
		{
			oos.writeObject(TrntChkReport.this);
		}
		catch (final Throwable e)
		{
		}
	}
	
	public static TrntChkReport load(final Session session, final File file)
	{
		try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getReportFile(session, file)))))
		{
			TrntChkReport report = (TrntChkReport)ois.readObject();
			report.file = file;
			report.file_modified = getReportFile(session, file).lastModified();
			return report;
		}
		catch (final Throwable e)
		{
			Log.err(e.getMessage(),e);
			// may fail to load because serialized classes did change since last cache save 
		}
		return null;
	}

	
	private TrntChkReport(TrntChkReport report, List<FilterOptions> filterOptions)
	{
		filterPredicate = report.filterPredicate;
		file_modified = report.file_modified;
		uid_cnt = new AtomicLong();
		nodes = report.filter(filterOptions);
		all = report.all;
	}
	
	public TrntChkReport clone(List<FilterOptions> filterOptions)
	{
		return new TrntChkReport(this, filterOptions);
	}
	
	/**
	 * Filter subjects using current {@link FilterPredicate}
	 * @param filterOptions the {@link FilterOptions} {@link List} to apply
	 * @return a {@link List} of {@link Subject}
	 */
	public List<Child> filter(final List<FilterOptions> filterOptions)
	{
		filterPredicate = new FilterPredicate(filterOptions);
		return nodes.stream().filter(filterPredicate)/*.map(n -> n.clone())*/.collect(Collectors.toList());
	}

}