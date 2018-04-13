package jrm.profiler.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profiler.data.Entity;

public class EntryMissing extends Note
{
	Entity entity;
	
	public EntryMissing(Entity entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryMissing.Missing"), parent.machine.name, entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissing.Missing")), toBlue(parent.machine.name), toBold(entity.getName()))); //$NON-NLS-2$
	}
	
}
