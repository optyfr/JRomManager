package jrm.profile.report;

import java.io.Serializable;

import jrm.locale.Messages;
import jrm.profile.data.Entry;

/**
 * Entry is unneeded
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryUnneeded extends EntryExtNote implements Serializable
{
	/**
	 * The constructor
	 * @param entry The {@link Entry} that is not needed
	 */
	public EntryUnneeded(final Entry entry)
	{
		super(null, entry);
	}

	@Override
	public String toString()
	{
		final String hash;
		if (entry.getSha1() != null)
			hash = entry.getSha1();
		else if (entry.getMd5() != null)
			hash = entry.getMd5();
		else
			hash = entry.getCrc();
		return String.format(Messages.getString("EntryUnneeded.Unneeded"), parent.ware.getFullName(), entry.getRelFile(), hash); //$NON-NLS-1$
	}

	@Override
	public String getDocument()
	{
		final String hash;
		if (entry.getSha1() != null)
			hash = entry.getSha1();
		else if (entry.getMd5() != null)
			hash = entry.getMd5();
		else
			hash = entry.getCrc();
		return toDocument(String.format(escape(Messages.getString("EntryUnneeded.Unneeded")), toBoldBlack(parent.ware.getFullName()), toBoldBlack(entry.getRelFile()), hash)); //$NON-NLS-1$
	}

}
