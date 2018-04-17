package jrm.profiler.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profiler.data.Entry;

public class EntryUnneeded extends Note
{
	Entry entry;

	public EntryUnneeded(Entry entry)
	{
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryUnneeded.Unneeded"), parent.ware.getFullName(), entry.file, entry.sha1); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryUnneeded.Unneeded")), toBold(parent.ware.getFullName()), toBold(entry.file), entry.sha1)); //$NON-NLS-1$
	}
}
