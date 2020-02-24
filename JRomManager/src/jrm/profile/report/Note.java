package jrm.profile.report;

import java.io.Serializable;

import jrm.misc.HTMLRenderer;

/**
 * A Subject is a report node about an entry of a container
 * @author optyfr
 *
 */
public abstract class Note implements HTMLRenderer,Serializable
{
	private static final long serialVersionUID = 2L;
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
	
	public abstract String getDetail(); 
	
	public abstract String getName();

	public abstract String getCrc();

	public int getId()
	{
		return id;
	}
	
	@Override
	public int hashCode()
	{
		return System.identityHashCode(this);
	}
}
