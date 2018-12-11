package jrm.profile.report;

import java.io.*;
import java.util.List;

import jrm.locale.Messages;
import jrm.profile.data.AnywareBase;

/**
 * Information about suspicious CRC found<br>
 * A suspicious CRC is when 2 entries have same CRC value but different SHA1/MD5 values
 * @author optyfr
 *
 */
public class RomSuspiciousCRC extends Subject implements Serializable
{
	private static final long serialVersionUID = 1L;

	/**
	 * The suspicious crc hex value
	 */
	String crc;

	private static final ObjectStreamField[] serialPersistentFields = {new ObjectStreamField("crc", String.class)};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("crc", crc);
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		crc = (String)fields.get("crc", null);
	}

	/**
	 * constructor with no {@link AnywareBase} in relation
	 * @param crc the suspicious crc hex value
	 */
	public RomSuspiciousCRC(final String crc)
	{
		super(null);
		this.crc = crc;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("RomSuspiciousCRC.SuspiciousCRC"), crc); //$NON-NLS-1$
	}

	@Override
	public String getHTML()
	{
		return toString();
	}
	
	@Override
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		return new RomSuspiciousCRC(crc);
	}

	@Override
	public void updateStats()
	{

	}
}
