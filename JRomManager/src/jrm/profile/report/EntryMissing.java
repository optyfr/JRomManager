package jrm.profile.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profile.data.Entity;

public class EntryMissing extends Note
{
	final Entity entity;

	public EntryMissing(final Entity entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryMissing.Missing"), parent.ware.getFullName(), entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissing.Missing")), toBlue(parent.ware.getFullName()), toBold(entity.getName())));
	}

}
