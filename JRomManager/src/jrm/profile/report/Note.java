package jrm.profile.report;

import java.io.Serializable;

import jrm.misc.HTMLRenderer;

/**
 * A Subject is a report node about an entry of a container
 * @author optyfr
 *
 */
public abstract class Note implements HTMLRenderer, Serializable
{
	private static final long serialVersionUID = 2L;
	/**
	 * The parent {@link Subject}
	 */
	transient Subject parent;
	
	transient int id = -1;

	@Override
	public abstract String toString();
	
	public abstract String getDetail(); 
	
	public abstract String getName();

	public abstract String getCrc();

	public abstract String getSha1();

	public int getId()
	{
		return id;
	}
	
	@Override
	public int hashCode()
	{
		return System.identityHashCode(this);
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
}
