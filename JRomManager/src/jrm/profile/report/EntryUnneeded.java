package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entry;

/**
 * Entry is unneeded
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryUnneeded extends Note implements Serializable
{
	/**
	 * The {@link Entry} that is not needed
	 */
	final Entry entry;

	/**
	 * The constructor
	 * @param entry The {@link Entry} that is not needed
	 */
	public EntryUnneeded(final Entry entry)
	{
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryUnneeded.Unneeded"), parent.ware.getFullName(), entry.getRelFile(), entry.sha1==null?(entry.md5==null?entry.crc:entry.md5):entry.sha1); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryUnneeded.Unneeded")), toBold(parent.ware.getFullName()), toBold(entry.getRelFile()), entry.sha1==null?(entry.md5==null?entry.crc:entry.md5):entry.sha1)); //$NON-NLS-1$
	}

	@Override
	public String getDetail()
	{
		String msg="";
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
		return entry.getName();
	}

	@Override
	public String getCrc()
	{
		return entry.crc;
	}

	@Override
	public String getSha1()
	{
		return entry.sha1;
	}
}
