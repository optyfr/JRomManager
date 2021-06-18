package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;

/**
 * Entry can be added
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryAdd extends Note implements Serializable
{
	/**
	 * the related {@link EntityBase} (a Rom, a Disk, or a Sample)
	 */
	final EntityBase entity;
	/**
	 * The {@link Entry} to add
	 */
	final Entry entry;

	/**
	 * The constructor for this entry report
	 * @param entity the related {@link EntityBase} (a Rom, a Disk, or a Sample)
	 * @param entry the {@link Entry} to add
	 */
	public EntryAdd(final EntityBase entity, final Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryAddAdd"), parent.ware.getFullName(), entity.getNormalizedName(), entry.getParent().getRelFile().getName(), entry.getRelFile()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryAddAdd")), toBlue(parent.ware.getFullName()), toBold(entity.getNormalizedName()), toItalic(entry.getParent().getRelFile().getName()), toBold(entry.getRelFile()))); //$NON-NLS-1$
	}

	@Override
	public String getDetail()
	{
		var msg="";
		msg += "== Expected == \n";
		msg += "Name : " + entity.getBaseName() + "\n";
		if(entity instanceof Entity)
		{
			Entity e = (Entity)entity;
			if (e.getSize() >= 0)		msg += "Size : " + e.getSize() + "\n";
			if (e.getCrc() != null)	msg += "CRC : " + e.getCrc() + "\n";
			if (e.getMd5() != null)	msg += "MD5 : " + e.getMd5() + "\n";
			if (e.getSha1() != null)	msg += "SHA1 : " + e.getSha1() + "\n";
		}
		msg += "== Current == \n";
		msg += "Name : " + entry.getName() + "\n";
		if (entry.getSize() >= 0)	msg += "Size : " + entry.getSize() + "\n";
		if (entry.getCrc() != null)	msg += "CRC : " + entry.getCrc() + "\n";
		if (entry.getMd5() != null)	msg += "MD5 : " + entry.getMd5() + "\n";
		if (entry.getSha1() != null)	msg += "SHA1 : " + entry.getSha1() + "\n";
		return msg;
	}

	@Override
	public String getName()
	{
		return entity.getBaseName();
	}

	@Override
	public String getCrc()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getCrc();
		return null;
	}

	@Override
	public String getSha1()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getSha1();
		return null;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
