package jrm.profile.report;

import java.io.Serializable;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.Entry;

/**
 * This {@link Entry} is present but has wrong hash when compared to its related {@link Entity}
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryWrongHash extends EntryExtNote implements Serializable
{
	private static final String ENTRY_WRONG_HASH_WRONG = "EntryWrongHash.Wrong";

	/**
	 * The constructor
	 * @param entity related {@link Entity}
	 * @param entry  Wrong hash {@link Entry}
	 */
	public EntryWrongHash(final Entity entity, final Entry entry)
	{
		super(entity, entry);
	}

	@Override
	public String toString()
	{
		if(entry.getMd5() == null && entry.getSha1() == null)
			return String.format(Messages.getString(ENTRY_WRONG_HASH_WRONG), parent.ware.getFullName(), entry.getRelFile(), "CRC", entry.getCrc(), getCrc()); //$NON-NLS-1$ //$NON-NLS-2$
		else if(entry.getSha1() == null)
			return String.format(Messages.getString(ENTRY_WRONG_HASH_WRONG), parent.ware.getFullName(), entry.getRelFile(), "MD5", entry.getMd5(), getMd5()); //$NON-NLS-1$ //$NON-NLS-2$
		else
			return String.format(Messages.getString(ENTRY_WRONG_HASH_WRONG), parent.ware.getFullName(), entry.getRelFile(), "SHA-1", entry.getSha1(), getSha1()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getDocument()
	{
		if(entry.getMd5() == null && entry.getSha1() == null)
			return toDocument(String.format(escape(Messages.getString(ENTRY_WRONG_HASH_WRONG)), toBlue(parent.ware.getFullName()), toBold(entry.getRelFile()), "CRC", entry.getCrc(), getCrc())); //$NON-NLS-1$ //$NON-NLS-2$
		else if(entry.getSha1() == null)
			return toDocument(String.format(escape(Messages.getString(ENTRY_WRONG_HASH_WRONG)), toBlue(parent.ware.getFullName()), toBold(entry.getRelFile()), "MD5", entry.getMd5(), getMd5())); //$NON-NLS-1$ //$NON-NLS-2$
		else
			return toDocument(String.format(escape(Messages.getString(ENTRY_WRONG_HASH_WRONG)), toBlue(parent.ware.getFullName()), toBold(entry.getRelFile()), "SHA-1", entry.getSha1(), getSha1())); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
