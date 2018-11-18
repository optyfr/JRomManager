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
import java.util.zip.CRC32;

import jrm.security.Session;

public final class TrntChkReport implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public List<Node> nodes = new ArrayList<>();
	public Map<Long,Node> all = new HashMap<>();
	
	private transient AtomicLong uid_cnt = new AtomicLong(); 
	private transient File file = null;
	private transient long file_modified = 0L;
	
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
	
	public final static class NodeData implements Serializable
	{
		private static final long serialVersionUID = 1L;
		public String title;
		public Long length = null;
		public Status status = Status.UNKNOWN;
	}
	
	public final class Node implements Serializable
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
			return report;
		}
		catch (final Throwable e)
		{
			// may fail to load because serialized classes did change since last cache save 
		}
		return null;
	}

}