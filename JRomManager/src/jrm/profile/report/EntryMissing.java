package jrm.profile.report;

import java.io.Serializable;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;

/**
 * An {@link Entry} is missing for an {@link EntityBase} and has not been found
 * @author optyfr
 *
 */
public class EntryMissing extends EntryNote implements Serializable
{
	private static final String ENTRY_MISSING_MISSING = "EntryMissing.Missing";

	private static final long serialVersionUID = 3L;

	/**
	 * The constructor
	 * @param entity The related {@link EntityBase}
	 */
	public EntryMissing(final EntityBase entity)
	{
		super(entity);
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString(ENTRY_MISSING_MISSING), parent.ware.getFullName(), entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getDocument()
	{
		if(entity instanceof Entity e)
		{
			final String hash;
			if (e.getSha1() != null)
				hash = e.getSha1();
			else if (e.getMd5() != null)
				hash = e.getMd5();
			else
				hash = e.getCrc();
			return toDocument(String.format(escape(Messages.getString(ENTRY_MISSING_MISSING)), toBlue(parent.ware.getFullName()), toBold(entity.getName())) + " ("+hash+")"); //$NON-NLS-1$
		}
		return toDocument(String.format(escape(Messages.getString(ENTRY_MISSING_MISSING)), toBlue(parent.ware.getFullName()), toBold(entity.getName()))); //$NON-NLS-1$
	}

}
