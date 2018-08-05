package jrm.profile.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import jrm.misc.HTMLRenderer;
import jrm.profile.data.AnywareBase;

/**
 * A Subject is generally a report node about a container
 * @author optyfr
 *
 */
public abstract class Subject implements TreeNode,HTMLRenderer
{
	/**
	 * The related {@link AnywareBase}
	 */
	protected final AnywareBase ware;

	/**
	 * the {@link List} of {@link Note}s
	 */
	protected final List<Note> notes;

	/**
	 * The {@link Report} root as parent
	 */
	protected Report parent;

	/**
	 * the public constructor with emptied {@link List}&lt;{@link Note}&gt;
	 * @param machine The related {@link AnywareBase}
	 */
	public Subject(final AnywareBase machine)
	{
		ware = machine;
		notes = new ArrayList<>();
	}

	/**
	 * The internal constructor
	 * @param machine The related {@link AnywareBase}
	 * @param notes a {@link List}&lt;{@link Note}&gt;
	 */
	Subject(final AnywareBase machine, final List<Note> notes)
	{
		ware = machine;
		this.notes = notes;
	}

	/**
	 * Clone this subject according a {@link List}&lt;{@link FilterOptions}&gt;
	 * @param filterOptions {@link List}&lt;{@link FilterOptions}&gt; to apply while cloning
	 * @return the cloned {@link Subject}
	 */
	public abstract Subject clone(List<FilterOptions> filterOptions);

	/**
	 * add a {@link Note} to this {@link Subject}
	 * @param note the {@link Note} to add
	 * @return true on success
	 */
	public boolean add(final Note note)
	{
		note.parent = this;
		final boolean result = notes.add(note);
		return result;
	}

	/**
	 * get the Ware Name
	 * @return the {@link AnywareBase#getFullName()} of {@link #ware} if not null, otherwise an empty {@link String}
	 */
	public String getWareName()
	{
		if(ware!=null)
			return ware.getFullName();
		return ""; //$NON-NLS-1$
	}

	/**
	 * update Report Statistics
	 */
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
