package jrm.profile.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;

/**
 * This {@link Entry} is present but has wrong hash when compared to its related {@link Entity}
 * @author optyfr
 *
 */
public class EntryWrongHash extends Note
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
		if(entry.md5 == null && entry.sha1 == null)
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.ware.getFullName(), entry.file, "CRC", entry.crc, entity.crc); //$NON-NLS-1$ //$NON-NLS-2$
		else if(entry.sha1 == null)
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.ware.getFullName(), entry.file, "MD5", entry.md5, entity.md5); //$NON-NLS-1$ //$NON-NLS-2$
		else
			return String.format(Messages.getString("EntryWrongHash.Wrong"), parent.ware.getFullName(), entry.file, "SHA-1", entry.sha1, entity.sha1); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getHTML()
	{
		if(entry.md5 == null && entry.sha1 == null)
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryWrongHash.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.file), "CRC", entry.crc, entity.crc)); //$NON-NLS-1$ //$NON-NLS-2$
		else if(entry.sha1 == null)
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryWrongHash.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.file), "MD5", entry.md5, entity.md5)); //$NON-NLS-1$ //$NON-NLS-2$
		else
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryWrongHash.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.file), "SHA-1", entry.sha1, entity.sha1)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
