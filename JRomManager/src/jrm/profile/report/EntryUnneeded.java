package jrm.profile.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entry;

/**
 * Entry is unneeded
 * @author optyfr
 *
 */
public class EntryUnneeded extends Note
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
		return String.format(Messages.getString("EntryUnneeded.Unneeded"), parent.ware.getFullName(), entry.file, entry.sha1==null?(entry.md5==null?entry.crc:entry.md5):entry.sha1); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryUnneeded.Unneeded")), toBold(parent.ware.getFullName()), toBold(entry.file), entry.sha1==null?(entry.md5==null?entry.crc:entry.md5):entry.sha1)); //$NON-NLS-1$
	}
}
