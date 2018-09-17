package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;

/**
 * An {@link Entry} is missing for an {@link EntityBase} and has not been found
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryMissing extends Note implements Serializable
{
	/**
	 * The related {@link EntityBase}
	 */
	final EntityBase entity;

	/**
	 * The constructor
	 * @param entity The related {@link EntityBase}
	 */
	public EntryMissing(final EntityBase entity)
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
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissing.Missing")), toBlue(parent.ware.getFullName()), toBold(entity.getName()))); //$NON-NLS-1$
	}

}
