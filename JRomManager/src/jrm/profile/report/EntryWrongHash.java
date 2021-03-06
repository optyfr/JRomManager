package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;

/**
 * This {@link Entry} is present but has wrong hash when compared to its related {@link Entity}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryWrongHash extends Note implements Serializable
{
	/**
	 * related {@link Entity} 
	 */
	final Entity entity;
	/**
	 * Wrong hash {@link Entry}
	 */
	final Entry entry;

	/**
	 * The constructor
	 * @param entity related {@link Entity}
	 * @param entry  Wrong hash {@link Entry}
	 */
	public EntryWrongHash(final Entity entity, final Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		if(entry.getMd5() == null && entry.getSha1() == null)
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.ware.getFullName(), entry.getRelFile(), "CRC", entry.getCrc(), entity.getCrc()); //$NON-NLS-1$ //$NON-NLS-2$
		else if(entry.getSha1() == null)
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.ware.getFullName(), entry.getRelFile(), "MD5", entry.getMd5(), entity.getMd5()); //$NON-NLS-1$ //$NON-NLS-2$
		else
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.ware.getFullName(), entry.getRelFile(), "SHA-1", entry.getSha1(), entity.getSha1()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getHTML()
	{
		if(entry.getMd5() == null && entry.getSha1() == null)
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryWrongHash.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.getRelFile()), "CRC", entry.getCrc(), entity.getCrc())); //$NON-NLS-1$ //$NON-NLS-2$
		else if(entry.getSha1() == null)
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryWrongHash.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.getRelFile()), "MD5", entry.getMd5(), entity.getMd5())); //$NON-NLS-1$ //$NON-NLS-2$
		else
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryWrongHash.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.getRelFile()), "SHA-1", entry.getSha1(), entity.getSha1())); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getDetail()
	{
		String msg="";
		msg += "== Expected == \n";
		msg += "Name : " + entity.getBaseName() + "\n";
		if (entity.getSize() >= 0)		msg += "Size : " + entity.getSize() + "\n";
		if (entity.getCrc() != null)	msg += "CRC : " + entity.getCrc() + "\n";
		if (entity.getMd5() != null)	msg += "MD5 : " + entity.getMd5() + "\n";
		if (entity.getSha1() != null)	msg += "SHA1 : " + entity.getSha1() + "\n";
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
		return entity.getCrc();
	}

	@Override
	public String getSha1()
	{
		return entity.getSha1();
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
