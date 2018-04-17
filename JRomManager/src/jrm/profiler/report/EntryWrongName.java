package jrm.profiler.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profiler.data.Entity;
import jrm.profiler.data.Entry;

public class EntryWrongName extends Note
{
	Entity entity;
	Entry entry;
	
	public EntryWrongName(Entity entity, Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryWrongName.Wrong"), parent.ware.getFullName(), entry.getName(), entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryWrongName.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.getName()), toBold(entity.getName()))); //$NON-NLS-1$
	}
}
