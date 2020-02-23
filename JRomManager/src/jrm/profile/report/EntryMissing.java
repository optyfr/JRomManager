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
	private static final long serialVersionUID = 2L;

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
		if(entity instanceof Entity)
		{
			Entity e = (Entity)entity;
			String hash = e.sha1==null?(e.md5==null?e.crc:e.md5):e.sha1;
			return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissing.Missing")), toBlue(parent.ware.getFullName()), toBold(entity.getName())) + " ("+hash+")"); //$NON-NLS-1$
		}
		return toHTML(String.format(StringEscapeUtils.escapeHtml4(Messages.getString("EntryMissing.Missing")), toBlue(parent.ware.getFullName()), toBold(entity.getName()))); //$NON-NLS-1$
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

}
