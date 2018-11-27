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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

import javax.swing.tree.TreeNode;

import jrm.misc.HTMLRenderer;
import jrm.profile.report.FilterOptions;
import jrm.profile.report.Subject;
import jrm.security.Session;
import jrm.ui.batch.BatchTrrntChkReportTreeModel;

public final class TrntChkReport implements Serializable, TreeNode, HTMLRenderer
{
	private static final long serialVersionUID = 1L;
	
	public List<Node> nodes = new ArrayList<>();
	public Map<Long,Node> all = new HashMap<>();
	
	private transient AtomicLong uid_cnt = new AtomicLong(); 
	private transient File file = null;
	private transient long file_modified = 0L;
	
	/**
	 * the linked UI tree model
	 */
	private transient BatchTrrntChkReportTreeModel model = null;

	public TrntChkReport(File src)
	{
		this.file = src;
		this.model = new BatchTrrntChkReportTreeModel(this);
		this.model.initClone();
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
	class FilterPredicate implements Predicate<Node>
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
		public boolean test(final Node t)
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

	public final static class NodeData implements Serializable
	{
		private static final long serialVersionUID = 1L;
		public String title;
		public Long length = null;
		public Status status = Status.UNKNOWN;
	}
	
	public final class Node implements Serializable, TreeNode, HTMLRenderer
	{
		private static final long serialVersionUID = 1L;

		public List<Node> children;

		public long uid;
		public Node parent = null;
		public NodeData data = null;

		public Node()
		{
			uid = uid_cnt.incrementAndGet();
			all.put(uid, this);
			this.parent = null;
			this.data = new NodeData();
		}

		public Node(Node parent)
		{
			uid = uid_cnt.incrementAndGet();
			all.put(uid, this);
			this.parent = parent;
			this.data = new NodeData();
		}

		Node add(String title)
		{
			Node node = new Node(this);
			node.data.title = title;
			if (children == null)
				children = new ArrayList<>();
			children.add(node);
			return node;
		}
		
		Node add(Node org)
		{
			Node node = new Node(this);
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
				for (Node child : children)
					sb.append(child);
			return sb.toString();
		}

		@Override
		public TreeNode getChildAt(int childIndex)
		{
			return children==null?null:children.get(childIndex);
		}

		@Override
		public int getChildCount()
		{
			return children==null?0:children.size();
		}

		@Override
		public TreeNode getParent()
		{
			return parent;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return children==null?-1:children.indexOf(node);
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return children==null||children.size()==0;
		}

		@Override
		public Enumeration<Node> children()
		{
			return children==null?null:Collections.enumeration(children);
		}
		
		public Node clone()
		{
			Node node = new Node();
			node.uid = this.uid;
			node.children = this.children;
			node.parent = this.parent;
			node.data = this.data;
			return node;
		}
	}
	
	Node add(String title)
	{
		Node node = new Node();
		node.data.title = title;
		nodes.add(node);
		return node;
	}
	
	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		for (Node node : nodes)
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
			report.model = new BatchTrrntChkReportTreeModel(report);
			report.model.initClone();
			return report;
		}
		catch (final Throwable e)
		{
			e.printStackTrace();
			// may fail to load because serialized classes did change since last cache save 
		}
		return null;
	}

	@Override
	public TreeNode getChildAt(int childIndex)
	{
		return nodes.get(childIndex);
	}

	@Override
	public int getChildCount()
	{
		return nodes.size();
	}

	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return nodes.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return nodes.size()==0;
	}

	@Override
	public Enumeration<Node> children()
	{
		return Collections.enumeration(nodes);
	}

	public BatchTrrntChkReportTreeModel getModel()
	{
		return this.model;
	}
	
	private TrntChkReport(TrntChkReport report, List<FilterOptions> filterOptions)
	{
		filterPredicate = report.filterPredicate;
		file_modified = report.file_modified;
		uid_cnt = new AtomicLong();
		nodes = report.filter(filterOptions);
		model = report.model;
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
	public List<Node> filter(final List<FilterOptions> filterOptions)
	{
		filterPredicate = new FilterPredicate(filterOptions);
		return nodes.stream().filter(filterPredicate)/*.map(n -> n.clone())*/.collect(Collectors.toList());
	}

}