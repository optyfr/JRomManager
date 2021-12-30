package jrm.profile.report;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Stream;

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
	private static final long serialVersionUID = 2L;

	/**
	 * The suspicious crc hex value
	 */
	private String crc;

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
	public Subject clone(final Set<FilterOptions> filterOptions)
	{
		return new RomSuspiciousCRC(crc);
	}
	
	@Override
	public Stream<Note> stream(Set<FilterOptions> filterOptions)
	{
		return notes.stream();
	}

	@Override
	public void updateStats()
	{
		// do nothing
	}
	
	@Override
	public boolean equals(Object o)
	{
		return super.equals(o);
	}
	
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
}
