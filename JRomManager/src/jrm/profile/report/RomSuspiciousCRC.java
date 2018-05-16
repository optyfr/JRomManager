package jrm.profile.report;

import java.util.List;

import jrm.Messages;

public class RomSuspiciousCRC extends Subject
{
	final String crc;

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
