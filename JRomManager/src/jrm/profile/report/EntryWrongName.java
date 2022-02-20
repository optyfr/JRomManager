package jrm.profile.report;

import java.io.Serializable;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;

/**
 * An {@link Entry} has been found for a related {@link Entity}, but is wrongly named
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryWrongName extends EntryExtNote implements Serializable
{
	/**
	 * The constructor
	 * @param entity The related {@link Entity}
	 * @param entry The entry wrongly named
	 */
	public EntryWrongName(final Entity entity, final Entry entry)
	{
		super(entity, entry);
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryWrongName.Wrong"), parent.ware.getFullName(), entry.getName(), entity.getNormalizedName()); //$NON-NLS-1$
	}

	@Override
	public String getDocument()
	{
		return toDocument(String.format(escape(Messages.getString("EntryWrongName.Wrong")), toBlue(parent.ware.getFullName()), toBold(entry.getName()), toBold(entity.getNormalizedName()))); //$NON-NLS-1$
	}

}
