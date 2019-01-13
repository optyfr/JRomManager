package jrm.profile.report;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
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
public class EntryMissing extends Note implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The related {@link EntityBase}
	 */
	EntityBase entity;

	private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("entity", EntityBase.class)};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("entity", entity);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		entity = (EntityBase)fields.get("entity", null);
	}

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
