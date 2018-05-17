package jrm.profile.report;

import org.apache.commons.text.StringEscapeUtils;

import jrm.Messages;
import jrm.profile.data.EntityBase;

public class EntryOK extends Note
{
	final EntityBase entity;

	public EntryOK(final EntityBase entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryOK.OK"), parent.ware.getFullName(), entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryOK.OK")), toBlue(parent.ware.getFullName()), toBold(entity.getName()))); //$NON-NLS-1$
	}

}
