package jrm.profiler.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryWrongHash extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryWrongHash(Entity entity, Entry entry)
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
