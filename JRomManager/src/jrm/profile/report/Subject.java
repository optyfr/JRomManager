package jrm.profile.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import jrm.misc.HTMLRenderer;
import jrm.profile.data.Anyware;

public abstract class Subject implements TreeNode,HTMLRenderer
{
	protected final Anyware ware;
	
	protected final List<Note> notes;
	
	protected Report parent;

	public Subject(Anyware machine)
	{
		this.ware = machine;
		this.notes = new ArrayList<>();
	}
	
	Subject(Anyware machine, List<Note> notes)
	{
		this.ware = machine;
		this.notes = notes;
	}
	
	public abstract Subject clone(List<FilterOptions> filterOptions);
	
	public boolean add(Note note)
	{
		note.parent = this;
		boolean result = notes.add(note);
		return result;
	}
	
	public String getWareName()
	{
		if(ware!=null)
			return ware.getFullName();
		return ""; //$NON-NLS-1$
	}
	
	public abstract void updateStats();
	
	public abstract String toString();

	@Override
	public TreeNode getChildAt(int childIndex)
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
	public int getIndex(TreeNode node)
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
