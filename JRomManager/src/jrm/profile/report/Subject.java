package jrm.profile.report;

import java.io.*;
import java.util.*;

import javax.swing.tree.TreeNode;

import jrm.misc.HTMLRenderer;
import jrm.profile.data.AnywareBase;

/**
 * A Subject is generally a report node about a container
 * @author optyfr
 *
 */
public abstract class Subject extends AbstractList<Note> implements TreeNode, HTMLRenderer, Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The related {@link AnywareBase}
	 */
	protected AnywareBase ware;

	/**
	 * the {@link List} of {@link Note}s
	 */
	protected List<Note> notes;

	/**
	 * The {@link Report} root as parent
	 */
	protected transient Report parent;
	
	protected transient int id;

	private static final ObjectStreamField[] serialPersistentFields = { new ObjectStreamField("ware", AnywareBase.class), new ObjectStreamField("notes", List.class)}; //$NON-NLS-1$ //$NON-NLS-2$

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("ware", ware); //$NON-NLS-1$
		fields.put("notes", notes); //$NON-NLS-1$
		stream.writeFields();
	}

	@SuppressWarnings("unchecked")
	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		ware = (AnywareBase) fields.get("ware", null); //$NON-NLS-1$
		notes = (List<Note>) fields.get("notes", new ArrayList<>()); //$NON-NLS-1$
		notes.forEach(n -> n.parent = this);
	}
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
	@Override
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
	public Report getParent()
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

	public int getId()
	{
		return id;
	}

	@Override
	public int size()
	{
		return notes.size();
	}
	
	@Override
	public Note get(int index)
	{
		return notes.get(index);
	}
}
