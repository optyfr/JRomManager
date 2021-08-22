package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;

/**
 * The entry is missing but can be duplicated from the same container
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryMissingDuplicate extends Note implements Serializable
{
	/**
	 * The missing {@link Entity}
	 */
	final Entity entity;
	/**
	 * The candidate {@link Entry}
	 */
	final Entry entry;

	/**
	 * The constructor
	 * @param entity The missing {@link Entity}
	 * @param entry The candidate {@link Entry}
	 */
	public EntryMissingDuplicate(final Entity entity, final Entry entry)
	{
		this.entity = entity;
		this.entry = entry;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryMissingDuplicate.MissingDuplicate"), parent.ware.getFullName(), entry.getRelFile(), entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissingDuplicate.MissingDuplicate")), toBlue(parent.ware.getFullName()), toBold(entry.getRelFile()), toBold(entity.getName()))); //$NON-NLS-1$
	}

	@Override
	public String getDetail()
	{
		return getExpectedEntity(entity) + getCurrentEntry(entry);
	}

	@Override
	public String getName()
	{
		return entity.getBaseName();
	}

	@Override
	public String getCrc()
	{
		return entity.getCrc();
	}

	@Override
	public String getSha1()
	{
		return entity.getSha1();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

}
