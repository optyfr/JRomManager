package jrm.profile.report;

import java.io.Serializable;

import jrm.aui.status.StatusRendererFactory;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;
import lombok.Getter;

/**
 * A Subject is a report node about an entry of a container
 * @author optyfr
 *
 */
public abstract class Note implements StatusRendererFactory, Serializable
{
	private static final long serialVersionUID = 2L;
	/**
	 * The parent {@link Subject}
	 */
	transient @Getter Subject parent;
	
	transient int id = -1;

	@Override
	public abstract String toString();
	
	public abstract String getDetail(); 
	
	public abstract String getName();

	public abstract String getCrc();

	public abstract String getMd5();

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
	
	protected String getExpectedEntity(EntityBase entity)
	{
		if (entity == null)
			return "";
		String msg = "";
		msg += "== Expected == \n";
		msg += "Name : " + entity.getBaseName() + "\n";
		if (entity instanceof Entity e1)
		{
			if (e1.getSize() >= 0)		msg += "Size : " + e1.getSize() + "\n";
			if (e1.getCrc() != null)	msg += "CRC : " + e1.getCrc() + "\n";
			if (e1.getMd5() != null)	msg += "MD5 : " + e1.getMd5() + "\n";
			if (e1.getSha1() != null)	msg += "SHA1 : " + e1.getSha1() + "\n";
		}
		return msg;
	}

	protected String getCurrentEntry(Entry entry)
	{
		if (entry == null)
			return "";
		String msg = "";
		msg += "== Current == \n";
		msg += "Name : " + entry.getName() + "\n";
		if (entry.getSize() >= 0)	msg += "Size : " + entry.getSize() + "\n";
		if (entry.getCrc() != null)	msg += "CRC : " + entry.getCrc() + "\n";
		if (entry.getMd5() != null)	msg += "MD5 : " + entry.getMd5() + "\n";
		if (entry.getSha1() != null)	msg += "SHA1 : " + entry.getSha1() + "\n";
		return msg;
	}

}