package jrm.profile.report;

import java.io.Serializable;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

import jrm.misc.HTMLRenderer;

/**
 * A Subject is a report node about an entry of a container
 * @author optyfr
 *
 */
public abstract class Note implements TreeNode,HTMLRenderer,Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	 * The parent {@link Subject}
	 */
	transient Subject parent;
	
	transient int id = -1;
/*
	private static final ObjectStreamField[] serialPersistentFields = {};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
//		final ObjectOutputStream.PutField fields = stream.putFields();
//		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
//		final ObjectInputStream.GetField fields = stream.readFields();
//		parent = null;
	}
*/
	@Override
	public abstract String toString();

	@Override
	public TreeNode getChildAt(final int childIndex)
	{
		return null;
	}

	@Override
	public int getChildCount()
	{
		return 0;
	}

	@Override
	public Subject getParent()
	{
		return parent;
	}

	@Override
	public int getIndex(final TreeNode node)
	{
		return -1;
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

	public int getId()
	{
		return id;
	}
}
