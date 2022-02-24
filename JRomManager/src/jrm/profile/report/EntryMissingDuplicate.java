package jrm.profile.report;

import java.io.Serializable;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;

/**
 * The entry is missing but can be duplicated from the same container
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryMissingDuplicate extends EntryExtNote implements Serializable
{
	/**
	 * The constructor
	 * @param entity The missing {@link Entity}
	 * @param entry The candidate {@link Entry}
	 */
	public EntryMissingDuplicate(final Entity entity, final Entry entry)
	{
		super(entity, entry);
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryMissingDuplicate.MissingDuplicate"), parent.ware.getFullName(), entry.getRelFile(), entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getDocument()
	{
		return toDocument(String.format(escape(Messages.getString("EntryMissingDuplicate.MissingDuplicate")), toBlue(parent.ware.getFullName()), toBoldBlack(entry.getRelFile()), toBoldBlack(entity.getName()))); //$NON-NLS-1$
	}
}
