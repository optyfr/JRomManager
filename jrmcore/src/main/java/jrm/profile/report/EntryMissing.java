package jrm.profile.report;

import java.io.Serializable;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;

/**
 * Report note indicating that a required expected metadata entity is missing and could not be resolved.
 * <p>
 * This status describes roms, disks, or samples defined in the active profile database that are completely absent
 * from the scanned filesystem path.
 *
 * @author optyfr
 * @since 1.0
 */
public class EntryMissing extends EntryNote implements Serializable
{
	/**
	 * Resource bundle key for missing entries localization messages.
	 */
	private static final String ENTRY_MISSING_MISSING = "EntryMissing.Missing";

	/**
	 * Serial version identifier for object serialization compatibility.
	 */
	private static final long serialVersionUID = 3L;

	/**
	 * Constructs a new EntryMissing note for the specified expected metadata entity.
	 *
	 * @param entity the expected rom, disk, or sample metadata
	 */
	public EntryMissing(final EntityBase entity)
	{
		super(entity);
	}

	/**
	 * Returns a localized string summarizing that the entry is missing.
	 *
	 * @return the localized message string
	 */
	@Override
	public String toString()
	{
		return String.format(Messages.getString(ENTRY_MISSING_MISSING), parent.ware.getFullName(), entity.getName()); //$NON-NLS-1$
	}

	/**
	 * Renders an HTML-styled diagnostic document summarizing this note.
	 *
	 * @return the styled HTML diagnostic document string
	 */
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
			return toDocument(String.format(escape(Messages.getString(ENTRY_MISSING_MISSING)), toBlue(parent.ware.getFullName()), toBoldBlack(entity.getName())) + " ("+hash+")"); //$NON-NLS-1$
		}
		return toDocument(String.format(escape(Messages.getString(ENTRY_MISSING_MISSING)), toBlue(parent.ware.getFullName()), toBoldBlack(entity.getName()))); //$NON-NLS-1$
	}

	/**
	 * Gets the abbreviation code representing this note type.
	 *
	 * @return the abbreviation "MISS"
	 */
	@Override
	public String getAbbrv()
	{
		return "MISS";
	}

}
