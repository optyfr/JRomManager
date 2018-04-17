package jrm.profiler.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryAdd extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryAdd(Entity entity, Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryAddAdd"), parent.ware.getFullName(), entity.getName(), entry.parent.file.getName(), entry.file); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryAddAdd")), toBlue(parent.ware.getFullName()), toBold(entity.getName()), toItalic(entry.parent.file.getName()), toBold(entry.file))); //$NON-NLS-1$
	}
}
