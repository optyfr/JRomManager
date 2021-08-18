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

import jrm.aui.batch.TrntChkReportTreeHandler;
import jrm.misc.HTMLRenderer;
import jrm.misc.Log;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Subject;
import jrm.security.Session;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

public final class TrntChkReport implements Serializable, HTMLRenderer
{
	private static final long serialVersionUID = 4L;
	
	
	private transient AtomicLong uidCnt = new AtomicLong(); 
	private transient File file = null;
	private transient long fileModified = 0L;
	
	private @Getter List<Child> nodes = new ArrayList<>();
	private @Getter Map<Long,Child> all = new HashMap<>();

	/**
	 * the linked UI tree model
	 */
	private transient @Setter @Getter TrntChkReportTreeHandler handler = null;

	public TrntChkReport(final File src)
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
			if(filterOptions.contains(FilterOptions.HIDEMISSING) && t.data.status==Status.MISSING)	//NOSONAR
				return false;
			return true;
		}

	}

	/**
	 * the current filter predicate (initialized with an empty {@link List} of {@link FilterOptions})
	 */
	private transient FilterPredicate filterPredicate = new FilterPredicate(new ArrayList<>());

	public static final class ChildData implements Serializable
	{
		private static final long serialVersionUID = 2L;
		private @Getter String title;
		private @Getter @Setter @Accessors(chain=true) Long length = null;
		private @Getter Status status = Status.UNKNOWN;
	}
	
	public final class Child implements Serializable, HTMLRenderer
	{
		private static final long serialVersionUID = 3L;

		private @Getter List<Child> children;

		private @Getter long uid;
		private @Getter Child parent = null;
		private @Getter ChildData data = null;

		public Child()
		{
			uid = uidCnt.incrementAndGet();
			all.put(uid, this);
			this.parent = null;
			this.data = new ChildData();
		}

		public Child(Child parent)
		{
			uid = uidCnt.incrementAndGet();
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
			final StringBuilder sb = new StringBuilder();
			sb.append(String.format("%s%-50s %12d %s%n", parent==null?"":"|_ ", data.title, data.length, data.status));
			if (children != null)
				for (Child child : children)
					sb.append(child);
			return sb.toString();
		}
		
		public Child copy()
		{
			final var node = new Child();
			node.uid = this.uid;
			node.children = this.children;
			node.parent = this.parent;
			node.data = this.data;
			return node;
		}
	}
	
	Child add(String title)
	{
		final Child node = new Child();
		node.data.title = title;
		nodes.add(node);
		return node;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		for (final Child node : nodes)
			sb.append(node);
		return sb.toString();
	}

	public File getFile()
	{
		return this.file;
	}
	
	public long getFileModified()
	{
		return fileModified;
	}
	
	public File getReportFile(final Session session)
	{
		return getReportFile(session, getFile());
	}

	public static File getReportFile(final Session session, final File file)
	{
		final CRC32 crc = new CRC32();
		crc.update(file.getAbsolutePath().getBytes());
		final File reports = session.getUser().getSettings().getWorkPath().resolve("reports").toFile(); //$NON-NLS-1$
		reports.mkdirs();
		return new File(reports, String.format("%08x", crc.getValue()) + ".tc_report"); //$NON-NLS-1$
	}

	public void save(final File file)
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file))))
		{
			oos.writeObject(TrntChkReport.this);
		}
		catch (final Exception e)
		{
			Log.warn(e.getMessage());
		}
	}
	
	public static TrntChkReport load(final Session session, final File file)
	{
		try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getReportFile(session, file)))))
		{
			final TrntChkReport report = (TrntChkReport)ois.readObject();
			report.file = file;
			report.fileModified = getReportFile(session, file).lastModified();
			return report;
		}
		catch (final Exception e)
		{
			Log.warn(e.getMessage());
			// may fail to load because serialized classes did change since last cache save 
		}
		return null;
	}

	
	private TrntChkReport(TrntChkReport report, List<FilterOptions> filterOptions)
	{
		filterPredicate = report.filterPredicate;
		fileModified = report.fileModified;
		uidCnt = new AtomicLong();
		handler = report.handler;
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