package jrm.ui.profile.report;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.TreeNode;

import jrm.profile.report.Note;
import jrm.profile.report.Report;
import jrm.profile.report.Subject;
import lombok.Getter;

public class ReportNode implements TreeNode
{
	final private @Getter Report report;
	
	final private Map<Integer,SubjectNode> subjectNodeCache = new HashMap<>();

	public ReportNode(final Report report)
	{
		this.report = report;
	}

	public SubjectNode getNode(Subject subject)
	{
		SubjectNode node;
		if(null==(node=subjectNodeCache.get(subject.getId())))
			subjectNodeCache.put(subject.getId(), node=new SubjectNode(subject));
		return node;
	}

	@Override
	public SubjectNode getChildAt(int childIndex)
	{
		return getNode(report.getSubjects().get(childIndex));
	}

	@Override
	public int getChildCount()
	{
		return report.getSubjects().size();
	}

	@Override
	public TreeNode getParent()
	{
		return null;
	}

	@Override
	public int getIndex(TreeNode node)
	{
		return report.getSubjects().indexOf(((SubjectNode)node).subject);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return report.getSubjects().size()==0;
	}

	@Override
	public Enumeration<SubjectNode> children()
	{
		return new Enumeration<SubjectNode>()
		{
			private Iterator<Subject> iterator = report.getSubjects().iterator();

			@Override
			public boolean hasMoreElements()
			{
				return iterator.hasNext();
			}

			@Override
			public SubjectNode nextElement()
			{
				return getNode(iterator.next());
			}
		};
	}

	public final class SubjectNode implements TreeNode
	{
		final private @Getter Subject subject;

		final private Map<Integer,NoteNode> noteNodeCache = new HashMap<>();
		
		public SubjectNode(Subject subject)
		{
			this.subject = subject;
		}
		
		public NoteNode getNode(Note note)
		{
			NoteNode node;
			if(null==(node=noteNodeCache.get(note.getId())))
				noteNodeCache.put(note.getId(), node=new NoteNode(note));
			return node;
		}
		
		@Override
		public NoteNode getChildAt(int childIndex)
		{
			return getNode(subject.getNotes().get(childIndex));
		}

		@Override
		public int getChildCount()
		{
			return subject.getNotes().size();
		}

		@Override
		public SubjectNode getParent()
		{
			return SubjectNode.this;
		}

		@Override
		public int getIndex(TreeNode node)
		{
			return subject.getNotes().indexOf(((NoteNode)node).note);
		}

		@Override
		public boolean getAllowsChildren()
		{
			return true;
		}

		@Override
		public boolean isLeaf()
		{
			return subject.getNotes().size()==0;
		}

		@Override
		public Enumeration<NoteNode> children()
		{
			return new Enumeration<NoteNode>()
			{
				private Iterator<Note> iterator = subject.getNotes().iterator();

				@Override
				public boolean hasMoreElements()
				{
					return iterator.hasNext();
				}

				@Override
				public NoteNode nextElement()
				{
					return getNode(iterator.next());
				}
			};
		}
		
		
		public final class NoteNode implements TreeNode
		{
			final private @Getter Note note;
			
			public NoteNode(final Note note)
			{
				this.note = note;
			}

			@Override
			public TreeNode getChildAt(int childIndex)
			{
				return null;
			}

			@Override
			public int getChildCount()
			{
				return 0;
			}

			@Override
			public SubjectNode getParent()
			{
				return SubjectNode.this;
			}

			@Override
			public int getIndex(TreeNode node)
			{
				return 0;
			}

			@Override
			public boolean getAllowsChildren()
			{
				return false;
			}

			@Override
			public boolean isLeaf()
			{
				return true;
			}

			@Override
			public Enumeration<? extends TreeNode> children()
			{
				return null;
			}
			
		}
	}
	
}
