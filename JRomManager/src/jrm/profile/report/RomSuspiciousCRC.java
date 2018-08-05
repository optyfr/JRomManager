package jrm.profile.report;

import java.util.List;

import jrm.Messages;
import jrm.profile.data.AnywareBase;

/**
 * Information about suspicious CRC found<br>
 * A suspicious CRC is when 2 entries have same CRC value but different SHA1/MD5 values
 * @author optyfr
 *
 */
public class RomSuspiciousCRC extends Subject
{
	/**
	 * The suspicious crc hex value
	 */
	final String crc;

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
	public Subject clone(final List<FilterOptions> filterOptions)
	{
		return new RomSuspiciousCRC(crc);
	}

	@Override
	public void updateStats()
	{

	}
}
