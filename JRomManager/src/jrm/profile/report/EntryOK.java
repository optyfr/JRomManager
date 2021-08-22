package jrm.profile.report;

import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;

/**
 * Entry is OK (found or not needed) for this {@link EntityBase}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryOK extends Note implements Serializable
{
	/**
	 * The {@link EntityBase} where has been found OK
	 */
	final EntityBase entity;

	/**
	 * The constructor
	 * @param entity The {@link EntityBase} where has been found OK
	 */
	public EntryOK(final EntityBase entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryOK.OK"), parent.ware.getFullName(), entity.getNormalizedName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryOK.OK")), toBlue(parent.ware.getFullName()), toBold(entity.getNormalizedName()))); //$NON-NLS-1$
	}

	@Override
	public String getDetail()
	{
		return getExpectedEntity(entity);
	}

	@Override
	public String getName()
	{
		return entity.getBaseName();
	}

	@Override
	public String getCrc()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getCrc();
		return null;
	}

	@Override
	public String getSha1()
	{
		if(entity instanceof Entity)
			return ((Entity)entity).getSha1();
		return null;
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
