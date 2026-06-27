package jrm.ui.profile.report;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.tree.TreeNode;

import jrm.profile.report.Note;
import jrm.profile.report.Report;
import jrm.profile.report.Subject;
import jrm.ui.basic.AbstractNGTreeNode;
import lombok.Getter;

public class ReportNode extends ReportNodeGeneric<Report> {
    private final Map<Integer, SubjectNode> subjectNodeCache = new HashMap<>();

    public ReportNode(final Report report) {
        super(report);
    }

    public SubjectNode getNode(Subject subject) {
        return subjectNodeCache.computeIfAbsent(subject.getId(), _ -> new SubjectNode(subject));
    }

    @Override
    public SubjectNode getChildAt(int childIndex) {
        return getNode(report.getSubjects().get(childIndex));
    }

    @Override
    public int getChildCount() {
        return report.getSubjects().size();
    }

    @Override
    public int getIndex(TreeNode node) {
        return report.getSubjects().indexOf(((SubjectNode) node).subject);
    }

    @Override
    public boolean isLeaf() {
        return report.getSubjects().isEmpty();
    }

    @Override
    public Enumeration<SubjectNode> children() {
        return new Enumeration<SubjectNode>() {
            private Iterator<Subject> iterator = report.getSubjects().iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public SubjectNode nextElement() {
                return getNode(iterator.next());
            }
        };
    }

    public final class SubjectNode implements TreeNode {
        private final @Getter Subject subject;

        private final Map<Integer, NoteNode> noteNodeCache = new HashMap<>();

        public SubjectNode(Subject subject) {
            this.subject = subject;
        }

        public NoteNode getNode(Note note) {
            return noteNodeCache.computeIfAbsent(note.getId(), _ -> new NoteNode(note));
        }

        @Override
        public NoteNode getChildAt(int childIndex) {
            return getNode(subject.getNotes().get(childIndex));
        }

        @Override
        public int getChildCount() {
            return subject.getNotes().size();
        }

        @Override
        public TreeNode getParent() {
            return ReportNode.this;
        }

        @Override
        public int getIndex(TreeNode node) {
            return subject.getNotes().indexOf(((NoteNode) node).note);
        }

        @Override
        public boolean getAllowsChildren() {
            return true;
        }

        @Override
        public boolean isLeaf() {
            return subject.getNotes().isEmpty();
        }

        @Override
        public Enumeration<NoteNode> children() {
            return new Enumeration<NoteNode>() {
                private Iterator<Note> iterator = subject.getNotes().iterator();

                @Override
                public boolean hasMoreElements() {
                    return iterator.hasNext();
                }

                @Override
                public NoteNode nextElement() {
                    return getNode(iterator.next());
                }
            };
        }

        public final class NoteNode extends AbstractNGTreeNode {
            private final @Getter Note note;

            public NoteNode(final Note note) {
                this.note = note;
            }

            @Override
            public SubjectNode getParent() {
                return SubjectNode.this;
            }
        }
    }

}
