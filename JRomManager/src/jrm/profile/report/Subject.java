package jrm.profile.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import jrm.misc.HTMLRenderer;
import jrm.profile.data.AnywareBase;

public abstract class Subject implements TreeNode,HTMLRenderer
{
	protected final AnywareBase ware;

	protected final List<Note> notes;

	protected Report parent;

	public Subject(final AnywareBase machine)
	{
		ware = machine;
		notes = new ArrayList<>();
	}

	Subject(final AnywareBase machine, final List<Note> notes)
	{
		ware = machine;
		this.notes = notes;
	}

	public abstract Subject clone(List<FilterOptions> filterOptions);

	public boolean add(final Note note)
	{
		note.parent = this;
		final boolean result = notes.add(note);
		return result;
	}

	public String getWareName()
	{
		if(ware!=null)
			return ware.getFullName();
		return ""; //$NON-NLS-1$
	}

	public abstract void updateStats();

	@Override
	public abstract String toString();

	@Override
	public TreeNode getChildAt(final int childIndex)
	{
		return notes.get(childIndex);
	}

	@Override
	public int getChildCount()
	{
		return notes.size();
	}

	@Override
	public TreeNode getParent()
	{
		return parent;
	}

	@Override
	public int getIndex(final TreeNode node)
	{
		return notes.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public boolean isLeaf()
	{
		return notes.size()==0;
	}

	@Override
	public Enumeration<Note> children()
	{
		return Collections.enumeration(notes);
	}
}
