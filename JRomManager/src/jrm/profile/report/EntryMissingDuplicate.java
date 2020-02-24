package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;

/**
 * The entry is missing but can be duplicated from the same container
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryMissingDuplicate extends Note implements Serializable
{
	/**
	 * The missing {@link Entity}
	 */
	final Entity entity;
	/**
	 * The candidate {@link Entry}
	 */
	final Entry entry;

	/**
	 * The constructor
	 * @param entity The missing {@link Entity}
	 * @param entry The candidate {@link Entry}
	 */
	public EntryMissingDuplicate(final Entity entity, final Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryMissingDuplicate.MissingDuplicate"), parent.ware.getFullName(), entry.file, entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissingDuplicate.MissingDuplicate")), toBlue(parent.ware.getFullName()), toBold(entry.file), toBold(entity.getName()))); //$NON-NLS-1$
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
		if (entry.size >= 0)	msg += "Size : " + entry.size + "\n";
		if (entry.crc != null)	msg += "CRC : " + entry.crc + "\n";
		if (entry.md5 != null)	msg += "MD5 : " + entry.md5 + "\n";
		if (entry.sha1 != null)	msg += "SHA1 : " + entry.sha1 + "\n";
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
}
