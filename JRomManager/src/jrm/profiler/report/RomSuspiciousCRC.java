package jrm.profiler.report;

import java.util.List;

import jrm.Messages;

public class RomSuspiciousCRC extends Subject
{
	String crc;
	
	public RomSuspiciousCRC(String crc)
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
	public Subject clone(List<FilterOptions> filterOptions)
	{
		return new RomSuspiciousCRC(crc);
	}
}
