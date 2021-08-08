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
		final String hash;
		if (entry.getSha1() != null)
			hash = entry.getSha1();
		else if (entry.getMd5() != null)
			hash = entry.getMd5();
		else
			hash = entry.getCrc();
		return String.format(Messages.getString("EntryUnneeded.Unneeded"), parent.ware.getFullName(), entry.getRelFile(), hash); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		final String hash;
		if (entry.getSha1() != null)
			hash = entry.getSha1();
		else if (entry.getMd5() != null)
			hash = entry.getMd5();
		else
			hash = entry.getCrc();
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryUnneeded.Unneeded")), toBold(parent.ware.getFullName()), toBold(entry.getRelFile()), hash)); //$NON-NLS-1$
	}

	@Override
	public String getDetail()
	{
		String msg="";
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
		return entry.getName();
	}

	@Override
	public String getCrc()
	{
		return entry.getCrc();
	}

	@Override
	public String getSha1()
	{
		return entry.getSha1();
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
