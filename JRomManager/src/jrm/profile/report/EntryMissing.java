package jrm.profile.report;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;

import org.apache.commons.text.StringEscapeUtils;

import jrm.locale.Messages;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Entry;

/**
 * An {@link Entry} is missing for an {@link EntityBase} and has not been found
 * @author optyfr
 *
 */
public class EntryMissing extends Note implements Serializable
{
	private static final String ENTRY_MISSING_MISSING = "EntryMissing.Missing";

	private static final String ENTITY_STR = "entity";

	private static final long serialVersionUID = 2L;

	/**
	 * The related {@link EntityBase}
	 */
	EntityBase entity;

	private static final ObjectStreamField[] serialPersistentFields = {	//NOSONAR
		new ObjectStreamField(ENTITY_STR, EntityBase.class)
	};
	
	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put(ENTITY_STR, entity);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		entity = (EntityBase)fields.get(ENTITY_STR, null);
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
		return String.format(Messages.getString(ENTRY_MISSING_MISSING), parent.ware.getFullName(), entity.getName()); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		if(entity instanceof Entity)
		{
			Entity e = (Entity)entity;
			final String hash;
			if (e.getSha1() != null)
				hash = e.getSha1();
			else if (e.getMd5() != null)
				hash = e.getMd5();
			else
				hash = e.getCrc();
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString(ENTRY_MISSING_MISSING)), toBlue(parent.ware.getFullName()), toBold(entity.getName())) + " ("+hash+")"); //$NON-NLS-1$
		}
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString(ENTRY_MISSING_MISSING)), toBlue(parent.ware.getFullName()), toBold(entity.getName()))); //$NON-NLS-1$
	}

	@Override
	public String getDetail()
	{
		String msg = "";
		msg += "== Expected == \n";
		msg += "Name : " + entity.getBaseName() + "\n";
		if (entity instanceof Entity)
		{
			final Entity e1 = (Entity) entity;
			if (e1.getSize() >= 0)		msg += "Size : " + e1.getSize() + "\n";
			if (e1.getCrc() != null)	msg += "CRC : " + e1.getCrc() + "\n";
			if (e1.getMd5() != null)	msg += "MD5 : " + e1.getMd5() + "\n";
			if (e1.getSha1() != null)	msg += "SHA1 : " + e1.getSha1() + "\n";
		}
		return msg;
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
