package jrm.profile.report;

import java.io.Serializable;

import jrm.locale.Messages;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;

/**
 * Entry can be added
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class EntryAdd extends EntryExtNote implements Serializable
{
	/**
	 * The constructor for this entry report
	 * @param entity the related {@link EntityBase} (a Rom, a Disk, or a Sample)
	 * @param entry the {@link Entry} to add
	 */
	public EntryAdd(final EntityBase entity, final Entry entry)
	{
		super(entity, entry);
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryAddAdd"), parent.ware.getFullName(), entity.getNormalizedName(), entry.getParent().getRelFile().getName(), entry.getRelFile()); //$NON-NLS-1$
	}

	@Override
	public String getDocument()
	{
		return toDocument(String.format(escape(Messages.getString("EntryAddAdd")), toBlue(parent.ware.getFullName()), toBoldBlack(entity.getNormalizedName()), toItalicBlack(entry.getParent().getRelFile().getName()), toBoldBlack(entry.getRelFile()))); //$NON-NLS-1$
	}

	@Override
	public String getAbbrv()
	{
		return "ADD";
	}
}
